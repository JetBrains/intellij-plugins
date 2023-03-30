<script setup>
import { vFocus, <warning>unused</warning> } from "./vFocus.js";
import { vFocus as vSomeFocus } from "./vFocus.js";

const vRenamedFocus = vSomeFocus;
</script>

<template>
    <div class="hello">
        <input type="text" v-focus />
        <input type="text" v-some-focus />
        <input type="text" v-renamed-focus />
        <input type="text" v-<warning descr="Unrecognized Vue directive">another-focus</warning> />
    </div>
</template>
