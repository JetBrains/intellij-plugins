<template>
    <div @click="foo = <weak_warning descr="Assigned expression type 12 is not assignable to type String">annualReportPeriodId</weak_warning>">
        {{ <weak_warning descr="Unresolved variable or type abc">abc</weak_warning> }}
        {{ <weak_warning descr="Method expression is not of Function type">annualReportPeriodId</weak_warning>() }}
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
