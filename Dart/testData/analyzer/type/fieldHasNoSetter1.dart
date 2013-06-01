class Foo {
   Bar get bar => null;
 }

 class Bar {}

 main() {
   var foo = new Foo();
   foo.bar = new Bar();
 }