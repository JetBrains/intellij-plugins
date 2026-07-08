//test for inspection ReservedWordUsedAsName
package <error descr="Package name 'inspections.ReservedWordUsedAsName' does not correspond to file path ''">inspections.ReservedWordUsedAsName</error> {
public class <error descr="Class 'byte' should be defined in file 'byte.as'"><warning descr="Reserved word 'byte' used as name">byte</warning></error> {

    private var <warning descr="Reserved word 'cast' used as name">cast</warning>:char;

    public function <warning descr="Reserved word 'debugger' used as name">debugger</warning>(<warning descr="Reserved word 'double' used as name">double</warning>:enum = export, ...<warning descr="Reserved word 'float' used as name">float</warning>):intrinsic {
        var <warning descr="Reserved word 'long' used as name">long</warning>:byte = debugger(double);
        float = {<warning descr="Reserved word 'long' used as name">long</warning>: "long",
            "prototype": "prototype"};
        float.short = 5;
        cast = null;
        int(5);
        long = synchronized;
        var f:Function = function <warning descr="Reserved word 'throws' used as name">throws</warning>():void {
            throws()
        }
        var <warning descr="Reserved word 'to' used as name">to</warning>:*;
        var <warning descr="Reserved word 'transient' used as name">transient</warning>:*;
        var <warning descr="Reserved word 'type' used as name">type</warning>:*;
        var <warning descr="Reserved word 'virtual' used as name">virtual</warning>:*;
        var <warning descr="Reserved word 'volatile' used as name">volatile</warning>:*;
        var Transient:*;
        var type1:*;
    }
}
}
