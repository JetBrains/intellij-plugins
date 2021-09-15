// Constructor Tearoffs
// https://github.com/dart-lang/language/blob/master/accepted/future-releases/constructor-tearoffs/feature-specification.md

class C<T> {
  final T x;
  const C.new(this.x); // Same as: `const C(this.x);`
  C.other(T x) : this.new(x); // Same as: `: this(x)`
  factory C.d(T x) = D<T>.new;  // same as: `= D<T>;`
}
class D<T> extends C<T> {
  const D(T x) : super.new(x); // Same as: `: super(x);`
}
void main() {
  const C.new(0); // Same as: `const C(0);`. (Inferred `T` = `int`.)
  const C<num>.new(0); // Same as: `const C<num>(0);`.
  new C.new(0); // Same as `new C(0);`.
  new C<num>.new(0); // Same as `new C<num>(0);`.
  C.new(0); // Same as `C(0);`.
  C<num>.new(0); // Same as `C<num>(0);`.
  var f1 = C.new; // New tear-off, not expressible without `.new`.
  var f2 = C<num>.new; // New tear-off, not expressible without `.new`.
}

var v1 = C.name(args);
var v2 = (C.name)(args);

var v3 = C<typeArgs>.name(args);
var v4 = (C<typeArgs>.name)(args);
var v5 = (C.name)<typeArgs>(args);

class A {
  static List<E> filled$tearoff<E>(int count, E fill) => List<E>.filled(count, fill);
}

typedef IntList = List<int>; // Non-generic alias.
typedef NumList<T extends num> = List<T>; // Generic alias.
typedef MyList<T extends dynamic> = List<T>; // Generic alias *and* "proper rename" of List.

C<typeArgs> A$name$tearoff<typeParams>(params) => C<typeArgs>.name(args);

List<int> IntList$filled$tearoff(int length, int value) =>
    List<int>.filled(length, value);

List<T> NumList$filled$tearoff<T extends num>(int length, T value) =>
    List<T>.filled(length, value);

List<T> MyList$filled$tearoff<T extends dynamic>(int length, T value) =>
    List<T>.filled(length, value);

List<int> Function(int, int) f = NumList.filled;
// equivalent for inference:   = NumList$filled$tearoff;
// Infers type argument <int>: = NumList$filled$tearoff<int>;
// equivalent to writing:      = NumList<int>.filled;

// Equivalent to `List<int>.filled` or `List.filled$tearoff<int>`
var makeIntList = NumList<int>.filled;
// Same as `List<double>.filled` after inference.
List<double> Function(int, double) makeDoubleList = NumList.filled;

typedef Ignore2<T, S> = List<T>;

void foo<X>() {
  List.filled<int>(4, 4);
  var c = Ignore2<int, X>.filled; // Aka. List<int>.filled, but is *not constant*.
}

typedef ListList<T> = List<List<T>>;
// Corresponding factory function
List<List<T>> ListList$filled$tearoff<T>(int length, List<T> value) =>
    List<List<T>>.filled(length, value);
var f = ListList.filled; // Equivalent to `= ListList$filled$tearoff;`

class C<T1 extends num, T2 extends Object?> {}
// Proper rename, bounds are mutual subtypes:
typedef A1<X extends num, Y extends dynamic> = C<X, Y>;
// Not proper rename! Different bound:
typedef A1<X extends num, Y extends num> = C<X, Y>;
// Not proper rename! Different order:
typedef A1<X extends Object?, Y extends num> = C<Y, X>;
// Not proper rename! Different count:
typedef A1<X extends num> = C<X, Object?>;

var f = MyList.filled; // Equivalent to `List.filled` or `List.filled$tearoff`

main() {
// Instantiated type aliases use the aliased type,
// and are constant and canonicalized when the type is constant.
  print(identical(MyList <int>.filled, NumList <int>.filled)); // true
  print(identical(MyList <int>.filled, List <int>.filled)); // true
  print(identical(NumList <int>.filled, List <int>.filled)); // true

// Non-instantiated type aliases have their own generic function.
  print(identical(MyList.filled, MyList.filled)); // true
  print(identical(NumList.filled, NumList.filled)); // true
  print(identical(MyList.filled, NumList.filled)); // false
  print(identical(MyList.filled, List.filled)); // true (proper rename!)

// Implicitly instantiated tear-off.
  List<int> Function(int, int) myList = MyList.filled;
  List<int> Function(int, int) numList = NumList.filled;

// Same as `MyList<int>.filled` vs `NumList<int>.filled`.
  print(identical(myList, numList)); // true

}

class C<T extends Object?> {
  C.name();

  List<T> createList() => <T>[];
}
// Proper rename, different, but equivalent, bound.
typedef A<T extends dynamic> = C<T>;

void main() {
  // Static type : C<T> Function<T extends Object?>()
  // Runtime type: C<T> Function<T extends Object?>()
  var cf = C.name;
  // Static type : C<T> Function<T extends dynamic>()
  // Runtime type: C<T> Function<T extends Object?>()
  var af = A.name;
  var co = (cf as dynamic)();
  var ao = (af as dynamic)();
  // Dynamic instantiate to bounds uses actual bounds.
  print(co.runtimeType); // C<Object?>
  print(ao.runtimeType); // C<Object?>
}

class C {
  final int x;

  const C.new(this.x); // declaration.
}

class D extends C {
  D(int x) : super.new(x * 2); // super constructor reference.
}

void main() {
  D.new(1); // normal invocation.
  const C.new(1); // const invocation.
  new C.new(1); // explicit new invocation.
  var f = C.new; // tear-off.
  f(1);
}

T id<T>(T value) => value;
int Function(int) idInt = id; // Implicitly instantiated tear-off.
// and
typedef IntList = List<int>;

Type intList = IntList;

T id<T>(T value) => value;
var idInt = id<int>; // Explicitly instantiated tear-off, saves on function types.
// and
Type intList = List<int>; // In-line instantiated type literal.

class A {
  List<X> m<X>(X x) => [x];
}

extension FunctionApplier on Function {
  void applyAndPrint(List<Object?> positionalArguments) =>
      print(Function.apply(this, positionalArguments, const {}));
}

void main() {
  A()
    ..m<int>.applyAndPrint([2])
  ..m<String>.applyAndPrint(['three']);
}

class Id {
  T call<T>(T value) => value;
}

int Function(int) intId = Id();

int Function(int) intId = Id().call<int>;

var intId = Id()<int>;

ambig() {
  f(a<b, c>(d)); // Existing ambiguity, resolved to a generic method call.
  f(x.a<b, c>[d]); // f((x.a<b, c>)[d]) or f((x.a < b), (c > [d]))
  f(x.a<b, c> - d); // f((x.a<b, c>)-d) or f((x.a < b), (c > -d))

  // There is a number of tokens which very consistently end an expression, and we include all those:
  // ), }, ], ;, :, ,
  // Then we include tokens which we predict will continue a generic instantiation:
  // ( . == !=
}
