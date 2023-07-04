<template>
  <!-- missing title attribute -->
  <head></head>
  <!-- extra closing tag -->
  <link><warning descr="Closing tag is redundant"></link></warning>
  <area></area>
  <!-- missing alt attribute -->
  <area></area>
  <<warning descr="Element img doesn't have required attribute src"><warning descr="Missing required 'alt' attribute">img</warning></warning>><warning descr="Closing tag is redundant"></<warning descr="Element img doesn't have required attribute src">img</warning>></warning>
  <<warning descr="Missing required 'alt' attribute"><warning descr="Obsolete tag">applet</warning></warning>></<warning descr="Obsolete tag">applet</warning>>
</template>
<script>
import { Head, Area } from "somewhere";
export default {
  components: {
    Head,
    Area
  }
}
</script>