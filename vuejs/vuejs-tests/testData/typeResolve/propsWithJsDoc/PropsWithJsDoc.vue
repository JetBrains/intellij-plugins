<template>
  <p>
    {{ prop1.strVar }}
    {{ prop2.strVar2 }}
    {{ prop1Obj.strVar }}
    {{ prop2Obj.strVar2 }}
    {{ prop1Val }}
    {{ prop2Val }}
    {{ barProp.userName }}
    {{ barProp1.password }}
  </p>
</template>

<script>

/**
 * @typedef {Object} FooType
 * @property {string} userName - name.
 * @property {string} password - password.
 */

export default {
  props: {
    /** @type {import('vue').PropType<{strVar: string | string[]}>} */
    prop1: Object,
    /** @type {import('vue').PropType<Prop2>} */
    prop2: Object,
    /** @type {import('vue').Prop<FooType>} */
    barProp: {
      type: Object,
      required: true,
    },
    barProp1: {
      /** @type {import('vue').PropType<FooType>} */
      type: Object,
      required: true,
    },
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