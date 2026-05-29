<script setup lang="ts">
const count = 1
</script>

<template>
  <div ref="first"></div>>
  <div ref="second"></div>>
  {{ count }}
  {{ $refs.first.tabIndex }}
  {{ $refs.second.tabIndex }}
</template>
