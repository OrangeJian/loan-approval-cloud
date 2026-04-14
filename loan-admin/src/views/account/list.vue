<template>
  <div>
    <h2>账务管理</h2>
    <el-card style="margin-top:20px">
      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="loanNo" label="贷款号" />
        <el-table-column prop="customerName" label="客户姓名" />
        <el-table-column prop="loanAmount" label="借款金额" />
        <el-table-column prop="totalAmount" label="总金额" />
        <el-table-column prop="repaidAmount" label="已还金额" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }"><el-tag :type="row.status === 'CLEARED' ? 'success' : 'warning'">{{ row.status === 'CLEARED' ? '已还清' : '还款中' }}</el-tag></template>
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
    { id: 1, loanNo: 'LN001', customerName: '张三', loanAmount: 50000, totalAmount: 54000, repaidAmount: 27000, status: 'ACTIVE' },
    { id: 2, loanNo: 'LN003', customerName: '王五', loanAmount: 300000, totalAmount: 324000, repaidAmount: 324000, status: 'CLEARED' }
  ]
})
</script>
