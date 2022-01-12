enum Foo { FOO, BAR }
@meta @data enum Foo{}
enum Foo{a}
enum Foo{a , b ,}
enum Foo{a , b ;}
enum Foo{a , b,;}

var foo = Foo.FOO;

main() {
  for (Foo foo in Foo.values) {
    print(f);
  }
}


// 2.17 change, Enhanced Enums:
// https://github.com/dart-lang/language/blob/master/accepted/future-releases/enhanced-enums/feature-specification.md

mixin Mixin1 {}
mixin Mixin2 {}

class Interface1 {}
class Interface2 {}

enum Name<T extends Object?> with Mixin1, Mixin2 implements Interface1, Interface2 {
  id1<int>(args1), id2<String>(args2), id3<bool>(args3);
  const Name();
  const Name.copy(id1, id2, id3) : id1 = id1, id2 = id2, id3 = id3, super();
  const Name.copy2(id1, id2, id3) : id1 = id1, id2 = id2, id3 = id3 {}
}

mixin EnumComparable<T extends Enum> on Enum implements Comparable<T> {
  int compareTo(T other) => this.index - other.index;
}

// With type parameter, mixin and interface.
enum Complex<T extends Pattern> with EnumComparable<Complex> implements Pattern {
  whitespace<RegExp>(r"\s+", RegExp.new),
  alphanum<RegExp>.captured(r"\w+", RegExp.new),
  anychar<Glob>("?", Glob.new),
  ;

  // Static variables. (Could use Expando, this is more likely efficient.)
  static final List<Pattern?> _patterns = List<Pattern?>.filled(3, null);

  // Final instance variables.
  final String _patternSource;
  final T Function(String) _factory;

  // Unnamed constructor. Non-redirecting.
  const Complex(String pattern, T Function(String) factory)
      : _patternSource = pattern,
        _factory = factory;

  // Factory constructor.
  factory Complex.matching(String text) {
    for (var value in values) {
      if (value
          .allMatches(text)
          .isNotEmpty && value is Complex<T>) {
        return value;
      }
    }
    throw UnsupportedError("No pattern matching: $text");
  }

  // Named constructor. Redirecting.
  const Complex.captured(String regexpPattern)
      : this("($regexpPattern)", RegExp);

  // Can expose the implicit name.
  String get name => EnumName(this).name;

  // Instance getter.
  Pattern get pattern => _patterns[this.index] ??= _factory(_patternSource);

  // Instance methods.
  Iterable<Match> allMatches(String input, [int start = 0]) =>
      pattern.allMatches(input, start);

  Match? matchAsPrefix(String input, [int start = 0]) =>
      pattern.matchAsPrefix(input, start);

  // Specifies `toString`.
  String toString() => "Complex<$T>($_patternSource)";
}