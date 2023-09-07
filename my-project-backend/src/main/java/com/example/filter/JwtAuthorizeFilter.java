package com.example.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthorizeFilter extends OncePerRequestFilter {

    @Resource
    JwtUtils utils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 从请求头中获取 Authorization 字段，通常包含用户的身份验证令牌
        String authorization = request.getHeader("Authorization");

        // 解析令牌，获取用户信息
        DecodedJWT jwt = utils.resolveJwt(authorization);

        // 如果成功解析出用户信息
        if (jwt != null) {
            // 将用户信息转换为 Spring Security 的 UserDetails 对象
            UserDetails user = utils.toUser(jwt);

            // 构建认证令牌
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

            // 设置认证令牌的详细信息，包括 IP 地址等
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 将认证令牌放入 Spring Security 的安全上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 将用户ID放入请求的属性中，以便后续处理使用
            request.setAttribute("id", utils.toId(jwt));
        }

        // 继续处理请求的传递，执行后续的过滤器或处理器
        filterChain.doFilter(request, response);
    }

}
