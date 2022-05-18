final topLevel1 = ()<fold text='{...}' expand='true'>{
}</fold>

final topLevel2 = () =><fold text='...' expand='true'>
null</fold>;

/// Async
final topLevel3 = () async <fold text='{...}' expand='true'>{
}</fold>;

// Fat arrow, async
final topLevel4 = () async =><fold text='...' expand='true'> false ||
    true</fold>;
final topLevel4 = () sync =><fold text='...' expand='true'> false ||
    true</fold>;
final topLevel4 = () async* =><fold text='...' expand='true'> false ||
    true</fold>;

// Fat arrow, async, no white space
final topLevel4 = ()async=><fold text='...' expand='true'>false||
    true</fold>;

/// Classes
class A <fold text='{...}' expand='true'>{
  A()
  <fold text='{...}' expand='true'>{
  }</fold>

  final returnBool = () =><fold text='...' expand='true'> true ||
    false ||
    true</fold>;

  final returnBool1 = () => true || false || true;
}</fold>

abstract class B <fold text='{...}' expand='true'>{
  final fun1 = (x) <fold text='{...}' expand='true'>{
  }</fold>

  fun2();
  final fun3 = () => 1;
}</fold>

/// Anonymous function as parameter
void main() <fold text='{...}' expand='true'>{

  final arr = [1, 2, 3];
  arr.map((element) <fold text='{...}' expand='true'>{
    return element;
  }</fold>);

  arr.map((element) =><fold text='...' expand='true'>
    element</fold>;
  );
}</fold>

// examples from https://pub.dev/packages/test/
void main() <fold text='{...}' expand='true'>{

  group("String", () <fold text='{...}' expand='true'>{
    test(".split() splits the string on the delimiter", () <fold text='{...}' expand='true'>{
      var string = "foo,bar,baz";
      expect(string.split(","), equals(["foo", "bar", "baz"]));
    }</fold>);

     test(".trim() removes surrounding whitespace", () <fold text='{...}' expand='true'>{
      var string = "  foo ";
      expect(string.trim(), equals("foo"));
    }</fold>, retry: 3);
     test(".trim() removes surrounding whitespace", () <fold text='{...}' expand='true'>{expect(string.trim(), equals("foo"));}</fold>, retry: 3);
  }</fold>, retry: 4);

  group("int", () <fold text='{...}' expand='true'>{
    test(".remainder() returns the remainder of division", () <fold text='{...}' expand='true'>{
      expect(11.remainder(3), equals(2));
    }</fold>);

    test(".toRadixString() returns a hex string", () =><fold text='...' expand='true'>
      expect(11.toRadixString(16), equals("b"))</fold>
    );
  }</fold>);

   // some test methods not in a group:
  test("String.split() splits the string on the delimiter - 2", () <fold text='{...}' expand='true'>{
    var string = "foo,bar,baz";
    expect(string.split(","), equals(["foo", "bar", "baz"]));
  }</fold>);

  test("String.trim() removes surrounding whitespace - 2", () => expect(string.trim(), equals("foo")));

}</fold>