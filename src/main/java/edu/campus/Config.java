package edu.campus;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class Config implements WebMvcConfigurer {
  @Override public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new HandlerInterceptor() {
      @Override public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler) throws Exception {
        if(!"POST".equals(request.getMethod()) || "/login".equals(request.getServletPath())) return true;
        Object token=request.getSession(false)==null?null:request.getSession(false).getAttribute("csrf");
        if(token==null || !token.equals(request.getHeader("X-CSRF-Token"))) { response.sendError(403,"CSRF token invalid"); return false; }
        return true;
      }
    });
  }
}