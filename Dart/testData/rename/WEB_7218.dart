import 'dart:html';

void main() {
  query("#text")
  ..text = "Click me!"
  ..onMouseOver.listen(colorText);
}

void color<caret>Text(MouseEvent event) {
  query("#text").style
  ..color = "red";
}