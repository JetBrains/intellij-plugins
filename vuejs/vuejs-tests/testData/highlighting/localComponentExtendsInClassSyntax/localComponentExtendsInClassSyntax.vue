<template>
  <<warning descr="Element ClassA doesn't have required attribute prop-from-a1">ClassA</warning> />
</template>

<script>
import { Vue, Component } from 'vue-property-decorator'
import CompAForClass from './CompAForClass'

@Component({
  name: "ClassA",
  extends: CompAForClass
})
export default class ClassA extends Vue {
}
</script>