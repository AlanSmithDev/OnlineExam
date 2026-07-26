// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import axios from 'axios' // 引入 axios

Vue.config.productionTip = false

Vue.use(ElementUI)

axios.interceptors.request.use((config) => {
  try {
    const token = sessionStorage.getItem('AUTHORIZATION') || '';
    if (token) {
      config.headers = config.headers || {};
      config.headers.AUTHORIZATION = token;
      config.headers.Authorization = token;
    }
  } catch (e) {}
  return config;
});

Vue.prototype.$axios = axios // 将 axios 挂载到 Vue 原型上，解决 this.$axios 未定义的问题

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  components: { App },
  template: '<App/>'
})
