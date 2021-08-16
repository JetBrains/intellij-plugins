<style scoped>
/* deprecated selector */
::v-deep .foo {}
/* deep selectors */
::v-deep(.foo) {}
/* shorthand */
:deep(.foo) {}

/* targeting slot content */
::v-slotted(.foo) {}
/* shorthand */
:slotted(.foo) {}

/* one-off global rule */
::v-global(.foo) {}
/* shorthand */
:global(.foo) {}

::<error descr="Unknown pseudo selector 'v-glob'">v-glob</error> {

}
</style>
