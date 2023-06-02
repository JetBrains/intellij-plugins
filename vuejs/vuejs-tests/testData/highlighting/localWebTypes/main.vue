<template>
  <div>
    <<warning descr="Component TestFoo2 is not imported">TestFoo2</warning> :foo='<weak_warning descr="Type  number  is not assignable to type  string | undefined   Type number is not assignable to type string">12</weak_warning>' :bar="<weak_warning descr="Type number is not assignable to type string">12</weak_warning>" :fooBar="12"> </TestFoo2>
    <<warning descr="Component TestFoo1 is not imported">TestFoo1</warning> :foo='<weak_warning descr="Type  number  is not assignable to type  string | undefined   Type number is not assignable to type string">12</weak_warning>' :bar="<weak_warning descr="Type number is not assignable to type string">12</weak_warning>" :fooBar="12"> </TestFoo1>
  </div>
</template>
