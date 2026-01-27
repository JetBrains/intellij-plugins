<script setup>
import {ref} from "vue"

let aaaCount = 12
let aaaFoo = ref("test")
</script>

<style lang="sass">
div
  color: v-bind(<caret>)
</style>