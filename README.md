# 校园活动报名与签到系统

按课程设计报告实现的 Spring MVC 5.3、Spring Service、MyBatis、JSP、MySQL 8 应用。使用 JDK 17、Tomcat 9（`javax.servlet`）。

## 启动

1. 在 MySQL 8 中执行 [建表脚本](sql/schema.sql)。
2. 设置 `DB_URL`、`DB_USER`、`DB_PASSWORD` 环境变量。默认 URL 指向本机 `campus_activity`。
3. 运行 `mvn exec:java -Dexec.mainClass=edu.campus.Seed`，创建演示账号 `admin`、`owner`、`student1`、`student2`；默认密码为 `demo123`，可用 `DEMO_PASSWORD` 改写。
4. 运行 `mvn package`，把 `target/activity-signin-1.0.0.war` 部署到 Tomcat 9，打开 `/activity-signin-1.0.0/`。

演示顺序：负责人创建草稿并提交 → 管理员通过 → 学生报名 → 活动开始后负责人开启签到 → 学生输入现场签到码 → 负责人查看统计。活动时间需覆盖当前时间才能开启签到。

## 说明

报名和取消在数据库事务中锁定活动行，容量不会超额。签到码只存 BCrypt 哈希，错误次数累计到五次后拒绝继续签到。所有写接口需要登录后的 `X-CSRF-Token`；页面自动发送。

本机若没有 JDK 17、Maven、MySQL 8 和 Tomcat 9，需先安装这些运行依赖。

在 Windows PowerShell 中查看本项目的 Tomcat 日志时，运行 `powershell -File scripts/view-logs.ps1`。日志文件使用 UTF-8；直接用未指定编码的 `Get-Content` 读取会显示乱码。查看应用错误可传入 `-Name localhost`。

项目的普通 Git 仓库位于本目录。运行 `git log --oneline` 可查看每一步的本地提交。
