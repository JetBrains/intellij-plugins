<template>
  {{ it<caret>ems }}
</template>

<script>
export default {
  data() {
    return {
      /** @type {{name: string, pass: number}[]} */
      items: []
    }
  }
}
</script>