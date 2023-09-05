package com.example.config;

import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.entity.vo.response.AuthorizeVO;
import com.example.filter.JwtAuthorizeFilter;
import com.example.service.AccountService;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfiguration {

    @Resource
    JwtUtils utils;

    @Resource
    JwtAuthorizeFilter jwtAuthorizeFilter;

    @Resource
    AccountService service;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(conf -> conf
                        .requestMatchers("/api/auth/**", "/error").permitAll() // 允许指定路径的请求放行
                        .anyRequest().authenticated() // 其他请求需要认证
                )
                .formLogin(conf -> conf
                        .loginProcessingUrl("/api/auth/login") // 登录请求的路径
                        .failureHandler(this::onAuthenticationFailure) // 登录失败的处理
                        .successHandler(this::onAuthenticationSuccess) // 登录成功的处理
                )
                .logout(conf -> conf
                        .logoutUrl("/api/auth/logout") // 登出请求的路径
                        .logoutSuccessHandler(this::onLogoutSuccess) // 登出成功的处理
                )
                .exceptionHandling(conf -> conf
                        .authenticationEntryPoint(this::onUnauthorized) // 未授权的处理
                        .accessDeniedHandler(this::onAccessDeny) // 访问被拒绝的处理
                )
                .csrf(AbstractHttpConfigurer::disable) // 禁用 CSRF
                .sessionManagement(conf -> conf
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 不使用会话
                .addFilterBefore(jwtAuthorizeFilter, UsernamePasswordAuthenticationFilter.class) // 添加 JWT 鉴权过滤器
                .build();
    }

    public void onAccessDeny(HttpServletRequest request,
                             HttpServletResponse response,
                             AccessDeniedException exception) throws IOException, ServletException {
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(RestBean.forbidden(exception.getMessage()).asJsonString()); // 返回被禁止访问的响应

    }

    public void onUnauthorized(HttpServletRequest request,
                               HttpServletResponse response,
                               AuthenticationException exception) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(RestBean.unauthorized(exception.getMessage()).asJsonString()); // 返回未授权的响应

    }

    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        response.setContentType("application/json;charset=utf-8");
        User user = (User) authentication.getPrincipal(); // 获取用户信息
        Account account = service.findAccountByNameOrEmail(user.getUsername()); // 查询用户账户信息
        String token = utils.createJwt(user, account.getId(), account.getUsername()); // 创建 JWT
        AuthorizeVO vo = new AuthorizeVO();
        vo.setExpire(utils.expireTime()); // 设置 JWT 过期时间
        vo.setRole(account.getRole()); // 设置用户角色
        vo.setToken(token); // 设置 JWT
        vo.setUsername(account.getUsername()); // 设置用户名
        response.getWriter().write(RestBean.success(vo).asJsonString()); // 返回认证成功的响应
    }

    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        String authorization = request.getHeader("Authorization"); // 获取请求头中的 Authorization
        if (utils.invalidateJwt(authorization)) { // 使 JWT 失效
            writer.write(RestBean.success().asJsonString()); // 返回登出成功的响应
        } else {
            writer.write(RestBean.failure(400, "logout failed!").asJsonString()); // 返回登出失败的响应
        }
    }

    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(RestBean.unauthorized(exception.getMessage()).asJsonString()); // 返回认证失败的响应
    }
}
