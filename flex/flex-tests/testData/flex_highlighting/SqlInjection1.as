class SqlInjection1 {
    import flash.data.*;

    public native function set text(value:String);

    public function f(statement, st, p) {
        var d = new SQLStatement();
        d.text = "SELECT * FROM<error> </error>";
        var a = d;
        a.text = "SELECT * FROM<error> </error>";
    }
}