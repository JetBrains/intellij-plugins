export const state = () => ({
    propInModule2: "Initial"
})

export const mutations = {
    update(state) {
        state.propInModule2 = "updated"
    }
}

export const actions = {}

export const getters = {}
