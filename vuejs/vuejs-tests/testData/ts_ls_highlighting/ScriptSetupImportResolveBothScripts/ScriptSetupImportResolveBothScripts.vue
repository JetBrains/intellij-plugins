<script lang='ts'>
</script>
<script setup lang="ts">
import {ref} from "vue";
import INotebook from "./INotebook";
import Foo from <error descr="TS2307: Cannot find module 'foo' or its corresponding type declarations.">"<error descr="Module is not listed in package.json dependencies">foo</error>"</error>

let <error descr="TS2739: Type '{}' is missing the following properties from type 'INotebook': color, price">foo</error>: INotebook = {}
<error descr="TS2322: Type 'string' is not assignable to type 'number'.">foo.price</error> = "12"

ref(0).<error descr="TS2339: Property 'foo' does not exist on type 'Ref<number>'.">foo</error>()

Foo.bar = 12
</script>