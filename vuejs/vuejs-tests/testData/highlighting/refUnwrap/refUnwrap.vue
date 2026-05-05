<template>
  <p>BAD: {{ obj.child.n }}</p>
  <p>OK: {{ obj.child.value.n }}</p>
  <p>OK: {{rootRef.toExponential()}}</p>
  <p>OK: {{object.nestedRef.value.toExponential()}}</p>
  <p>BAD: {{object.nestedRef.toExponential()}}</p>
</template>
<script lang="ts">
import {computed, ref} from "vue";

export default {
  name: 'NewComponent',
  setup() {
    const obj = computed(() => ({
      child: ref({ n: 1234 })
    }))

    return {
      obj,
      rootRef: ref(1),
      object: {
        nestedRef: ref(3)
      }
    }
  }

}
</script>
