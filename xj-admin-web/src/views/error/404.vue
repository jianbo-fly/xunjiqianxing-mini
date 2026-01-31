<template>
  <div class="error-page">
    <div class="error-content">
      <h1>404</h1>
      <p class="error-message">抱歉，您访问的页面不存在</p>
      <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<style scoped>
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
}

.error-content {
  text-align: center;
}

.error-content h1 {
  font-size: 120px;
  color: #409eff;
  margin: 0;
  font-weight: bold;
}

.error-message {
  font-size: 18px;
  color: #606266;
  margin: 20px 0 30px;
}
</style>
