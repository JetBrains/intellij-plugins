<template>
  <script-setup clearable :value="value" :on-change="onChange" /> <!-- should be error on onChange -->
  <script-setup clearable :value="12" :on-change="12" />
  <script-setup :value="value" :on-change="onChange" />
  <script-setup :value="12" :on-change="12" />
</template>

<script setup lang='ts'>
import {ref} from "vue";
import ScriptSetup from "./scriptSetupGeneric.vue";

let value: string = ""
function onChange(<warning descr="Unused parameter v">v</warning>: string) {
}
</script>