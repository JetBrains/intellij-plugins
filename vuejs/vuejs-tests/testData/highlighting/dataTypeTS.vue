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
  <div @click="userInput = <error descr="Assigned expression type number is not assignable to type string">msg</error>" />
  <div @click="<error descr="Attempt to assign to const or readonly variable">msg</error> = <error descr="Assigned expression type string is not assignable to type number">userInput</error>" />
  <div @click="this.userInput = this.msg"/>
  <div @click="this.userInput = this.msg" />
  <div @click="this.<error descr="Attempt to assign to const or readonly variable">msg</error> = this.userInput" />
  <div @click="userInput = <error descr="Assigned expression type number is not assignable to type string">this.msg</error>" />
  <div @click="<error descr="Attempt to assign to const or readonly variable">msg</error> = this.userInput" />
  <div @click="this.<error descr="Attempt to assign to const or readonly variable">msg</error> = <error descr="Assigned expression type string is not assignable to type number">userInput</error>" />
  <div @click="this.userInput = msg" />
</template>
