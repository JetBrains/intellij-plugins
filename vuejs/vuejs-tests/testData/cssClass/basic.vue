<template>
  <div :class="{'foo1 b': true, foo1: true}" class="some"></div>
  <div :class="['foo1 b']" class="some"></div>
  <div :class="'foo1 b'" class="some"></div>

  <div :class="{'foo bar': true, bar: true}" class="some"></div>
  <div :class="['foo bar']" class="some"></div>
  <div :class="'foo bar'" class="some"></div>
</template>

<script>
export default {
  name: "cssTest"
}
</script>

<style>
.foo {

}

div.bar {

}

.boo {

}
</style>