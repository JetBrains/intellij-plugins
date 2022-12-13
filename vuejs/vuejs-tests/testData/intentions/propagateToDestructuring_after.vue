<template>
    <li v-for="{name}<caret> in items">
      {{ name[0] }} {{ name[1] }}
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
