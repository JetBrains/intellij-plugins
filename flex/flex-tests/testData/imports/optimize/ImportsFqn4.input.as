package {

    import com.a.*;

    var f : ClassA;

    function foo() {
        import com.b.ClassA;

        var g : ClassA;

        var s : com.a.ClassB;

        var r : com.c.ClassC;
    }

}