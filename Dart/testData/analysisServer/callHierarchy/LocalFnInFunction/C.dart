foo() {
  bar((q) {
    bar((r) {
      baz();
    });
    baz();
  });
  baz();
}
bar(a) {}
void baz() {}
