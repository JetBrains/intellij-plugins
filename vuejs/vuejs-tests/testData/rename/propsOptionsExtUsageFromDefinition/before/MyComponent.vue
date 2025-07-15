<script>
export default {
  name: "cmp",
  props: {
    opt<caret>ionsApiWithType: {
      type: String,
      required: true
    }
  }
}
</script>

<template>
  {{ optionsApiWithType }}
</template>
