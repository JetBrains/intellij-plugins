<template>
    <div @click="foo = annualReportPeriodId">
        {{ abc }}
        {{ annualReportPeriodId() }}
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
