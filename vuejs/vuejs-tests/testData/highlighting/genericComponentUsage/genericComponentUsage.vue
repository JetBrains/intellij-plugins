<template>
  <script-setup clearable :value="value" :on-change="onChange" /> <!-- should be error on onChange -->
  <script-setup clearable :value="12" :on-change="<error descr="Type  12  is not assignable to type OnChange<number, true> | undefined  Type  12  is not assignable to type (value: (number | null)) => void">12</error>" />
  <script-setup :value="value" :on-change="onChange" />
  <script-setup :value="12" :on-change="<error descr="Type  12  is not assignable to type OnChange<number, unknown> | undefined  Type  12  is not assignable to type  (value: number) => void ">12</error>" />
</template>

<script setup lang='ts'>
import {ref} from "vue";
import ScriptSetup from "./scriptSetupGeneric.vue";

let value: string = ""
function onChange(<warning descr="Unused parameter v">v</warning>: string) {
}
</script>