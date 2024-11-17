// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

class A {}

abstract class B <fold text='{...}' expand='true'>{
// comment

}</fold>

class C extends B <fold text='{...}' expand='true'>{
// comment

}</fold>

enum Foo1{}
enum Foo2{a}
enum Foo3<fold text='{...}' expand='true'>{a }</fold>
enum Foo4<fold text='{...}' expand='true'>{
a,
b,
//c
}</fold>

mixin Mix1{}
mixin Mix2{a}
mixin Mix3<fold text='{...}' expand='true'>{a }</fold>
mixin Mix4<fold text='{...}' expand='true'>{
a,
b,
//c
}</fold>

extension NumberParsing on String <fold text='{...}' expand='true'>{
  int parseInt() <fold text='{...}' expand='true'>{
    return int.parse(this);
  }</fold>

  double parseDouble() <fold text='{...}' expand='true'>{
    return double.parse(this);
  }</fold>
}</fold>

extension NumberParsing2 on String {}

extension type Id(String _) {}
extension type Id2(String _) {a}
extension type Id3(String _)<fold text='{...}' expand='true'>{a }</fold>
extension type Id4(String _)<fold text='{...}' expand='true'>{
a,
b,
//c
}</fold>
