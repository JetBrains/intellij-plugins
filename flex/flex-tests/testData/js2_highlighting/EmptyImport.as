package {

<warning descr="Unused import">import com.bar.A;</warning>

public class EmptyImport {

    import com.bar.A;
    import<EOLError descr="* or type name expected"></EOLError>

    public function foo(): <warning descr="Qualified name may be replaced with import statement">com.bar.A</warning> {
        return null;
    }

}
}