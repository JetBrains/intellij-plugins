<script setup>
import {ref} from "vue"

let aaaCount = 12
let aaaFoo = ref("test")
</script>

<style lang="scss">
div {
  color: v-bind(<caret>)
}
</style>