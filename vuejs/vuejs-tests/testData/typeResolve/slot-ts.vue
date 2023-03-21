<script setup lang="ts"/>
<template>
  <div>
    <template #default="{ row }: {row: {uid: boolean}}">
      {{ row.uid }}
    </template>
    <template #default="bag: {row: {uid: boolean}}">
      {{ bag.row.uid }}
    </template>
  </div>
</template>