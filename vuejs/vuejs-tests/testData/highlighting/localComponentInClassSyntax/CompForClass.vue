<template>
  <div>{{ propFromA2 }}</div>
</template>

<script>
export default {
  name: "CompForClass",
  props: {
    propFromA2: {
      required: true
    }
  }
}
</script>