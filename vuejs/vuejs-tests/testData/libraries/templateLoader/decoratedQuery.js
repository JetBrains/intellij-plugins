import Vue from 'vue'
import Component from 'vue-class-component'
import WithRender from './decoratedQuery.html?style=./app.css'

@WithRender
@Component
export default class App extends Vue {
  text = 'Example text'
  fooBar = 12
}
