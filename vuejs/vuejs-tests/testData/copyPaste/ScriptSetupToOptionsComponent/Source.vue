<script setup lang='ts'>
import FooBar from "./Foo.vue";
import {Colors} from "./colors"

defineProps({
    title: String,
    foo: String
})
</script>

<template>
    <selection><foo-bar :value="Colors.blue"/>
    <script-setup-component :title="'copy-paste!'" :foo/></selection>
</template>