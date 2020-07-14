<template>
  <img src="./src/logo.png"/>
  <img src="./src/foo.png"/>
  <img src="@/logo.png"/>
  <img src="~@/logo.png"/>
  <img src="~foo/logo.png"/>
  <img src="~foo/foo.png"/>
  <img src="~bar/logo.png"/>
  <img src="~bar_module/logo.png"/>
  <img src="~bar_module/foo.png"/>
</template>
<style>
@import "~@/logo.png";
.cls {
  background: url('bar.png');
}
.cls1 {
  background: url('~@/logo.png');
}
.cls2 {
  background: url('~foo/logo.png');
}
.cls3 {
  background: url('~foo/bar.png');
}
</style>