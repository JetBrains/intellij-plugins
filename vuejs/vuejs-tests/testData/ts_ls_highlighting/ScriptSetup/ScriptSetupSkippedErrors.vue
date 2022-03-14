<script setup lang="ts">
import {expression, NamedComponent} from './helper';
const x = 1, y = 10;
const constant = "hello";
class C {
  used = 1
}

const user = {
  id: 42,
  isVerified: true
};

const {id, isVerified} = user;
</script>

<template>
  <h1>{{x}} {{constant}} {{new C}}</h1>
  <div v-if="isVerified">Id is: {{ id }}</div>
  <NamedComponent>{{expression}}</NamedComponent>
</template>
