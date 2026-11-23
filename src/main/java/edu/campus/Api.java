package edu.campus;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class Api {
  private final Store db; private final Business business;
  private final BCryptPasswordEncoder bcrypt=new BCryptPasswordEncoder();
  Api(Store db,Business business) { this.db=db; this.business=business; }
  private long auth(HttpSession s,String role) {
    Object id=s.getAttribute("userId"); if(id==null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"请先登录");
    if(role!=null && !role.equals(s.getAttribute("role"))) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"权限不足");
    return (Long)id;
  }
  @PostMapping("/login") public Map<String,Object> login(@RequestParam String account,@RequestParam String password,HttpSession s) {
    Map<String,Object> u=db.user(account);
    if(u==null || !bcrypt.matches(password,(String)u.get("password_hash"))) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"账号或密码错误");
    s.invalidate();
    // A new session avoids session fixation.
    return Map.of("message","请刷新页面以建立新会话");
  }
  @PostMapping("/logout") public void logout(HttpSession s) { s.invalidate(); }
  @GetMapping("/me") public Map<String,Object> me(HttpSession s) { auth(s,null); return Map.of("id",s.getAttribute("userId"),"role",s.getAttribute("role"),"name",s.getAttribute("name"),"csrf",s.getAttribute("csrf")); }
  @GetMapping("/activities") public List<Map<String,Object>> activities(@RequestParam(defaultValue="") String q,@RequestParam(defaultValue="1") int page) { return db.activities(q,20,Math.max(0,page-1)*20); }
  @GetMapping("/owner/activities") public List<Map<String,Object>> owned(HttpSession s) { return db.owned(auth(s,"OWNER")); }
  @GetMapping("/admin/activities") public List<Map<String,Object>> pending(HttpSession s) { auth(s,"ADMIN"); return db.pending(); }
  @PostMapping("/owner/activities") public void create(HttpSession s,@RequestParam String title,@RequestParam String category,@RequestParam String location,@RequestParam(defaultValue="") String description,@RequestParam int capacity,@RequestParam String signupDeadline,@RequestParam String startAt,@RequestParam String endAt) {
    long id=auth(s,"OWNER");
    if(title.isBlank() || category.isBlank() || location.isBlank() || capacity<1 || signupDeadline.compareTo(startAt)>=0 || startAt.compareTo(endAt)>=0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"活动信息或时间无效");
    db.create(id,title,category,location,description,capacity,signupDeadline,startAt,endAt);
  }
  @PostMapping("/owner/activities/{id}/submit") public void submit(@PathVariable long id,HttpSession s) { business.submit(id,auth(s,"OWNER")); }
  @PostMapping("/admin/activities/{id}/reviews") public void review(@PathVariable long id,HttpSession s,@RequestParam boolean approve,@RequestParam(defaultValue="") String remark) { business.review(id,auth(s,"ADMIN"),approve,remark); }
  @PostMapping("/activities/{id}/register") public void register(@PathVariable long id,HttpSession s) { business.register(id,auth(s,"STUDENT")); }
  @PostMapping("/activities/{id}/cancel-registration") public void cancel(@PathVariable long id,HttpSession s) { business.cancel(id,auth(s,"STUDENT")); }
  @PostMapping("/owner/activities/{id}/checkin/open") public Map<String,String> open(@PathVariable long id,HttpSession s) { return Map.of("code",business.open(id,auth(s,"OWNER"))); }
  @PostMapping("/owner/activities/{id}/checkin/close") public void close(@PathVariable long id,HttpSession s) { business.close(id,auth(s,"OWNER")); }
  @PostMapping("/activities/{id}/checkin") public void checkin(@PathVariable long id,HttpSession s,@RequestParam String code) { business.checkin(id,auth(s,"STUDENT"),code); }
  @GetMapping("/owner/activities/{id}/attendance") public Map<String,Object> attendance(@PathVariable long id,HttpSession s) { return business.stats(id,auth(s,"OWNER")); }
}
