<template>
  <x-template><template v-slot:xTemplate1></template></x-template>
  <script-template-vue><template v-slot:scriptTemplateVue1></template></script-template-vue>
  <require-decorators><template v-slot:default></template></require-decorators>
  <export-import><template v-slot:exportImport1></template></export-import>

  <x-template><div slot="xTemplate1"></div></x-template>
  <script-template-vue><div slot="scriptTemplateVue1"></div></script-template-vue>
  <require-decorators><div slot="default"></div></require-decorators>
  <export-import><div slot="exportImport1"></div></export-import>
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
