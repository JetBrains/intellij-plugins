<script setup lang="ts">
import {expression, NamedComponent} from './helper.ts';
import {MyType} from "./Helper.vue";

const x = 1, y = 10;
const constant = "hello";
class C {
  used = 1
}

export interface Demo {}

const user = {
  id: 42,
  isVerified: true
};

const {id, isVerified} = user;

let foo: MyType
</script>

<template>
  <h1>{{x}} {{constant}} {{new C}}</h1>
  <div v-if="isVerified">Id is: {{ id }}</div>
  <NamedComponent>{{expression}}</NamedComponent>
</template>
