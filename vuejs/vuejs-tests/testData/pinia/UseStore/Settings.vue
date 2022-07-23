<template>
  <q-page class="column items-center justify-start">
    <h4 class="q-ma-lg">Settings</h4>
    <q-list>
      <q-item>
        <q-item-section side>
          <q-toggle v-model="isAutoChangeTheme"/>
        </q-item-section>
      </q-item>
      <transition
          enter-active-class="animated slideInDown"
          leave-active-class="animated zoomOut"
      >
        <q-item>
          <q-item-section side>
            <q-toggle v-model="isDa<caret>rkMode"/>
            <q-toggle v-model="isOfflineMode"/>
          </q-item-section>
        </q-item>
      </transition>
    </q-list>
  </q-page>
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
