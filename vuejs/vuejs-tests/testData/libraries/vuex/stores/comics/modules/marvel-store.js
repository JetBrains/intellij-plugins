const namespaced = true

const state = {}

const getters = {}

const mutations = {}

const actions = {
    async save() {},

    async callSupermanFromMarvel() {
      this.dispatch('dc/callSuperman', {caller: 'marvel'})
    },
}

export default {
  namespaced,
  state,
  mutations,
  actions,
  getters,
}
