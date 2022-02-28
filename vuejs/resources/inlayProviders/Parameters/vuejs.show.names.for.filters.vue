<template>
  <div v-bind:title="'hello' | capitalize(true)"></div>
</template>

<script>
export default {
  filters: {
    capitalize: function (value, withBang) {
      if (!value) return ''
      value = value.toString()
      return value.charAt(0).toUpperCase() + value.slice(1) + (withBang ? '!' : '')
    }
  }
}
</script>
