<script setup>
import { vFocus } from "./vFocus.js";
import { vFocus as vSomeFocus } from "./vFocus.js";

const vRenamedFocus = vFocus;
</script>

<template>
    <div class="hello">
        <input type="text" v-focus />
        <input type="text" v-some-focus />
        <input type="text" v-renamed-focus />
    </div>
</template>
