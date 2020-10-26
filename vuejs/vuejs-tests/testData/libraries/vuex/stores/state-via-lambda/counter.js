export default {
    namespaced: true,
    state: () => ({
        value: 0,
    }),
    mutations: {
        increment(state) {
            state.value++;
        }
    }
}