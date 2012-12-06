class Foo<T,U> {
}

foo(Foo<Foo, Foo> param) {

}

void main() {
    foo(<caret>);
}