library custom;

import "foo";
import 'dart:html' as html;
import '''package:z''' hide x, y, z show a, b, c;
import """bar""" show a;
import r"""bar""" hide a;
import r'''bar''' deferred as prefix show x hide a;

part 'Util.dart';

void main() {
  "".length;
  [].length;
}
