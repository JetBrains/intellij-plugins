<template>
  <div v-for="el1 in arr">{{ el1 }}</div>
  <div v-for="num1 in aNum">{{ num1 }}</div>
  <div v-for="str1 in aString">{{ str1 }}</div>
  <div v-for="obj1 in anObject">{{ obj1 }}</div>
  <div v-for="objNum1 in anObjectNum">{{ objNum1 }}</div>
  <div v-for="state1 in shopStates">{{ state1 }}</div>

  <div v-for="(el2, el2Ind) in arr">{{ el2, el2Ind }}</div>
  <div v-for="(num2, num2Ind ) in aNum">{{ num2, num2Ind }}</div>
  <div v-for="(str2, str2Ind ) in aString">{{ str2, str2Ind }}</div>
  <div v-for="(obj2, obj2Ind ) in anObject">{{ obj2, obj2Ind }}</div>
  <div v-for="(objNum2, objNum2Ind) in anObjectNum">{{ objNum2, objNum2Ind }}</div>
  <div v-for="(state2, state2Ind ) in shopStates">{{ state2, state2Ind }}</div>

  <div v-for="(el3, el3Ind) in ['12', 'sss', 'foo']">{{ el3, el3Ind }}</div>
  <div v-for="(num3, num3Ind ) in 12">{{ num3, num3Ind }}</div>
  <div v-for="(str3, str3Ind ) in 'foo'">{{ str3, str3Ind }}</div>
  <div v-for="(obj3, obj3Ind ) in {foo: true, bar: false}">{{ obj3, obj3Ind }}</div>
  <div v-for="(objNum3, objNum3Ind ) in {12: 'foo', 56: 'bar'}">{{ objNum3, objNum3Ind }}</div>
  <div v-for="({allPage: state3 }, state3Ind ) in shopStates">{{ state3, state3Ind }}</div>

</template>
<script lang="ts">
  interface Foo2 {

  }

  interface ShopState {
    allPage: Foo2
    allResult: number
  }

  @Component
  export default class FooComponent {
    @Prop() arr: string[]
    @Prop() aNum: number
    @Prop() aString: string
    @Prop() anObject: {
      [key:string]: boolean
    }
    @Prop() anObjectNum: {
      [key:number]: string
    }
    @Prop shopStates: ShopState[]
  }
</script>
