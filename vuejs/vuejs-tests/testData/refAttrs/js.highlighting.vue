<template>
  <div class='hello'>
    <div ref='divRef'>1</div>
    <div ref='divRef'>2</div>
    <div ref='divRef3'>3</div>
    <input v-for='d in [1,2,3]' :key='d' ref='inputRef'>
    <<warning descr="Unknown html tag router-view">router-view</warning> ref='router'>{{ $refs.inputRef[0].validationMessage }}</<warning descr="Unknown html tag router-view">router-view</warning>>
    <About ref='about'></About>
    <table ref='tableRef'>4</table>
    <div @click='insert()' ref='insert'>Insert</div>
  </div>
</template>

<script lang='js'>
import {defineComponent} from 'vue'
import About from '@/views/About.vue';

export default defineComponent({
  components: { About },
  methods: {
    insert () {
      this.$refs.about.$refs
      this.$refs.about.$rafs
      this.$refs.divRef3
      this.$refs.divRef3.<weak_warning descr="Unresolved variable foo">foo</weak_warning>
      this.$refs.inputRef[0].<weak_warning descr="Unresolved variable validationMesage">validationMesage</weak_warning>
      this.$refs.div.innerHTML
      this.$refs.div.<weak_warning descr="Unresolved variable foo">foo</weak_warning>
    }
  }
})
</script>