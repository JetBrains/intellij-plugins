package {
public class Foo {
    public function Fo<caret>o() {
    }
}
}

class Bar extends Foo {
    public function Bar() {
        super();
    }
}

class Zzz extends Foo {
    public function Zzz() {
        super();
    }
}
