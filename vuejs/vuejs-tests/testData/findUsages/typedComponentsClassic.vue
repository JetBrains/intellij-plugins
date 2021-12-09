<template>
  <Dialog>
  <dialog>
  <n-affix>
  <NAffix>
  <Foo>
  <Bar>
</template>
<script>
import {Dialog} from "@headlessui/vue";
import {NAffix} from "naive-ui"
export default {
  components: {
    Dialog,
    Foo: Dialog,
    NAffix,
    Bar: NAffix
  }
}
</script>