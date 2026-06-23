# 拾光旅记 (Tourist Check-in) 项目报告

> 更新日期: 2026-06-23

---

## 一、项目概述

**拾光旅记** 是一个全栈旅游打卡社交平台，灵感来自小红书/Instagram，支持用户在旅行中打卡、分享游记、规划行程、互动交流。项目包含三个子项目：Web 前端（Vue 3）、后端 API（Spring Boot）和微信小程序。

- **项目名称**: 拾光旅记 - 旅游打卡系统
- **架构**: 前后端分离 + 微信小程序
- **Web 前端**: Vue 3 + TypeScript + Vite + Element Plus + Tailwind CSS
- **微信小程序**: 原生微信小程序框架
- **后端**: Spring Boot 3.2.5 + MyBatis Plus + MySQL + Redis

---

## 二、项目结构

```
Tourist check-in/
├── tourist-checkin-backend/          # 后端 (Spring Boot + Maven)
│   ├── src/main/java/com/travel/
│   │   ├── TouristCheckinApplication.java   # 启动类
│   │   ├── config/                  # 配置类 (Web, Redis, MyBatis, WebSocket 等)
│   │   ├── controller/              # REST 控制器 (20 个)
│   │   ├── service/                 # 业务逻辑层 (19 个)
│   │   ├── mapper/                  # MyBatis Mapper 接口
│   │   ├── entity/                  # 数据库实体 (23 个)
│   │   ├── dto/                     # 请求数据传输对象
│   │   ├── vo/                      # 视图响应对象
│   │   ├── utils/                   # 工具类 (JWT, 文件存储等)
│   │   ├── endpoint/                # WebSocket 端点
│   │   ├── interceptor/             # 拦截器 (JWT 认证, WebSocket 认证)
│   │   └── exception/               # 自定义异常
│   ├── src/main/resources/
│   │   └── application.yml          # 主配置文件
│   ├── src/test/                    # 测试代码
│   └── pom.xml                      # Maven 依赖
│
├── tourist-checkin-frontend/         # Web 前端 (Vue 3 + Vite)
│   ├── src/
│   │   ├── api/                     # API 请求模块 (17 个)
│   │   ├── components/              # 公共组件 (13 个)
│   │   ├── views/                   # 页面视图 (23 个)
│   │   │   └── admin/               # 管理后台视图 (15 个)
│   │   ├── stores/                  # Pinia 状态管理 (7 个)
│   │   ├── composables/             # Vue Composition API 复用逻辑
│   │   ├── router/                  # 路由配置
│   │   ├── utils/                   # 工具函数 (日期, 图片压缩, WebSocket)
│   │   └── types/                   # TypeScript 类型定义
│   ├── .env.development             # 开发环境变量
│   ├── .env.production              # 生产环境变量
│   ├── vite.config.ts               # Vite 配置 (含 API 代理)
│   ├── tailwind.config.js           # Tailwind CSS 配置
│   └── package.json                 # NPM 依赖
│
├── tourist-checkin-miniprogram/      # 微信小程序
│   ├── pages/                       # 页面 (30 个页面)
│   │   ├── index/                   # 首页 (瀑布流打卡 Feed)
│   │   ├── explore/                 # 探索/关注页
│   │   ├── checkin/                 # 打卡发布
│   │   ├── checkin-detail/          # 打卡详情
│   │   ├── note-list/               # 游记列表
│   │   ├── note-create/             # 创建游记
│   │   ├── note-detail/             # 游记详情
│   │   ├── plan-list/               # 行程列表
│   │   ├── plan-create/             # 创建行程
│   │   ├── plan-detail/             # 行程详情
│   │   ├── profile/                 # 个人中心
│   │   ├── user-profile/            # 他人主页
│   │   ├── follow-list/             # 关注/粉丝列表
│   │   ├── notifications/           # 通知列表
│   │   ├── achievements/            # 成就墙
│   │   ├── points/                  # 积分历史
│   │   ├── topic-feed/              # 话题动态
│   │   ├── stats/                   # 数据统计
│   │   ├── merchants/               # 商家推荐列表
│   │   ├── merchant-detail/         # 商家详情
│   │   ├── search/                  # 搜索
│   │   ├── login/                   # 登录页
│   │   ├── forgot-password/         # 忘记密码
│   │   ├── share-poster/            # 分享海报生成
│   │   ├── shop-recommend/          # 美食推荐发布
│   │   ├── shop-recommend-list/     # 美食推荐列表
│   │   ├── notes-calendar/          # 游记日历视图
│   │   ├── hot-checkins/            # 热门打卡
│   │   └── collected-notes/         # 收藏游记
│   ├── components/                  # 公共组件
│   ├── custom-tab-bar/              # 自定义底部导航栏
│   ├── utils/                       # 工具函数 (API 封装等)
│   ├── assets/                      # 静态资源
│   ├── app.js                       # 小程序入口
│   ├── app.json                     # 小程序全局配置
│   └── app.wxss                     # 小程序全局样式
│
├── init_complete.sql                 # 完整数据库初始化脚本
├── MIGRATION_2026-06-05.sql          # 增量迁移脚本
└── PROJECT_REPORT.md                 # 本报告
```

---

## 三、技术栈详解

### 3.1 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.5 | 应用框架 |
| Java | 17 | 开发语言 |
| MyBatis Plus | 3.5.7 | ORM 框架 |
| MySQL | 8.0+ | 关系数据库 |
| Redis | 7.0+ | 缓存 / 会话 |
| JWT (jjwt) | 0.12.3 | 身份认证 |
| Spring Security Crypto | - | 密码加密 (BCrypt) |
| WebSocket | - | 实时通知推送 |
| fastjson2 | 2.0.46 | JSON 处理 |
| ZXing | 3.5.3 | 二维码生成 |
| Sensitive Word (DFA) | 0.13.0 | 敏感词过滤 |
| Lombok | - | 代码简化 |
| Spring Boot Mail | - | 邮件发送 (验证码) |

### 3.2 Web 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4.21 | UI 框架 |
| TypeScript | 5.4 | 类型语言 |
| Vite | 5.2 | 构建工具 |
| Vue Router | 4.3 | 前端路由 |
| Pinia | 2.1.7 | 状态管理 |
| Element Plus | 2.6.3 | UI 组件库 |
| Tailwind CSS | 3.4.1 | 原子化 CSS |
| Axios | 1.6.8 | HTTP 客户端 |
| 高德地图 JSAPI | 1.0.1 | 地图功能 |
| dayjs | 1.11.10 | 日期处理 |

### 3.3 微信小程序技术栈

| 技术 | 说明 |
|------|------|
| 原生微信小程序框架 | 使用 WXML / WXSS / JS |
| 自定义 TabBar | 5 个底部导航 (首页/关注/打卡/游记/我的) |
| WebSocket | 实时通知 |

---

## 四、数据库结构

数据库名: `tourist_checkin` | 字符集: `utf8mb4`

### 模块与表一览

#### 模块 1: 用户系统
| 表名 | 说明 |
|------|------|
| `user` | 用户表 (账号/微信双登录, 角色: USER/ADMIN) |

#### 模块 2: 地点系统
| 表名 | 说明 |
|------|------|
| `location` | 地点信息表 (经纬度/分类/城市/介绍) |

#### 模块 3: 打卡系统
| 表名 | 说明 |
|------|------|
| `check_in` | 打卡记录 (用户/位置/内容/图片/时间) |
| `check_in_like` | 打卡点赞 |

#### 模块 4: 评论系统
| 表名 | 说明 |
|------|------|
| `comment` | 评论表 (支持楼中楼回复/关联打卡与游记) |
| `comment_like` | 评论点赞 |

#### 模块 5: 关注系统
| 表名 | 说明 |
|------|------|
| `follow` | 关注关系表 (双向) |

#### 模块 6: 通知系统
| 表名 | 说明 |
|------|------|
| `notification` | 通知消息 (点赞/评论/关注/系统通知) |

#### 模块 7: 话题/标签系统
| 表名 | 说明 |
|------|------|
| `topic` | 话题标签 |
| `check_in_topic` | 打卡-话题关联 |

#### 模块 8: 成就系统
| 表名 | 说明 |
|------|------|
| `achievement` | 成就定义 |
| `user_achievement` | 用户成就记录 |

#### 模块 9: 游记系统
| 表名 | 说明 |
|------|------|
| `travel_note` | 游记主表 |
| `travel_note_image` | 游记图片 |
| `travel_note_like` | 游记点赞 |
| `travel_note_collect` | 游记收藏 |

#### 模块 10: 积分与会员体系
| 表名 | 说明 |
|------|------|
| `user_point` | 用户积分余额 |
| `point_record` | 积分流水记录 |

#### 模块 11: 商家推荐
| 表名 | 说明 |
|------|------|
| `merchant_position` | 商户入驻/定位信息 |

#### 模块 12: 行程规划
| 表名 | 说明 |
|------|------|
| `trip_plan` | 行程计划主表 |
| `trip_day` | 行程天 |
| `trip_poi` | 行程 POI 点 |

#### 模块 13: 内容审核
| 表名 | 说明 |
|------|------|
| `sensitive_word` | 敏感词库 |
| `report` | 用户举报记录 |
| `image_audit` | 图片审核记录 |

#### 模块 14: 美食推荐
| 表名 | 说明 |
|------|------|
| `shop_recommend` | 用户自荐美食店铺 |
| `shop_recommend_like` | 美食推荐点赞 |

---

## 五、功能模块总览

| 功能模块 | 说明 | 前端 | 小程序 |
|----------|------|:----:|:-----:|
| **用户系统** | 注册/登录 (JWT + 微信)、个人资料、头像上传、忘记密码 | ✓ | ✓ |
| **打卡功能** | 基于地理位置的打卡、文字内容、多图上传 | ✓ | ✓ |
| **地点浏览** | 地点列表、分类筛选、高德地图展示 | ✓ | ✓ |
| **社交互动** | 点赞、评论、关注、通知推送 (WebSocket) | ✓ | ✓ |
| **话题标签** | #话题 创建与浏览、热门话题 | ✓ | ✓ |
| **成就系统** | 打卡里程碑达成成就徽章 | ✓ | ✓ |
| **积分系统** | 行为积分、积分流水、积分排行 | ✓ | ✓ |
| **游记功能** | 图文游记创建/编辑/浏览/收藏/日历视图 | ✓ | ✓ |
| **行程规划** | 多日行程规划、POI 添加、路线管理 | ✓ | ✓ |
| **商家推荐** | 商户入驻、位置标注、详情展示 | ✓ | ✓ |
| **美食推荐** | 用户自荐美食店铺、点赞互动 | ✓ | ✓ |
| **数据统计** | 打卡统计、图表可视化 | ✓ | ✓ |
| **搜索功能** | 用户/地点/打卡内容搜索 | - | ✓ |
| **分享海报** | 生成打卡分享海报 (含二维码) | - | ✓ |
| **管理后台** | 用户/内容/举报/审核管理 (15 个子模块) | ✓ | - |
| **内容审核** | 敏感词过滤 (DFA)、图片审核、举报机制 | ✓ | ✓ |
| **实时通知** | WebSocket 推送 + 通知列表 | ✓ | ✓ |
| **热门推荐** | 热门打卡/热门游记/热门话题 | ✓ | ✓ |

---

## 六、后端 API 控制器

| 控制器 | 端点前缀 | 说明 |
|--------|----------|------|
| `UserController` | `/api/user` | 用户注册/登录/个人资料/微信登录 |
| `CheckInController` | `/api/checkin` | 打卡发布/列表/详情/热门/删除 |
| `CommentController` | `/api/comment` | 评论发布/列表/删除/点赞 |
| `FollowController` | `/api/follow` | 关注/取关/关注列表/粉丝列表 |
| `LocationController` | `/api/location` | 地点 CRUD/搜索/附近地点 |
| `NotificationController` | `/api/notification` | 通知列表/标记已读/未读数 |
| `TopicController` | `/api/topic` | 话题创建/列表/热门 |
| `AchievementController` | `/api/achievement` | 成就列表/用户成就/检查 |
| `PointController` | `/api/point` | 积分查询/积分记录/排行榜 |
| `TravelNoteController` | `/api/travel-note` | 游记 CRUD/热门/收藏/点赞 |
| `TripPlanController` | `/api/trip-plan` | 行程规划 CRUD |
| `ShareController` | `/api/share` | 分享海报生成 |
| `StatisticsController` | `/api/statistics` | 用户数据统计 |
| `MerchantController` | `/api/merchant` | 商户列表/详情/入驻 |
| `ShopRecommendController` | `/api/shop-recommend` | 美食推荐 CRUD/点赞 |
| `ContentAuditController` | `/api/audit` | 敏感词/举报/图片审核 |
| `ReportController` | `/api/report` | 举报提交/处理 |
| `FileController` | `/api/file` | 文件上传 |
| `GlobalExceptionHandler` | - | 全局异常处理 |

---

## 七、启动流程

### 前置条件

- **JDK 17+**
- **Maven 3.8+**
- **MySQL 8.0+**
- **Redis 7.0+**
- **Node.js 18+** (Web 前端开发)

### 第一步: 初始化数据库

```bash
mysql -u root -p < init_complete.sql
```

这会创建 `tourist_checkin` 数据库及全部 23+ 张表。

### 第二步: 配置环境变量

后端通过环境变量注入敏感配置，本地开发可使用默认值:

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `DB_USERNAME` | 数据库用户名 | `root` |
| `DB_PASSWORD` | 数据库密码 | 无 (必填) |
| `JWT_SECRET` | JWT 签名密钥 | 无 (必填) |
| `JWT_EXPIRATION` | JWT 过期时间 (ms) | `86400000` (24h) |
| `REDIS_HOST` | Redis 地址 | `localhost` |
| `REDIS_PORT` | Redis 端口 | `6379` |
| `REDIS_PASSWORD` | Redis 密码 | 空 |
| `AMAP_KEY` | 高德地图 API Key | 空 |
| `WX_APPID` | 微信小程序 AppID | 空 |
| `WX_SECRET` | 微信小程序 Secret | 空 |
| `MAIL_HOST` | 邮件服务器 | `smtp.qq.com` |
| `MAIL_USERNAME` | 邮箱地址 | 空 |
| `MAIL_PASSWORD` | 邮箱授权码 | 空 |
| `CORS_ORIGINS` | 允许的跨域来源 | `http://localhost:5173` |
| `BASE_URL` | 服务基础 URL | `http://localhost:8080` |

> **安全提示**: 生产环境务必通过环境变量注入所有密码和密钥，不要在配置文件中硬编码。

### 第三步: 启动后端

```bash
cd tourist-checkin-backend

# 设置必要环境变量后启动
mvn spring-boot:run
# 或打包运行:
mvn clean package -DskipTests
java -jar target/tourist-checkin2.1.jar
```

后端在 **http://localhost:8080** 启动。

### 第四步: 启动 Web 前端

```bash
cd tourist-checkin-frontend

# 首次运行安装依赖
npm install

# 配置高德地图 Key (.env.development 中设置 VITE_AMAP_KEY)

# 启动开发服务器
npm run dev
```

Web 前端在 **http://localhost:5173** 启动，API 请求自动代理到后端 8080 端口。

### 第五步: 运行微信小程序

使用微信开发者工具打开 `tourist-checkin-miniprogram` 目录，配置 AppID 后即可运行。

---

## 八、默认端口一览

| 服务 | 端口 |
|------|------|
| Web 前端开发服务器 | 5173 |
| 后端 API 服务 | 8080 |
| MySQL | 3306 |
| Redis | 6379 |

---

## 九、微信小程序页面路由

| 页面路径 | 说明 |
|----------|------|
| `pages/index/index` | 首页 (瀑布流打卡 Feed) |
| `pages/login/login` | 登录页 |
| `pages/explore/explore` | 关注动态 |
| `pages/checkin/checkin` | 发布打卡 |
| `pages/checkin/my` | 我的打卡 |
| `pages/checkin-detail/checkin-detail` | 打卡详情 |
| `pages/note-list/note-list` | 游记列表 (Tab 页) |
| `pages/note-create/note-create` | 创建游记 |
| `pages/note-detail/note-detail` | 游记详情 |
| `pages/plan-list/plan-list` | 行程列表 |
| `pages/plan-create/plan-create` | 创建行程 |
| `pages/plan-detail/plan-detail` | 行程详情 |
| `pages/profile/profile` | 个人中心 (Tab 页) |
| `pages/user-profile/user-profile` | 他人主页 |
| `pages/follow-list/follow-list` | 关注/粉丝列表 |
| `pages/notifications/notifications` | 通知列表 |
| `pages/achievements/achievements` | 成就墙 |
| `pages/points/points` | 积分记录 |
| `pages/topic-feed/topic-feed` | 话题动态 |
| `pages/stats/stats` | 数据统计 |
| `pages/merchants/merchants` | 商家推荐列表 |
| `pages/merchant-detail/merchant-detail` | 商家详情 |
| `pages/search/search` | 搜索 |
| `pages/forgot-password/forgot-password` | 忘记密码 |
| `pages/collected-notes/collected-notes` | 收藏游记 |
| `pages/share-poster/share-poster` | 分享海报 |
| `pages/notes-calendar/notes-calendar` | 游记日历 |
| `pages/shop-recommend/shop-recommend` | 发布美食推荐 |
| `pages/shop-recommend-list/shop-recommend-list` | 美食推荐列表 |
| `pages/hot-checkins/hot-checkins` | 热门打卡 |

---

## 十、关键依赖说明

- **高德地图**: Web 前端使用 `@amap/amap-jsapi-loader` 加载地图，小程序使用微信原生地图组件，需配置 API Key
- **文件存储**: 图片上传使用本地存储到 `./uploads` 目录，通过 `/uploads/` 路径访问
- **WebSocket**: 用于实时通知推送，小程序通过 `wx.connectSocket` 连接
- **敏感词过滤**: 基于 DFA 算法的 `sensitive-word` 库，支持自定义敏感词库
- **二维码生成**: 使用 ZXing 生成打卡分享海报的二维码
- **自定义 TabBar**: 微信小程序使用自定义底部导航，支持 5 个 Tab 和动态角标

---

## 十一、版本历史

| 提交 | 说明 |
|------|------|
| `bfef65b` | 初始化项目: 旅游打卡系统 |
| `24f4bf4` | 首次提交完整代码 |
| `a7e4500` | 隐藏数据库密码为占位符 |
| `565ddcc` | 修复登录相关问题 |

---

## 十二、安全注意事项

1. **所有密码和密钥通过环境变量注入**，不在代码中硬编码
2. **JWT 令牌** 用于 API 认证，有效期 24 小时
3. **密码加密** 使用 BCrypt 单向哈希
4. **敏感词过滤** 自动检测用户生成内容
5. **CORS 配置** 白名单控制允许的跨域来源
6. **文件上传** 限制大小 (单文件 10MB，总请求 90MB)
7. `.env` 文件中的 API Key 不提交到公开仓库
