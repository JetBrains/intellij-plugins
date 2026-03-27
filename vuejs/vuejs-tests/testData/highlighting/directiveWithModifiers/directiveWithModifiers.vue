<template>
  <div>
    <b-button v-b-modal></b-button>
    <b-button v-b-modal.myModal></b-button>
    <b-button v-b-modal="<weak_warning descr="Unresolved variable or type myModal">myModal</weak_warning>"></b-button>
  </div>
</template>