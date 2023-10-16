<script lang="ts" setup>
import {ref} from "vue";

const count = ref(0)

const emit = defineEmits<{
    (e: 'add', value: number, more: boolean): void,
    (e: 'change'): void,
    (e: 'delete', uuid: string): void,
    (e: 'insert', idx: number): void,
    (e: 'save', idx: number): void,
    (e: 'reload', idx: number): void,
    (e: 'something', idx: string): void,
}>();

function handle(value: number, more: boolean): void {
    console.log(value, more);
}

const handlers = {
    nested: {
        handle(uuid: string) {
            console.log(uuid);
        }
    }
}

function handleNotAssignable(value: number, more: string): void {
    console.log(value, more);
}
</script>

<template>
    <CustomEvents
          v-on:add="handle"
          @change="<error descr="Type (value: number, more: string) => void is not assignable to type () => void">handleNotAssignable</error>"
          v-on:delete="count--"
          v-on:insert="<error descr="Type (idx: any, extraArg: any) => void is not assignable to type (idx: number) => void">(idx, extraArg) => { console.log(idx, extraArg); }</error>"
          v-on:save="idx => { console.log(idx); }"
          v-on:reload="<error descr="Type (uuid: string) => void is not assignable to type (idx: number) => void  Type number is not assignable to type string    Type string is not assignable to type number">handlers.nested['handle']</error>"
          v-on:something="handlers.nested['handle']"/>
</template>
