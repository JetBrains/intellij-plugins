<script lang="ts" setup>
interface Props {
    label?: string,
}

const props = withDefaults(defineProps<Props>(), {
    label: () => 'Hello world!',
})
</script>

<template>
    {{ props.label }} <input type="text">
</template>
