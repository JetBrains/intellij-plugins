<script setup="foos" lang="ts">
// imported components are also directly usable in template
import Foo from './Foo.vue'
import fooBar from './fooBar.vue'
import { directive as vClickOutside } from 'v-click-outside'
import { ref } from 'vue'

// write Composition API code just like in a normal setup()
// but no need to manually return everything
const count = ref(0)
const inc = () => {
  count.value++
}

// expects props options
const props = defineProps({
  foo: String
})

const emit = defineEmits<{
  (event: 'add', id: number): void,
  (event: 'remove', force: boolean): void
}>();

// WEB-54758
interface SelectProps {
  value: string,
  children: Array<SelectProps>
}
declare const selectProps: SelectProps;
const selectPropsRef = ref(pp);

</script>

<template>
  <Foo :count="count" @click="inc" v/>
  <ScriptSetupTs :foo="12"/>
</template>