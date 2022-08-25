package {
public class SuperClasses extends Base {
  override function foo() {}
  public override function zz() {}
}
}

<structure activeFilters="HIDE_INHERITED, HIDE_INHERITED_FROM_OBJECT" activeGroupers="SHOW_CLASSES">
    <node text="SuperClasses.as">
        <node text="SuperClasses" icon="AS_CLASS_ICON">
          <node text="Super" icon="(GROUP)">
            <node text="foo():*" icon="Method"/>
          </node>
          <node text="IFoo" icon="(GROUP)">
            <node text="zz():*" icon="Method"/>
          </node>
        </node>
    </node>
</structure>
