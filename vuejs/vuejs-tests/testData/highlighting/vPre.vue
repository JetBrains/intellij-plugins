<template>
  <div>
    <div v-pre>
      {{ foo }}
      <div v-for="let a in b"></div>
      <div <warning descr="Attribute for is not allowed here">for</warning>="let a in b"></div>
    </div>
    {{ <weak_warning descr="Unresolved variable or type foo">foo</weak_warning> }}
    <div v-for="<warning descr="Unused local variable let">let</warning><error descr="'in' or 'of' expected"> </error><weak_warning descr="Unresolved variable or type a">a</weak_warning> in <weak_warning descr="Unresolved variable or type b">b</weak_warning>"></div>
    <div <warning descr="Attribute for is not allowed here">for</warning>="let a in b"></div>
  </div>
</template>