<template>
        <textarea
          :<warning descr="Unrecognized attribute or property name">foo</warning>="12"
          :value="value"
          @change="e => $emit('input', e.target.value)"
          @keyup="e => $emit('input', e.target.value)"
        />
</template>
<script>
export default {
  props: {
    value: String
  }
}
</script>
