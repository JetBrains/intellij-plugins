<script setup lang="ts">
import { ref } from 'vue'

const {ms<caret>g} = defineProps<{ msg: string }>()

const count = ref(0)
</script>