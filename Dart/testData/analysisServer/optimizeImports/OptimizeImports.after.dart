import 'dart:core';
import "dart:html";

import "OptimizeImports_other1.dart" as foo;

export "OptimizeImports_other1.dart";
export """OptimizeImports_other2.dart""";

part 'OptimizeImports_other1.dart';
part 'OptimizeImports_other2.dart';

HtmlElement h = foo.inOther1();
