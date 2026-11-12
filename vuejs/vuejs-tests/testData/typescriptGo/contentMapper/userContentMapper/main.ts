import { host, port, debug, <error descr="TS2305: Module '\"./config.foo\"' has no exported member 'missing'.">missing</error> } from "./config.foo"

const url: string = host + ":" + port
const <error descr="TS2322: Type 'number' is not assignable to type 'string'.">portText</error>: string = port
const enabled: boolean = debug

export { url, portText, enabled, missing }
