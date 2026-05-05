<template>
  <<warning descr="Element comp-with-mixin doesn't have required attribute required-mixin-prop">comp-with-mixin</warning> mixin-prop=123>
  1</<warning descr="Element comp-with-mixin doesn't have required attribute required-mixin-prop">comp-with-mixin</warning>>
</template>
<script>
import CompWithMixin from './CompWithMixin'

export default {
  components: { CompWithMixin }
}
</script>