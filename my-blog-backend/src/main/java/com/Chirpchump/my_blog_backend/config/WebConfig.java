package com.Chirpchump.my_blog_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-url-prefix}")
    private String accessUrlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        System.out.println("--- WebConfig ---");
        System.out.println("accessUrlPrefix: " + accessUrlPrefix);
        System.out.println("uploadDir: " + uploadDir);
        System.out.println("Mapping " + accessUrlPrefix + "** to file:" + uploadDir);

        registry.addResourceHandler(accessUrlPrefix + "**")
                .addResourceLocations("file:" + uploadDir);
    }
}