<template>
  <div>
    <q-table :rows="rows" :columns="columns" row-key="id" flat>
      <template v-slot:body="props">
        <MyTableRow :data="props.key"/>
        <MyTableRow :data="props.<error descr="Unresolved variable keys">keys</error>"/>
        <MyTableRow :data="<error descr="Type number is not assignable to type MyData">props.rowIndex</error>"/>
      </template>
      <template v-slot:header="{selected, sort: foo} ">
        <MyTableRow :data="<error descr="Type boolean is not assignable to type MyData">selected</error>"/>
        <MyTableRow :data="<error descr="Type (col: any) => void is not assignable to type MyData">foo</error>"/>
      </template>
    </q-table>
  </div>
</template>

<script lang="ts" setup>
import { QTableColumn } from 'quasar';

import MyTableRow from  './MyTableRow.vue';
import { MyData } from './my-data';

const columns: QTableColumn<MyData>[] = [
  {
    name: 'id',
    field: 'id',
    label: '',
    align: 'center',
    sortable: true,
  },
  {
    name: 'name',
    field: 'name',
    label: 'Name',
    align: 'left',
    sortable: true,
  },
  {
    name: 'qty',
    field: 'qty',
    label: 'Qty/Sold',
    align: 'left',
    sortable: false,
  }
];

const rows: MyData[] = [
  {
    id: 1,
    name: 'Cool Widget',
    qty: 7,
    sold: 10,
  },
  {
    id: 2,
    name: 'Another Widget',
    qty: 7,
    sold: 10,
  },
  {
    id: 3,
    name: 'Good Widget',
    qty: 7,
    sold: 10,
  },
];
</script>
