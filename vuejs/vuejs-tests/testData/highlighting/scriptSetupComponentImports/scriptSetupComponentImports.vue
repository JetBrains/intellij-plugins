<template>
  <TreeModal></TreeModal>
  <BasicModal></BasicModal>
  <ComplexModal></ComplexModal>
  <DefaultModal></DefaultModal>
  <BasicModalDirect/>
  <tree-modal-direct></tree-modal-direct>
  <ComplexModalDirect></ComplexModalDirect>
  <default-modal-direct></default-modal-direct>
</template>
<script setup lang="ts">
import {TreeModal, BasicModal, ComplexModal, DefaultModal} from './hooks/Modal'

import BasicModalDirect from "./hooks/Modal/BasicModal.vue"
import TreeModalDirect from "./hooks/Modal/BasicModal.vue"
import ComplexModalDirect from "./hooks/Modal/BasicModal.vue"
import DefaultModalDirect from "./hooks/Modal/BasicModal.vue"

<warning descr="Unused import Foo from \"./hooks/Modal/BasicModal.vue\"">import Foo from "./hooks/Modal/BasicModal.vue"</warning>

</script>