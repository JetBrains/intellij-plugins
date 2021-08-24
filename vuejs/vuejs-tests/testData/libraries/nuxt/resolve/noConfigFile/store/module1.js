export const state = () => ({
    propInModule1: "Initial"
})

export const mutations = {
    update(state) {
        state.propInModule1 = "Updated"
    }
}

export const actions = {
    update({commit}) {
        commit("update")
    }
}

export const getters = {}
