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
          @change="handleNotAssignable"
          v-on:delete="count--"
          v-on:insert="(idx, extraArg) => { console.log(idx, extraArg); }"
          v-on:save="idx => { console.log(idx); }"
          v-on:reload="handlers.nested['handle']"
          v-on:something="handlers.nested['handle']"/>
</template>
