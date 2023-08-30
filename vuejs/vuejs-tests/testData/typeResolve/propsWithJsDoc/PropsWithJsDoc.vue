<template>
  <p>
    {{ prop1.strVar }}
    {{ prop2.strVar2 }}
    {{ prop1Obj.strVar }}
    {{ prop2Obj.strVar2 }}
    {{ prop1Val }}
    {{ prop2Val }}
  </p>
</template>

<script>
export default {
  props: {
    /** @type {import('vue').PropType<{strVar: string | string[]}>} */
    prop1: Object,
    /** @type {import('vue').PropType<Prop2>} */
    prop2: Object,
  },
  data() {
    return {
      prop1Obj: this.prop1,
      prop2Obj: this.prop2,

      prop1Val: this.prop1.strVar,
      prop2Val: this.prop2.strVar2,
    }
  },
}
</script>