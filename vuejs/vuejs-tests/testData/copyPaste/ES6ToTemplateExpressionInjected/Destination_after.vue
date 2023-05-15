<template>
    {{Colors.blue}}
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