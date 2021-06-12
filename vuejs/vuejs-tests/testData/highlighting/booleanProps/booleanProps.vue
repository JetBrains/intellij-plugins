<template>
  <div>
    <A foo <warning descr="Wrong attribute value">bar</warning> unknown></A>
  </div>
</template>

<script>
import A from 'a-component'
export default {
  components: {
    A
  }
}
</script>