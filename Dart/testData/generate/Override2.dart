class Foo extends Bar<Baz> {
  <caret>
}

class Bar<T> {
  T find(boolean condition(T item)) {
    return false;
  }
}

class Baz {}