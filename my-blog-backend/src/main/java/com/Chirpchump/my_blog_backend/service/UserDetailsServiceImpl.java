package com.Chirpchump.my_blog_backend.service;

import com.Chirpchump.my_blog_backend.model.User;
import com.Chirpchump.my_blog_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户未找到: " + username));

        // 将用户的角色字符串 (如 "ROLE_ADMIN,ROLE_USER") 转换为 GrantedAuthority 集合
        // 假设 User 实体中 role 字段存储的是单个角色字符串，如 "ROLE_ADMIN"
        Set<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(user.getRole()));
        // 如果 role 字段存储的是逗号分隔的多个角色，例如 "ROLE_ADMIN,ROLE_EDITOR"
        // Set<GrantedAuthority> authorities = Stream.of(user.getRole().split(","))
        //         .map(SimpleGrantedAuthority::new)
        //         .collect(Collectors.toSet());


        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(), // 数据库中存储的已加密密码
                authorities);
    }
}

