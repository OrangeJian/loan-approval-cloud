<template>
  <div>
    <h2>产品管理</h2>
    <el-card style="margin-top:20px">
      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="productCode" label="产品代码" />
        <el-table-column prop="productName" label="产品名称" />
        <el-table-column prop="productType" label="产品类型">
          <template #default="{ row }"><el-tag>{{ row.productType === 'REVOLVING' ? '循环贷' : '一次性' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="金额范围">
          <template #default="{ row }">{{ row.minAmount }} - {{ row.maxAmount }}</template>
        </el-table-column>
        <el-table-column label="利率范围">
          <template #default="{ row }">{{ (row.minInterestRate * 100).toFixed(2) }}% - {{ (row.maxInterestRate * 100).toFixed(2) }}%</template>
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
    { id: 1, productCode: 'REV_CREDIT', productName: '循环信用贷', productType: 'REVOLVING', minAmount: 5000, maxAmount: 100000, minInterestRate: 0.006, maxInterestRate: 0.012, status: '1' },
    { id: 2, productCode: 'TERM_PERSONAL', productName: '个人消费贷', productType: 'TERM', minAmount: 5000, maxAmount: 500000, minInterestRate: 0.0045, maxInterestRate: 0.01, status: '1' }
  ]
})
</script>
