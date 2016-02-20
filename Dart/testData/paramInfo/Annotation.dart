class ExampleAnnotation {
  final String itemA;
  final int itemB;
  const ExampleAnnotation(this.itemA, this.itemB);
}

@ExampleAnnotation(<caret>)
class Foo{}
