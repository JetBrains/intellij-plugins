import 'dart:html';

main() {
  var element = querySelector("foo");
  element.onClick.listen(cli<caret>);
}

clickHandler() {}