<script lang="ts" setup>
const emit = defineEmits<{
    (e: 'add', value: number, more: boolean): void,
    (e: 'change'): void,
    (e: 'delete', uuid: string): void,
}>();
</script>

<template>
    <EmitEvents @a<caret>dd="(value, more) => {}" />
</template>
