<template>
  <div>{{ propFromA }}</div>
</template>

<script>
export default {
  name: "CompA",
  props: {
    propFromA: {
      required: true
    }
  }
}
</script>