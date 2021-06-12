<template>
  <div>Hello</div>
</template>

<script>
import DComponent from 'd-component'
export default {
  name: 'b-component',
  props: {
    fromB: {
      required: true
    }
  },
  mixins: [ DComponent ]
}
</script>