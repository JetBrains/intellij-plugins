<script setup lang="ts">
const rows = [
  ['a1', 'a2'],
  ['b1', 'b2'],
]
</script>

<template>
  <table>
    <tr v-for="(row, rowIndex) in rows" :key="rowIndex">
      <td v-for="(cell, colIndex) in row" :key="colIndex">{{ cell }}</td>
    </tr>
  </table>
</template>
