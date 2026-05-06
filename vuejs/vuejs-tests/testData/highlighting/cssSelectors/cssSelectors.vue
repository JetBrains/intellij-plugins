<style scoped>
/* deprecated selector */
::<weak_warning descr="v-deep is deprecated, consult docs for better alternative">v-deep</weak_warning> .foo {
  color: aqua;  
}
/* deep selectors */
::v-deep(.foo) {
  color: aqua;  
}
/* shorthand */
:deep(.foo) {
  color: aqua;  
}

/* targeting slot content */
::v-slotted(.foo) {
  color: aqua;  
}
/* shorthand */
:slotted(.foo) {
  color: aqua;  
}

/* one-off global rule */
::v-global(.foo) {
  color: aqua;  
}
/* shorthand */
:global(.foo) {
  color: aqua;  
}

<warning descr="Vue: Do not use empty rulesets">::<error descr="Unknown pseudo selector 'v-glob'">v-glob</error></warning> {

}
</style>
