<template>
  <div>
    <Test2 :foo='<weak_warning descr="Type  number  is not assignable to type  string | undefined   Type  number  is not assignable to type  string ">12</weak_warning>' :bar="<weak_warning descr="Type  number  is not assignable to type  string ">12</weak_warning>" :fooBar="12"> </Test2>
    <Test1 :foo='<weak_warning descr="Type  number  is not assignable to type  string | undefined   Type  number  is not assignable to type  string ">12</weak_warning>' :bar="<weak_warning descr="Type  number  is not assignable to type  string ">12</weak_warning>" :fooBar="12"> </Test1>
  </div>
</template>
<script>
import Test1 from "./Test1"
import Test2 from "./Test2"
export default {
  components: {Test2, Test1}
}
</script>