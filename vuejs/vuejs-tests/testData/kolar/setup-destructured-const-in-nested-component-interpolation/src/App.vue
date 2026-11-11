<script setup lang="ts">
const { env, prNumber } = useAppConfig().buildInfo
const showLogo = computed(() => true)
const isSearchExpanded = computed(() => false)
</script>

<template>
  <header>
    <span>
      {{ env === 'release' ? 'alpha' : env }}
    </span>
    <NuxtLink
      v-if="showLogo && !isSearchExpanded && prNumber"
      :to="`https://github.com/npmx-dev/npmx.dev/pull/${prNumber}`"
      :aria-label="$t('header.pr', { prNumber })"
    >
      <span class="text-xs px-1.5 py-0.5 rounded badge-green font-sans font-medium">
        PR #{{ prNumber }}
      </span>
    </NuxtLink>
  </header>
</template>
