import <warning descr="Unused import: 'dart:html'." type="UNUSED_SYMBOL">"dart:html"</warning>;
import "dart:core";
import <warning descr="Duplicate import." type="UNUSED_SYMBOL">"dart:core"</warning>;
import <error descr="Target of URI doesn't exist: 'dart:incorrect'." type="ERROR">"dart:incorrect"</error>;

main() {
  <error descr="The function 'foo' isn't defined." type="ERROR">foo</error>();
  <warning descr="'bar' is deprecated and shouldn't be used." type="DEPRECATED">bar</warning>();
  // todo smth
  var <warning descr="The value of the local variable 'unused' isn't used." type="UNUSED_SYMBOL">unused</warning>;
  return;
  <warning descr="Dead code." type="UNUSED_SYMBOL">1 + 1;</warning>
}

@deprecated
int <weak_warning descr="This function has a return type of 'int', but doesn't end with a return statement." type="WEAK_WARNING">bar</weak_warning>(){}

// TODO highlighted by IDE engine
class <warning descr="The declaration '_Foo' isn't referenced.">_Foo</warning> {
  int <warning descr="The value of the field '_unusedField' isn't used." type="UNUSED_SYMBOL">_unusedField</warning>;
}