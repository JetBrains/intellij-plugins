<template>
  <div>
    <main-menu :value='bar' @fooChange="bar = $event"></main-menu>
    <main-menu :value='bar' @change="bar = $event"></main-menu>
    <main-menu :value='bar' @fooChange="bar = isNaN(parseFloat($event)) ? $event : parseFloat($event)"></main-menu>
    <main-menu :value='fooo' @fooChange="fooo = typeof $event === 'string' ? $event.trim() : $event"></main-menu>
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
