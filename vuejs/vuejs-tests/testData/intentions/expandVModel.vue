<template>
  <div>
    <main-menu v-model='bar'></main-menu>
    <main-menu v-model.lazy='bar'></main-menu>
    <main-menu v-model.number='bar'></main-menu>
    <main-menu v-model.trim='fooo'></main-menu>
  </div>
</template>
<script lang='ts'>
import {Component, Vue} from 'vue-property-decorator'
import ICUPageMixin, {data} from './mixins'

@Component({
  name: "MainMenu",
  model: {
    event: 'fooChange'
  }
})
export default class MainMenu extends Vue {

}
</script>
