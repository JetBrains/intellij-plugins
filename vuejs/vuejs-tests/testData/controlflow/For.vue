<script setup lang="ts">
function createUnion(): number | string | number[] {
  const r = Math.random();
  return r > 0.66 ? 42 : (r > 0.33 ? "hello" : [1, 2, 3]) ;
}

let union = createUnion();

function renderNumber(num: number): string {
  return num.toExponential(3);
}
</script>

<template>
  <section>
    {{renderNumber(<weak_warning descr="Argument type number | string | number[] is not assignable to parameter type number  Type string is not assignable to type number">union</weak_warning>)}}
    <div v-for="item in union">{{ renderNumber(item) }}</div> <!-- TODO Argument type string | number is not assignable to parameter type number -->
    <div v-if="typeof union === `number`">
      <!-- Vue treats numbers in v-for as range iteration -->
      <div v-for="item in union">{{ renderNumber(item) }}</div>
    </div>
    <div v-else-if="typeof union === `string`">
      <div v-for="item in union">{{ renderNumber(item) }}</div> <!-- TODO Argument type string is not assignable to parameter type number -->
    </div>
    <div v-else>
      <div v-for="item in union">{{ renderNumber(item) }}</div>
    </div>
    <!-- Order of attributes is irrelevant, in Vue 3 v-if comes before v-for in control flow -->
    <aside v-for="item in union" v-if="Array.isArray(union)">{{ renderNumber(item) }}</aside>
  </section>
</template>
