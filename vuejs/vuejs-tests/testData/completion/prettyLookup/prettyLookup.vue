<template>
  {{ call<caret> }}
</template>
<script>
export default {
  methods: {
    callMe(aaa, bbb) {
      return 5;
    }
  }
}
</script>