package edu.campus;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration @EnableWebMvc @EnableTransactionManagement
@ComponentScan("edu.campus") @MapperScan("edu.campus")
public class Config implements WebMvcConfigurer {
  @Bean public DataSource dataSource() {
    HikariDataSource ds = new HikariDataSource();
    ds.setJdbcUrl(env("DB_URL", "jdbc:mysql://localhost:3306/campus_activity?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai"));
    ds.setUsername(env("DB_USER", "root")); ds.setPassword(env("DB_PASSWORD", ""));
    return ds;
  }
  private String env(String name, String fallback) { String v = System.getenv(name); return v == null ? fallback : v; }
  @Bean public SqlSessionFactory sqlSessionFactory(DataSource ds) throws Exception {
    SqlSessionFactoryBean bean = new SqlSessionFactoryBean(); bean.setDataSource(ds);
    return bean.getObject();
  }
  @Bean public DataSourceTransactionManager transactionManager(DataSource ds) { return new DataSourceTransactionManager(ds); }
  @Bean public InternalResourceViewResolver viewResolver() {
    InternalResourceViewResolver r = new InternalResourceViewResolver();
    r.setPrefix("/WEB-INF/views/"); r.setSuffix(".jsp"); return r;
  }
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
