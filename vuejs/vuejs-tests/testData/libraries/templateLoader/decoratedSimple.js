import Vue from 'vue'
import Component from 'vue-class-component'
import WithRender from './decoratedSimple.html'

@WithRender
@Component
export default class App extends Vue {
  text = 'Example text'
  fooBar = 12
}
