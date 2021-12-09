<template>
  <img src="<warning descr="Cannot resolve file 'a.svg'">a.svg</warning>"/>
  <img src="/a.svg"/>
  <img src="<warning descr="Cannot resolve file 'b.svg'">b.svg</warning>"/>
  <img src="/<warning descr="Cannot resolve file 'b.svg'">b.svg</warning>"/>
</template>
<script>

import {mapActions} from 'vuex'

export default {
  computed: {
    ...mapActions({
                    changeData: 'changeData',
                    changeData2: '<weak_warning descr="Unknown Vuex module namespace foo">foo</weak_warning>/<weak_warning descr="Cannot resolve Vuex action changeData">changeData</weak_warning>',
    }),
  }
}
</script>
