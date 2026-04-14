<template>
  <div>
    <h2>客户管理</h2>
    <el-card style="margin-top:20px">
      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="customerNo" label="客户号" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="riskLevel" label="风险等级">
          <template #default="{ row }">{{ row.riskLevel === 'LOW' ? '低' : row.riskLevel === 'MEDIUM' ? '中' : '高' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }"><el-tag :type="row.status === '1' ? 'success' : 'danger'">{{ row.status === '1' ? '启用' : '禁用' }}</el-tag></template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
const tableData = ref([])
onMounted(() => {
  tableData.value = [
    { id: 1, customerNo: 'C001', name: '张三', phone: '13800138000', riskLevel: 'LOW', status: '1' },
    { id: 2, customerNo: 'C002', name: '李四', phone: '13900139000', riskLevel: 'MEDIUM', status: '1' },
    { id: 3, customerNo: 'C003', name: '王五', phone: '13700137000', riskLevel: 'HIGH', status: '1' }
  ]
})
</script>
