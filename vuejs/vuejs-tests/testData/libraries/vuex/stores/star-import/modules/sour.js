export const namespaced = true;

export const state = {
  loading: false,
  tasks: [],
}

export const actions = {
  getTasks({commit}) {

  },
}

export const getters = {
  uniqueStatuses(state) {
    const array = state.tasks.map(task => task.status)
    return [...new Set(array)]
  },
}
