package com.freediving.config;

import com.freediving.web.AdminTokenInterceptor;
import com.freediving.web.UserTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AdminTokenInterceptor adminTokenInterceptor;
    private final UserTokenInterceptor userTokenInterceptor;

    public WebConfig(AdminTokenInterceptor adminTokenInterceptor, UserTokenInterceptor userTokenInterceptor) {
        this.adminTokenInterceptor = adminTokenInterceptor;
        this.userTokenInterceptor = userTokenInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminTokenInterceptor)
                .addPathPatterns("/api/admin/**");
        registry.addInterceptor(userTokenInterceptor)
                .addPathPatterns("/api/user/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
