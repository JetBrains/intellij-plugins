class Implement1 implements IFoo<Bar> {
    <caret>
}

interface IFoo<T> {
    T getFoo();
}

class Bar {

}