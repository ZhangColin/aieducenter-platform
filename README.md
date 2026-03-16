# 海创元智研云平台

> 面向学校和企业的 AI 聚合 SaaS 平台

## 简介

海创元智研云平台是一个集成多种大模型能力的一站式 AI 服务平台，为学校和企业提供：

- **模型统一接入**：一次接入，使用多家大模型（GPT、Claude、文心一言等）
- **智能体市场**：预制和自定义 AI 智能体，处理复杂任务（PPT 生成、文档分析、编程助手等）
- **费用统一管理**：虚拟币计费，企业配额控制，用量统计透明
- **多租户支持**：企业/机构入驻，成员管理，权限配置

## 核心功能

### 对话能力
- 多模型对话切换
- 流式响应输出
- 上下文记忆管理
- 文件上传分析

### 智能体
- PPT 生成助手
- 文档总结分析
- 代码生成助手
- 自定义智能体

### 创作工具
- AI 绘画（文生图）
- AI 编程
- AI 音视频生成（规划中）

### 企业管理
- 成员邀请管理
- 角色权限配置
- 用量统计报表
- 配额控制设置

### 费用管理
- 虚拟币充值
- 消耗明细查询
- 模型汇率配置
- 企业发票管理

## 技术架构

### 后端技术栈
- Java 21 + Spring Boot 3.4
- cartisan-boot 框架（DDD 六边形架构）
- PostgreSQL + Redis
- Redis Streams / Kafka 消息队列

### 前端技术栈
- Next.js 15（App Router）
- TypeScript
- shadcn/ui + Tailwind CSS
- Zustand 状态管理

## 快速开始

### 环境要求

- **用户访问**：现代浏览器（Chrome、Firefox、Safari）
- **开发环境**：见 [开发指南](docs/guides/development-guide.md)

### 本地运行

```bash
# 克隆项目
git clone <repository-url>
cd aieducenter-platform

# 安装依赖
pnpm install

# 启动前端（用户端）
pnpm dev:web
# 访问 http://localhost:3000

# 启动前端（管理后台）
pnpm dev:admin
# 访问 http://localhost:3001
```

## 产品截图

> 待补充...

## 文档

| 文档 | 说明 |
|------|------|
| **面向用户/运维** |
| [使用手册](docs/guides/aieducenter-platform-使用手册.md) | 功能说明、系统操作、部署指南 |
| **面向开发者** |
| [开发手册](docs/guides/aieducenter-platform-开发手册.md) | 开发步骤、命令、示例、注意事项 |
| [cartisan-boot 使用手册](docs/guides/cartisan-boot-使用手册.md) | 后端框架使用指南 |
| [AI 协作开发 SOP](docs/sop/AI协作开发SOP.md) | 完整开发流程规范 |
| **参考资料** |
| [项目设计](docs/海创元智研云平台设计.md) | 技术架构、业务模型设计 |
| [架构决策](docs/decisions/DECISIONS.md) | 技术决策记录 |
| [团队规则](docs/skills/SKILL.md) | 开发规范、踩坑经验 |
| [Epic 1 规格](docs/specs/epic-001-project-skeleton/) | 项目骨架详细规格文档 |

## 开源协议

Copyright © 2025 海创元
