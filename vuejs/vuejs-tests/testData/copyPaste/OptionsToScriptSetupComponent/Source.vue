<template>
  <div><selection><foo-bar :value="Colors.blue"/>
      <classic-source :title="'copy-paste!'" :foo/>
  </selection></div>
</template>
<script>
import {Colors} from "./colors";
import FooBar from "./Foo.vue";

export default {
  name: "ClassicSource",
  components: {FooBar},
  computed: {
    Colors() {
      return Colors
    }
  }

}
</script>