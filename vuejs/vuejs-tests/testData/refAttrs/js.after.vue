<template>
  <div class='hello'>
    <div ref='divRef'>1</div>
    <div ref='divRef'>2</div>
    <div ref='divRef3'>3</div>
    <input v-for='d in [1,2,3]' :key='d' ref='inputRef'>
    <router-view ref='router'>{{ $refs.inputRef[0].validationMessage }}</router-view>
    <About ref='about'></About>
    <table ref='tableRef'>4</table>
    <div @click='insert()' ref='insert'>Insert</div>
  </div>
</template>

<script lang='js'>
import {Vue} from 'vue'
import About from '@/views/About.vue';

export default Vue.extend({
  components: { About },
  methods: {
    insert () {
      this.$refs.about.$refs
      this.$refs.divRef3
      this.$refs.div
    }
  }
})
</script>