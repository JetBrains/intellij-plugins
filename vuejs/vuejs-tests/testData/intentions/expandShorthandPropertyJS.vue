<script lang="js"></script>

<template>
  <v-data-table>
    <template #item.name="{ i<caret>tem }">
      {{ item.name }}
    </template>
  </v-data-table>
</template>