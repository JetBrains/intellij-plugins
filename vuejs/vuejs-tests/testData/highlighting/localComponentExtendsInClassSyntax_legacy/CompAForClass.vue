<template>
  <div>{{ propFromA1 }}</div>
</template>

<script>
export default {
  name: "CompAForClass",
  props: {
    propFromA1: {
      required: true
    }
  }
}
</script>