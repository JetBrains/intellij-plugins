package {
class From implements IFrom {
    var v = 0;

    public function foo(p:From, p2:IFrom) {
        p.v = 0;
    }

}
}
