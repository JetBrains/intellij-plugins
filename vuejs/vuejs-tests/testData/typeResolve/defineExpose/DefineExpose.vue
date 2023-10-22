<template>
  <HelloWorld ref="triggerRef" msg="Hello!"/>
</template>

<script setup lang="ts">
import {ref} from "vue";
import HelloWorld from "./HelloWorld.vue"

type TypeofHello = typeof HelloWorld;
type InstanceHello = InstanceType<TypeofHello>
type HelloProps = InstanceType<TypeofHello>['$props']
type HelloPropsPropRef = HelloProps['msg']
type ShouldBeUnRefed = InstanceType<typeof HelloWorld>['twoRefNum']

const triggerRef = ref<InstanceType<typeof HelloWorld>>()
const result1 = triggerRef.value?.twoRefNum;
const result2 = triggerRef.value?.someFunc();
</script>
