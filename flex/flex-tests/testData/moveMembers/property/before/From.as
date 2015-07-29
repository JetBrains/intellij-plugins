package {
public class From {

    private static function abc() {
        write = read.toString();
        readwrite = readwrite;
    }

    public static function get read():int {
        write = "test";
        readwrite = readwrite;
        return 0;
    }

    public static function set write(p:String) {
        var v = read;
        readwrite = readwrite;
    }

    internal static function get readwrite():From {
        return From(read);
    }

    internal static function set readwrite(p:From) {
        write = p;
    }

}
}