<template>
    <div><foo-bar :value="Colors.blue"/>
      <script-setup-component :title="'copy-paste!'" :foo/>
    </div>
</template>
<script>
import {Colors} from "./colors";
import FooBar from "./Foo.vue";

export default {
  components: {FooBar},
  computed: {
    Colors() {
      return Colors
    }
  }

}
</script>