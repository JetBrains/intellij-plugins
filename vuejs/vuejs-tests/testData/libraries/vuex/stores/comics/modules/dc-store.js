const namespaced = true

const state = {}

const getters = {}

const mutations = {}

const actions = {
  async save() {},

  async callSuperman(vuex, {caller = 'dc'}) {
    console.log(`superman was called by ${caller}`)
  },

  async callBatman(vuex, {caller = 'dc'}) {
    console.log(`batman was called by ${caller}`)
  },

  async callSupermanFromDc() {
    this.dispatch('dc/callSuperman', {caller: 'a friend at DC'})
  },
}

export default {
  namespaced,
  state,
  mutations,
  actions,
  getters,
}
