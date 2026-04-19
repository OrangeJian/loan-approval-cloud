<template>
  <div>
    <h2>产品管理</h2>
    <el-card style="margin-top:20px">
      <!-- 搜索和操作栏 -->
      <div style="margin-bottom:16px;display:flex;gap:12px;flex-wrap:wrap;align-items:center">
        <el-input v-model="searchKeyword" placeholder="搜索产品名称或代码" style="width:200px" clearable @clear="loadProducts" @keyup.enter="handleSearch" />
        <el-select v-model="searchType" placeholder="产品类型" style="width:150px" clearable @change="handleSearch">
          <el-option label="全部" value="" />
          <el-option label="循环贷" value="REVOLVING" />
          <el-option label="一次性" value="TERM" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button type="success" @click="openDialog(null)">新增产品</el-button>
      </div>

      <!-- 产品列表 -->
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="productCode" label="产品代码" />
        <el-table-column prop="productName" label="产品名称" />
        <el-table-column prop="productType" label="产品类型">
          <template #default="{ row }">
            <el-tag :type="row.productType === 'REVOLVING' ? 'primary' : 'warning'">
              {{ row.productType === 'REVOLVING' ? '循环贷' : '一次性' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额范围">
          <template #default="{ row }">{{ formatAmount(row.minAmount) }} - {{ formatAmount(row.maxAmount) }}</template>
        </el-table-column>
        <el-table-column label="利率范围">
          <template #default="{ row }">{{ (row.minInterestRate * 100).toFixed(2) }}% - {{ (row.maxInterestRate * 100).toFixed(2) }}%</template>
        </el-table-column>
        <el-table-column label="期限（月）">
          <template #default="{ row }">{{ row.minTerm }} - {{ row.maxTerm }}</template>
        </el-table-column>
        <el-table-column prop="repaymentType" label="还款方式" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === '1' ? 'success' : 'danger'">
              {{ row.status === '1' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑产品' : '新增产品'" width="600px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="产品代码" prop="productCode">
          <el-input v-model="form.productCode" placeholder="如：REV_CREDIT" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="form.productName" placeholder="如：循环信用贷" />
        </el-form-item>
        <el-form-item label="产品类型" prop="productType">
          <el-select v-model="form.productType" placeholder="请选择" style="width:100%">
            <el-option label="循环贷" value="REVOLVING" />
            <el-option label="一次性贷款" value="TERM" />
          </el-select>
        </el-form-item>
        <el-form-item label="最小金额" prop="minAmount">
          <el-input-number v-model="form.minAmount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="最大金额" prop="maxAmount">
          <el-input-number v-model="form.maxAmount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="最小期限（月）" prop="minTerm">
          <el-input-number v-model="form.minTerm" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="最大期限（月）" prop="maxTerm">
          <el-input-number v-model="form.maxTerm" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="最小利率" prop="minInterestRate">
          <el-input-number v-model="form.minInterestRate" :min="0" :max="1" :precision="4" :step="0.0001" style="width:100%" />
        </el-form-item>
        <el-form-item label="最大利率" prop="maxInterestRate">
          <el-input-number v-model="form.maxInterestRate" :min="0" :max="1" :precision="4" :step="0.0001" style="width:100%" />
        </el-form-item>
        <el-form-item label="还款方式" prop="repaymentType">
          <el-input v-model="form.repaymentType" placeholder="如：等额本息" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="1">启用</el-radio>
            <el-radio value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'

const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const searchKeyword = ref('')
const searchType = ref('')

const defaultForm = () => ({
  productCode: '',
  productName: '',
  productType: 'REVOLVING',
  minAmount: 5000,
  maxAmount: 100000,
  minTerm: 1,
  maxTerm: 12,
  minInterestRate: 0.005,
  maxInterestRate: 0.015,
  repaymentType: '等额本息',
  status: '1'
})

const form = ref(defaultForm())

const rules = {
  productCode: [{ required: true, message: '请输入产品代码', trigger: 'blur' }],
  productName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  productType: [{ required: true, message: '请选择产品类型', trigger: 'change' }],
  minAmount: [{ required: true, message: '请输入最小金额', trigger: 'blur' }],
  maxAmount: [{ required: true, message: '请输入最大金额', trigger: 'blur' }],
  minTerm: [{ required: true, message: '请输入最小期限', trigger: 'blur' }],
  maxTerm: [{ required: true, message: '请输入最大期限', trigger: 'blur' }],
  minInterestRate: [{ required: true, message: '请输入最小利率', trigger: 'blur' }],
  maxInterestRate: [{ required: true, message: '请输入最大利率', trigger: 'blur' }],
  repaymentType: [{ required: true, message: '请输入还款方式', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const formatAmount = (val) => {
  if (!val) return '0'
  return new Intl.NumberFormat('zh-CN').format(val)
}

const loadProducts = async () => {
  loading.value = true
  try {
    const res = await request.get('/product/all')
    tableData.value = res.data || []
  } catch (e) {
    ElMessage.error('加载产品列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value && !searchType.value) {
    loadProducts()
    return
  }
  loading.value = true
  try {
    const params = {}
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (searchType.value) params.productType = searchType.value
    const res = await request.get('/product/list', { params })
    tableData.value = (res.data?.records || res.data || [])
  } catch (e) {
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

const openDialog = (row) => {
  if (row) {
    isEdit.value = true
    form.value = { ...row }
  } else {
    isEdit.value = false
    form.value = defaultForm()
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put('/product', form.value)
      ElMessage.success('更新成功')
    } else {
      await request.post('/product', form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadProducts()
  } catch (e) {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除产品「${row.productName}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await request.delete(`/product/${row.id}`)
    ElMessage.success('删除成功')
    loadProducts()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadProducts()
})
</script>
