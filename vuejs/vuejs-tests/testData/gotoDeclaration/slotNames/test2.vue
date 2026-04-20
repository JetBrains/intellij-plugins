<template>
  <x-template><template v-slot:xTemplate1></template></x-template>
  <script-template-vue><template v-slot:scriptTemplateVue1></template></script-template-vue>
  <require-decorators><template v-slot:default></template></require-decorators>
  <export-import><template v-slot:exportImport1></template></export-import>
  <no-script-section><template v-slot:noScriptSection1></template></no-script-section>

  <x-template><div slot="xTemplate1"></div></x-template>
  <script-template-vue><div slot="scriptTemplateVue1"></div></script-template-vue>
  <require-decorators><div slot="default"></div></require-decorators>
  <export-import><div slot="exportImport1"></div></export-import>
  <no-script-section><div slot="noScriptSection1"></div></no-script-section>
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
