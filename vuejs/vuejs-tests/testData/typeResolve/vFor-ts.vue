<template>
  <div v-for="el1 in arr">{{ el1 }}</div>
  <div v-for="num1 in aNum">{{ num1 }}</div>
  <div v-for="str1 in aString">{{ str1 }}</div>
  <div v-for="obj1 in anObject">{{ obj1 }}</div>
  <div v-for="objNum1 in anObjectNum">{{ objNum1 }}</div>
  <div v-for="objMix1 in anObjectMix">{{ objMix1 }}</div>
  <div v-for="objIter1 in anObjectIter">{{ objIter1 }}</div>
  <div v-for="objInit1 in anObjectInit">{{ objInit1 }}</div>
  <div v-for="state1 in shopStates">{{ state1 }}</div>
  <div v-for="union1 in union">{{ union1 }}</div>

  <div v-for="(el2, el2Ind) in arr">{{ el2, el2Ind }}</div>
  <div v-for="(num2, num2Ind ) in aNum">{{ num2, num2Ind }}</div>
  <div v-for="(str2, str2Ind ) in aString">{{ str2, str2Ind }}</div>
  <div v-for="(obj2, obj2Ind ) in anObject">{{ obj2, obj2Ind }}</div>
  <div v-for="(objNum2, objNum2Ind) in anObjectNum">{{ objNum2, objNum2Ind }}</div>
  <div v-for="(objMix2, objMix2Ind) in anObjectMix">{{ objMix2, objMix2Ind }}</div>
  <div v-for="(objIter2, objIter2Ind) in anObjectIter">{{ objIter2, objIter2Ind }}</div>
  <div v-for="(objInit2, objInit2Ind) in anObjectInit">{{ objInit2, objInit2Ind }}</div>
  <div v-for="(state2, state2Ind ) in shopStates">{{ state2, state2Ind }}</div>
  <div v-for="(union2, union2Ind) in union">{{ union2, union2Ind }}</div>

  <div v-for="(el3, el3Ind) in ['12', 'sss', 'foo']">{{ el3, el3Ind }}</div>
  <div v-for="(num3, num3Ind ) in 12">{{ num3, num3Ind }}</div>
  <div v-for="(str3, str3Ind ) in 'foo'">{{ str3, str3Ind }}</div>
  <div v-for="(obj3, obj3Ind ) in {foo: true, bar: false}">{{ obj3, obj3Ind }}</div>
  <div v-for="(objNum3, objNum3Ind ) in {12: 'foo', 56: 'bar'}">{{ objNum3, objNum3Ind }}</div>
  <div v-for="(objMix3, objMix3Ind) in {12: 'foo', foo: true}">{{ objMix3, objMix3Ind }}</div>
  <div v-for="(objIter3, objIter3Ind) in anObjectIter">{{ objIter3, objIter3Ind }}</div>
  <div v-for="(objInit3, objInit3Ind) in {'a':1, b:2, c:3, d:4}">{{ objInit3, objInit3Ind }}</div>
  <div v-for="({allPage: state3 }, state3Ind ) in shopStates">{{ state3, state3Ind }}</div>
  <div v-for="(union3, union3Ind) in union">{{ union3, union3Ind }}</div>

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
    @Prop() anObjectMix: {
      [key:number]: Foo2
      [key:string]: boolean
    }
    @Prop() anObjectIter: {
      [Symbol.iterator]: Iterator<boolean>
      [key:number]: Foo2
    }
    @Prop shopStates: ShopState[]
    @Prop anObjectInit = {
      a: 1,
      b: 2,
      c: 3,
      d: 4
    }
    @Prop() union: number | string | number[]
  }
</script>
