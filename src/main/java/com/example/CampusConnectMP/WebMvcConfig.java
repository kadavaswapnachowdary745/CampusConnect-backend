package com.example.CampusConnectMP;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger logger = Logger.getLogger(WebMvcConfig.class.getName());

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir);
        String uploadAbsolutePath = uploadPath.toFile().getAbsolutePath();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadAbsolutePath + "/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoggingInterceptor());
    }

    private static class LoggingInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            logger.info(String.format("Incoming request: %s %s from %s",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr()));
            return true;
        }

        @Override
        public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, 
                              org.springframework.web.servlet.ModelAndView modelAndView) throws Exception {
            // No-op
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, 
                                   Exception ex) throws Exception {
            // No-op
        }
    }
}
