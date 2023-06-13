<script setup>
import {inject} from 'vue'

const injectedProp = inject('providedInCall')
</script>

<template>
  {{ injectedProp }}
  {{ <weak_warning descr="Unresolved variable or type providedInCall">providedInCall</weak_warning> }}
</template>