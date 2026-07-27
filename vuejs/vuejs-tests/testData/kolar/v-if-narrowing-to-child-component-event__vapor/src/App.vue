<script vapor lang="ts">
import MyCounter from './MyCounter.vue'

interface Foo {
  bar?: {
    baz: () => void
  }
}

const foo: Foo = {}
</script>

<template>
  <MyCounter v-if="foo.bar" @click="foo.bar.baz" />
</template>
