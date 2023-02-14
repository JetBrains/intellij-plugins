<template>
    <div><foo-bar :value="Colors.blue"/>
      {{ Checker.state() }}
      <script-setup-component :title="'copy-paste!'" :foo/>
    </div>
</template>
<script setup>
import {Colors} from "./colors";
import {Checker} from "./checker";
import ScriptSetupComponent from "./ScriptSetupComponent.vue";
import FooBar from "./Foo.vue";
</script>