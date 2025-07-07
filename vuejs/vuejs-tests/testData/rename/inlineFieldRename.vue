<template>
  <div class="home" :about='test'>
    {{ te<caret>st }} | {{ test1 }}
  </div>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';

@Component({})
export default class Home extends Vue {
  private test = 'test';
  private test1 = 'test1'
}
</script>
