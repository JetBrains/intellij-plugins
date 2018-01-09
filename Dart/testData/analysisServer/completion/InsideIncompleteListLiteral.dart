main() {
  new Foo(
    children: <Object>[
      new <caret>
      new DateTime.now()
    ]
  );
}

class Foo {
  Foo({children}) {}
}
