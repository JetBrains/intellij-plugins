<template>
  <Book>
    <template v-slot:scopedSlot="slotScope">
      {{ slotScope.<error descr="Attempt to assign to const or readonly variable">foo</error> = <weak_warning descr="Assigned expression type boolean is not assignable to type string | number  Type boolean is not assignable to type number    Type boolean is not assignable to type string">false</weak_warning> }}
      {{ slotScope.<weak_warning descr="Unresolved variable bar">bar</weak_warning> }}
    </template>
    <template v-slot:scopedSlot="{foo: bar}">
      <Catalogue :test='<weak_warning descr="Type string | number is not assignable to type boolean | undefined  Type string is not assignable to type boolean | undefined">bar</weak_warning>'></Catalogue>
    </template>
  </Book>
</template>
<script setup lang="ts">
import Book from "./Book.vue";

defineProps({
  test: Boolean
})

</script>
