<template>
  <div id="scroller" v-on:click="foo = 12"></div>
  <div id="scrollers" v-on:click="onScrolled"></div>
  {{12 + foo}}
  {{bar}}
</template>

<script lang="ts">
  import {Component, Vue} from 'vue-property-decorator'

  export default @Component({})
  class Index extends Vue {

    constructor(private bar) {
      super()
    }

    private onScrolled() {
      this.foo = this.foo + 12 + this.bar;
    }

    private get foo() {

    }

    private set foo(a) {
      foo + 12;
      bar + 14;
    }
  }
  foo + 12;
  bar + 14;
</script>
