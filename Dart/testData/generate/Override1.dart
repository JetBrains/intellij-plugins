class Override1 extends Foo<Bar> {
<caret>
}

class Foo<T> {
  T getFoo() {
    return null;
  }
}

class Bar {

}