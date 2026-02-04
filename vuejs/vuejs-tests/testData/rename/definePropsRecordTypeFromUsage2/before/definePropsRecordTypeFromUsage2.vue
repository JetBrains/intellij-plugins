<script setup lang="ts">
import MyLocalButton from './MyLocalButton.vue'
</script>

<template>
  <my-local-button options-api-with-type="foo"></my-local-button>
  <my-local-button optionsApiWithType="foo"></my-local-button>
  <my-local-button OptionsApiWithType="foo"></my-local-button>
  <my-local-button :options-api-with-type="'foo'"></my-local-button>
  <my-local-button :optionsApiWithType="'foo'"></my-local-button>
  <my-local-button :OptionsApiWithType="'foo'"></my-local-button>
  <my-local-button v-bind:options-api-with-type="'foo'"></my-local-button>
  <my-local-button v-bind:optionsApiWithType="'foo'"></my-local-button>
  <my-local-button v-bind:OptionsApiWithType="'foo'"></my-local-button>
</template>
