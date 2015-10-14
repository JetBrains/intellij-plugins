package {

public class PrivateMemberAccessibleWithinFile {

    private var privateVar:int;
    var internalVar1:int;
    internal var internalVar2:int;
    protected var protectedVar:int

    public function foo(param:PrivateMemberAccessibleWithinFile_other):void {
        param.privateVar = 0;
        param.internalVar1 = 0;
        param.internalVar2 = 0;
        param.protectedVar = 0;
        param.privateFun();
        param.internalFun1();
        param.internalFun2();
        param.protectedFun();

    }

    private function privateFun():void {}
    function internalFun1():void {}
    internal function internalFun2():void {}
    protected function protectedFun():void {}
}
}
