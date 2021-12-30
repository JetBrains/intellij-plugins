import { createApp } from 'vue'
import App from './App.vue'
import Foo from "./foo.vue"
import TheComponent from "./TheComponent.vue"

const app = createApp(App)
  .directive("foo", () => {})
  .component("Bar", Foo)
  .component("NewCar", TheComponent)


app.mixin({
  components: {
    "FooBar": Foo,
    "BarFoo": TheComponent
  }
})
app.mount('#app')
