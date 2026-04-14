<template>
  <div>
    <h2>审批管理</h2>
    <el-card style="margin-top:20px">
      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="loanNo" label="贷款号" />
        <el-table-column prop="customerName" label="客户姓名" />
        <el-table-column prop="amount" label="申请金额" />
        <el-table-column prop="stage" label="审批阶段" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PASS' ? 'success' : row.status === 'REJECT' ? 'danger' : 'warning'">
              {{ row.status === 'PASS' ? '通过' : row.status === 'REJECT' ? '拒绝' : '待审批' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" size="small" type="success" @click="handleApprove(row, 'PASS')">通过</el-button>
            <el-button v-if="row.status === 'PENDING'" size="small" type="danger" @click="handleApprove(row, 'REJECT')">拒绝</el-button>
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
  { id: 1, loanNo: 'LN001', customerName: '张三', amount: 50000, stage: '人工风控', status: 'PASS' },
  { id: 2, loanNo: 'LN002', customerName: '李四', amount: 100000, stage: '人工额度', status: 'PENDING' }
])
const handleApprove = (row, result) => {
  row.status = result
  ElMessage.success('审批完成')
}
</script>
