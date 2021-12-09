<template>
  <<warning descr="Unknown html tag b-comp">b-comp</warning>>
  </<warning descr="Unknown html tag b-comp">b-comp</warning>>
  <<warning descr="Element hidden-component doesn't have required attribute from-hidden">hidden-component</warning>/>
</template>

<script>
import E from 'e-component'
export default {
  mixins: [ E ]
}
</script>