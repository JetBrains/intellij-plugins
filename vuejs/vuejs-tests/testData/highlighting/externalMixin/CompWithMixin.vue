<template>
  <div>
    <div>{{ mixinProp }}</div>
  </div>
</template>
<script>
import Mixin from "./MixinWithProp"

export default {
  mixins: [Mixin]
}
</script>