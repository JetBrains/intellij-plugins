<script setup lang="ts">
function createUnion<T>(): number | undefined {
  return undefined;
}

let union = createUnion();

function acceptNumber(x: number): number {
  return x;
}
</script>

<template>
  <section>
    <span v-text="acceptNumber(<error descr="Argument type number | undefined is not assignable to parameter type number  Type undefined is not assignable to type number">union</error>)"></span>
    <span>{{ acceptNumber(<error descr="Argument type number | undefined is not assignable to parameter type number  Type undefined is not assignable to type number">union</error>) }}</span>
    <div v-if="union != null">
      <span v-text="acceptNumber(union)"></span>
      <span>{{ acceptNumber(union) }}</span>
    </div>
    <div v-else>
      <span v-text="acceptNumber(<error descr="Argument type undefined is not assignable to parameter type number">union</error>)"></span>
      <span>{{ acceptNumber(<error descr="Argument type undefined is not assignable to parameter type number">union</error>) }}</span>
    </div>
    <span v-text="acceptNumber(<error descr="Argument type number | undefined is not assignable to parameter type number  Type undefined is not assignable to type number">union</error>)"></span>
    <span>{{ acceptNumber(<error descr="Argument type number | undefined is not assignable to parameter type number  Type undefined is not assignable to type number">union</error>) }}</span>
  </section>
</template>

