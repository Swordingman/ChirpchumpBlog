package com.Chirpchump.my_blog_backend;

import com.Chirpchump.my_blog_backend.model.User;
import com.Chirpchump.my_blog_backend.repository.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;



@SpringBootApplication
@ComponentScan(basePackages = "com.Chirpchump.my_blog_backend")
public class MyBlogBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyBlogBackendApplication.class, args);
	}
}