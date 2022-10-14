<template>
  <<warning descr="Element CompForClass doesn't have required attribute prop-from-a2">CompForClass</warning> />
  <<warning descr="Component OtherCompForClass is not imported">OtherCompForClass</warning>/>
</template>

<script>
import { Vue, Component } from 'vue-property-decorator'
import CompForClass from './CompForClass'

@Component({
  name: "ClassA",
  components: { CompForClass }
})
export default class ClassAB extends Vue {
}
</script>