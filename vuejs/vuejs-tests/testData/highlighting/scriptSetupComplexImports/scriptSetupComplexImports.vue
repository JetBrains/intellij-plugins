<template>
  <TreeModal></TreeModal>
  <BasicModal></BasicModal>
  <ComplexModal></ComplexModal>
  <DefaultModal></DefaultModal>
  <BasicModalDirect/>
  <tree-modal-direct></tree-modal-direct>
  <ComplexModalDirect></ComplexModalDirect>
  <default-modal-direct></default-modal-direct>
  {{ printString(<error descr="Argument type 12 is not assignable to parameter type string">Bar</error>) + printString(<error descr="Argument type 12 is not assignable to parameter type string">Foo</error>) }}

  <TabGroup>
    <TabList>
    </TabList>
  </TabGroup>
  <<warning descr="Element ScriptSetupComplexImports doesn't have required attribute foo-bar">ScriptSetupComplexImports</warning>/>
</template>
<script setup lang="ts">
import {TreeModal, BasicModal, ComplexModal, DefaultModal, Bar} from './hooks/Modal'

import BasicModalDirect from "./hooks/Modal/BasicModal.vue"
import TreeModalDirect from "./hooks/Modal/BasicModal.vue"
import ComplexModalDirect from "./hooks/Modal/BasicModal.vue"
import DefaultModalDirect from "./hooks/Modal/BasicModal.vue"

<warning descr="Unused import FooBar from \"./hooks/Modal/BasicModal.vue\"">import FooBar from "./hooks/Modal/BasicModal.vue"</warning>

import { <warning descr="Unused import specifier Tab">Tab</warning>, TabGroup, TabList } from "@headlessui/vue";

const Foo = 12

function printString(<warning descr="Unused parameter str">str</warning>: string) {

}


const props = defineProps<{
  fooBar: string;
  barFoo?: number;
}>();

</script>