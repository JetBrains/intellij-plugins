<template>
    <div @click="foo = <error descr="Assigned expression type 12 is not assignable to type string">annualReportPeriodId</error>">
        {{ <error descr="Unresolved variable or type abc">abc</error> }}
        {{ <error descr="Method expression is not of Function type">annualReportPeriodId</error>() }}
    </div>
</template>
<script lang="ts">
import Vue from 'vue';

export default Vue.extend({
    props: {
      foo: String
    },
    computed: {
        annualReportPeriodId() {
            return 12
        }
    }
})
</script>
