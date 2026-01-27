<template>
  <child-comp :></child-comp>
  <a v-bind:></child-comp>
</template>
<script>
export default {
  name: 'childComp',
  props: {
    twoWords: {
    }
  },
  methods: {
    useMe(){}
  }
}
</script>