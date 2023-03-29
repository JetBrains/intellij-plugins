<script lang="ts">
interface TopObj {
  b: string
}
let foo: Clearable
foo.;
</script>

<script
  setup
  lang="ts"
  generic="Clearable extends TopObj, ValueType extends MyObj"
>
interface MyObj {
  a: string
}

const {clearable, value} = defineProps<{
  clearable: Clearable;
  value: ValueType;
}>()

clearable.;
value.;

</script>
<template>
  <div v-if="(value as Clearable)."></div>
  {{ (value as Clearable). }}
</template>