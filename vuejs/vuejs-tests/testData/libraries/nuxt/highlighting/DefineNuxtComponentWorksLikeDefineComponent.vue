<script lang="ts">
declare const defineNuxtComponent; // the test does not have nuxt in dependencies, but it does not matter here
export default defineNuxtComponent({
    data() {
        return {
            count: 0
        };
    },
    props: {
        example: {
            type: String,
            required: true
        }
    },
    methods: {
        test() {}
    }
});
</script>

<template>
    <p @click="test">
        {{ example }}
        {{ count }}
        {{ <error descr="Unresolved variable or type data">data</error> }}
    </p>
</template>
