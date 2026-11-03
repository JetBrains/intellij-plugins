<script vapor lang="ts">
</script>

<template>
  <main>
    <RouterView v-slot="{ Component }">
      <Transition name="fade" mode="out-in">
      </Transition>
    </RouterView>
  </main>
</template>
