set text(String newText) {
  _text = newText;
}

String get text => _text;

get foo() => text.len<caret>gth;