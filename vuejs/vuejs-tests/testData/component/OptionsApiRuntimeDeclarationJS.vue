<script>
export default {
  name: "TestComponent",
  props: {
    optional: String,
    optionalUnion: [String, Number],
    required: { type: String, required: true },
    requiredUnion: { type: [String, Number], required: true },
  }
}
</script>

<template>
  {{optional}}
  {{optionalUnion}}
  {{required}}
  {{requiredUnion}}
</template>
