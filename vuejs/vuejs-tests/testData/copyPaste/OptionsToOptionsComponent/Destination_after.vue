<template>
  <div><foo-bar :value="Colors.blue"/>
    <classic-source :title="'copy-paste!'" :foo/>
  </div>
</template>
<script>
import ClassicSource from "./Source.vue";
import FooBar from "./Foo.vue";

export default {
  components: {FooBar, ClassicSource}

}
</script>