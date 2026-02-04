<script setup lang="ts">
import MyLocalButton from './MyLocalButton.vue'
</script>

<template>
  <my-local-button new-name="foo"></my-local-button>
  <my-local-button newName="foo"></my-local-button>
  <my-local-button OptionsApiWithType="foo"></my-local-button>
  <my-local-button :new-name="'foo'"></my-local-button>
  <my-local-button :newName="'foo'"></my-local-button>
  <my-local-button :OptionsApiWithType="'foo'"></my-local-button>
  <my-local-button v-bind:new-name="'foo'"></my-local-button>
  <my-local-button v-bind:newName="'foo'"></my-local-button>
  <my-local-button v-bind:OptionsApiWithType="'foo'"></my-local-button>
</template>
