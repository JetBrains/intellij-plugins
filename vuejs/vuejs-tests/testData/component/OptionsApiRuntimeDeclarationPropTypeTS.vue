<script lang="ts">
import { PropType } from 'vue'

interface Book {
  title: string
  author: string
  year: number
}

export default {
  name: "TestComponent",
  props: {
    book: { type: Object as PropType<Book>, required: true }
  }
}
</script>

<template>
  <TestComponent :book="<weak_warning descr="Type {year: number, title: string} is not assignable to type Book">{title: 'WebStorm', year: 2022}</weak_warning>" />
  <TestComponent :book="{title: 'WebStorm', year: 2022, author: 'JB'}" />
</template>
