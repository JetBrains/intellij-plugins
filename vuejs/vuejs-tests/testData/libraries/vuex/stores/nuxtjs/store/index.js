export const state = () => ( {
	test: {
		data: ''
	}
});

export const mutations = {
	 UPDATE_TEST_DATA( { state }, data ) {
		state.test.data = data;
	}
};
