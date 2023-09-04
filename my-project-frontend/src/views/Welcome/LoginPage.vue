<script setup>
import {Aim, User} from '@element-plus/icons';
import {reactive, ref} from 'vue';
import {login} from "@/net";
import router from "../../../router";

const formRef = ref()

const form = reactive({
  username: '',
  password: '',
  remember: false
})

const rule = {
  username: [
    {required: true, message: '请输入用户名'},
  ],
  password: [
    {required: true, message: '请输入密码'},
  ]
}

function userLogin() {
  formRef.value.validate((valid) => {
    if (valid) {
      login(form.username, form.password, form.remember, () => router.push('/index'))
    }
  });
}

</script>

<template>
  <div style="text-align: center;margin: 0 20px">
    <div style="margin-top:150px">
      <div style="font-size: 25px;font-weight: bold">登录</div>
      <div style="font-size: 14px;color: #999;margin-top: 10px">please input your id and password</div>
    </div>
    <div style="margin-top: 50px">
      <el-form :model="form" :rules="rule" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" maxlength="10" type="text" placeholder="username or email">
            <template #prefix>
              <el-icon>
                <User/>
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" maxlength="20" type="password" placeholder="password">
            <template #prefix>
              <el-icon>
                <Aim/>
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-row>
          <el-col :span="12" style="text-align: left">
            <el-form-item prop="remember">
              <el-checkbox v-model="form.remember" label="remeber me"/>
            </el-form-item>
          </el-col>
          <el-col :span="12" style="text-align: right">
            <el-button type="text" size="small">forget password</el-button>
          </el-col>
        </el-row>
      </el-form>
    </div>
    <div style="margin-top: 30px">
      <el-button @click="userLogin" type="primary" style="width: 100%" plain>login</el-button>
    </div>
    <div>
      <el-divider>
      <span style="font-size: 13px;color: grey">
        没有账号
      </span>
      </el-divider>
      <div>
        <el-button style="width: 270px" type="warning" plain>注册</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>