<template>
<p>test</p>
</template>
<script lang="ts">
import type {Foo, Foo2} from './VueSimpleOptimize_2';

declare let z: Foo;
console.log(z);
</script>