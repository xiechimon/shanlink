import { createStore } from 'vuex'

// 创建一个新的 store 实例
const store = createStore({
  state() {
    return {
      // 短链接默认域名：部署后改成你的实际短链接域名
      domain: ''
    }
  }
})

export default store
