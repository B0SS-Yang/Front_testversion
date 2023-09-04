import axios from "axios";
import {ElMessage} from "element-plus";

// 认证信息存储的键名
const authItemName = "access_token";

// 默认请求失败处理函数
const defaultFailure = (message, code, url) => {
    console.warn(`请求地址: ${url} 错误信息: ${message} 错误码: ${code}`);
    ElMessage.warning(message);
};

// 默认错误处理函数
const defaultError = (err) => {
    console.error(err);
    ElMessage.warning("发生错误！");
};

// 从本地存储获取访问令牌
function takeAccessToken() {
    const str = localStorage.getItem(authItemName) || sessionStorage.getItem(authItemName);
    if (!str) {
        return null;
    }
    const authObj = JSON.parse(str);
    if (authObj.expire < new Date().getTime()) {
        deleteAccessToken();
        ElMessage.warning("登录已过期！");
        return null;
    }
    return authObj.token;
}

// 将访问令牌存储到本地
function storeAccessToken(token, remember, expire) {
    const authObj = {
        token: token,
        expire: expire,
    };
    const str = JSON.stringify(authObj);
    if (remember) {
        localStorage.setItem(authItemName, str);
    } else {
        sessionStorage.setItem(authItemName, str);
    }
}

// 删除访问令牌
function deleteAccessToken() {
    localStorage.removeItem(authItemName);
    sessionStorage.removeItem(authItemName);
}

// 构建请求头部，包含访问令牌
function accessHeader() {
    const token = takeAccessToken();
    return token ? {
        "Authorization": `Bearer ${takeAccessToken()}`
    } : {};
}

// 发送内部POST请求
function internalPost(url, data, header, success, failure, error) {
    axios.post(url, data, {headers: header}).then(({data}) => {
        if (data.code === 200) {
            success(data.data);
        } else {
            failure(data.message, data.code, url);
        }
    }).catch(err => error(err));
}

// 发送内部GET请求
function internalGet(url, header, success, failure, error) {
    axios.get(url, {headers: header}).then(({data}) => {
        if (data.code === 200) {
            success(data.data);
        } else {
            failure(data.message, data.code, url);
        }
    }).catch(err => error(err));
}

/**
 * 发起GET请求
 *
 * @param {string} url 请求的URL
 * @param {function} success 请求成功回调函数
 * @param {function} failure 请求失败回调函数（可选）
 */
function get(url, success, failure = defaultFailure) {
    internalGet(url, accessHeader(), success, failure);
}

/**
 * 发起POST请求
 *
 * @param {string} url 请求的URL
 * @param {object} data 请求的数据
 * @param {function} success 请求成功回调函数
 * @param {function} failure 请求失败回调函数（可选）
 */
function post(url, data, success, failure = defaultFailure) {
    internalPost(url, data, accessHeader(), success, failure);
}

/**
 * 用户登录
 *
 * @param {string} username 用户名
 * @param {string} password 密码
 * @param {boolean} remember 是否记住登录状态
 * @param {function} success 登录成功回调函数
 * @param {function} failure 请求失败回调函数（可选）
 * @param {function} error 发生错误时回调函数（可选）
 */
function login(username, password, remember, success, failure = defaultFailure, error = defaultError) {
    internalPost('/api/auth/login', {
        username: username,
        password: password,
    }, {
        'Content-Type': 'application/x-www-form-urlencoded'
    }, (data) => {
        storeAccessToken(data.token, remember, data.expire);
        ElMessage.success(`登录成功！欢迎${data.username}！`);
        success(data);
    }, failure);
}

/**
 * 用户登出
 *
 * @param {function} success 登出成功回调函数
 * @param {function} failure 请求失败回调函数（可选）
 */
function logout(success, failure = defaultFailure) {
    get('api/auth/logout', () => {
        deleteAccessToken();
        ElMessage.success('登出成功！');
        success();
    }, failure);
}

function unauthorized() {
    return !takeAccessToken();
}

export {login, logout, get, post, unauthorized};