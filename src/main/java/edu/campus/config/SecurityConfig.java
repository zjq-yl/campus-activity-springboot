package edu.campus.config;

import edu.campus.mapper.Store;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
  @Bean UserDetailsService users(Store db) {
    return account -> {
      Map<String,Object> row=db.user(account);
      if(row==null) throw new UsernameNotFoundException("账号不存在");
      return User.withUsername(account).password((String)row.get("password_hash")).roles((String)row.get("role")).build();
    };
  }
  @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
  @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception { return configuration.getAuthenticationManager(); }
  @Bean SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.ignoringRequestMatchers("/login"));
    http.authorizeHttpRequests(auth -> auth
      .requestMatchers("/", "/login", "/css/**", "/js/**", "/error").permitAll()
      .requestMatchers(HttpMethod.GET,"/activities/**", "/points/categories").permitAll()
      .requestMatchers(HttpMethod.POST,"/activities/**").hasRole("STUDENT")
      .requestMatchers("/hall", "/student", "/my/**").hasRole("STUDENT")
      .requestMatchers("/owner", "/owner/**").hasRole("OWNER")
      .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
      .anyRequest().authenticated());
    http.exceptionHandling(errors -> errors.authenticationEntryPoint((request,response,error)->response.sendError(401))
      .accessDeniedHandler((request,response,error)->response.sendError(403)));
    http.formLogin(form -> form.disable());
    http.httpBasic(basic -> basic.disable());
    http.logout(logout -> logout.logoutUrl("/logout").logoutSuccessHandler((request,response,authentication)->response.setStatus(204)));
    return http.build();
  }
}
