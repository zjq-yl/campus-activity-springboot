# 校园活动报名与签到系统

基于 Spring Boot、Spring MVC、MyBatis、Thymeleaf 和 MySQL 的校园活动系统。使用 JDK 17，Spring Boot 内置 Web 服务器。

## 启动

1. 在 MySQL 8 中执行 [建表脚本](sql/schema.sql)。
2. 设置 `DB_URL`、`DB_USER`、`DB_PASSWORD` 环境变量。默认 URL 指向本机 `campus_activity`。
3. 运行 `mvn exec:java -Dexec.mainClass=edu.campus.Seed`，创建演示账号 `admin`、`owner`、`student1`、`student2`、`student3` 和示例活动；默认密码为 `demo123`，可用 `DEMO_PASSWORD` 改写。重复运行不会重复创建示例活动。
4. 运行 `mvn package`，再运行 `java -jar target/activity-signin-1.0.0.jar`，打开 `http://localhost:8080/`。

演示顺序：负责人创建草稿并提交 → 管理员通过 → 学生报名 → 活动开始后负责人开启签到 → 学生输入现场签到码 → 负责人查看统计。活动时间需覆盖当前时间才能开启签到。

## 说明

报名和取消在数据库事务中锁定活动行，容量不会超额。签到码只存 BCrypt 哈希，错误次数累计到五次后拒绝继续签到。所有写接口需要登录后的 `X-CSRF-Token`；页面自动发送。

本机需要 JDK 17、Maven 和 MySQL 8。无需单独安装 Tomcat。

项目的普通 Git 仓库位于本目录。运行 `git log --oneline` 可查看每一步的本地提交。
