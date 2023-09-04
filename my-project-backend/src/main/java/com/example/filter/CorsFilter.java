package com.example.filter;

import com.example.utils.Const;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 跨域资源共享（CORS）过滤器类，为HTTP响应添加CORS头部。
 * 允许来自不同域的控制访问资源。
 */
@Component
@Order(Const.ORDER_CORS)
public class CorsFilter extends HttpFilter {

    /**
     * 过滤并增强HTTP响应，在将请求传递给过滤器链中的下一个过滤器之前添加CORS头部。
     *
     * @param request  传入的HTTP Servlet请求。
     * @param response 传出的HTTP Servlet响应。
     * @param chain    用于处理请求和响应的过滤器链。
     * @throws IOException      如果在过滤处理期间发生I/O错误。
     * @throws ServletException 如果在过滤处理期间发生与Servlet相关的错误。
     */
    @Override
    protected void doFilter(HttpServletRequest request,
                            HttpServletResponse response,
                            FilterChain chain) throws IOException, ServletException {
        // 添加CORS头部到响应中
        this.addCorsHeader(request, response);

        // 通过过滤器链继续处理请求
        chain.doFilter(request, response);
    }

    /**
     * 根据请求的来源和允许的HTTP方法，向HTTP响应添加CORS头部。
     *
     * @param request  HTTP Servlet请求。
     * @param response HTTP Servlet响应。
     */
    private void addCorsHeader(HttpServletRequest request, HttpServletResponse response) {
        // 将允许的来源设置为请求中的"Origin"头部值
        response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));

        // 定义CORS请求的允许的HTTP方法
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");

        // 指定CORS请求的允许的头部
        response.addHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
    }
}
