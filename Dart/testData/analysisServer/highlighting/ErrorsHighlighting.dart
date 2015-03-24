import <weak_warning descr="Unused import" type="UNUSED_SYMBOL">"dart:html"</weak_warning>;
import "dart:core";
import <weak_warning descr="Duplicate import" type="UNUSED_SYMBOL">"dart:core"</weak_warning>;
import <error descr="Target of URI does not exist: 'dart:incorrect'" type="ERROR">"dart:incorrect"</error>;

main() {
  <warning descr="The function 'foo' is not defined" type="WARNING">foo</warning>();
  <weak_warning descr="'bar' is deprecated" type="DEPRECATED">bar</weak_warning>();
  // todo smth
  var <weak_warning descr="The value of the local variable 'unused' is not used" type="UNUSED_SYMBOL">unused</weak_warning>;
}

@deprecated
<weak_warning descr="This function declares a return type of 'int', but does not end with a return statement" type="WEAK_WARNING">int</weak_warning> bar(){}

class <weak_warning descr="The class '_Foo' is not used" type="UNUSED_SYMBOL">_Foo</weak_warning> {
  int <weak_warning descr="The value of the field '_unusedField' is not used" type="UNUSED_SYMBOL">_unusedField</weak_warning>;
}