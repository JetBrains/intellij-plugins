class Foo {
   Bar get bar => null;
 }

 class Bar {}

 main() {
   Foo foo = new Foo();
   foo.bar = new Bar();
 }