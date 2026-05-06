<template>
  <div v-once></div>
  <div v-once=<warning descr="v-once does not accept any value">"foo"</warning>></div>
  <error descr="Vue: v-else/v-else-if has no adjacent v-if or v-else-if."><div v-else></div></error>
  <error descr="Vue: v-else/v-else-if has no adjacent v-if or v-else-if."><div v-else=<warning descr="v-else does not accept any value">"foo"</warning>></div></error>
  <div v-pre></div>
  <div v-pre=<warning descr="v-pre does not accept any value">"foo"</warning>></div>
  <div v-cloak></div>
</template>