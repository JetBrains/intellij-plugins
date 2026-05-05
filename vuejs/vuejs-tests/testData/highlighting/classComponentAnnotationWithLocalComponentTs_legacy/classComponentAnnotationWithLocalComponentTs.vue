<template>
  <LongVue/>
  <ShortComponent/>
  <<warning descr="Unknown html tag UnknownComponent">UnknownComponent</warning>/>
  <UsageComponent/>
</template>
<script lang="ts">
import { Component } from 'vue-property-decorator';
// just for test
import { Vue } from 'vue.d.ts';
import LongComponent from './LongComponent';
import ShortComponent from './ShortComponent';
@Component({
  name: "UsageComponent",
  components: {
    "LongVue": LongComponent,
    ShortComponent
  }
})
export default class UsageComponent extends Vue {
}
</script>