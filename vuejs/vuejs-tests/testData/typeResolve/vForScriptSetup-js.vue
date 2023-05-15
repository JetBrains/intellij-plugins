<script setup lang="js">
let emptyObj = {};

class IPerson {
  name = 'name'
  age = 25
  verified = true
}

const personInstance = new IPerson()
</script>

<template>
  <div v-for="empty1 in emptyObj">{{ empty1 }}</div>
  <div v-for="person1 in personInstance">{{ person1 }}</div>

  <div v-for="(empty2, empty2Ind) in emptyObj">{{ empty2, empty2Ind }}</div>
  <div v-for="(person2, person2Ind) in personInstance">{{ person2, person2Ind }}</div>
</template>