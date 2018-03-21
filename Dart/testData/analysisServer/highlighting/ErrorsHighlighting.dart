// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
import <warning descr="Unused import: 'dart:html'." type="UNUSED_SYMBOL">"dart:html"</warning>;
import "dart:core";
import <warning descr="Duplicate import." type="UNUSED_SYMBOL">"dart:core"</warning>;
import <error descr="Target of URI doesn't exist: 'dart:incorrect'." type="ERROR">"dart:incorrect"</error>;

main() {
  <warning descr="The function 'foo' isn't defined." type="WARNING">foo</warning>();
  <warning descr="'bar' is deprecated and shouldn't be used." type="DEPRECATED">bar</warning>();
  // todo smth
  var <warning descr="The value of the local variable 'unused' isn't used." type="UNUSED_SYMBOL">unused</warning>;
  return;
  <warning descr="Dead code." type="UNUSED_SYMBOL">1 + 1;</warning>
}

@deprecated
<weak_warning descr="This function declares a return type of 'int', but doesn't end with a return statement." type="WEAK_WARNING">int</weak_warning> bar(){}

// TODO highlighted by IDE engine
class <warning descr="The class '_Foo' isn't used." type="UNUSED_SYMBOL">_Foo</warning> {
  int <warning descr="The value of the field '_unusedField' isn't used." type="UNUSED_SYMBOL">_unusedField</warning>;
}