<script setup>
import {provide, ref} from 'vue'
import Inject from './InjectInProperties.vue'

provide('scriptSetup', true)
provide('scriptSetupRef', ref('abc'))
</script>

<template>
  <Inject/>
</template>