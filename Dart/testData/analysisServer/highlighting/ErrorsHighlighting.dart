import <weak_warning descr="Unused import" type="UNUSED_SYMBOL">"dart:html"</weak_warning>;
import "dart:core";
import <weak_warning descr="Duplicate import" type="UNUSED_SYMBOL">"dart:core"</weak_warning>;
import <error descr="Target of URI does not exist: 'dart:incorrect'" type="ERROR">"dart:incorrect"</error>;

main() {
  <warning descr="The function 'foo' is not defined">foo</warning>();
  <weak_warning descr="'bar' is deprecated" type="DEPRECATED">bar</weak_warning>();
  // todo smth
}

@deprecated
<weak_warning descr="This function declares a return type of 'int', but does not end with a return statement">int</weak_warning> bar(){}
