<template>
    <div :title="Colors.blue">
    </div>
</template>
<script>
import {Colors} from "./colors";

export default {
  computed: {
    Colors() {
      return Colors
    }
  }

}
</script>