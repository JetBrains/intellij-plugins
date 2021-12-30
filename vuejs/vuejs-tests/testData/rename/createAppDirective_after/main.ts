import { createApp } from 'vue'
import App from './App.vue'
import Foo from "./foo.vue"
import TheComponent from "./TheComponent.vue"

const app = createApp(App)
  .directive("bar", () => {})
  .component("Bar", Foo)
  .component("Car", TheComponent)


app.mixin({
  components: {
    "FooBar": Foo,
    "BarFoo": TheComponent
  }
})
app.mount('#app')
