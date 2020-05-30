<template>
    <li v-for="<caret>x in items">
        {{x.name[0]}} {{x.name[1]}}
    </li>
</template>

<script lang="ts">
import Vue from 'vue';
import Component from 'vue-class-component';
import { Prop } from 'vue-property-decorator';

@Component
export default class DishDetails extends Vue {
  @Prop()
  public items: {name: [string, string]}[];
}
</script>
