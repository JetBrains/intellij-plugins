<template>
  <div class="home" :about='test'>
    {{ test }} | {{ test1 }}
  </div>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';

@Component({})
export default class Home extends Vue {
  private te<caret>st = 'test';
  private test1 = 'test1'
}
</script>
