<script vapor lang="ts">
const route = useRoute()
</script>

<template>
  <div>
    <NuxtRouteAnnouncer v-slot="{ message }">
      {{ route.name === 'search' ? `${$t('search.title_packages')} - npmx` : message }}
    </NuxtRouteAnnouncer>
  </div>
</template>
