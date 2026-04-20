<script lang="ts">
import {ITest} from "./Button.vue";
import {defineProps} from "vue";

export default {}
</script>

<script setup lang="ts">
const props = defineProps<{
  items: ITest[]
}>()
</script>
