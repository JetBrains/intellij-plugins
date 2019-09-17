<template>
  <li v-for="{name} in items">
    {{name.<caret>}}
  </li>
</template>

<script lang="ts">
  import Vue from 'vue';
  import Component from 'vue-class-component';
  import {Prop} from 'vue-property-decorator';

  @Component
  export default class PersonDetails extends Vue {
    @Prop()
    public items: { name: { first, last } }[];

    m() {

    }
  }
</script>
