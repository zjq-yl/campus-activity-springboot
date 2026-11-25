package edu.campus.service;

import edu.campus.mapper.Store;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StudentAdminService {
  private final Store db;
  private final PasswordEncoder encoder;
  public StudentAdminService(Store db,PasswordEncoder encoder) { this.db=db; this.encoder=encoder; }

  private void validate(String account,String name,String studentNo,Integer year) {
    if(account==null || !account.matches("[A-Za-z0-9_]{3,50}") || name==null || name.isBlank() || name.length()>50 || studentNo==null || !studentNo.matches("[A-Za-z0-9-]{3,30}") || year==null || year<2000 || year>2100)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"请填写有效的账号、姓名、学号和入学年份");
  }
  private String hash(String password) {
    if(password==null || password.length()<6) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"密码至少六位");
    return encoder.encode(password);
  }
  @Transactional public void create(String account,String password,String name,String studentNo,String gender,Integer year,String college,String major,String className) {
    validate(account,name,studentNo,year);
    String encoded=hash(password);
    try { db.addStudent(account,encoded,name,studentNo,gender,year,college,major,className); }
    catch(DataIntegrityViolationException error) { throw new ResponseStatusException(HttpStatus.CONFLICT,"账号或学号已存在"); }
  }
  @Transactional public void update(long id,String account,String password,String name,String studentNo,String gender,Integer year,String college,String major,String className) {
    validate(account,name,studentNo,year);
    if(db.activeStudent(id)==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"学生不存在");
    try {
      db.updateStudent(id,account,name,studentNo,gender,year,college,major,className);
      if(password!=null && !password.isBlank()) db.resetStudentPassword(id,hash(password));
    } catch(DataIntegrityViolationException error) { throw new ResponseStatusException(HttpStatus.CONFLICT,"账号或学号已存在"); }
  }
  @Transactional public void disable(long id) {
    if(db.disableStudent(id)==0) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"学生不存在");
  }
}
