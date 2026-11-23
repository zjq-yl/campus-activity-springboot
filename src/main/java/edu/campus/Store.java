package edu.campus;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.*;

public interface Store {
  @Select("SELECT * FROM sys_user WHERE account=#{account}") Map<String,Object> user(String account);
  @Select("SELECT * FROM activity WHERE id=#{id}") Map<String,Object> activity(long id);
  @Select("SELECT * FROM activity WHERE id=#{id} FOR UPDATE") Map<String,Object> lockActivity(long id);
  @Select("SELECT * FROM activity WHERE status='PUBLISHED' AND (#{q}='' OR title LIKE CONCAT('%',#{q},'%')) AND (#{category}='' OR category=#{category}) ORDER BY start_at,id LIMIT #{limit} OFFSET #{offset}") List<Map<String,Object>> activities(@Param("q") String q,@Param("category") String category,@Param("limit") int limit,@Param("offset") int offset);
  @Select("SELECT DISTINCT category FROM activity WHERE status='PUBLISHED' ORDER BY category") List<String> categories();
  @Select("SELECT * FROM activity WHERE owner_id=#{owner} ORDER BY id DESC") List<Map<String,Object>> owned(long owner);
  @Select("SELECT a.*,r.status AS registration_status,t.signed_at FROM registration r JOIN activity a ON a.id=r.activity_id LEFT JOIN attendance t ON t.registration_id=r.id WHERE r.student_id=#{student} ORDER BY a.start_at DESC") List<Map<String,Object>> mine(long student);
  @Select("SELECT * FROM activity WHERE status='PENDING' ORDER BY id") List<Map<String,Object>> pending();
  @Insert("INSERT INTO activity(owner_id,title,category,location,description,capacity,signup_deadline,start_at,end_at) VALUES(#{owner},#{title},#{category},#{location},#{description},#{capacity},#{deadline},#{start},#{end})")
  void create(@Param("owner") long owner,@Param("title") String title,@Param("category") String category,@Param("location") String location,@Param("description") String description,@Param("capacity") int capacity,@Param("deadline") String deadline,@Param("start") String start,@Param("end") String end);
  @Update("UPDATE activity SET title=#{title},category=#{category},location=#{location},description=#{description},capacity=#{capacity},signup_deadline=#{deadline},start_at=#{start},end_at=#{end} WHERE id=#{id}")
  void edit(@Param("id") long id,@Param("title") String title,@Param("category") String category,@Param("location") String location,@Param("description") String description,@Param("capacity") int capacity,@Param("deadline") String deadline,@Param("start") String start,@Param("end") String end);
  @Update("UPDATE activity SET status=#{status} WHERE id=#{id}") void status(@Param("id") long id,@Param("status") String status);
  @Insert("INSERT INTO activity_review(activity_id,reviewer_id,result,remark,reviewed_at) VALUES(#{id},#{reviewer},#{result},#{remark},NOW())") void review(@Param("id") long id,@Param("reviewer") long reviewer,@Param("result") String result,@Param("remark") String remark);
  @Select("SELECT * FROM registration WHERE activity_id=#{id} AND student_id=#{student}") Map<String,Object> registration(@Param("id") long id,@Param("student") long student);
  @Insert("INSERT INTO registration(activity_id,student_id,status,registered_at) VALUES(#{id},#{student},'ACTIVE',NOW())") void register(@Param("id") long id,@Param("student") long student);
  @Update("UPDATE registration SET status='ACTIVE',registered_at=NOW(),cancelled_at=NULL,failed_checkins=0 WHERE id=#{id}") void reactivate(long id);
  @Update("UPDATE registration SET status='CANCELLED',cancelled_at=NOW() WHERE id=#{id}") void cancel(long id);
  @Update("UPDATE activity SET enrolled_count=enrolled_count+#{delta} WHERE id=#{id}") void count(@Param("id") long id,@Param("delta") int delta);
  @Select("SELECT * FROM checkin_session WHERE activity_id=#{id}") Map<String,Object> session(long id);
  @Insert("INSERT INTO checkin_session(activity_id,code_hash,opened_at,closes_at) VALUES(#{id},#{hash},NOW(),DATE_ADD(NOW(),INTERVAL 10 MINUTE))") void open(@Param("id") long id,@Param("hash") String hash);
  @Update("UPDATE checkin_session SET closed_at=NOW() WHERE activity_id=#{id} AND closed_at IS NULL") void close(long id);
  @Select("SELECT * FROM attendance WHERE registration_id=#{id}") Map<String,Object> attendance(long id);
  @Insert("INSERT INTO attendance(registration_id,signed_at) VALUES(#{id},NOW())") void sign(long id);
  @Update("UPDATE registration SET failed_checkins=failed_checkins+1 WHERE id=#{id}") void failed(long id);
  @Select("SELECT r.id,r.student_id,u.display_name,r.status,a.signed_at FROM registration r JOIN sys_user u ON u.id=r.student_id LEFT JOIN attendance a ON a.registration_id=r.id WHERE r.activity_id=#{id} ORDER BY r.id") List<Map<String,Object>> roster(long id);
}
