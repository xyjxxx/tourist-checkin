# 拾光旅记 (Tourist Check-in) 项目报告

> 更新日期: 2026-06-01

---

## 一、项目概述

**拾光旅记** 是一个全栈旅游打卡社交平台，灵感来自小红书/Instagram，支持用户在旅行中打卡、分享游记、规划行程、互动交流。

- **项目名称**: 拾光旅记 - 旅游打卡系统
- **架构**: 前后端分离
- **前端**: Vue 3 + TypeScript + Vite
- **后端**: Spring Boot 3.2.5 + MyBatis Plus + MySQL

---

## 二、项目结构

```
Tourist check-in/
├── tourist-checkin-backend/          # 后端 (Spring Boot + Maven)
│   ├── src/main/java/com/travel/
│   │   ├── TouristCheckinApplication.java   # 启动类
│   │   ├── config/                  # 配置类 (WebConfig, Redis, MyBatis, WebSocket等)
│   │   ├── controller/              # 控制器 (User, Location, Comment, Follow等)
│   │   ├── service/                 # 业务逻辑层
│   │   ├── mapper/                  # MyBatis Mapper接口
│   │   ├── entity/                  # 数据库实体
│   │   ├── dto/                     # 请求数据传输对象
│   │   ├── vo/                      # 视图响应对象
│   │   ├── utils/                   # 工具类 (JWT, OSS, 文件存储等)
│   │   ├── endpoint/                # WebSocket端点
│   │   └── exception/               # 自定义异常
│   ├── src/main/resources/
│   │   └── application.yml          # 主配置文件
│   └── pom.xml                      # Maven依赖
│
├── tourist-checkin-frontend/         # 前端 (Vue 3 + Vite)
│   ├── src/
│   │   ├── api/                     # API请求模块 (15个)
│   │   ├── components/              # 公共组件 (12个)
│   │   ├── views/                   # 页面组件 (16个)
│   │   ├── stores/                  # Pinia状态管理 (7个)
│   │   ├── router/                  # 路由配置
│   │   ├── utils/                   # 工具函数
│   │   └── types/                   # TypeScript类型定义
│   ├── .env.development              # 开发环境变量
│   ├── vite.config.ts               # Vite配置 (含API代理)
│   ├── tailwind.config.js           # Tailwind CSS配置
│   └── package.json                 # NPM依赖
│
├── init.sql                          # 数据库初始化脚本
├── migration_v2.sql                  # 数据库迁移脚本 v2
├── migration_v3_fixes.sql            # 数据库迁移脚本 v3
├── migration_v4_account.sql          # 数据库迁移脚本 v4
├── fix_test_user.sql                 # 测试用户修复脚本
└── PROJECT_REPORT.md                 # 本报告
```

---

## 三、技术栈详解

### 3.1 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.5 | 应用框架 |
| Java | 17 | 开发语言 |
| MyBatis Plus | 3.5.7 | ORM框架 |
| MySQL | - | 关系数据库 |
| Redis | - | 缓存/会话 |
| JWT (jjwt) | 0.12.3 | 身份认证 |
| Spring Security Crypto | - | 密码加密(BCrypt) |
| WebSocket | - | 实时通知推送 |
| 阿里云OSS SDK | 3.17.4 | 文件存储(已弃用，改用本地) |
| fastjson2 | 2.0.46 | JSON处理 |
| ZXing | 3.5.3 | 二维码生成 |
| Sensitive Word | 0.13.0 | 敏感词过滤(DFA) |

### 3.2 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4.21 | UI框架 |
| TypeScript | 5.4 | 类型语言 |
| Vite | 5.2 | 构建工具 |
| Vue Router | 4.3 | 前端路由 |
| Pinia | 2.1.7 | 状态管理 |
| Element Plus | 2.6.3 | UI组件库 |
| Tailwind CSS | 3.4.1 | 原子化CSS |
| Axios | 1.6.8 | HTTP客户端 |
| 高德地图 JSAPI | 1.0.1 | 地图功能 |
| dayjs | 1.11.10 | 日期处理 |

---

## 四、数据库结构

数据库名: `tourist_checkin` | 字符集: `utf8mb4`

### 核心表 (共 20+ 张)

| 表名 | 说明 |
|------|------|
| `user` | 用户表 (含角色 USER/ADMIN) |
| `location` | 地点信息表 (经纬度、分类、城市) |
| `check_in` | 打卡记录表 (关联用户+地点+图片) |
| `check_in_like` | 打卡点赞表 |
| `comment` | 评论表 |
| `comment_like` | 评论点赞表 |
| `follow` | 关注关系表 |
| `topic` / `check_in_topic` | 话题标签 |
| `achievement` / `user_achievement` | 成就系统 |
| `user_point` / `point_record` | 积分系统 |
| `travel_note` / `travel_note_image` | 游记功能 |
| `travel_note_like` / `travel_note_collect` | 游记互动 |
| `trip_plan` / `trip_day` / `trip_poi` | 行程规划 |
| `merchant_position` | 商户定位 |
| `notification` | 通知消息 |
| `sensitive_word` | 敏感词库 |
| `report` / `image_audit` | 举报与图片审核 |

---

## 五、功能模块

| 功能模块 | 说明 |
|----------|------|
| **用户系统** | 注册/登录(JWT)、个人资料、头像上传 |
| **打卡功能** | 基于地理位置的打卡、文字内容、图片上传 |
| **地点浏览** | 地点列表、分类筛选、高德地图展示 |
| **社交互动** | 点赞、评论、关注、通知推送(WebSocket) |
| **话题标签** | #话题 创建与浏览、热门话题 |
| **成就系统** | 打卡里程碑达成成就徽章 |
| **积分系统** | 行为积分、积分记录、积分排名 |
| **游记功能** | 图文游记创建/编辑/浏览/收藏 |
| **行程规划** | 多日行程规划、POI添加、路线管理 |
| **数据统计** | 打卡统计、图表可视化 |
| **管理后台** | 用户管理、内容审核、举报处理 |
| **内容审核** | 敏感词过滤(DFA)、图片审核、举报机制 |
| **分享功能** | 生成分享海报(二维码) |

---

## 六、路由此 (前端)

| 路径 | 页面 | 权限 |
|------|------|------|
| `/login` | 登录页 | 公开 |
| `/` | 首页/信息流 | 需要登录 |
| `/explore` | 发现/地图浏览 | 需要登录 |
| `/profile` | 个人主页 | 需要登录 |
| `/profile/:userId` | 他人主页 | 需要登录 |
| `/notifications` | 通知列表 | 需要登录 |
| `/follows/:userId` | 关注/粉丝列表 | 需要登录 |
| `/topic/:name` | 话题动态 | 需要登录 |
| `/achievements` | 成就墙 | 需要登录 |
| `/points` | 积分历史 | 需要登录 |
| `/travel-notes` | 游记列表 | 需要登录 |
| `/travel-notes/create` | 创建游记 | 需要登录 |
| `/travel-notes/:id` | 游记详情 | 需要登录 |
| `/trip-plans` | 行程列表 | 需要登录 |
| `/trip-plans/create` | 创建行程 | 需要登录 |
| `/trip-plans/:id` | 行程详情 | 需要登录 |
| `/stats` | 统计仪表盘 | 需要登录 |
| `/admin` | 管理后台 | 管理员 |

---

## 七、后端API控制器

| 控制器 | 说明 |
|--------|------|
| `UserController` | 用户注册/登录/个人资料 |
| `LocationController` | 地点CRUD/搜索 |
| `CheckInController` | 打卡发布/列表/详情/删除 |
| `CommentController` | 评论发布/列表/删除 |
| `FollowController` | 关注/取关/列表 |
| `NotificationController` | 通知列表/已读 |
| `TopicController` | 话题相关 |
| `AchievementController` | 成就相关 |
| `StatisticsController` | 数据统计 |
| `TravelNoteController` | 游记CRUD |
| `TripPlanController` | 行程规划CRUD |
| `GlobalExceptionHandler` | 全局异常处理 |

---

## 八、启动流程

### 前置条件

- **JDK 17+**
- **Maven 3.8+**
- **MySQL 8.0+**
- **Redis 7.0+** (用于缓存和会话)
- **Node.js 18+** 和 npm

### 第一步: 初始化数据库

```bash
# 用 root 用户登录 MySQL 执行初始化脚本
mysql -u root -p < init.sql
```

这会:
1. 创建 `tourist_checkin` 数据库和所有表
2. 插入 8 条示例地点数据
3. 创建测试用户 `test` 和管理员 `admin`（密码均为 `123456`）

**可选**: 执行迁移脚本（按顺序执行）:
```bash
mysql -u root -p tourist_checkin < migration_v2.sql
mysql -u root -p tourist_checkin < migration_v3_fixes.sql
mysql -u root -p tourist_checkin < migration_v4_account.sql
mysql -u root -p tourist_checkin < fix_test_user.sql
```

### 第二步: 配置环境变量 (可选)

默认配置已可用于本地开发，生产环境需设置以下变量:

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `DB_USERNAME` | root | 数据库用户名 |
| `DB_PASSWORD` | xyj2003517 | 数据库密码 |
| `JWT_SECRET` | (内置dev key) | JWT签名密钥 |
| `REDIS_HOST` | localhost | Redis地址 |
| `AMAP_KEY` | (空) | 高德地图API Key |

### 第三步: 启动后端

```bash
cd tourist-checkin-backend

# 方式一: Maven 启动
mvn spring-boot:run

# 方式二: 先打包再运行
mvn clean package -DskipTests
java -jar target/tourist-checkin-backend-1.0.0.jar
```

后端将在 **http://localhost:8080** 启动。

### 第四步: 启动前端

```bash
cd tourist-checkin-frontend

# 安装依赖 (首次运行)
npm install

# 启动开发服务器
npm run dev
```

前端将在 **http://localhost:5173** 启动。

Vite 已配置 API 代理，`/api` 和 `/uploads` 请求会自动转发到后端 `http://localhost:8080`。

### 第五步: 访问应用

打开浏览器访问 **http://localhost:5173**，使用以下账号登录:

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 普通用户 | test | 123456 |
| 管理员 | admin | 123456 |

---

## 九、默认端口一览

| 服务 | 端口 |
|------|------|
| 前端开发服务器 | 5173 |
| 后端API服务 | 8080 |
| MySQL | 3306 |
| Redis | 6379 |

---

## 十、关键依赖说明

- **高德地图**: 前端使用 `@amap/amap-jsapi-loader` 加载地图，需配置 `VITE_AMAP_KEY`（已在 `.env.development` 中配置）
- **文件存储**: 图片上传使用本地存储到 `./uploads` 目录，通过 `/uploads/` 路径访问
- **阿里云OSS**: 代码保留OSS工具类，但配置已注释，当前默认为本地存储模式
- **WebSocket**: 用于实时通知推送，端点注册在 Spring WebSocket 配置中
