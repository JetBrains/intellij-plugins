package foo.b<caret>ar.baz {
  import foo.bar.baz.XXX;
  [ArrayElementType("foo.bar.baz.Foo")]
  class Foo {
      var x:foo.bar.baz.Foo;
  }
}