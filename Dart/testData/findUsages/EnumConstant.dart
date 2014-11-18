enum Foo { B<caret>AR }

main() {
  BAR + foo.BAR + Foo.BAR;
  print(Foo.BAR);
}
