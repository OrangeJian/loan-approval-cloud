<template>
  <div>
    <h2>合同管理</h2>
    <el-card style="margin-top:20px">
      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="contractNo" label="合同编号" />
        <el-table-column prop="loanNo" label="贷款号" />
        <el-table-column prop="customerName" label="客户姓名" />
        <el-table-column prop="amount" label="合同金额" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }"><el-tag :type="row.status === 'SIGNED' ? 'success' : 'warning'">{{ row.status === 'SIGNED' ? '已签署' : '未签署' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button v-if="row.status !== 'SIGNED'" size="small" type="success" @click="handleSign(row)">签署</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
const tableData = ref([
  { id: 1, contractNo: 'CT001', loanNo: 'LN001', customerName: '张三', amount: 50000, status: 'SIGNED' },
  { id: 2, contractNo: 'CT002', loanNo: 'LN002', customerName: '李四', amount: 100000, status: 'PENDING' }
])
const handleSign = (row) => { row.status = 'SIGNED'; ElMessage.success('签署成功') }
</script>
