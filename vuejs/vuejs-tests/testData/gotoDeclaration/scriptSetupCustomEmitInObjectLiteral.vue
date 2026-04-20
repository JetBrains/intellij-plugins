<script setup lang="ts">

function add(ctx: number) {
    return true;
}

const emit = defineEmits({
    add,
    change(ctx) {
        return false;
    },
    remove: (ctx) => {
        return false;
    }
});
</script>

<template>
    <ScriptSetupCustomEmitInObjectLiteral @add="ctx => {}" @change="args => {}" v-on:remove="args => {}"/>
</template>
