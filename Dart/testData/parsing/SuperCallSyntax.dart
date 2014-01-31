// Test super calls.
class SuperCallSyntax {
  method() {
    super.foo();
    super.foo(1);
    super.foo(1, 2);

    super.foo().x;
    super.foo()[42];
    super.foo().x++;

    super.foo()();
    super.foo(1, 2)(3, 4);

    var v1 = super.foo();
    var v2 = super.foo(1);
    var v3 = super.foo(1, 2);

    var v4 = super.foo().x;
    var v5 = super.foo()[42];
    var v6 = super.foo().x++;

    var v7 = super.foo()();
    var v8 = super.foo(1, 2)(3, 4);
  }

  get field {
    super.field;
    super.field = 42;
    super.field += 87;

    super['baz'];
    super['baz'] = 42;
    super['baz'] += 87;
  }
}