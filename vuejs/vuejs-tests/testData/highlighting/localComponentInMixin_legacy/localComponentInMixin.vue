<template>
  <<warning descr="Element b-comp doesn't have required attribute from-b">b-comp</warning>>
</<warning descr="Element b-comp doesn't have required attribute from-b">b-comp</warning>>
</template>

<script>
import A from 'a-component'
export default {
  mixins: [ A ]
}
</script>