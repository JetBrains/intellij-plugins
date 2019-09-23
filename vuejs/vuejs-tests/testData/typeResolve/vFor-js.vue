<template>
  <div v-for="el1 in arr">{{ el1 }}</div>
  <div v-for="num1 in aNum">{{ num1 }}</div>
  <div v-for="str1 in aString">{{ str1 }}</div>
  <div v-for="obj1 in anObject">{{ obj1 }}</div>

  <div v-for="(el2, el2Ind) in arr">{{ el2, el2Ind }}</div>
  <div v-for="(num2, num2Ind ) in aNum">{{ num2, num2Ind }}</div>
  <div v-for="(str2, str2Ind ) in aString">{{ str2, str2Ind }}</div>
  <div v-for="(obj2, obj2Ind ) in anObject">{{ obj2, obj2Ind }}</div>

  <div v-for="(el3, el3Ind) in [12, 13, 'foo']">{{ el3, el3Ind }}</div>
  <div v-for="(num3, num3Ind ) in 12">{{ num3, num3Ind }}</div>
  <div v-for="(str3, str3Ind ) in 'foo'">{{ str3, str3Ind }}</div>
  <div v-for="(obj3, obj3Ind ) in {foo: 'bar', bar: 12}">{{ obj3, obj3Ind }}</div>

</template>
<script>
  export default {
    props: {
      arr: Array,
      aNum: Number,
      aString: String,
      anObject: Object
    }
  }
</script>
