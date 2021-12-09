<script lang="ts">
import Def from './NoScriptSection.vue'
import Vue from 'vue'

const <error>a</error>: string = 12
const <error>b</error>: string = Def

export default Def.extend({
  components: {Def}
})
Def.<error descr="TS2339: Property 'foo' does not exist on type 'VueConstructor<Vue>'.">foo</error>()

Vue.extend({
  components: {Def}
})
</script>