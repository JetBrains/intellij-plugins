class Implement1 implements IFoo<Bar> {
    <caret>
}

class IFoo<T> {
  T getFoo();
}

class Bar {

}