<template>
  <div>
    <A foo <warning descr="bar requires value">bar</warning> unknown></A>
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