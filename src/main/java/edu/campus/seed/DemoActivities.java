package edu.campus.seed;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/** Idempotent showcase data for the student pages and the organizer dashboard. */
final class DemoActivities {
  private DemoActivities() {}
  private record Event(String title,String category,String pointCategory,int points,boolean checkin,int day,String location,String description,int capacity) {}
  private static final Event[] EVENTS={
    new Event("青春思政主题讲座","主题讲座","思想成长",3,true,-12,"图书馆报告厅","围绕青年成长与校园责任开展主题分享和互动交流。",80),
    new Event("社区服务实践日","社会实践","实践实习",4,false,-10,"东湖社区服务站","走进社区，参与便民服务与社会调查。",30),
    new Event("校园环保志愿行动","志愿服务","志愿公益",5,true,-8,"学校南门广场","分组开展垃圾分类宣传和校园环境整理。",40),
    new Event("创新创业项目路演","创新竞赛","创新创业",6,false,-6,"创新中心 201","学生团队展示项目成果，交流项目设计与答辩经验。",60),
    new Event("秋季校园羽毛球赛","体育比赛","文体活动",4,true,-4,"体育馆羽毛球场","面向全校学生的羽毛球友谊赛。",32),
    new Event("学生骨干能力工作坊","学生工作","社会工作",3,false,-3,"学生活动中心 305","学习活动策划、团队协作与现场组织。",45),
    new Event("Python 数据分析训练营","技能培训","技能特长",5,true,-2,"实验楼 B204","从数据清洗到可视化，完成一次实践练习。",35),
    new Event("校园急救知识普及","公益讲座","志愿公益",2,true,-1,"教学楼 C102","学习基础急救知识与现场应对方法。",50),
    new Event("青年成长与理想分享会","主题讲座","思想成长",3,true,1,"图书馆报告厅","优秀学生分享学习经历与成长规划。",60),
    new Event("周末城市观察实践","社会实践","实践实习",4,false,2,"学校东门集合","走访城市公共空间并提交观察记录。",25),
    new Event("校园创意设计大赛","创新竞赛","创新创业",6,true,3,"创新中心一楼","围绕校园生活提出设计方案并现场展示。",40),
    new Event("冬季趣味运动会","体育比赛","文体活动",4,false,5,"田径场","团队协作与趣味项目相结合的校园运动会。",100),
    new Event("英语表达与演讲实践","技能培训","技能特长",3,true,7,"教学楼 A208","进行英文表达训练和小组展示。",50)
  };
  static void populate(Connection db) throws SQLException {
    long owner=user(db,"owner"),admin=user(db,"admin"),student1=user(db,"student1"),student2=user(db,"student2"),student3=user(db,"student3");
    LocalDateTime base=LocalDateTime.now().withHour(14).withMinute(0).withSecond(0).withNano(0);
    for(int i=0;i<EVENTS.length;i++) {
      Event event=EVENTS[i];
      long id=activity(db,owner,event,base.plusDays(event.day));
      if(event.day<0) {
        registration(db,id,student1,true,base.plusDays(event.day-2));
        if(i!=7 && event.checkin) attendance(db,id,student1,base.plusDays(event.day).plusMinutes(12));
        if(i%3==0) registration(db,id,student2,false,base.plusDays(event.day-2));
        else registration(db,id,student2,true,base.plusDays(event.day-2));
        if(i%2==0) registration(db,id,student3,true,base.plusDays(event.day-2));
      } else if(i==8 || i==10) {
        registration(db,id,student1,true,LocalDateTime.now());
        registration(db,id,student2,true,LocalDateTime.now());
      }
      recount(db,id);
      audit(db,id,admin,base.plusDays(event.day-4));
    }
  }
  private static long user(Connection db,String account) throws SQLException {
    try(PreparedStatement s=db.prepareStatement("SELECT id FROM sys_user WHERE account=?")) {
      s.setString(1,account);try(ResultSet r=s.executeQuery()){if(r.next())return r.getLong(1);}
    }
    throw new SQLException("Missing demo account: "+account);
  }
  private static long activity(Connection db,long owner,Event e,LocalDateTime start) throws SQLException {
    try(PreparedStatement s=db.prepareStatement("INSERT INTO activity(owner_id,title,category,point_category,points,checkin_required,location,description,capacity,status,signup_deadline,start_at,end_at) SELECT ?,?,?,?,?,?,?,?,?, 'PUBLISHED',?,?,? WHERE NOT EXISTS (SELECT 1 FROM activity WHERE owner_id=? AND title=?)")) {
      s.setLong(1,owner);s.setString(2,e.title);s.setString(3,e.category);s.setString(4,e.pointCategory);s.setInt(5,e.points);s.setBoolean(6,e.checkin);s.setString(7,e.location);s.setString(8,e.description);s.setInt(9,e.capacity);s.setTimestamp(10,Timestamp.valueOf(start.minusHours(6)));s.setTimestamp(11,Timestamp.valueOf(start));s.setTimestamp(12,Timestamp.valueOf(start.plusHours(2)));s.setLong(13,owner);s.setString(14,e.title);s.executeUpdate();
    }
    try(PreparedStatement s=db.prepareStatement("SELECT id FROM activity WHERE owner_id=? AND title=?")){s.setLong(1,owner);s.setString(2,e.title);try(ResultSet r=s.executeQuery()){r.next();return r.getLong(1);}}
  }
  private static void registration(Connection db,long activity,long student,boolean active,LocalDateTime at) throws SQLException {
    try(PreparedStatement s=db.prepareStatement("INSERT INTO registration(activity_id,student_id,status,registered_at,cancelled_at) SELECT ?,?,?,?,? WHERE NOT EXISTS (SELECT 1 FROM registration WHERE activity_id=? AND student_id=?)")) {
      s.setLong(1,activity);s.setLong(2,student);s.setString(3,active?"ACTIVE":"CANCELLED");s.setTimestamp(4,Timestamp.valueOf(at));s.setTimestamp(5,active?null:Timestamp.valueOf(at.plusHours(1)));s.setLong(6,activity);s.setLong(7,student);s.executeUpdate();
    }
  }
  private static void attendance(Connection db,long activity,long student,LocalDateTime at) throws SQLException {
    try(PreparedStatement s=db.prepareStatement("INSERT INTO attendance(registration_id,signed_at) SELECT r.id,? FROM registration r WHERE r.activity_id=? AND r.student_id=? AND r.status='ACTIVE' AND NOT EXISTS (SELECT 1 FROM attendance WHERE registration_id=r.id)")) {
      s.setTimestamp(1,Timestamp.valueOf(at));s.setLong(2,activity);s.setLong(3,student);s.executeUpdate();
    }
  }
  private static void recount(Connection db,long id) throws SQLException {
    try(PreparedStatement s=db.prepareStatement("UPDATE activity SET enrolled_count=(SELECT COUNT(*) FROM registration WHERE activity_id=? AND status='ACTIVE') WHERE id=?")){s.setLong(1,id);s.setLong(2,id);s.executeUpdate();}
  }
  private static void audit(Connection db,long activity,long admin,LocalDateTime at) throws SQLException {
    try(PreparedStatement s=db.prepareStatement("INSERT INTO activity_review(activity_id,reviewer_id,result,remark,reviewed_at) SELECT ?,?,'APPROVED','演示数据审核通过',? WHERE NOT EXISTS (SELECT 1 FROM activity_review WHERE activity_id=? AND result='APPROVED')")) {
      s.setLong(1,activity);s.setLong(2,admin);s.setTimestamp(3,Timestamp.valueOf(at));s.setLong(4,activity);s.executeUpdate();
    }
  }
}
