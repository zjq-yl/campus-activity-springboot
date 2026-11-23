package edu.campus;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.security.SecureRandom;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class Business {
  private final Store db;
  private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
  private final SecureRandom random = new SecureRandom();
  Business(Store db) { this.db = db; }
  private ResponseStatusException bad(String message) { return new ResponseStatusException(HttpStatus.CONFLICT, message); }
  private LocalDateTime time(Object value) { return ((Timestamp)value).toLocalDateTime(); }
  private long number(Object value) { return ((Number)value).longValue(); }
  private Map<String,Object> required(long id) { Map<String,Object> a=db.activity(id); if(a==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND); return a; }
  private void owner(Map<String,Object> a,long user) { if(number(a.get("owner_id"))!=user) throw new ResponseStatusException(HttpStatus.FORBIDDEN); }
  @Transactional public void submit(long id,long user) {
    Map<String,Object> a=db.lockActivity(id); if(a==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND); owner(a,user);
    if(!List.of("DRAFT","REJECTED").contains(a.get("status"))) throw bad("当前状态不能提交审核");
    db.status(id,"PENDING");
  }
  @Transactional public void review(long id,long reviewer,boolean approve,String remark) {
    Map<String,Object> a=db.lockActivity(id); if(a==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    if(!"PENDING".equals(a.get("status"))) throw bad("活动未待审核");
    if(approve && !time(a.get("signup_deadline")).isAfter(LocalDateTime.now())) throw bad("报名截止时间已过");
    if(!approve && (remark==null || remark.isBlank())) throw bad("驳回需要理由");
    db.review(id,reviewer,approve?"APPROVED":"REJECTED",remark); db.status(id,approve?"PUBLISHED":"REJECTED");
  }
  @Transactional public void register(long id,long student) {
    Map<String,Object> a=db.lockActivity(id); if(a==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    if(!"PUBLISHED".equals(a.get("status")) || !time(a.get("signup_deadline")).isAfter(LocalDateTime.now())) throw bad("报名已关闭");
    Map<String,Object> r=db.registration(id,student); if(r!=null && "ACTIVE".equals(r.get("status"))) return;
    if(number(a.get("enrolled_count"))>=number(a.get("capacity"))) throw bad("名额已满");
    if(r==null) db.register(id,student); else db.reactivate(number(r.get("id")));
    db.count(id,1);
  }
  @Transactional public void cancel(long id,long student) {
    Map<String,Object> a=db.lockActivity(id); if(a==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    if(!time(a.get("signup_deadline")).isAfter(LocalDateTime.now())) throw bad("已过取消期限");
    Map<String,Object> r=db.registration(id,student);
    if(r==null || !"ACTIVE".equals(r.get("status"))) return;
    db.cancel(number(r.get("id"))); db.count(id,-1);
  }
  @Transactional public String open(long id,long user) {
    Map<String,Object> a=db.lockActivity(id); if(a==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND); owner(a,user);
    LocalDateTime now=LocalDateTime.now();
    if(!"PUBLISHED".equals(a.get("status")) || now.isBefore(time(a.get("start_at"))) || !now.isBefore(time(a.get("end_at")))) throw bad("活动不在签到时间内");
    if(db.session(id)!=null) throw bad("签到场次已存在");
    String code=String.format("%06d",random.nextInt(1_000_000)); db.open(id,bcrypt.encode(code)); return code;
  }
  @Transactional public void close(long id,long user) {
    Map<String,Object> a=db.lockActivity(id); if(a==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND); owner(a,user); db.close(id);
  }
  @Transactional public void checkin(long id,long student,String code) {
    Map<String,Object> a=db.lockActivity(id); if(a==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    Map<String,Object> r=db.registration(id,student);
    if(r==null || !"ACTIVE".equals(r.get("status"))) throw bad("没有有效报名");
    long rid=number(r.get("id")); if(db.attendance(rid)!=null) return;
    Map<String,Object> s=db.session(id); LocalDateTime now=LocalDateTime.now();
    if(s==null || s.get("closed_at")!=null || now.isBefore(time(s.get("opened_at"))) || !now.isBefore(time(s.get("closes_at"))) || !now.isBefore(time(a.get("end_at")))) throw bad("签到已关闭");
    if(number(r.get("failed_checkins"))>=5) throw bad("错误次数已达上限，请联系负责人");
    if(code==null || !bcrypt.matches(code,(String)s.get("code_hash"))) { db.failed(rid); return; }
    db.sign(rid);
  }
  public Map<String,Object> stats(long id,long user) {
    Map<String,Object> a=required(id); owner(a,user);
    List<Map<String,Object>> rows=db.roster(id); long active=rows.stream().filter(r->"ACTIVE".equals(r.get("status"))).count();
    long signed=rows.stream().filter(r->"ACTIVE".equals(r.get("status")) && r.get("signed_at")!=null).count();
    return Map.of("registered",active,"signed",signed,"absent",active-signed,"rate",active==0?0.0:100.0*signed/active,"roster",rows);
  }
}
