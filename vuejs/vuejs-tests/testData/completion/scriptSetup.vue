<script setup="foos">
// imported components are also directly usable in template
import Foo from './Foo.vue'
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
</script>

<template>
  <Foo :count="count" @click="inc" v/>
  <ScriptSetup :foo="12"/>
</template>