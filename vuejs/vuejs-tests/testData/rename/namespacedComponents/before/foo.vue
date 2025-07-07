<template>
  <slot name="foo"/>
  <b-alert
</template>
<script>
import {Components} from "./components";

export default {
  name: 'my-checkbox',
  props: {
    /**
     * @model
     */
    value: String
  }
}
</script>
