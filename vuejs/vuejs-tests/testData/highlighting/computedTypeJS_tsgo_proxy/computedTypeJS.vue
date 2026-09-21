<template>
    <div @click="foo = annualReportPeriodId">
        {{ <weak_warning descr="Unresolved variable or type abc">abc</weak_warning> }}
        {{ <weak_warning descr="Method expression is not of Function type">annualReportPeriodId</weak_warning>() }}
    </div>
</template>
<script>
export default {
    props: {
      foo: String
    },
    computed: {
        annualReportPeriodId() {
            return 12
        }
    }
}
</script>
