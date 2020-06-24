<template>
  <x-template><template v-slot:</x-template>
  <script-template-vue><template v-slot:</script-template-vue>
  <require-decorators><template v-slot:</require-decorators>
  <export-import><template v-slot:</export-import>
  <some-lib><template v-slot:</some-lib>
  <no-script-section><template v-slot:</no-script-section>

  <x-template><div slot=""</x-template>
  <script-template-vue><div slot=""</script-template-vue>
  <require-decorators><div slot=""</require-decorators>
  <export-import><div slot=""</export-import>
  <some-lib><div slot=""</some-lib>
  <no-script-section><div slot=""</no-script-section>
</template>
<script>
  import XTemplate from "./XTemplate/xTemplate";
  import ScriptTemplateVue from "./scriptTemplateVue/scriptTemplateVue";
  import RequireDecorators from "./requireDecorators/requireDecorators";
  import ExportImport from "./exportImport/exportImport";
  import NoScriptSection from "./noScriptSection/noScriptSection";

  export default {
    components: {XTemplate,ScriptTemplateVue,RequireDecorators,ExportImport,NoScriptSection}
  }
</script>
