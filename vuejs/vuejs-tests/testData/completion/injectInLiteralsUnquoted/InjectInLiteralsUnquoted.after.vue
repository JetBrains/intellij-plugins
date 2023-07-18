<script>
import {defineComponent} from "vue";

export default defineComponent({
  name: "InjectInLiteralsUnquoted",
  inject: [
    "provideGlobal",
    "provideInApp"
  ]
})
</script>
