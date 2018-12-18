<template lang="pug">
  ul
    li(ref="someRef")
    li(slot="someSlot")
    li(slot-scope="someSlot")

    li(v-text="someText")
    li(v-html="someHtml")
    li(v-show="someShow")
    li(v-if="someIf")
    li(v-else="someElse")
    li(v-else-if="someElseIf")
    li(v-for="someFor")
    li(v-model="someModel")
    li(v-pre="somePre")
    li(v-cloak="someCloak")
    li(v-once="someOnce")

    li(v-on:click="someOn")
    li(v-on:click.ctrl.capture="someOn")
    li(@click="someOn")
    li(@click.stop="someOn")

    li(v-bind="someBind")
    li(v-bind:key="someBind")
    li(v-bind:is="someBind")
    li(v-bind:align="someBind")
    li(:key="someBind")
    li(:is="someBind")
    li(:align="someBind")
</template>