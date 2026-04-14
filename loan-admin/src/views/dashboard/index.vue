<template>
  <div>
    <h2>首页统计</h2>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="6"><el-card><div>总申请数</div><div style="font-size:24px;font-weight:bold">{{ stats.totalApplications }}</div></el-card></el-col>
      <el-col :span="6"><el-card><div>待审批</div><div style="font-size:24px;font-weight:bold;color:#E6A23C">{{ stats.pendingApprovals }}</div></el-card></el-col>
      <el-col :span="6"><el-card><div>已通过</div><div style="font-size:24px;font-weight:bold;color:#67C23A">{{ stats.approved }}</div></el-card></el-col>
      <el-col :span="6"><el-card><div>已放款</div><div style="font-size:24px;font-weight:bold;color:#409EFF">{{ stats.loaned }}</div></el-card></el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
const stats = ref({ totalApplications: 0, pendingApprovals: 0, approved: 0, loaned: 0 })
onMounted(() => { stats.value = { totalApplications: 10, pendingApprovals: 3, approved: 5, loaned: 2 } })
</script>
