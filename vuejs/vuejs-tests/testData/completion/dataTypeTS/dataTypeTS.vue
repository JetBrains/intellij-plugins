<template>
    <div @click="this.userInput = this.msg"></div>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
    name: 'HelloWorld',
    props: {
        msg: {
            type: Number,
            required: true
        },
    },
    data() {
        return {
            userInput: '' as string,
            tasks: [ ] as string[],
        }
    },
    methods: {
        addItemToList() {
            const taskToAdd = this.userInput
            this.tasks.push(taskToAdd)
            this.userInput = ''
        },
    },
})
</script>
