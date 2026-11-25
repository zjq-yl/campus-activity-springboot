package edu.campus.controller;

import edu.campus.mapper.Store;
import edu.campus.service.Business;
import edu.campus.service.StudentAdminService;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class Api {
  private final Store db; private final Business business; private final StudentAdminService students; private final AuthenticationManager authenticationManager;
  public Api(Store db,Business business,StudentAdminService students,AuthenticationManager authenticationManager) { this.db=db; this.business=business; this.students=students; this.authenticationManager=authenticationManager; }
  private long auth(HttpSession s,String role) {
    Object id=s.getAttribute("userId"); if(id==null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"请先登录");
    if(role!=null && !role.equals(s.getAttribute("role"))) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"权限不足");
    return (Long)id;
  }
  @PostMapping("/login") public Map<String,Object> login(@RequestParam String account,@RequestParam String password,HttpServletRequest request) {
    Authentication authentication;
    try { authentication=authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(account,password)); }
    catch(AuthenticationException error) { throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"账号或密码错误"); }
    Map<String,Object> u=db.user(account);
    HttpSession old=request.getSession(false); if(old!=null) old.invalidate();
    HttpSession s=request.getSession(true);
    s.setAttribute("userId",((Number)u.get("id")).longValue());
    s.setAttribute("role",u.get("role")); s.setAttribute("name",u.get("display_name"));
    SecurityContext context=SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);
    s.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,context);
    return Map.of("id",s.getAttribute("userId"),"role",s.getAttribute("role"),"name",s.getAttribute("name"));
  }
  @GetMapping("/me") public Map<String,Object> me(HttpSession s,HttpServletRequest request) { auth(s,null); CsrfToken token=(CsrfToken)request.getAttribute(CsrfToken.class.getName()); return Map.of("id",s.getAttribute("userId"),"role",s.getAttribute("role"),"name",s.getAttribute("name"),"csrf",token.getToken()); }
  @GetMapping("/activities") public List<Map<String,Object>> activities(@RequestParam(defaultValue="") String q,@RequestParam(defaultValue="") String category,@RequestParam(defaultValue="1") int page) { return db.activities(q,category,20,Math.min(10000,Math.max(0,page-1))*20); }
  @GetMapping("/activities/categories") public List<String> categories() { return db.categories(); }
  @GetMapping("/activities/{id}") public Map<String,Object> activity(@PathVariable long id) { Map<String,Object> a=db.publishedActivity(id); if(a==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND); return a; }
  @GetMapping("/owner/activities") public List<Map<String,Object>> owned(HttpSession s) { return db.owned(auth(s,"OWNER")); }
  @GetMapping("/my/activities") public List<Map<String,Object>> mine(HttpSession s) { return db.mine(auth(s,"STUDENT")); }
  @GetMapping("/my/profile") public Map<String,Object> profile(HttpSession s) { return db.profile(auth(s,"STUDENT")); }
  @GetMapping("/my/points") public Map<String,Object> points(HttpSession s) { return business.points(auth(s,"STUDENT")); }
  @GetMapping("/points/categories") public List<String> pointCategories() { return Business.POINT_CATEGORIES; }
  @GetMapping("/admin/activities") public List<Map<String,Object>> pending(HttpSession s) { auth(s,"ADMIN"); return db.pending(); }
  @GetMapping("/admin/reviews") public List<Map<String,Object>> allReviews(HttpSession s) { auth(s,"ADMIN"); return db.allReviews(); }
  @GetMapping("/admin/summary") public Map<String,Object> adminSummary(HttpSession s) { auth(s,"ADMIN"); return db.adminSummary(); }
  @GetMapping("/admin/students/data") public List<Map<String,Object>> students(HttpSession s,@RequestParam(defaultValue="") String q) { auth(s,"ADMIN"); return db.students(q); }
  @PostMapping("/admin/students") public void addStudent(HttpSession s,@RequestParam String account,@RequestParam String password,@RequestParam String name,@RequestParam String studentNo,@RequestParam(defaultValue="") String gender,@RequestParam Integer year,@RequestParam(defaultValue="") String college,@RequestParam(defaultValue="") String major,@RequestParam(defaultValue="") String className) { auth(s,"ADMIN"); students.create(account,password,name,studentNo,gender,year,college,major,className); }
  @PostMapping("/admin/students/{id}/edit") public void editStudent(@PathVariable long id,HttpSession s,@RequestParam String account,@RequestParam(defaultValue="") String password,@RequestParam String name,@RequestParam String studentNo,@RequestParam(defaultValue="") String gender,@RequestParam Integer year,@RequestParam(defaultValue="") String college,@RequestParam(defaultValue="") String major,@RequestParam(defaultValue="") String className) { auth(s,"ADMIN"); students.update(id,account,password,name,studentNo,gender,year,college,major,className); }
  @PostMapping("/admin/students/{id}/disable") public void disableStudent(@PathVariable long id,HttpSession s) { auth(s,"ADMIN"); students.disable(id); }
  @PostMapping("/owner/activities") public void create(HttpSession s,@RequestParam String title,@RequestParam String category,@RequestParam(defaultValue="思想成长") String pointCategory,@RequestParam(defaultValue="1") int points,@RequestParam(defaultValue="true") boolean checkinRequired,@RequestParam String location,@RequestParam(defaultValue="") String description,@RequestParam int capacity,@RequestParam String signupDeadline,@RequestParam String startAt,@RequestParam String endAt) {
    long id=auth(s,"OWNER");
    if(title.isBlank() || category.isBlank() || location.isBlank() || capacity<1 || signupDeadline.compareTo(startAt)>=0 || startAt.compareTo(endAt)>=0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"活动信息或时间无效");
    business.validatePoints(pointCategory,points);
    db.create(id,title,category,pointCategory,points,checkinRequired,location,description,capacity,signupDeadline.replace('T',' '),startAt.replace('T',' '),endAt.replace('T',' '));
  }
  @PostMapping("/owner/activities/{id}/submit") public void submit(@PathVariable long id,HttpSession s) { business.submit(id,auth(s,"OWNER")); }
  @PostMapping("/owner/activities/{id}/edit") public void edit(@PathVariable long id,HttpSession s,@RequestParam String title,@RequestParam String category,@RequestParam(defaultValue="思想成长") String pointCategory,@RequestParam(defaultValue="1") int points,@RequestParam(defaultValue="true") boolean checkinRequired,@RequestParam String location,@RequestParam(defaultValue="") String description,@RequestParam int capacity,@RequestParam String signupDeadline,@RequestParam String startAt,@RequestParam String endAt) {
    business.edit(id,auth(s,"OWNER"),title,category,pointCategory,points,checkinRequired,location,description,capacity,signupDeadline,startAt,endAt);
  }
  @PostMapping("/admin/activities/{id}/reviews") public void review(@PathVariable long id,HttpSession s,@RequestParam boolean approve,@RequestParam(defaultValue="") String remark) { business.review(id,auth(s,"ADMIN"),approve,remark); }
  @PostMapping("/activities/{id}/register") public void register(@PathVariable long id,HttpSession s) { business.register(id,auth(s,"STUDENT")); }
  @PostMapping("/activities/{id}/cancel-registration") public void cancel(@PathVariable long id,HttpSession s) { business.cancel(id,auth(s,"STUDENT")); }
  @PostMapping("/owner/activities/{id}/checkin/open") public Map<String,String> open(@PathVariable long id,HttpSession s) { return Map.of("code",business.open(id,auth(s,"OWNER"))); }
  @PostMapping("/owner/activities/{id}/checkin/close") public void close(@PathVariable long id,HttpSession s) { business.close(id,auth(s,"OWNER")); }
  @PostMapping("/activities/{id}/checkin") public void checkin(@PathVariable long id,HttpSession s,@RequestParam String code) { business.checkin(id,auth(s,"STUDENT"),code); }
  @GetMapping("/owner/activities/{id}/attendance") public Map<String,Object> attendance(@PathVariable long id,HttpSession s) { return business.stats(id,auth(s,"OWNER")); }
  @GetMapping("/owner/activities/{id}/reviews") public List<Map<String,Object>> reviews(@PathVariable long id,HttpSession s) { return business.reviews(id,auth(s,"OWNER")); }
}
