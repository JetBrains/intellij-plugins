main() {
  <weak_warning descr="SomeClass is deprecated">SomeClass</weak_warning>.<weak_warning descr="foo is deprecated">foo</weak_warning>();
}

@deprecated
class SomeClass {
  const SomeClass();
  @deprecated
  @<weak_warning descr="SomeClass is deprecated">SomeClass</weak_warning>()
  static <weak_warning descr="SomeClass is deprecated">SomeClass</weak_warning> foo() {}
}