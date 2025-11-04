<script vapor lang="ts">
import StandardLabel from './StandardLabel.vue';
</script>

<template>
  <main>
    <StandardLabel :label="$t1('aaa', 'bbb')"/>
    <StandardLabel :label="$t1(13, 42)"/>
    <StandardLabel :label="$t1(4n, 8n)"/>
  </main>

  <main>
    <StandardLabel>{{ $t1('aaa', 'bbb') }}</StandardLabel>
    <StandardLabel>{{ $t1(13, 42) }}</StandardLabel>
    <StandardLabel>{{ $t1(4n, 8n) }}</StandardLabel>
  </main>
</template>
