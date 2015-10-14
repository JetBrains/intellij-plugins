package {
class From {
    public function movedMethod(p:AuxClass, p2:AuxInterface, p3:AuxClassPub, p4:AuxFunc, p5:AuxConst) {
        notMovedMethod();
        globalMethod();
    }

    private function notMovedMethod():AuxClass {}

    static function stat() {}
}
}
}