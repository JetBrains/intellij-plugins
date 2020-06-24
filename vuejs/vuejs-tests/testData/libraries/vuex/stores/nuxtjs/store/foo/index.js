export const state = () => ({
  data: ''
});

export const mutations = {
  UPDATE_TEST_DATA({state}, data) {
    state.data = data;
  }
};
