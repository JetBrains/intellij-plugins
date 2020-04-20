import Vue from 'vue';
import Vuex from 'vuex';
import myVuexModule from './myVuexModule';

Vue.use(Vuex);

const services = {
  fetchProp1: () => Promise.resolve('456'),
};

export default new Vuex.Store({
  namespaced: true,
  modules: { myVuexModule:  myVuexModule(services) },
});
