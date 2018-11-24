import <warning descr="Unused import" type="UNUSED_SYMBOL">"dart:html"</warning>;
import "dart:core";
import <warning descr="Duplicate import" type="UNUSED_SYMBOL">"dart:core"</warning>;
import <error descr="Target of URI does not exist: 'dart:incorrect'" type="ERROR">"dart:incorrect"</error>;

main() {
  <warning descr="The function 'foo' is not defined." type="WARNING">foo</warning>();
  <warning descr="'bar' is deprecated" type="DEPRECATED">bar</warning>();
  // todo smth
  var <warning descr="The value of the local variable 'unused' is not used" type="UNUSED_SYMBOL">unused</warning>;
  return;
  <warning descr="Dead code" type="UNUSED_SYMBOL">1 + 1;</warning>
}

@deprecated
<weak_warning descr="This function declares a return type of 'int', but does not end with a return statement" type="WEAK_WARNING">int</weak_warning> bar(){}

// TODO highlighted by IDE engine
class <warning descr="The class '_Foo' is not used" type="UNUSED_SYMBOL">_Foo</warning> {
  int <warning descr="The value of the field '_unusedField' is not used" type="UNUSED_SYMBOL">_unusedField</warning>;
}