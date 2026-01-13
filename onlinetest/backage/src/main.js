// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'

import axios from 'axios'
Vue.prototype.$axios = axios

// Attach token for all requests (backage-api, etc.)
axios.interceptors.request.use(
  (config) => {
    const token = sessionStorage.getItem('AUTHORIZATION')
    if (token) {
      config.headers = config.headers || {}
      if (!config.headers['AUTHORIZATION']) {
        config.headers['AUTHORIZATION'] = token
      }
      if (!config.headers['Authorization']) {
        config.headers['Authorization'] = token
      }
    }
    return config
  },
  (error) => Promise.reject(error)
)

axios.interceptors.response.use(
  (resp) => resp,
  (error) => {
    const status = error && error.response && error.response.status
    if (status === 401 || status === 403) {
      sessionStorage.clear()
      try {
        router.replace({ path: '/' })
      } catch (e) {
        // ignore
      }
    }
    return Promise.reject(error)
  }
)

// third-party: qs
import qs from 'qs'
Vue.prototype.$qs = qs

// ElementUI
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
Vue.use(ElementUI)

Vue.config.productionTip = false

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  components: { App },
  template: '<App/>'
})
