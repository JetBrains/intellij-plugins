<template>
    <div><foo-bar :value="Colors.blue"/>
      {{ Checker.state() }}
      <source :title="'copy-paste!'" :foo/>
    </div>
</template>
<script setup>
import {Colors} from "./colors";
import {Checker} from "./checker";
import Source from "./Source.vue";
import FooBar from "./Foo.vue";
</script>