class Foo {
  String get text => _text;

  set text(String newText) {
    _text = newText;
  }

  List<int> get lineStarts() {
    index = 0;
    if (_lineStarts == null) {
      while (index < text.len<caret>gth) {
        ++index;
      }
    }
    return _lineStarts;
  }
}