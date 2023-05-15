<script setup lang='ts'>
import FooBar from "./Foo.vue";
import {Colors} from "./colors"
import {Checker} from "./checker"

defineProps({
    title: String,
    foo: String
})
</script>

<template>
    <selection><foo-bar :value="Colors.blue"/>
        {{ Checker.state() }}
    <source :title="'copy-paste!'" :foo/></selection>
</template>