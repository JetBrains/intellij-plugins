<script lang="ts" setup>
const emit = defineEmits<{
    (e: 'add', value: number): void,
    (e: 'change'): void,
    (e: 'delete', uuid: string): void,
}>();
</script>

<template>
    <div>
        <button @click="$emit<error descr="Argument types do not match parameters">('add', '123')</error>">Add</button>

        <button @click="$emit('delete', '123')">Add</button>
    </div>
</template>