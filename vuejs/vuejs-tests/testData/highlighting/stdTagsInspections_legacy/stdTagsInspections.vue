<template>
  <!-- missing title attribute -->
  <head></head>
  <!-- extra closing tag -->
  <link><warning descr="Closing tag is redundant"></link></warning>
  <area></area>
  <!-- missing alt attribute -->
  <area></area>
  <<warning descr="Missing required 'alt' attribute">img</warning>><warning descr="Closing tag is redundant"></img></warning>
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