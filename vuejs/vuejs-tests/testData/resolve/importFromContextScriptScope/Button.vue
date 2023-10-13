<script lang="ts">
import {defineProps} from "vue";

export interface ITest {
  text: string
}

export default {}
</script>


<script setup lang="ts">
const props = defineProps<{
  item: ITest
}>()
</script>
