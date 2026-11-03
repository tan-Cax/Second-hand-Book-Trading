<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="login-card__title">二手书交易平台</h1>

      <el-tabs v-model="activeTab" class="login-card__tabs">
        <el-tab-pane label="登录" name="login">
          <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-width="0" @submit.prevent="handleLogin">
            <el-form-item prop="name">
              <el-input v-model="loginForm.name" placeholder="用户名" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="loginForm.password" type="password" placeholder="密码" size="large" show-password />
            </el-form-item>
            <el-button type="primary" size="large" :loading="loading" native-type="submit" style="width: 100%; height: 44px; font-size: 16px;">
              登 录
            </el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-width="0" @submit.prevent="handleRegister">
            <el-form-item prop="name">
              <el-input v-model="registerForm.name" placeholder="用户名" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="registerForm.password" type="password" placeholder="密码" size="large" show-password />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" size="large" show-password />
            </el-form-item>
            <el-button type="primary" size="large" :loading="loading" native-type="submit" style="width: 100%; height: 44px; font-size: 16px;">
              注 册
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const activeTab = ref('login')
const loading = ref(false)
const loginFormRef = ref()
const registerFormRef = ref()

const loginForm = reactive({ name: '', password: '' })
const registerForm = reactive({ name: '', password: '', confirmPassword: '' })

const loginRules = {
  name: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const registerRules = {
  name: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度为 2-20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

async function handleLogin() {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await authStore.login({ name: loginForm.name, password: loginForm.password })
    ElMessage.success('登录成功')
    if (authStore.isAdmin) {
      router.push('/admin')
    } else {
      router.push('/home')
    }
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await authStore.register({ name: registerForm.name, password: registerForm.password })
    ElMessage.success('注册成功，请登录')
    activeTab.value = 'login'
    loginForm.name = registerForm.name
    loginForm.password = ''
  } catch (e) {
    ElMessage.error(e.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;

.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, $primary-bg 0%, $bg-page 50%, $primary-light 100%);
}

.login-card {
  width: 400px;
  background: $bg-white;
  border-radius: 6px;
  padding: 40px 36px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);

  &__title {
    text-align: center;
    font-size: 22px;
    font-weight: 600;
    color: $primary-color;
    margin-bottom: 28px;
  }

  &__tabs {
    :deep(.el-tabs__header) {
      margin-bottom: 24px;
    }

    :deep(.el-tabs__item) {
      font-size: 15px;
      height: 40px;
      line-height: 40px;
    }

    :deep(.el-input__wrapper) {
      height: 42px;
    }
  }
}
</style>
