# WI-MEGA 部署文档

> **重要声明**: 该项目仅个人兴趣学习使用，请勿用于商业用途。

## 项目概述

WI-MEGA 是一个基于 Spring Boot 3.5.6 和 Java 21 的交易策略管理系统，支持币安交易所的自动化交易策略执行。

- **技术栈**: Spring Boot 3.5.6, Java 21, MySQL 8.x, Redis
- **应用端口**: 8887
- **API文档**: Knife4j (Swagger) (http://ip:8887/doc.html)
- **配套前端页面**: https://github.com/Artist2024man/demo-mega-web (目前不支持新增用户，要新增只能手动改库后重启服务生效，有需要的可以自己二开)

## 一、环境要求

### 1.1 基础环境

- **JDK**: Java 21 或更高版本
- **Maven**: 3.6+ (用于构建项目)
- **MySQL**: 8.0+ (推荐 8.4.7)
- **Redis**: 6.0+ (推荐使用 Redis 6.x 或 7.x)
- **Docker**: 可选，用于容器化部署

### 1.2 系统资源

- **内存**: 最低 4GB，推荐 8GB+
- **CPU**: 2核以上
- **磁盘**: 至少 50GB 可用空间

## 二、数据库部署

### 2.1 MySQL 安装与配置

#### 方式一：使用 Docker 部署 MySQL

```bash
# 拉取 MySQL 8.4 镜像
docker pull mysql:8.4

# 启动 MySQL 容器
docker run -d \
  --name mysql-wi-mega \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=your_mysql_password \
  -e MYSQL_DATABASE=wi_mega \
  -e TZ=Asia/Shanghai \
  -v mysql_data:/var/lib/mysql \
  mysql:8.4 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_general_ci

# 查看容器状态
docker ps | grep mysql
```

#### 方式二：本地安装 MySQL

1. 下载并安装 MySQL 8.4.7
2. 创建数据库：

```sql
CREATE DATABASE wi_mega CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

### 2.2 初始化数据库

执行项目根目录下的 `init.sql` 文件初始化数据库：

```bash
# 方式一：使用 MySQL 命令行
mysql -u root -p wi_mega < init.sql

# 方式二：使用 Docker 执行
docker exec -i mysql-wi-mega mysql -uroot -pyour_mysql_password wi_mega < init.sql

# 方式三：在 MySQL 客户端中执行
mysql -u root -p
use wi_mega;
source /path/to/init.sql;
```

**重要提示**：
- 数据库名称：`wi_mega`（注意：init.sql 中显示的是 `demo`，需要根据实际情况调整）
- 如果使用 `demo` 数据库，请修改配置文件中的数据库名称

### 2.3 验证数据库

```sql
-- 连接数据库
mysql -u root -p wi_mega

-- 查看表结构
SHOW TABLES;

-- 验证超管用户（用户名：ma123456）
SELECT id, name, username, user_type, status FROM app_user WHERE username = 'ma123456';
```

## 三、Redis 部署

### 3.1 使用 Docker 部署 Redis

```bash
# 拉取 Redis 镜像
docker pull redis:7-alpine

# 启动 Redis 容器（带密码）
docker run -d \
  --name redis-wi-mega \
  -p 6379:6379 \
  -e TZ=Asia/Shanghai \
  redis:7-alpine \
  redis-server --requirepass your_redis_password

# 查看容器状态
docker ps | grep redis
```

### 3.2 本地安装 Redis

```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install redis-server

# CentOS/RHEL
sudo yum install redis

# macOS
brew install redis

# 启动 Redis
redis-server

# 设置密码（编辑 redis.conf）
requirepass your_redis_password
```

### 3.3 验证 Redis

```bash
# 测试连接
redis-cli -h 127.0.0.1 -p 6379 -a your_redis_password

# 执行测试命令
PING
# 应返回: PONG
```

## 四、应用配置

### 4.1 配置文件说明

项目使用 Maven Profile 管理不同环境的配置：

- **local**: 本地开发环境（默认）
- **test**: 测试环境
- **prod**: 生产环境

配置文件位置：`src/main/resources/application-{profile}.yml`

### 4.2 生产环境配置示例

编辑 `src/main/resources/application-prod.yml`：

```yaml
spring:
  jackson:
    time-zone: Asia/Shanghai  # 根据实际时区调整
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://your_mysql_host:3306/wi_mega?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=Asia/Shanghai
    username: root
    password: your_mysql_password
    type: com.zaxxer.hikari.HikariDataSource
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      connection-test-query: SELECT 1
  data:
    redis:
      database: 0
      host: your_redis_host
      port: 6379
      password: your_redis_password
      timeout: 3000
      connectTimeout: 3000
      clientName:
```

**配置项说明**：
- `spring.datasource.url`: MySQL 连接地址
- `spring.datasource.username`: MySQL 用户名
- `spring.datasource.password`: MySQL 密码
- `spring.data.redis.host`: Redis 主机地址
- `spring.data.redis.port`: Redis 端口
- `spring.data.redis.password`: Redis 密码

## 五、应用构建

### 5.1 使用 Maven 构建

```bash
# 进入项目目录
cd /path/to/wi-mega

# 清理并构建项目（生产环境）
mvn clean package -Pprod -DskipTests

# 构建后的 JAR 文件位置
# target/wi-mega.jar
```

### 5.2 构建参数说明

- `-Pprod`: 使用生产环境配置
- `-Ptest`: 使用测试环境配置
- `-Plocal`: 使用本地环境配置（默认）
- `-DskipTests`: 跳过测试（可选）

## 六、应用部署

### 6.1 传统方式部署

#### 6.1.1 直接运行 JAR

```bash
# 运行应用（生产环境）
java -jar -Xms2048m -Xmx2048m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m \
  target/wi-mega.jar \
  --spring.profiles.active=prod

# 后台运行
nohup java -jar -Xms2048m -Xmx2048m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m \
  target/wi-mega.jar \
  --spring.profiles.active=prod > app.log 2>&1 &
```

#### 6.1.2 使用 systemd 管理（Linux）

创建服务文件 `/etc/systemd/system/wi-mega.service`：

```ini
[Unit]
Description=WI-MEGA Application
After=network.target mysql.service redis.service

[Service]
Type=simple
User=your_user
WorkingDirectory=/path/to/wi-mega
ExecStart=/usr/bin/java -jar -Xms2048m -Xmx2048m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m /path/to/wi-mega/target/wi-mega.jar --spring.profiles.active=prod
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

启动服务：

```bash
# 重载 systemd 配置
sudo systemctl daemon-reload

# 启动服务
sudo systemctl start wi-mega

# 设置开机自启
sudo systemctl enable wi-mega

# 查看服务状态
sudo systemctl status wi-mega

# 查看日志
sudo journalctl -u wi-mega -f
```

### 6.2 Docker 方式部署

#### 6.2.1 构建 Docker 镜像

```bash
# 确保已构建 JAR 文件
mvn clean package -Pprod -DskipTests

# 构建 Docker 镜像
docker build -t wi-mega:latest .

# 查看镜像
docker images | grep wi-mega
```

#### 6.2.2 运行 Docker 容器

```bash
# 运行容器
docker run -d \
  --name wi-mega \
  -p 8887:8887 \
  -e PARAMS="--spring.profiles.active=prod" \
  -e JAVA_OPTS="-Xms2048m -Xmx2048m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m" \
  --link mysql-wi-mega:mysql \
  --link redis-wi-mega:redis \
  wi-mega:latest

# 查看容器状态
docker ps | grep wi-mega

# 查看日志
docker logs -f wi-mega
```

#### 6.2.3 Docker Compose 部署（推荐）

创建 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.4
    container_name: mysql-wi-mega
    environment:
      MYSQL_ROOT_PASSWORD: your_mysql_password
      MYSQL_DATABASE: wi_mega
      TZ: Asia/Shanghai
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_general_ci
    restart: unless-stopped

  redis:
    image: redis:7-alpine
    container_name: redis-wi-mega
    ports:
      - "6379:6379"
    command: redis-server --requirepass your_redis_password
    restart: unless-stopped

  app:
    build: .
    container_name: wi-mega
    ports:
      - "8887:8887"
    environment:
      PARAMS: "--spring.profiles.active=prod"
      JAVA_OPTS: "-Xms2048m -Xmx2048m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m"
    depends_on:
      - mysql
      - redis
    restart: unless-stopped
    networks:
      - wi-mega-network

volumes:
  mysql_data:

networks:
  wi-mega-network:
    driver: bridge
```

启动服务：

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f app

# 停止服务
docker-compose down

# 停止并删除数据卷（谨慎操作）
docker-compose down -v
```

**注意**：使用 Docker Compose 时，需要修改 `application-prod.yml` 中的数据库和 Redis 连接地址：
- MySQL: `jdbc:mysql://mysql:3306/wi_mega...`
- Redis: `host: redis`

## 七、验证部署

### 7.1 健康检查

```bash
# 检查应用是否启动
curl http://localhost:8887/health

# 或访问浏览器
# http://your_server_ip:8887/health
```

### 7.2 API 文档

访问 Knife4j API 文档：

```
http://your_server_ip:8887/doc.html
```

默认登录信息（如果启用了 Basic 认证）：
- 用户名: `wuin`
- 密码: `123456`

### 7.3 登录验证

使用超管账号登录系统：

- **用户名**: `ma123456`
- **密码**: `ma123456`

## 八、常见问题

### 8.1 数据库连接失败

**问题**: 应用启动时提示无法连接数据库

**解决方案**:
1. 检查 MySQL 服务是否运行
2. 验证数据库连接配置（host、port、username、password）
3. 确认数据库 `wi_mega` 已创建
4. 检查防火墙规则，确保 3306 端口可访问
5. 验证 MySQL 用户权限

```sql
-- 检查用户权限
SHOW GRANTS FOR 'root'@'%';
```

### 8.2 Redis 连接失败

**问题**: 应用启动时提示无法连接 Redis

**解决方案**:
1. 检查 Redis 服务是否运行
2. 验证 Redis 连接配置（host、port、password）
3. 测试 Redis 连接：`redis-cli -h host -p port -a password ping`
4. 检查防火墙规则，确保 6379 端口可访问

### 8.3 端口被占用

**问题**: 应用启动失败，提示端口 8887 被占用

**解决方案**:

```bash
# Linux/macOS 查看端口占用
lsof -i :8887
# 或
netstat -tulpn | grep 8887

# 杀死占用进程
kill -9 <PID>

# 或修改应用端口（在 application.yml 中）
server:
  port: 8888
```

### 8.4 内存不足

**问题**: 应用运行缓慢或 OOM 错误

**解决方案**:
1. 增加 JVM 内存参数
2. 检查系统可用内存：`free -h`
3. 优化 JVM 参数（根据实际服务器配置调整）

### 8.5 时区问题

**问题**: 时间显示不正确

**解决方案**:
1. 检查系统时区：`date`
2. 修改配置文件中的时区设置
3. 确保数据库时区配置正确

## 九、监控与维护

### 9.1 日志查看

```bash
# 应用日志位置（根据 logback 配置）
# 通常在 logs/ 目录下

# Docker 容器日志
docker logs -f wi-mega

# systemd 服务日志
journalctl -u wi-mega -f
```

### 9.2 性能监控

- 监控 JVM 内存使用情况
- 监控数据库连接池状态
- 监控 Redis 连接状态
- 监控应用响应时间

### 9.3 备份建议

1. **数据库备份**:
```bash
# 定期备份数据库
mysqldump -u root -p wi_mega > backup_$(date +%Y%m%d).sql
```

2. **配置文件备份**: 定期备份配置文件
3. **日志归档**: 定期归档和清理日志文件

## 十、升级部署

### 10.1 升级步骤

1. 备份当前版本和数据库
2. 停止应用服务
3. 部署新版本 JAR 文件
4. 执行数据库迁移脚本（如有）
5. 启动应用服务
6. 验证功能正常

### 10.2 回滚方案

1. 停止当前服务
2. 恢复旧版本 JAR 文件
3. 恢复数据库备份（如需要）
4. 启动服务

## 十一、安全建议

1. **修改默认密码**: 生产环境务必修改数据库和 Redis 密码
2. **防火墙配置**: 限制数据库和 Redis 的访问来源
3. **SSL/TLS**: 生产环境建议启用 MySQL SSL 连接
4. **定期更新**: 及时更新依赖包，修复安全漏洞
5. **访问控制**: 限制应用管理接口的访问权限

## 十二、联系支持

如遇到部署问题，请检查：
1. 应用日志
2. 数据库日志
3. Redis 日志
4. 系统资源使用情况

---

**文档版本**: v1.0  
**最后更新**: 2026-01-12
