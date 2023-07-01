<script>
import {inject} from "vue";

export default {
  setup() {
    function fn() {
      if (a > b) {
        const injected = inject('injectSetup')
        console.log(injected);
        return injected
      }
    }

    return {
      injected: fn()
    }
  }
}
</script>