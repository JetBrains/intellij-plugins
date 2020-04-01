<template>
  <Foo :foo="12" @click.left="" v-on:click.right="" :style="" v-bind:id="" v-slot:foo v-dir1:aaa <caret>>
</template>
<script>
  export default {
    name: "Foo",
    props: ["foo", "bar"],
    directives: {
       dir1: function(){},
       dir2: function(){}
    }
  }
</script>
