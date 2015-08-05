import 'dart:html';

main() {
  var element = querySelector("foo");
  element.onClick.listen(clickHandler<caret>);
}

clickHandler() {}