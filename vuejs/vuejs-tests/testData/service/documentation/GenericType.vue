<script setup lang="ts">
import {ref} from "vue";

let <error>dummyErrorToSatisfyTestFramework</error>: string = 1;

/**
 * docs have to be removed!
 */
let local = ref(323 as number | string);

<caret>local;
</script>
