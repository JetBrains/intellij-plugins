<script lang="ts" setup>
import { computed } from 'vue'

type PropsType = 'primary' | 'secondary' | 'tertiary'
type PropsSize = 'large' | 'medium' | 'small'

interface Props {
    text?: string,
    type?: PropsType
    size?: PropsSize
}

const props = withDefaults(defineProps<Props>(), {
    type: () => 'primary',
    size: () => 'medium',
})

const padding = computed(() => {
    switch(props.size) {
        case 'large': return '20px'
        case 'medium': return '10px'
        case 'small': return '5px'
    }
})

const isOutline = computed(() => props.type === 'secondary')
const isFlat = computed(() => props.type === 'tertiary')
</script>

<template>
    Outline: {{ isOutline ? 'Y' : 'N' }}<br>
    Flat: {{ isFlat ? 'Y' : 'N' }}<br>
    Padding: {{ padding }}<br>

    <button>{{ text }}</button>
</template>
