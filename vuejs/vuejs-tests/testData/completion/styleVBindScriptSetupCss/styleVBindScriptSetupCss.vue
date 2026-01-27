<script setup>
import {ref} from "vue"

let aaaCount = 12
let aaaFoo = ref("test")
</script>

<style>
div {
  color: v-bind(<caret>)
}
</style>