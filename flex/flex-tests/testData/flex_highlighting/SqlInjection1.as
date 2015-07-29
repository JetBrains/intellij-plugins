class SqlInjection1 {
    import flash.data.*;

    public native function set text(value:String);

    public function f(statement, st, p) {
        var d = new SQLStatement();
        d.text = "SELECT * FROM<error> </error>";
        var a = d;
        a.text = "SELECT * FROM<error> </error>";
        var c = new SqlInjection1();
        c.text = "SELECT * FROM";
        statement.text = "SELECT * FROM<error> </error>";
        st.text = "SELECT * FROM<error> </error>";
        p.text = "SELECT * FROM ";
    }
}