enum Foo { FOO, BAR; }
@meta @data enum Foo{;}
enum Foo{a;}
enum Foo{a , b ;}

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
