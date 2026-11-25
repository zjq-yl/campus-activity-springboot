package edu.campus.seed;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Run manually for the classroom demo; never invoked by the web application. */
public class Seed {
  public static void main(String[] args) throws Exception {
    String url=env("DB_URL","jdbc:mysql://localhost:3306/campus_activity?serverTimezone=Asia/Shanghai");
    String password=env("DEMO_PASSWORD","demo123");
    String hash=new BCryptPasswordEncoder().encode(password);
    try(Connection c=DriverManager.getConnection(url,env("DB_USER","root"),env("DB_PASSWORD",""));
        PreparedStatement p=c.prepareStatement("INSERT INTO sys_user(account,password_hash,display_name,role) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE password_hash=VALUES(password_hash),display_name=VALUES(display_name),role=VALUES(role)")) {
      for(String[] row:new String[][]{{"admin","管理员","ADMIN"},{"owner","活动负责人","OWNER"},{"student1","学生甲","STUDENT"},{"student2","学生乙","STUDENT"},{"student3","学生丙","STUDENT"}}) {
        p.setString(1,row[0]);p.setString(2,hash);p.setString(3,row[1]);p.setString(4,row[2]);p.executeUpdate();
      }
      try(PreparedStatement profile=c.prepareStatement("UPDATE sys_user SET student_no=?,gender=?,enrollment_year=?,college=?,major=?,class_name=? WHERE account=?")) {
        for(int i=1;i<=3;i++) {
          profile.setString(1,"2026000"+i);profile.setString(2,i==2?"女":"男");profile.setInt(3,2026);profile.setString(4,"计算机学院");profile.setString(5,"软件工程");profile.setString(6,"软件工程 1 班");profile.setString(7,"student"+i);profile.executeUpdate();
        }
      }
      for(String[] row:new String[][]{{"校园技术分享会","讲座","教学楼 A101","PUBLISHED"},{"周末志愿服务","志愿服务","校园服务中心","PENDING"}}) {
        try(PreparedStatement activity=c.prepareStatement("INSERT INTO activity(owner_id,title,category,location,description,capacity,status,signup_deadline,start_at,end_at) SELECT id,?,?,?,?,2,?,DATE_ADD(NOW(),INTERVAL 1 DAY),DATE_ADD(NOW(),INTERVAL 2 DAY),DATE_ADD(DATE_ADD(NOW(),INTERVAL 2 DAY),INTERVAL 2 HOUR) FROM sys_user WHERE account='owner' AND NOT EXISTS (SELECT 1 FROM activity WHERE title=? AND owner_id=sys_user.id)")) {
          activity.setString(1,row[0]);activity.setString(2,row[1]);activity.setString(3,row[2]);activity.setString(4,"课程演示活动");activity.setString(5,row[3]);activity.setString(6,row[0]);activity.executeUpdate();
        }
      }
    }
    System.out.println("Demo accounts: admin, owner, student1, student2, student3; password: "+password);
  }
  private static String env(String name,String fallback) { String value=System.getenv(name);return value==null?fallback:value; }
}
