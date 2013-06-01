library foo.bar.baz;

abstract class Foo {
  Element operator[](Node node);
}

hide(Element e) {
    e.style.visibility = 'hidden';
}

show(Element e) {
  e.style.visibility = 'visible';
}

main() {
  Element hide = null;
  show(hide);

  for (Element export in exportedElements) {
    print(export);
  }
}