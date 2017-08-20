<template>
  <ul id="example-1">
    <li v-for="item in items"/>
    <li v-for="(item, key) in items"/>
  </ul>
</template>