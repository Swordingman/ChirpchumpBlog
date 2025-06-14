// src/views/AboutView.vue
<template>
  <div class="about-view">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <h1>关于我</h1>
        </div>
      </template>
      <div class="about-content">
        <p>你好！欢迎来到我的个人博客。</p>
        <p>我是一名热衷于技术的开发者，喜欢分享知识和经验。这个博客是我记录学习、思考和生活点滴的地方。</p>
        <p>主要技术栈包括：</p>
        <ul>
          <li>前端：Vue.js, React, Element Plus, Vite</li>
          <li>后端：Spring Boot, Node.js, Express</li>
          <li>数据库：MySQL, MongoDB</li>
          <li>其他：Docker, Git, Linux</li>
        </ul>
        <p>如果你有任何问题或建议，欢迎通过 505392837@qq.com 与我联系。</p>
        <p>感谢你的访问！</p>
        <img :src="avatar" alt="食草兽"  class="avatar"/>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import avatar from '@/assets/Chirpchump.png'
</script>

<style scoped>
.about-view {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
.card-header h1 {
  margin: 0;
  font-size: 1.8em;
  text-align: center;
}
.about-content {
  line-height: 1.8;
  font-size: 1.1em;
  color: #333;
  text-align: center;
}
.about-content p {
  margin-bottom: 1.2em;
}
.about-content ul {
  list-style-type: disc;
  padding-left: 250px;
  margin-bottom: 1.2em;
  text-align: left;
}
.avatar {
  width: 100%;
  max-width: 200px;
  border-radius: 50%;
  border: 3px solid lightpink;
  box-shadow: 0 0 10px #ccc;
  margin: 20px auto;
}
</style>