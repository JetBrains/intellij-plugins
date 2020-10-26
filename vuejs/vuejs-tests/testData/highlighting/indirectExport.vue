<template>
  <div>
    <h1> {{ msg }}</h1>
    <div v-text="`fooString: ${fooString}`"></div>
    <div>{{ computedFoo }}</div>
    <div v-on:input='onBar()'/>
  </div>
</template>
<script lang="ts">
import {Component, Prop, Vue} from 'vue-property-decorator';

@Component
class HelloWorld2 extends <error descr="No base constructor has the specified number of type arguments">Vue</error> {
  fooString: string | number = 'some string';
  barString: string | number | undefined;

  @Prop() private msg!: string;

  mounted() {
    setInterval(() => this.fooString = Date.now(), 2000)
  }

  onBar() {
    this.barString = this.fooString
  }

  get computedFoo() {
    return this.fooString
  }
}

export default HelloWorld2
</script>
