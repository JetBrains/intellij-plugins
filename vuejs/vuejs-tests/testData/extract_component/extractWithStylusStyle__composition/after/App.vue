<script setup>
import NewComponent from "./NewComponent.vue";

defineProps(['show'])
</script>

<template>
  <transition>
    <NewComponent :show="show"/>
  </transition>
</template>

<style lang="stylus">
$offset = 126
$duration = 1.4s

.notUsed
  transition opacity .15s ease

@keyframes rotator
  0%
    transform scale(0.5) rotate(0deg)
  100%
    transform scale(0.5) rotate(270deg)

@keyframes dash
  0%
    stroke-dashoffset $offset
  50%
    stroke-dashoffset ($offset/2)
    transform rotate(135deg)
  100%
    stroke-dashoffset $offset
    transform rotate(450deg)
</style>
