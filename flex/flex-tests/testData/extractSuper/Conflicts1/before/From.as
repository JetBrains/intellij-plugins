package {
public class From {
    public function movedMethod(p:AuxClass, p2:AuxInterface, p3:AuxClassPub, p4:AuxFunc, p5:AuxConst) {
        notMovedMethod();
    }

    private function notMovedMethod() {}
}
}