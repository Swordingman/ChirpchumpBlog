### **后端技术栈 (Backend)**

*   **核心框架 (Core Framework)**:
    *   **Spring Boot**: 用于快速构建、配置和运行独立的、生产级的 Spring 应用程序。
*   **数据持久化 (Data Persistence)**:
    *   **Spring Data JPA**: 简化了基于 JPA 的数据访问层的开发，通过 Repository 接口自动实现 CRUD。
    *   **Hibernate**: 作为 JPA 的底层实现，负责 ORM (对象关系映射)。
    *   **MySQL**: 关系型数据库，用于存储所有业务数据 (用户、文章、评论等)。
*   **安全与认证 (Security & Authentication)**:
    *   **Spring Security**: 强大的安全框架，用于处理认证 (Authentication) 和授权 (Authorization)。
    *   **JWT (JSON Web Tokens)**: 用于生成和验证无状态的认证令牌，是前后端分离项目的标准认证方案。
*   **API & Web**:
    *   **Spring Web (MVC)**: 用于构建 RESTful API 接口。
    *   **Bean Validation**: 用于对传入的 DTO (Data Transfer Objects) 进行数据校验。
*   **工具与库 (Utilities & Libraries)**:
    *   **Lombok**: 通过注解自动生成 Getter, Setter, Constructor 等模板代码，简化实体类和 DTO。
    *   **Maven / Gradle**: 项目构建和依赖管理工具。
    *   **`commonmark-java` (或类似库)**: (在早期方案中) 用于在后端将 Markdown 解析为 HTML。
    *   **jjwt**: 用于处理 JWT 的创建和解析。

---

### **前端技术栈 (Frontend)**

*   **核心框架 (Core Framework)**:
    *   **Vue 3**: 渐进式 JavaScript 框架，使用了 **Composition API** (`<script setup>`) 进行开发。
*   **构建工具 (Build Tool)**:
    *   **Vite**: 下一代前端构建工具，提供了极速的冷启动和热模块更新 (HMR)。
*   **路由管理 (Routing)**:
    *   **Vue Router 4**: Vue.js 的官方路由管理器，用于实现单页应用 (SPA) 的页面导航。
*   **状态管理 (State Management)**:
    *   **Pinia**: Vue 3 推荐的、类型安全、模块化的状态管理库，用于管理用户登录状态、Token 等全局数据。
*   **UI 组件库 (UI Component Library)**:
    *   **Element Plus**: 基于 Vue 3 的桌面端组件库，提供了丰富的、开箱即用的高质量组件 (如表格、表单、对话框、菜单等)。
*   **HTTP 请求 (HTTP Client)**:
    *   **Axios**: 一个基于 Promise 的 HTTP 客户端，用于与后端 API 进行交互。我们通过封装 `axiosInstance` 实现了请求拦截器 (自动附加 Token) 和响应拦截器 (统一错误处理)。
*   **Markdown 处理**:
    *   **`mavon-editor` (Vue 3 兼容版)**: 在后台文章编辑页面使用的 Markdown 编辑器组件。
    *   **`markdown-it`**: 在前台文章详情页，用于将 Markdown 文本实时解析为 HTML。
    *   **`highlight.js`**: 配合 `markdown-it`，用于实现代码块的语法高亮。
*   **样式与预处理器 (Styling)**:
    *   **Sass/SCSS**: (在部分组件中) CSS 预处理器，用于编写更结构化、可维护的样式代码。

---

### **开发与部署 (Development & Deployment)**

*   **版本控制 (Version Control)**:
    *   **Git** & **GitHub / Gitee**: 用于代码的版本管理和托管。
*   **开发工具 (Development Tools)**:
    *   **IntelliJ IDEA**: 后端 Java 开发 IDE。
    *   **VS Code**: 前端 Vue 开发 IDE。
    *   **Postman / Insomnia**: API 接口测试工具。
    *   **Navicat / DataGrip / DBeaver**: 数据库图形化管理工具。
*   **部署环境 (可选，但强烈推荐)**:
    *   **Linux (Ubuntu/CentOS)**: 服务器操作系统。
    *   **Nginx**: 高性能 Web 服务器，用于托管前端静态文件和配置反向代理。
    *   **Docker & Docker Compose**: (更进一步) 用于容器化部署，实现环境隔离和一键启停。
