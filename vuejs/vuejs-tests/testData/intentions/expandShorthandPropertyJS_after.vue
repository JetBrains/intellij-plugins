<script lang="js"></script>

<template>
  <v-data-table>
    <template #item.name="{ item : item }">
      {{ item.name }}
    </template>
  </v-data-table>
</template>