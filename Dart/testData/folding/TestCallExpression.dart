import "package:test/test.dart";

 // example from https://pub.dev/packages/test/
void main() <fold text='{...}' expand='true'>{
  group("String", <fold text='...' expand='true'>() {
    test(".split() splits the string on the delimiter", <fold text='...' expand='true'>() {
      var string = "foo,bar,baz";
      expect(string.split(","), equals(["foo", "bar", "baz"]));
    }</fold>);

     test(".trim() removes surrounding whitespace", <fold text='...' expand='true'>() {
      var string = "  foo ";
      expect(string.trim(), equals("foo"));
    }</fold>, retry: 3);
  }</fold>, retry: 4);

   group("int", <fold text='...' expand='true'>() {
    test(".remainder() returns the remainder of division", <fold text='...' expand='true'>() {
      expect(11.remainder(3), equals(2));
    }</fold>);

     test(".toRadixString() returns a hex string", <fold text='...' expand='true'>() {
      expect(11.toRadixString(16), equals("b"));
    }</fold>);
  }</fold>);

   // some test methods not in a group:
  test("String.split() splits the string on the delimiter - 2", <fold text='...' expand='true'>() {
    var string = "foo,bar,baz";
    expect(string.split(","), equals(["foo", "bar", "baz"]));
  }</fold>);

   test("String.trim() removes surrounding whitespace - 2", <fold text='...' expand='true'>() {
    var string = "  foo ";
    expect(string.trim(), equals("foo"));
  }</fold>);

   // test method that is a single line, not folded
  test("one-line test", () { expect("  foo ".trim, equals("foo")); });

   // Some malformed, and thus not-folded, examples:
  group("String", 2, () {
    test(() {
      var string = "  foo ";
      expect(string.trim(), equals("foo"));
    });
  });

   group(() {
    test(".toRadixString() returns a hex string", () {
      expect(11.toRadixString(16), equals("b"));
    }, 4);
  });

 }</fold>
