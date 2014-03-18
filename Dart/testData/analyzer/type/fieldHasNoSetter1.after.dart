class Foo {
  set bar(Bar value) {
    
  }

  Bar get bar => null;
}

class Bar {}

main() {
  Foo foo = new Foo();
  foo.bar = new Bar();
}