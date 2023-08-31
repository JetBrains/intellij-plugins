<template>
  <p>
    {{ barProp.userName }}
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
    /** @type {import('vue').PropOptions<FooType>} */
    barProp: {
      type: Object,
      required: true,
    },
  },
}
</script>