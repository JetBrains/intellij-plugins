library custom;

import "foo";
import 'dart:html' as html;
import '''package:z''' hide x, y, z show a, b, c;
import """bar""" show a;
import r"""bar""" hide a;
import r'''bar''' deferred as prefix show x hide a;
import augment 'a.dart';
import 'src/usage_impl_default.dart'
if (dart.library.js) 'src/usage_impl_html.dart'
if (dart.library.ui) 'src/usage_impl_flutter.dart'
if (dart.library.io) 'src/usage_impl_io.dart'
as impl show A,B hide C,D;

import 'foo.dart' if ( dart . library . js == '''foo''') 'bar' hide A;
import 'foo.dart'if(foo=="foo")'bar' as impl;
import 'foo.dart'if(foo=="foo")'bar';
export 'foo.dart'if(foo=="foo")'bar';
export 'foo.dart' if ( dart . library . js == '''foo''') 'bar' show A hide B;

part 'Util.dart';

void main() {
  "".length;
  [].length;
}
