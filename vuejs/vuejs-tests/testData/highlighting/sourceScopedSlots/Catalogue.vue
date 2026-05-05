<template>
  <Book>
    <template v-slot:scopedSlot="slotScope">
      {{ slotScope.foo = false }}
      {{ slotScope.bar }}
    </template>
    <template v-slot:scopedSlot="{foo: bar}">
      <Catalogue :test='bar'></Catalogue>
    </template>
  </Book>
</template>
<script setup lang="ts">
import Book from "./Book.vue";

defineProps({
  test: Boolean
})

</script>
