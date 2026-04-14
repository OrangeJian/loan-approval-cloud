<template>
  <div>
    <h2>贷款申请</h2>
    <el-card style="margin-top:20px">
      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="loanNo" label="贷款号" />
        <el-table-column prop="customerName" label="客户姓名" />
        <el-table-column prop="productName" label="产品" />
        <el-table-column prop="amount" label="申请金额" />
        <el-table-column prop="term" label="期限" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 'CREDIT_APPROVED' ? 'success' : row.status === 'CREDIT_REJECTED' ? 'danger' : 'warning'">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
const tableData = ref([])
const getStatusText = (status) => {
  const map = { CREDIT_PENDING: '待审批', CREDIT_APPROVED: '已通过', CREDIT_REJECTED: '已拒绝', LOAN_PENDING: '用信待审批', ACTIVE: '生效中', CLEARED: '已结清' }
  return map[status] || status
}
onMounted(() => {
  tableData.value = [
    { id: 1, loanNo: 'LN001', customerName: '张三', productName: '循环信用贷', amount: 50000, term: 12, status: 'CREDIT_APPROVED' },
    { id: 2, loanNo: 'LN002', customerName: '李四', productName: '个人消费贷', amount: 100000, term: 24, status: 'CREDIT_PENDING' },
    { id: 3, loanNo: 'LN003', customerName: '王五', productName: '个人消费贷', amount: 300000, term: 36, status: 'ACTIVE' }
  ]
})
</script>
