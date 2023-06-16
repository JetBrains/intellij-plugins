<template>
  <<warning descr="Element Definition doesn't have required attribute prop-c">Definition</warning>/>
  <Definition v-model="smtg"/>
  <<warning descr="Element Definition2 doesn't have required attribute model-value">Definition2</warning>/>
  <Definition2 v-model="smtg"/>
  <<warning descr="Element Definition3 doesn't have required attribute foo">Definition3</warning>/>
  <Definition3 v-model:foo="<weak_warning descr="Type  number  is not assignable to type  string ">smtg</weak_warning>"/>
</template>
<script>
import Definition from './Definition';
import Definition2 from './Definition2';
import Definition3 from './Definition3';
export default {
  components: { Definition, Definition2, Definition3 },
  data: function() {
    return {
      smtg: 12
    };
  }
}
</script>