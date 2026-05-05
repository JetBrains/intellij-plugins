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

<template>
  <!-- TODO all lines must error here -->
  <div @click="userInput = msg" />
  <div @click="msg = userInput" />
  <div @click="this.userInput = this.msg"/>
  <div @click="this.userInput = this.msg" />
  <div @click="this.msg = this.userInput" />
  <div @click="userInput = this.msg" />
  <div @click="msg = this.userInput" />
  <div @click="this.msg = userInput" />
  <div @click="this.userInput = msg" />
</template>
