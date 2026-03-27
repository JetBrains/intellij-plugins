<template>
  <p>BAD: {{ obj.child.<error descr="Unresolved variable n">n</error> }}</p>
  <p>OK: {{ obj.child.value.n }}</p>
  <p>OK: {{rootRef.toExponential()}}</p>
  <p>OK: {{object.nestedRef.value.toExponential()}}</p>
  <p>BAD: {{object.nestedRef.<error descr="Unresolved function or method toExponential()">toExponential</error>()}}</p>
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
