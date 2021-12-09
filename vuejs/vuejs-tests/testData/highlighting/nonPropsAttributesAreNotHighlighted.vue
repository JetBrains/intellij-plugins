<template>
  <non-props-component aaa="e" bbb/>
  <div <warning descr="Attribute aaa is not allowed here">aaa</warning>="1"></div>
</template>

<script>
export default {
  name: 'non-props-component'
}
</script>