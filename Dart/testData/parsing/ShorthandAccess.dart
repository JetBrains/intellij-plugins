void main() {
  HttpClientResponseCompressionState state = .compressed;
  Endian littleEndian = .little;
  Endian hostEndian = .host;
  Endian endian = firstWord == 0xFEFF ? .little : firstWord = 0xFFFE ? .big : .host;
  BigInt b0 = .zero;
  BigInt b1 = b0 + .one;
  String s = .fromCharCode(42);
  List<Endian> l = .filled(10, .big);
  int value = .parse(input);
  Zone zone = .current.errorZone;
  int posNum = .parse(userInput).abs();
  Future<List<int>> futures = .wait([.value(1), .value(2)]);
  Future futures = .wait<int>([.value(1), .value(2)]);
  Future<String> futures = .wait([lazyString(), lazyString()]).then((list) => list.join());
  Symbol symbol = .new<Args>();
  Symbol symbol = .new<Args>().new;
  Symbol symbol = .new<Args>().foo;
  Symbol symbol = .new<Args>().foo();
  Symbol symbol = .new<Args>.new;
  Symbol symbol = .new<Args>.foo;
  Symbol symbol = .new<Args>.foo();
  Symbol symbol = .new;
  Symbol symbol = .new.new;
  Symbol symbol = .new.foo;
  Symbol symbol = .new.foo();
  var list = [1, 2, .new("foo"), 4];
  Symbol s = flag ? .new("yes") : const .no;
  take(Symbol.new("foo"));
  someObject.doSomething(a: .new("bar"));
  take(.new("foo"));
  SomeClass x = .Factory<int, String>(42, "ok");
  SomeClass a = const .new();
  SomeClass b = const .named();
  SomeClass c = const .Value(10);
  SomeClass d = const .Value<String>('ok');
  E x = const .foo;
  E x = const .new;

  switch (e) {
  case .green: break;
  case const .new: break;
  case const .new(): break;
  case const .new("foo"): break;
  case const .named(42): break;
  case const .Option<String>('yes'): break;
  }
}