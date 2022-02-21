<template>
  <img src="./src/logo.png"/>
  <img src="./src/<warning descr="Cannot resolve file 'foo.png'">foo.png</warning>"/>
  <img src="@/logo.png"/>
  <img src="~@/logo.png"/>
  <img src="~foo/logo.png"/>
  <img src="~foo/<warning descr="Cannot resolve file 'foo.png'">foo.png</warning>"/>
  <img src="~<warning descr="Cannot resolve directory 'bar'">bar</warning>/<warning descr="Cannot resolve file 'logo.png'">logo.png</warning>"/>
  <img src="~bar_module/logo.png"/>
  <img src="~bar_module/<warning descr="Cannot resolve file 'foo.png'">foo.png</warning>"/>
</template>
<style>
@import "~@/logo.png";
.cls {
  background: url('<error descr="Cannot resolve file 'bar.png'">bar.png</error>');
}
.cls1 {
  background: url('~@/logo.png');
}
.cls2 {
  background: url('~foo/logo.png');
}
.cls3 {
  background: url('~foo/<error descr="Cannot resolve file 'bar.png'">bar.png</error>');
}
</style>