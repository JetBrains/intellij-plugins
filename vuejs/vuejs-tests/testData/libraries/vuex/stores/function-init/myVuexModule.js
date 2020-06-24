export default (services) => ({
  namespaced: true,
  state: {
    prop1: '123',
  },
  mutations: {
    setProp1(state, { value }) {
      state.prop1 = value;
    },
  },
  actions: {
    fetchProp1(context) {
      services.fetchProp1().then(prop1 => {
        context.commit('setProp1', { value: prop1 });
      });
    },
  },
});
