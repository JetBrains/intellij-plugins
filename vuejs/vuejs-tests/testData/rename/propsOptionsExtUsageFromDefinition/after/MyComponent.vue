<script>
export default {
  name: "cmp",
  props: {
    newName: {
      type: String,
      required: true
    }
  }
}
</script>

<template>
  {{ newName }}
</template>
