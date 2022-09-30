<template>
  <div class="column items-center justify-start">
    <div v-model="isAutoChangeTheme"/>
    <div v-model="isDa<caret>rkMode"/>
    <div v-model="isOfflineMode"/>
  </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue';
import useSettings from "@/useSettings";

export default defineComponent({
  name: 'Settings',
  setup() {
    const {isAutoChangeTheme, isDarkMode, isOfflineMode,} = useSettings()

    return {
      isAutoChangeTheme,
      isDarkMode,
      isOfflineMode,
    };
  },
});
</script>
