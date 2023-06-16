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
  <TestComponent :book="<error descr="Type {    year: 2022,    title: \"WebStorm\"} is not assignable to type  Book ">{title: 'WebStorm', year: 2022}</error>" />
  <TestComponent :book="{title: 'WebStorm', year: 2022, author: 'JB'}" />
</template>
