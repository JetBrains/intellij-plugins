<template>
  <div>
    <p>
    <<warning descr="Element p is not allowed here">p</warning>>
      <<warning descr="Element div is not allowed here">div</warning>></<warning descr="Element div is not allowed here">div</warning>>
    </<warning descr="Element p is not allowed here">p</warning>>
  <error descr="Element p is not closed"><</error>/div>
</template>