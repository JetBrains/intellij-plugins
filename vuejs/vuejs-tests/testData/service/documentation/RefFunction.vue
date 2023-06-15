<script setup lang="ts">
import {ref} from "vue";

let <error>dummyErrorToSatisfyTestFramework</error>: string = 1;

<caret>ref(323 as number | string);
</script>