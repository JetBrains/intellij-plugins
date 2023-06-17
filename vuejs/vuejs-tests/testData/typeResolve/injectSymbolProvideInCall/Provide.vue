<script setup>
import {provide} from 'vue'
import {myInjectionKey} from "./injectionKey";
import Inject from "./InjectSymbolProvideInCall.vue";

provide(myInjectionKey, {
  name: 'bob'
})
</script>

<template>
  <Inject/>
</template>
