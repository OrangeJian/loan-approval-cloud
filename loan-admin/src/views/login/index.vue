<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>贷款审批系统</h2>
      <el-form :model="form" @submit.prevent="handleLogin">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width:100%" @click="handleLogin">登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
import store from '../../utils/store'

const router = useRouter()
const form = ref({ username: 'admin', password: 'admin123' })

const handleLogin = async () => {
  try {
    // 简化登录：实际应该调用后端auth接口
    store.set('token', 'mock-token')
    store.set('username', form.value.username)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    console.error(e)
  }
}
</script>

<style scoped>
.login-container { display: flex; justify-content: center; align-items: center; height: 100vh; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.login-card { width: 400px; padding: 20px; }
h2 { text-align: center; margin-bottom: 30px; color: #333; }
</style>
