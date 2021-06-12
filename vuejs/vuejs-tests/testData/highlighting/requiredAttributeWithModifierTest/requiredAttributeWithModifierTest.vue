<template>
  <<warning descr="Element Definition doesn't have required attribute prop-c">Definition</warning>/>
  <Definition :propC.sync="smtg"/>
  <Definition propC.sync="smtg"/>
</template>
<script>
import Definition from './Definition';
export default {
  components: { Definition },
  data: function() {
    return {
      smtg() {}
    };
  }
}
</script>