<script setup>

</script>

<template>
  <div style="width: 100vw;height: 100vh;overflow: hidden;display: flex">
    <div style="flex: 1;background-color: black">
    <el-image style="width: 100%;height: 100%" fit="cover"
       src="https://t7.baidu.com/it/u=1819248061,230866778&fm=193&f=GIF"/>
    </div>
    <div class="welcome-title">
      <h1>欢迎使用</h1>
      <h2>欢迎使用</h2>
      <h3>欢迎使用</h3>
      <h4>欢迎使用</h4>
      <h5>欢迎使用</h5>
      <h6>欢迎使用</h6>
    </div>
    <div class="right-card">
      <router-view/>
    </div>
  </div>
</template>

<style scoped>
.welcome-title {
  position: absolute;
  bottom: 30px;
  left: 30px;
  color: white;
  text-shadow: 0 0 10px black;
}
.right-card{
  width: 400px;
  z-index: 1;
  background-color: #fff;
}
</style>