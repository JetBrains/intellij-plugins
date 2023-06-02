<template>
  <Book>
    <template v-slot:scopedSlot="slotScope">
      {{ slotScope.<error descr="Attempt to assign to const or readonly variable">foo</error> = <error descr="Assigned expression type  false  is not assignable to type  string | number   Type false is not assignable to type number    Type false is not assignable to type string">false</error> }}
      {{ slotScope.<error descr="Unresolved variable bar">bar</error> }}
    </template>
    <template v-slot:scopedSlot="{foo: bar}">
      <Catalogue :test='<error descr="Type  string | number  is not assignable to type  boolean | undefined   Type string is not assignable to type boolean | undefined">bar</error>'></Catalogue>
    </template>
  </Book>
</template>
<script setup lang="ts">
import Book from "./Book.vue";

defineProps({
  test: Boolean
})

</script>
