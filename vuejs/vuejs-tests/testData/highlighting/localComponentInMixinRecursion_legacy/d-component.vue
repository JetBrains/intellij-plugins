<template>
  <hidden-component/>
</template>

<script>
import HiddenComponent from "./hidden-component";
export default {
  name: "d-component",
  components: {HiddenComponent},
  props: {
    fromD: {
      required: true
    }
  }
}
</script>