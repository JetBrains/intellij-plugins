<script setup lang='ts'>
import FooBar from "./foo.vue";
import {Components} from "./components";
Components.red
defineProps({
    title: String,
    foos: String
})

</script>


<template>
    <foo-bar :value="12"/>
    <Bar :title="'copy-paste!'" :foos/>
</template>
