<template>
  <p>BAD: {{ obj.child.<weak_warning descr="Unresolved variable n">n</weak_warning> }}</p>
  <p>OK: {{ obj.child.value.n }}</p>
  <p>OK: {{rootRef.toExponential()}}</p>
  <p>OK: {{object.nestedRef.value.toExponential()}}</p>
  <p>BAD: {{object.nestedRef.<weak_warning descr="Unresolved function or method toExponential()">toExponential</weak_warning>()}}</p>
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
