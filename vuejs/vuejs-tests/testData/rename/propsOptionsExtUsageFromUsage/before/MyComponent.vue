<script>
export default {
  name: "cmp",
  props: {
    optionsApiWithType: {
      type: String,
      required: true
    }
  }
}
</script>

<template>
  {{ optionsApiWithType }}
</template>
