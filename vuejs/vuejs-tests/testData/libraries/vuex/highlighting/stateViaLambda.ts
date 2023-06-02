import Vuex from "vuex"

const state: any = () => ({
  value: "foo",
})

const getters = {
  getValueAsString2(state): number {
    return <error descr="Returned expression type  string  is not assignable to type  number ">state.value</error>
  },
  getUndefinedField2(state): string {
    return state.<error descr="Unresolved variable x">x</error>
  },
}

export const store = new Vuex.Store({
  state,
  getters,
})
