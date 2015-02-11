main() {
  <weak_warning descr="SomeClass is deprecated">SomeClass</weak_warning>.<weak_warning descr="foo is deprecated">foo</weak_warning>();
  new <weak_warning descr="SomeClass is deprecated">SomeClass</weak_warning>();
  new <weak_warning descr="SomeClass is deprecated">SomeClass</weak_warning>.named();
  new <weak_warning descr="SomeClass is deprecated">SomeClass</weak_warning>.factory();
}

@deprecated
class SomeClass {
  const SomeClass();
  SomeClass.named(){}
  factory SomeClass.factory(){}

  @deprecated
  @<weak_warning descr="SomeClass is deprecated">SomeClass</weak_warning>()
  static <weak_warning descr="SomeClass is deprecated">SomeClass</weak_warning> foo() {
    return null;
  }
}