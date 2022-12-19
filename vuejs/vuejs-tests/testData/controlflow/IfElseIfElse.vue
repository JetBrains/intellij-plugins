<script setup lang="ts">
function createUnion<T>(): number | string | number[] {
  const r = Math.random();
  return r > 0.66 ? 42 : (r > 0.33 ? "hello" : [1, 2, 3]);
}

let union = createUnion();
</script>

<template>
  <section>
    <span v-text="union.<error descr="Unresolved function or method toFixed()">toFixed</error>()"></span>
    <span v-text="union.<error descr="Unresolved function or method toUpperCase()">toUpperCase</error>()"></span>
    <span v-text="union.<error descr="Unresolved function or method join()">join</error>(`, `)"></span>
    <span>{{ union.<error descr="Unresolved function or method toFixed()">toFixed</error>() }}</span>
    <span>{{ union.<error descr="Unresolved function or method toUpperCase()">toUpperCase</error>() }}</span>
    <span>{{ union.<error descr="Unresolved function or method join()">join</error>(`, `) }}</span>
    <div v-if="typeof union === `number`">
      <span v-text="union.toFixed()"></span>
      <span v-text="union.<error descr="Unresolved function or method toUpperCase()">toUpperCase</error>()"></span>
      <span v-text="union.<error descr="Unresolved function or method join()">join</error>(`, `)"></span>
      <span>{{ union.toFixed() }}</span>
      <span>{{ union.<error descr="Unresolved function or method toUpperCase()">toUpperCase</error>() }}</span>
      <span>{{ union.<error descr="Unresolved function or method join()">join</error>(`, `) }}</span>
    </div>
    <div v-else-if="typeof union === `string`">
      <span v-text="union.<error descr="Unresolved function or method toFixed()">toFixed</error>()"></span>
      <span v-text="union.toUpperCase()"></span>
      <span v-text="union.<error descr="Unresolved function or method join()">join</error>(`, `)"></span>
      <span>{{ union.<error descr="Unresolved function or method toFixed()">toFixed</error>() }}</span>
      <span>{{ union.toUpperCase() }}</span>
      <span>{{ union.<error descr="Unresolved function or method join()">join</error>(`, `) }}</span>
    </div>
    <div v-else :title="union.join(`, `)">
      <span v-text="union.<error descr="Unresolved function or method toFixed()">toFixed</error>()"></span>
      <span v-text="union.<error descr="Unresolved function or method toUpperCase()">toUpperCase</error>()"></span>
      <span v-text="union.join(`, `)"></span>
      <span>{{ union.<error descr="Unresolved function or method toFixed()">toFixed</error>() }}</span>
      <span>{{ union.<error descr="Unresolved function or method toUpperCase()">toUpperCase</error>() }}</span>
      <span>{{ union.join(`, `) }}</span>
    </div>
    <span v-text="union"></span>
  </section>
</template>

