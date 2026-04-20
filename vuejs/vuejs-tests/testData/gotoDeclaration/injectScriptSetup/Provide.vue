<script setup>
import {provide} from 'vue'
import InjectScriptSetup from './InjectScriptSetup.vue'

provide('providedInCall', 123)
</script>

<template>
  <InjectScriptSetup/>
</template>