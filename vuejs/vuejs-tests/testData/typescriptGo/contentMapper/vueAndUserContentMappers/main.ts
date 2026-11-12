import { appTitle } from "./App.vue"
import { debug } from "./config.foo"

const <error descr="TS2322: Type 'string' is not assignable to type 'number'.">title</error>: number = appTitle
const <error descr="TS2322: Type 'boolean' is not assignable to type 'string'.">enabled</error>: string = debug

export { title, enabled }
