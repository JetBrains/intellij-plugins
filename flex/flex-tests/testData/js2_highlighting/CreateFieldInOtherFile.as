package {
public class CreateFieldInOtherFile {
    function foo():void {
        var aa:CreateFieldInOtherFile_other;
        aa.<error>zzz</error> = 5;
    }
}
}