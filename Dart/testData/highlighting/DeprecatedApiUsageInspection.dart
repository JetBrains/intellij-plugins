main() {
  <weak_warning descr="SomeClass is deprecated">SomeClass</weak_warning>.<weak_warning descr="foo is deprecated">foo</weak_warning>();
}

@deprecated
class SomeClass {
  const SomeClass();
  @deprecated
  @<weak_warning descr="SomeClass is deprecated">SomeClass</weak_warning>()
  static <weak_warning descr="SomeClass is deprecated">SomeClass</weak_warning> foo() {
    return null;
    <weak_warning descr="Dead code">var i = 0;
    i.<weak_warning descr="The method 'fooBar' is not defined for the class 'int'">fooBar</weak_warning>();</weak_warning>
  }
}