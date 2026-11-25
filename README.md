# 校园活动报名与签到系统

基于 Spring Boot、Spring MVC、Spring Security、MyBatis、Thymeleaf 和 MySQL 的校园活动系统。使用 JDK 17，Spring Boot 内置 Web 服务器。

## 启动

1. 在 MySQL 8 中执行 [建表脚本](sql/schema.sql)。
   已使用旧版建表脚本的数据库依次执行一次 [积分字段迁移](sql/migrations/001_points.sql) 和 [学生管理字段迁移](sql/migrations/002_student_management.sql)，不要重新执行建表脚本。
2. 设置 `DB_URL`、`DB_USER`、`DB_PASSWORD` 环境变量。默认 URL 指向本机 `campus_activity`。
3. 运行 `mvn exec:java -Dexec.mainClass=edu.campus.seed.Seed`，创建演示账号 `admin`、`owner`、`student1`、`student2`、`student3` 和模拟活动；默认密码为 `demo123`，可用 `DEMO_PASSWORD` 改写。脚本生成覆盖七类积分、过去和未来日期、现场签到与免签到的活动，以及报名、取消和签到记录。`student1` 默认有 30 分，可直接查看环形积分图。重复运行不会重复创建这些记录。
4. 运行 `mvn package`，再运行 `java -jar target/activity-signin-1.0.0.jar`，打开 `http://localhost:8080/`。

演示顺序：负责人创建草稿并提交 → 管理员通过 → 学生报名 → 活动开始后负责人开启签到 → 学生输入现场签到码 → 负责人查看统计。活动时间需覆盖当前时间才能开启签到。

登录后按角色进入独立页面，均可从左侧导航切换。学生有 `/student` 个人首页、`/hall` 活动大厅、`/student/history` 参与记录、`/student/points` 分类积分；负责人有 `/owner` 我的活动、`/owner/new` 发布活动、`/owner/ended` 结束与统计；管理员有 `/admin` 仪表盘、`/admin/students` 学生管理、`/admin/audits` 活动审核。

管理员可在学生管理页按学号、姓名、专业搜索，新增和编辑学生资料、重置密码、停用账号。停用后账号无法登录，历史报名与签到记录保留。

## 说明

报名和取消在数据库事务中锁定活动行，容量不会超额。签到码只存 BCrypt 哈希，错误次数累计到五次后拒绝继续签到。Spring Security 负责登录校验、角色权限和 CSRF 防护；页面从 `/me` 取得令牌后自动发送。

每个活动设置积分分类、分值和签到方式。需要签到的活动在学生成功签到后计分；免签到活动在结束后为有效报名学生自动计分。学生端显示七类积分和总分。

本机需要 JDK 17、Maven 和 MySQL 8。无需单独安装 Tomcat。

## 项目结构

- `controller`：页面路由、JSON 接口和错误响应。
- `service`：审核、报名、签到、积分与事务规则。
- `mapper`：MyBatis SQL 和数据库访问。
- `entity`：积分等业务类型。
- `config`：Web 拦截配置。
- `seed`：可重复执行的演示数据初始化。
- `src/main/resources/templates`：Thymeleaf 页面；`static`：共享样式与前端脚本。
- `sql`：新装建表脚本和旧库迁移脚本。

项目的普通 Git 仓库位于本目录。运行 `git log --oneline` 可查看每一步的本地提交。
