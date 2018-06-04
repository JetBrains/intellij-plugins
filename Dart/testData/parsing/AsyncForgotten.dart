foo() {
  yield foo.bar();
  if (false) {
    await foo.bar.baz();
  }
}
