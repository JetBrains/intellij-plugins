const counterModule = {
    namespaced: true,

    state() {
        return {
            counter: 0
        }
    },

    getters: {
        getCounter(state) { return state.counter }
    }
}

export default counterModule
