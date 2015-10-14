package {

CONFI<caret expected="CONFIG1::defined1,CONFIG1::defined2,CONFIG2::defined3">G1::  defi<caret expected="defined1,defined2">ned1
public dynamic class ConditionalCompilationConstantsInAs {

CONFI<caret expected="CONFIG1::defined1,CONFIG1::defined2,CONFIG2::defined3">G1  ::defined<caret expected="defined1,defined2">1 {
    var a:Boolean = CONFI<caret expected="CONFIG1::defined1,CONFIG1::defined2,CONFIG2::defined3">G1  ::  defi<caret expected="defined1,defined2">ned1;
    var b:Boolean = UNKNO<caret expected="">WN::defi<caret expected="">ned1;
}

    CONFI<caret expected="CONFIG1::defined1,CONFIG1::defined2,CONFIG2::defined3">G1::
    defi<caret expected="defined1,defined2">ned1
    public function doSomething():void {
        if (CONFI<caret expected="CONFIG1::defined1,CONFIG1::defined2,CONFIG2::defined3">G1::defi<caret expected="defined1,defined2">ned1) {}
        var f = function(){
            CONFI<caret expected="CONFIG1::defined1,CONFIG1::defined2,CONFIG2::defined3">G1::defi<caret expected="defined1,defined2">ned1
        }
    }
}
}