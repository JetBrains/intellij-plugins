<template>
</template>

<script>
export default {
  props: {
    /** @type {import('vue').PropType<{strVar: string | string[]}>} */
    prop1: Object,
  },
  data() {
    return {
      prop1Obj: this.prop1.str<caret>Var,
    }
  },
}
</script>