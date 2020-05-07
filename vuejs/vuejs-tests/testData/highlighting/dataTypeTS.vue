<template>
    <div @click="this.userInput = <weak_warning descr="Assigned expression type Number is not assignable to type string">this.msg</weak_warning>"></div>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
    name: 'HelloWorld',
    props: {
        msg: Number,
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
