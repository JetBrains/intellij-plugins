<template>
  {{ this.<caret> }}
</template>
<script>
  import _debounce from 'lodash/debounce';

  export default {
    computed: {
      exampleComputed: {
        get: function () {
        },
        set: function () {
        }
      },
      anotherComputed() {
      }
    },
    methods: {
      exampleMethod: _debounce(function() {/*...*/}, 500),
      anotherMethod: function () {}
    }
  }
</script>
