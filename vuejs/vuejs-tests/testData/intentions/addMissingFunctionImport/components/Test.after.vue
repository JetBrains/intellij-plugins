<template>
  <h1>{{ getClient() }}</h1>
</template>

<script>
import {getClient} from "../client";

export default {
  name: 'HelloWorld',
  methods: {getClient},
  props: {
    msg: String
  }
}
</script>