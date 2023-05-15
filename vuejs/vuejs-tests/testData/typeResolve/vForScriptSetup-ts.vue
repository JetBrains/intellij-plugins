<script setup lang="ts">
let union: string | number | number[];

let anyVar: any;

interface IPerson {
  name: string;
  age: number;
  verified: boolean;
}

type Alias = AliasOmitted
type AliasOmitted = Omit<IPerson, 'age'>

let aliasedVar: Alias;
</script>

<template>
  <div v-for="unionItem1 in union">{{ unionItem1 }}</div>
  <div v-for="cast1 in anyVar as IPerson">{{ cast1 }}</div>
  <div v-for="aliased1 in aliasedVar">{{ aliased1 }}</div>

  <div v-for="(unionItem2, unionItem2Ind) in union">{{ unionItem2, unionItem2Ind }}</div>
  <div v-for="(cast2, cast2Ind) in anyVar as IPerson">{{ cast2, cast2Ind }}</div>
  <div v-for="(aliased2, aliased2Ind) in aliasedVar">{{ aliased2, aliased2Ind }}</div>
</template>