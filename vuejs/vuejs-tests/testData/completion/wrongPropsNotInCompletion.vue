<template>
  <test-comp <caret>></test-comp>
</template>

<script>
export default {
  name: "test-comp",
  props: ["aaa", `sss`, 'ddd', true, 123]
};
</script>