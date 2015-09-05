package {
import flash.net.Socket;
import flash.utils.IDataInput;
import flash.utils.IDataOutput;

public class MultinamesInDecompiledSwc {
    private function test(arg1:IDataInput, arg2:IDataOutput):void {
        var socket:Socket = new Socket();
        test(socket, socket);
    }
}
}