import {withNull, noNull} from "../test";

let foo: string | null = null

withNull.cmd = foo
<error descr="TS2322: Type 'null' is not assignable to type 'string | any[]'.">noNull.cmd</error> = foo

