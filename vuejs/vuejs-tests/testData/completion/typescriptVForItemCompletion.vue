<template>
  <ul>
    <li v-for="item in goodTypes">{{item.<caret>}}</li>
  </ul>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';

@Component
export default class HelloWorld extends Vue {
  @Prop() private msg!: string;
  goodTypes: Array<string> = [];
}
</script>