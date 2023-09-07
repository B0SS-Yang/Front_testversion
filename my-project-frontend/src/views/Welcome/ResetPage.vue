<script setup>

import {computed, reactive, ref} from "vue";

import {Bell, Message, Lock} from "@element-plus/icons";
import {get, post} from "@/net";
import {ElMessage} from "element-plus";
import router from "../../../router";

const coldTime = ref(0)
const active = ref(0)
const isEmailValid = computed(() => /^[\w\.-]+@[\w\.-]+\.\w+$/.test(form.email))
const formRef = ref();

const form = reactive({
  email: '',
  code: '',
  password: '',
  password_repeat: ''
})

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}
const rules = {
  password: [
    {required: true, message: '请输入密码', trigger: ['blur', 'change']},
    {min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: ['blur', 'change']}
  ],
  password_repeat: [
    {validator: validatePassword, trigger: ['blur', 'change']}
  ],
  email: [
    {required: true, message: '请输入电子邮件地址', trigger: ['blur', 'change']},
    {type: 'email', message: '请输入正确的电子邮件地址', trigger: ['blur', 'change']}
  ],
  code: [
    {required: true, message: '请输入验证码', trigger: ['blur', 'change']},
  ]
}

function askCode() {
  if (isEmailValid) {
    coldTime.value = 60
    get(`/api/auth/ask-code?email=${form.email}&type=register`, () => {
      ElMessage.success(`验证码已发送至您的邮箱${form.email},请注意查收!`)
      setInterval(() => {
        if (coldTime.value > 0) {
          coldTime.value--
        }
      }, 1000)
    }, (message) => {
      ElMessage.error(message)
      coldTime.value = 0
    })
  } else {
    ElMessage.warning('请输入正确的电子邮件地址!')
  }
}

function confirmReset() {//第一阶段
  formRef.value.validate((valid) => {
    if (valid) {
      post('api/auth/reset-confirm', {
        email: form.email,
        code: form.code,
      }, () => active.value++)
    }
  })
}

function doReset() {
  formRef.value.validate((valid) => {
    if (valid) {
      post('api/auth/reset-password', {...form}, () => {
        ElMessage.success('密码重置成功!')
        router.push('/')
      })
    }
  })
}

</script>

<template>
  <div style="text-align: center;">
    <div style="margin-top: 30px">
      <el-steps :active="active" finish-status="success" align-center>
        <el-step title="验证电子邮件"/>
        <el-step title="重置密码"/>
      </el-steps>
    </div>
    <div v-if="active===0" style="margin: 0 20px">
      <div style="margin-top: 100px">
        <div style="font-size: 25px;font-weight: bold"> 重置密码</div>
        <div style="font-size: 14px;color: grey">Enter E-mail:</div>
      </div>
      <div style="margin-top: 50px; ">
        <el-form :model="form" :rules="rules" ref="formRef">
          <el-form-item prop="email">
            <el-input v-model="form.email" type="email" placeholder="email address">
              <template #prefix>
                <el-icon>
                  <Message/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="code">
            <el-row :gutter="10" style="width: 100%">
              <el-col :span="17">
                <el-input v-model="form.code" :maxlength="6" type="text" placeholder="请输入验证码">
                  <template #prefix>
                    <el-icon>
                      <Bell/>
                    </el-icon>
                  </template>
                </el-input>
              </el-col>
              <el-col :span="5">
                <el-button @click="askCode" :disabled="!isEmailValid || coldTime>0" type="success">
                  {{ coldTime > 0 ? coldTime + 's' : '获取验证码' }}
                </el-button>
              </el-col>
            </el-row>
          </el-form-item>

        </el-form>
      </div>
      <div style="margin-top: 20px">
        <el-button type="warning" @click="confirmReset" style="width: 270px">下一步</el-button>
      </div>
    </div>
    <div style="margin: 0 20px" v-if="active === 1">
      <div style="margin-top: 100px">
        <div style="font-size: 25px;font-weight: bold"> 重置密码</div>
        <div style="font-size: 14px;color: grey">请填写新密码</div>
      </div>
      <div style="margin-top: 50px">
        <el-form :model="form" :rules="rules" ref="formRef">
          <el-form-item prop="password">
            <el-input v-model="form.password" :maxlength="20" type="password" placeholder="密码">
              <template #prefix>
                <el-icon>
                  <Lock/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password_repeat">
            <el-input v-model="form.password_repeat" :maxlength="20" type="password" placeholder="重复密码">
              <template #prefix>
                <el-icon>
                  <Lock/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>

        </el-form>
      </div>
      <div style="margin-top: 20px">
        <el-button type="danger" @click="doReset" style="width: 270px">立即重置密码</el-button>

      </div>
    </div>
  </div>
</template>

<style scoped>

</style>