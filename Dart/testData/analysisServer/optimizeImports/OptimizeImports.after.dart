import 'dart:core';
import "dart:html";

import "OptimizeImports_other1.dart" as foo;
import "incorrect.dart";

export "OptimizeImports_other1.dart";
export """OptimizeImports_other2.dart""";
export "bar.dart";
export "incorrect.dart";

part 'OptimizeImports_other1.dart';
part 'OptimizeImports_other2.dart';
part 'incorrect.dart';

HtmlElement h = foo.inOther1();
