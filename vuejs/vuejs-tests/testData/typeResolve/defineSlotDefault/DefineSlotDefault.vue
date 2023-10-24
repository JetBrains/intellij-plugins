<script setup lang='ts'>
import TextField from './TextField.vue'
</script>

<template>
  <TextField v-slot='{ field }'>
    {{ field.class }}
  </TextField>

  <TextField>
    <template v-slot='{ field }'>
      {{ field.style }}
    </template>
  </TextField>

  <TextField>
    <template v-slot:default='{ field }'>
      {{ field.value }}
    </template>
  </TextField>
</template>
