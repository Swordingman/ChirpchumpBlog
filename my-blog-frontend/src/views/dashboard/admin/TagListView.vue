<template>
  <div class="category-list-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>标签管理</span>
          <el-button type="primary" :icon="Plus" @click="handleOpenDialog()">标签分类</el-button>
        </div>
      </template>
      <el-table :data="tags" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="100" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="slug" label="Slug" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" type="primary" :icon="Edit" @click="handleOpenDialog(row)" />
            <el-popconfirm title="确定删除该标签吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button size="small" type="danger" :icon="Delete" />
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" @close="handleCloseDialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="标签Slug" prop="slug">
          <el-input v-model="form.slug" placeholder="可选，留空会自动生成" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus, Edit, Delete } from '@element-plus/icons-vue';
import {
  fetchAllTagsAdmin,
  createTagAdmin,
  updateTagAdmin,
  deleteTagAdmin
} from '@/api/metaService';

const tags = ref([]);
const loading = ref(true);
const submitLoading = ref(false);
const dialogVisible = ref(false);
const formRef = ref(null);
const isEditMode = ref(false);
const currentEditId = ref(null);

const form = reactive({
  name: '',
  slug: '',
});

const rules = {
  name: [{ required: true, message: '请输入标签名称', trigger: 'blur' }],
};

const dialogTitle = computed(() => (isEditMode.value ? '编辑标签' : '新建标签'));

const loadTags = async () => {
  loading.value = true;
  try {
    tags.value = await fetchAllTagsAdmin();
  } catch (error) {
    ElMessage.error('加载标签列表失败: ' + (error.message || ''));
  } finally {
    loading.value = false;
  }
};

const handleOpenDialog = (row = null) => {
  if (row) {
    isEditMode.value = true;
    currentEditId.value = row.id;
    Object.assign(form, { name: row.name, slug: row.slug });
  } else {
    isEditMode.value = false;
    currentEditId.value = null;
  }
  dialogVisible.value = true;
};

const handleCloseDialog = () => {
  if (formRef.value) {
    Object.assign(form, { name: '', slug: '' });
    formRef.value.clearValidate();
  }
};

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true;
      try {
        if (isEditMode.value) {
          await updateTagAdmin(currentEditId.value, form);
        } else {
          await createTagAdmin(form);
        }
        ElMessage.success(isEditMode.value ? '更新成功！' : '创建成功！');
        dialogVisible.value = false;
        await loadTags();
      } catch (error) {
        ElMessage.error('操作失败: ' + (error.message || ''));
      } finally {
        submitLoading.value = false;
      }
    }
  });
};

const handleDelete = async (id) => {
  try {
    await deleteTagAdmin(id);
    ElMessage.success('删除成功！');
    await loadTags();
  } catch (error) {
    ElMessage.error('删除失败: ' + (error.message || ''));
  }
};

onMounted(() => {
  loadTags();
});
</script>

<style scoped>
.category-list-view {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.el-table .el-button + .el-button {
  margin-left: 8px;
}
</style>