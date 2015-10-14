package {
import com.AA;

public class ImportForNeighbourClass {
    var v : AA;
    var g : com.B;
    var u : com.CCC;
    var z : <error>B</error>;
    var n : <error>CCC</error>;
    var x : bar.<error>C</error>;
}
}
