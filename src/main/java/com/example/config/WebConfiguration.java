package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class WebConfiguration {

    @Bean
    BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }//这个bean是用来加密的，这里我们使用的是BCryptPasswordEncoder，它是Spring Security提供的一个密码加密工具，可以实现不可逆的加密


}
