<template>
  <div>
    <q-table :rows="rows" :columns="columns" row-key="id" flat>
      <template v-slot:body="props">
        <MyTableRow :data="props.key"/>
        <MyTableRow :data="props.<weak_warning descr="Unresolved variable keys">keys</weak_warning>"/>
        <MyTableRow :data="<weak_warning descr="Type number is not assignable to type MyData">props.rowIndex</weak_warning>"/>
      </template>
      <template v-slot:header="{selected, sort: foo} ">
        <MyTableRow :data="<weak_warning descr="Type boolean is not assignable to type MyData">selected</weak_warning>"/>
        <MyTableRow :data="<weak_warning descr="Type (col: any) => void is not assignable to type MyData">foo</weak_warning>"/>
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
