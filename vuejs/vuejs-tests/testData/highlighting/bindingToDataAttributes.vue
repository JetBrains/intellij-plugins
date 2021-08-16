<template>
  <div :data-foo="12"/>
  <div data-foo="12"/>
  <div v-bind:data-foo="12"/>
  <div :<warning descr="Unrecognized attribute or property name">datas-foo</warning>="12"/>
  <span :data-foo="12"/>
</template>