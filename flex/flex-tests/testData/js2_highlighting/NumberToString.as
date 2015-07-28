package {
class NumberToString {
    function foo():void {
        var n : Number;
        var s : String;

        s = n.toString();
        s = n.toExponential(1);
        s = n.toFixed(1);
        s = n.toPrecision(1);
    }
}
}