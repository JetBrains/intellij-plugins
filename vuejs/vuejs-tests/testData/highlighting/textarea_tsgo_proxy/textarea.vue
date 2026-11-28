<template>
        <textarea
          :<warning descr="Unrecognized attribute or property name">foo</warning>="12"
          :value="value"
          @change="e => $emit('input', e.target.<weak_warning descr="Unresolved variable value">value</weak_warning>)"
          @keyup="e => $emit('input', e.target.<weak_warning descr="Unresolved variable value">value</weak_warning>)"
        />
</template>
<script>
export default {
  props: {
    value: String
  }
}
</script>
