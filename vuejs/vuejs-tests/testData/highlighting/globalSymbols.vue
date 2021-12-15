<template>
  <div
    @click="encodeURI($event)"
  ></div>
  {{ Object.keys({ a: 12 }) }}
  {{ this.<weak_warning descr="Unresolved variable Object">Object</weak_warning>.keys({a: 12}) }}
  {{ <weak_warning descr="Unresolved variable or type names">names</weak_warning> }}
  {{ <weak_warning descr="Unresolved variable or type foo">foo</weak_warning> }}
</template>
