<template>
  {{<inject descr="null"> <info descr="instance field">newName</info> </inject>}}
  {{<inject descr="null"> <info descr="identifiers//global variable">window</info>.<info descr="instance field">name</info> </inject>}}
  {{<inject descr="null"> <info descr="identifiers//local function">foo12</info>() </inject>}}
  {{<inject descr="null"> <info descr="instance field">foo</info> </inject>}}
</template>

<script setup>

import {ref} <info descr="null">from</info> "vue";

export const <info descr="identifiers//exported variable">foo</info> = 12

let <info descr="identifiers//local variable">newName</info> = ref(12)

function <info descr="identifiers//local function">foo12</info>() {
  let <info descr="identifiers//local variable">bar</info> = {<info descr="instance field">a</info>: 12}
  return <info descr="identifiers//local variable">bar</info>.<info descr="instance field">a</info>.<info descr="instance method">toExponential</info>()
}

</script>
