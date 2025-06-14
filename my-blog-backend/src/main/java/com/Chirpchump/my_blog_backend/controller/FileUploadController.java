package com.Chirpchump.my_blog_backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
public class FileUploadController {

    // 1. 添加一个 Logger 实例，用于记录日志
    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    // 2. 从 application.properties 注入配置值
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-url-prefix}")
    private String accessUrlPrefix;

    @PostMapping("/upload")
    public ResponseEntity<?> handleFileUpload(@RequestParam("image") MultipartFile file) {
        // 检查上传的文件是否为空
        if (file.isEmpty()) {
            logger.warn("Received an empty file upload request.");
            return ResponseEntity.badRequest().body(Map.of("message", "上传的文件不能为空"));
        }

        logger.info("Received file upload request. Original filename: '{}', Size: {} bytes", file.getOriginalFilename(), file.getSize());
        logger.info("Base upload directory from properties: '{}'", uploadDir);

        try {
            // 3. 生成新的、唯一的文件名，以避免文件名冲突
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.lastIndexOf('.') > 0) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            // 4. 使用 Path API 来处理文件路径，这比直接拼接字符串更安全、更健壮
            Path uploadPath = Paths.get(this.uploadDir);
            Path destinationFile = uploadPath.resolve(newFilename).normalize();

            // 打印出最终要写入的绝对路径，这是排查问题的关键信息
            logger.info("Attempting to save file to absolute path: '{}'", destinationFile.toAbsolutePath());

            // 5. 确保目标目录存在，如果不存在则创建
            if (!Files.exists(uploadPath)) {
                logger.info("Upload directory does not exist. Creating directory: '{}'", uploadPath.toAbsolutePath());
                Files.createDirectories(uploadPath);
            }

            // 6. 将上传文件的输入流复制到目标文件，这是实际的写入操作
            Files.copy(file.getInputStream(), destinationFile);

            logger.info("File successfully saved: '{}'", destinationFile.toAbsolutePath());

            // 7. 构建可供外部访问的 URL，推荐使用 ServletUriComponentsBuilder
            String fileUrl = ServletUriComponentsBuilder
                    .fromCurrentContextPath()       // 获取基础URL，例如 http://localhost:8080/context-path
                    .path(this.accessUrlPrefix)     // 添加访问前缀，例如 /uploads/
                    .path(newFilename)              // 添加新的文件名
                    .toUriString();                 // 构建成完整的URL字符串

            logger.info("Generated accessible file URL: '{}'", fileUrl);

            // 8. 返回成功的响应，包含 mavon-editor 需要的 URL
            return ResponseEntity.ok(Map.of("url", fileUrl));

        } catch (IOException e) {
            // 捕获IO异常，例如磁盘已满、流被意外关闭等
            logger.error("Failed to store file '{}' due to an IO Exception.", file.getOriginalFilename(), e);
            return ResponseEntity.status(500).body(Map.of("message", "文件存储失败，请检查服务器日志。"));
        } catch (Exception e) {
            // 捕获所有其他可能的异常，例如权限不足 (AccessDeniedException)
            logger.error("An unexpected error occurred during upload of file '{}'.", file.getOriginalFilename(), e);
            return ResponseEntity.status(500).body(Map.of("message", "文件上传时发生未知错误，可能是权限不足。"));
        }
    }
}