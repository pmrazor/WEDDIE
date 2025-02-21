package com.ssafy.exhi.config;

import com.ssafy.exhi.interceptor.JWTInterceptor;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Slf4j
@Configuration
@AllArgsConstructor
@EnableAspectJAutoProxy
public class WebMvcConfiguration implements WebMvcConfigurer {
	
	private final List<String> patterns = List.of("");

	@Autowired
	private JWTInterceptor jwtInterceptor;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry
			.addMapping("/**")
				.allowedOrigins("https://weddie.ssafy.me")
//				.allowedOrigins("http://localhost:3000")
//				.allowedMethods(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(),
//						HttpMethod.DELETE.name(), HttpMethod.HEAD.name(), HttpMethod.OPTIONS.name(),
//						HttpMethod.PATCH.name())
//				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD")
				.allowCredentials(true).maxAge(3600)
				.allowedMethods("*")
				.maxAge(1800); // 1800초 동안 preflight 결과를 캐시에 저장

	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(jwtInterceptor)
				.excludePathPatterns("/**");
//				.excludePathPatterns("/css/**", "/images/**", "/js/**", "/user/login", "/user/signup", "/user/refresh");
	}

}
