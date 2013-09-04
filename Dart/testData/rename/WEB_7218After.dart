import 'dart:html';

void main() {
  query("#text")
  ..text = "Click me!"
  ..onMouseOver.listen(colorTextNew);
}

void colorTextNew(MouseEvent event) {
  query("#text").style
  ..color = "red";
}