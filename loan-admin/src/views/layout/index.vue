<template>
  <el-container class="layout-container">
    <el-aside width="200px" style="background:#304156">
      <div style="height:60px;line-height:60px;text-align:center;color:#fff;font-size:18px;font-weight:bold;background:#2b3a4d">贷款系统</div>
      <el-menu :default-active="$route.path" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF">
        <el-menu-item index="/dashboard"><span>首页</span></el-menu-item>
        <el-menu-item index="/customer"><span>客户管理</span></el-menu-item>
        <el-menu-item index="/product"><span>产品管理</span></el-menu-item>
        <el-menu-item index="/application"><span>贷款申请</span></el-menu-item>
        <el-menu-item index="/approval"><span>审批管理</span></el-menu-item>
        <el-menu-item index="/contract"><span>合同管理</span></el-menu-item>
        <el-menu-item index="/account"><span>账务管理</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="display:flex;align-items:center;justify-content:flex-end;background:#fff;border-bottom:1px solid #e6e6e6">
        <span style="margin-right:15px">{{ username }}</span>
        <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
      </el-header>
      <el-main style="background:#f0f2f5"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import store from '../../utils/store'

const router = useRouter()
const username = ref('')

onMounted(() => { username.value = store.get('username') || '用户' })

const handleLogout = () => { store.clear(); router.push('/login') }
</script>

<style scoped>
.layout-container { height: 100vh; }
</style>
