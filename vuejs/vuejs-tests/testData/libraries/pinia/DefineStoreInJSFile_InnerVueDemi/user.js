import { defineStore } from 'pinia'

export const useUserStore = defineStore({
  id: 'user',
  state: () => ({
    userInfo: '',
  }),
  actions: {
    init() {
      this.userInfo = ''
    }
  },
})

export const useOtherStore = defineStore({
  id: 'other',
  state: () => ({
    otherStateField: '',
  }),
  actions: {
    otherInit() {
    }
  },
})

