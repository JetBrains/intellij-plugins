<script>
export default {
  name: "TestComponent",
  props: ["optional", "required"]
}
</script>

<template>
  {{optional}}
  {{required}}
</template>
