# SVlogin
## 数据库
### 数据库连接配置
在./my-project-backend/src/main/resources/application.properties中修改数据库连接配置
### 数据库建表
建表语句为db_account.sql，
若使用默认配置，需要在本地创建名为test的数据库

## my-project-frontend前端
注意前端操作要在./my-project-frontend目录下进行
### Project Setup安装依赖

```sh
npm install
```

#### Compile and Hot-Reload for Development编译运行

```sh
npm run dev
```

#### Compile and Minify for Production

```sh
npm run build
```

## my-project-backend后端
注意后端操作要在./my-project-backend目录下进行
### Project Setup安装依赖
打开IDEA，导入项目，去pom.xml中下载依赖或者等待IDEA自动下载依赖
### 邮箱配置
在./my-project-backend/src/main/java/com/example/my-project-backend/resources/application.yml中修改邮箱配置
### Run
点击IDEA右上角的运行按钮，选择运行后端程序（MyProjectBackendApplication）

