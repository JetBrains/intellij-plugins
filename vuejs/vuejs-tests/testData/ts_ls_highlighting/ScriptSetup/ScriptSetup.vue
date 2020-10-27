<script lang='ts'>
import {defineComponent} from "vue"
import MyComponent from "./my-component.vue"
import <error>MyComponent2</error> from "./my-component2.vue"

export default defineComponent({
  components: {
    MyComponent,
    MyComponent2
  }
})
</script>