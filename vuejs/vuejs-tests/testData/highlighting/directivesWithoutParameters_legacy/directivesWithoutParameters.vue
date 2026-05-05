<template>
  <div v-once></div>
  <div v-once=<warning descr="v-once does not accept any value">"foo"</warning>></div>
  <div v-else></div>
  <div v-else=<warning descr="v-else does not accept any value">"foo"</warning>></div>
  <div v-pre></div>
  <div v-pre=<warning descr="v-pre does not accept any value">"foo"</warning>></div>
  <div v-cloak></div>
</template>