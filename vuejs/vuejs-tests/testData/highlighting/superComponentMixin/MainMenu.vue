<template>
  <div>
    File size {{ fileSize | formatDigitsAfterDecimal(1) }} GB <br/>
    File size {{ fileSize | <weak_warning descr="Unresolved filter formatDigitAfterDecimal">formatDigitAfterDecimal</weak_warning>(1) }} GB
  </div>

</template>

<script lang="ts">
import { Component } from 'vue-property-decorator';
import { ConversionMixin } from './mixins';

@Component
export default class MainMenu extends ConversionMixin {
  private <warning descr="Unused field freeSpace">freeSpace</warning>: number = 0;
  private fileSize: number = 0;

}
</script>

<style lang="scss" scoped>
</style>
