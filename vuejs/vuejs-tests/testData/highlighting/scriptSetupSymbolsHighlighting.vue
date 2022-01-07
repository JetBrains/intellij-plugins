<template>
  {{<inject descr="null"> <info descr="instance field">newName</info> </inject>}}
  {{<inject descr="null"> <info descr="global variable">window</info>.<info descr="instance field">name</info> </inject>}}
  {{<inject descr="null"> <info descr="local function">foo12</info>() </inject>}}
  {{<inject descr="null"> <info descr="instance field">foo</info> </inject>}}
</template>

<script setup>

import {ref} from "vue";

export const <info descr="exported variable">foo</info> = 12

let <info descr="local variable">newName</info> = ref(12)

function <info descr="local function">foo12</info>() {
  let <info descr="local variable">bar</info> = {<info descr="instance field">a</info>: 12}
  return <info descr="local variable">bar</info>.<info descr="instance field">a</info>.<info descr="instance method">toExponential</info>()
}

</script>
