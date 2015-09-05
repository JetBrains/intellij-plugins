package {
public class Foo {
    public function Fo<caret>o(i:int) {
    }
}
}

class Bar extends Foo {
    public function Bar() {
        super(0);
    }
}

class Zzz extends Foo {
    public function Zzz() {
        super(5);
    }
}

class Zzz2 extends Zzz {
    public function Zzz2() {
        super();
    }
}