export const state = () => ({
    propInRoot: "Initial"
})

export const mutations = {
    update(state) {
        state.propInRoot = "updated"
    }
}

export const actions = {}

export const getters = {}
