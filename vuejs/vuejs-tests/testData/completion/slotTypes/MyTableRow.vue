<template>
  <q-tr>
    <q-td key="id" class="text-center">
      {{ data.id }}
    </q-td>
    <q-td key="name">
      {{ data.name }}
    </q-td>
    <q-td key="qtySold"> {{ data.qty }} / {{ data.sold }} </q-td>
  </q-tr>
</template>

<script lang="ts" setup>
import { MyData } from './my-data';

defineProps<{
  data: MyData;
}>();
</script>
