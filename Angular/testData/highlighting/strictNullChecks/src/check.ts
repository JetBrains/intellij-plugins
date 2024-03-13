import {withNull, noNull} from "../test";

let foo: string | null = null

withNull.cmd = foo
noNull.cmd = <error descr="Assigned expression type null is not assignable to type any[] | string  Type null is not assignable to type string    Type null is not assignable to type any[]">foo</error>

