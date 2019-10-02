<template>
  <x-template><template v-slot:</x-template>
  <script-template-vue><template v-slot:</script-template-vue>
  <require-decorators><template v-slot:</require-decorators>
  <export-import><template v-slot:</export-import>
  <some-lib><template v-slot:</some-lib>

  <x-template><div slot=""</x-template>
  <script-template-vue><div slot=""</script-template-vue>
  <require-decorators><div slot=""</require-decorators>
  <export-import><div slot=""</export-import>
  <some-lib><div slot=""</some-lib>
</template>
<script>
  import XTemplate from "./XTemplate/xTemplate";
  import ScriptTemplateVue from "./scriptTemplateVue/scriptTemplateVue";
  import RequireDecorators from "./requireDecorators/requireDecorators";
  import ExportImport from "./exportImport/exportImport";

  export default {
    components: {XTemplate,ScriptTemplateVue,RequireDecorators,ExportImport}
  }
</script>
