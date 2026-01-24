# Git 提交 Skill

当用户请求 `/commit` 或要求提交代码时，执行以下步骤：

## 步骤

### 1. 检查当前状态
运行以下命令获取仓库状态：
```bash
git status
git diff --stat
git log --oneline -3
```

### 2. 查看详细变更
```bash
git diff
git diff --cached
```

### 3. 分析变更内容
根据变更内容判断提交类型：
- `feat`: 新功能
- `fix`: Bug修复
- `docs`: 文档更新
- `style`: 代码格式调整（不影响功能）
- `refactor`: 重构（不是新功能也不是修复）
- `perf`: 性能优化
- `test`: 测试相关
- `chore`: 构建/工具相关

### 4. 生成提交信息
格式：`<type>(<scope>): <description>`

示例：
- `feat(order): 添加订单状态枚举`
- `fix(payment): 修复支付回调解析错误`
- `refactor(user): 优化用户查询逻辑`

### 5. 执行提交
```bash
git add .
git commit -m "<生成的提交信息>

Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>"
```

### 6. 确认结果
```bash
git status
git log --oneline -1
```

## 注意事项
- 提交信息使用中文描述
- 不要提交敏感文件（.env, credentials 等）
- 如果有未暂存的更改，先询问用户是否全部提交
- 提交前确保代码可以编译通过
