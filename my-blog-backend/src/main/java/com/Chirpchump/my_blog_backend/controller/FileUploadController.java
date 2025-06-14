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
    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-url-prefix}")
    private String accessUrlPrefix;

    @PostMapping("/upload")
    public ResponseEntity<?> handleFileUpload(@RequestParam("image") MultipartFile file) {
        if (file.isEmpty()) {
            logger.warn("Received an empty file upload request.");
            return ResponseEntity.badRequest().body(Map.of("message", "上传的文件不能为空"));
        }

        logger.info("Received file upload request. Original filename: '{}', Size: {} bytes", file.getOriginalFilename(), file.getSize());
        logger.info("Base upload directory from properties: '{}'", uploadDir);

        try {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.lastIndexOf('.') > 0) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            Path uploadPath = Paths.get(this.uploadDir);
            Path destinationFile = uploadPath.resolve(newFilename).normalize();

            logger.info("Attempting to save file to absolute path: '{}'", destinationFile.toAbsolutePath());

            if (!Files.exists(uploadPath)) {
                logger.info("Upload directory does not exist. Creating directory: '{}'", uploadPath.toAbsolutePath());
                Files.createDirectories(uploadPath);
            }

            Files.copy(file.getInputStream(), destinationFile);

            logger.info("File successfully saved: '{}'", destinationFile.toAbsolutePath());

            String fileUrl = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path(this.accessUrlPrefix)
                    .path(newFilename)
                    .toUriString();

            logger.info("Generated accessible file URL: '{}'", fileUrl);

            return ResponseEntity.ok(Map.of("url", fileUrl));

        } catch (IOException e) {
            logger.error("Failed to store file '{}' due to an IO Exception.", file.getOriginalFilename(), e);
            return ResponseEntity.status(500).body(Map.of("message", "文件存储失败，请检查服务器日志。"));
        } catch (Exception e) {
            logger.error("An unexpected error occurred during upload of file '{}'.", file.getOriginalFilename(), e);
            return ResponseEntity.status(500).body(Map.of("message", "文件上传时发生未知错误，可能是权限不足。"));
        }
    }
}