package edu.campus;

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
        PreparedStatement p=c.prepareStatement("INSERT INTO sys_user(account,password_hash,display_name,role) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE display_name=VALUES(display_name),role=VALUES(role)")) {
      for(String[] row:new String[][]{{"admin","管理员","ADMIN"},{"owner","活动负责人","OWNER"},{"student1","学生甲","STUDENT"},{"student2","学生乙","STUDENT"}}) {
        p.setString(1,row[0]);p.setString(2,hash);p.setString(3,row[1]);p.setString(4,row[2]);p.executeUpdate();
      }
    }
    System.out.println("Demo accounts: admin, owner, student1, student2; password: "+password);
  }
  private static String env(String name,String fallback) { String value=System.getenv(name);return value==null?fallback:value; }
}
