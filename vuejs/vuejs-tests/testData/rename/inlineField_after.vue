<template>
  <div class="home" :about='foo'>
    {{ foo }} | {{ test1 }}
  </div>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';

@Component({})
export default class Home extends Vue {
  private foo = 'foo';
  private test1 = 'test1'
}
</script>
