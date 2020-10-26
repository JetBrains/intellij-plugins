import Vuex from "vuex"

interface IStateTest {
  value: number
}

const state: any = {
  value: "foo",
}

const getters = {
  getValueAsString(state: IStateTest): string {
    return <error descr="Returned expression type number is not assignable to type string">state.value</error>
  },
  getUndefinedField(state: IStateTest): string {
    return state.<error descr="Unresolved variable x">x</error>
  },
  getValueAsString2(state): number {
    return <error descr="Returned expression type string is not assignable to type number">state.value</error>
  },
  getUndefinedField2(state): string {
    return state.<error descr="Unresolved variable x">x</error>
  },
}

export const store = new Vuex.Store({
  state,
  getters,
})
