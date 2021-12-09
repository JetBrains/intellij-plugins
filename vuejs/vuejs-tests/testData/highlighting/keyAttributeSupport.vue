<template>
  <input v-for='d in [1,2,3]' :key='d'>
  <div class='hello'
       v-show='<weak_warning descr="Unresolved variable or type msg">msg</weak_warning>'
       key='ley1'
  <warning descr="Attribute kay is not allowed here">kay</warning>='1' >
  </div>
</template>