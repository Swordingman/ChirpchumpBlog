package com.Chirpchump.my_blog_backend;
import com.Chirpchump.my_blog_backend.model.User;
import com.Chirpchump.my_blog_backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    // 仅用于开发环境初始化或密码更新，生产环境不应如此操作
    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // 示例：创建或更新管理员用户
            String adminUsername = "admin";
            String adminPassword = "chirpchump";
            String adminEmail = "505392837@qq.com";
            if (!userRepository.existsByUsername(adminUsername)) {
                User admin = new User();
                admin.setUsername(adminUsername);
                admin.setPassword(passwordEncoder.encode(adminPassword)); // 设置一个强密码
                admin.setEmail(adminEmail);
                admin.setRole("ROLE_ADMIN"); // 确保角色名称与SecurityConfig中一致
                userRepository.save(admin);
                System.out.println("Created admin user with default password.");
            } else {
                // 如果需要更新现有用户的密码
                User admin = userRepository.findByUsername(adminUsername).orElse(null);
                if (admin != null && !passwordEncoder.matches(adminPassword, admin.getPassword())) {
                    // admin.setPassword(passwordEncoder.encode("newpassword123"));
                    // userRepository.save(admin);
                    // System.out.println("Updated admin password.");
                }
            }
        };
    }
}
