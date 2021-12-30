<script setup >

import {ref} from "vue"

let count = 12
let foo = ref("test")

</script>

<style>
div {
  color: v-bind(<caret>)
}

</style>