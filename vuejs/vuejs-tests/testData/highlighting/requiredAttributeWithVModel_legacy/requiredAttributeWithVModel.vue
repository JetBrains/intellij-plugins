<template>
  <<warning descr="Element Definition doesn't have required attribute prop-c">Definition</warning>/>
  <Definition v-model="smtg"/>
  <<warning descr="Element Definition2 doesn't have required attribute value">Definition2</warning>/>
  <Definition2 v-model="smtg"/>
</template>
<script>
import Definition from './Definition';
import Definition2 from './Definition2';
export default {
  components: { Definition, Definition2 },
  data: function() {
    return {
      smtg() {}
    };
  }
}
</script>