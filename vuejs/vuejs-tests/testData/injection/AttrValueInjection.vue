<template>
  <div ref="someRef"></div>
  <div slot="someSlot"></div>

  <div v-text="someText"></div>
  <div v-html="someHtml"></div>
  <div v-show="someShow"></div>
  <div v-if="someIf"></div>
  <div v-else="someElse"></div>
  <div v-else-if="someElseIf"></div>
  <div v-for="someFor"></div>
  <div v-on="someOn"></div>
  <div v-model="someModel"></div>
  <div v-pre="somePre"></div>
  <div v-cloak="someCloak"></div>
  <div v-once="someOnce"></div>

  <div v-bind="someBind"></div>
  <div v-bind:key="someBind"></div>
  <div v-bind:is="someBind"></div>
  <div :key="someBind"></div>
  <div :is="someBind"></div>
</template>