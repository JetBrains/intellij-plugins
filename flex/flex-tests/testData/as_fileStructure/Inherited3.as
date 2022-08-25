package {
public class Inherited3 extends Impl {
    function fooMine() {}

    override function foo1Ex() {}
}
}
<structure>
    <node text="Inherited3.as">
        <node text="Inherited3" icon="AS_CLASS_ICON">
            <node text="fooMine():*" icon="Method"/>
            <node text="foo1Ex():*" icon="Method"/>
            <node text="foo1():*" icon="Method" inherited="true"/>
            <node text="foo2():*" icon="Method" inherited="true"/>
            <node text="fooImpl():*" icon="Method" inherited="true"/>
        </node>
    </node>
</structure>

<structure activeGroupers="SHOW_CLASSES">
    <node text="Inherited3.as">
        <node text="Inherited3" icon="AS_CLASS_ICON">
            <node text="Int1Ex" icon="(GROUP)">
                <node text="foo1Ex():*" icon="Method"/>
            </node>
            <node text="Int1" icon="(GROUP)">
                <node text="foo1():*" icon="Method" inherited="true"/>
            </node>
            <node text="Int2" icon="(GROUP)">
                <node text="foo2():*" icon="Method" inherited="true"/>
            </node>
            <node text="Impl" icon="(GROUP)">
                <node text="fooImpl():*" icon="Method" inherited="true"/>
            </node>
            <node text="fooMine():*" icon="Method"/>
        </node>
    </node>
</structure>

<structure activeFilters="HIDE_INHERITED">
    <node text="Inherited3.as">
        <node text="Inherited3" icon="AS_CLASS_ICON">
            <node text="fooMine():*" icon="Method"/>
            <node text="foo1Ex():*" icon="Method"/>
        </node>
    </node>
</structure>

<structure activeFilters="HIDE_INHERITED" activeGroupers="SHOW_CLASSES">
    <node text="Inherited3.as">
        <node text="Inherited3" icon="AS_CLASS_ICON">
            <node text="Int1Ex" icon="(GROUP)">
                <node text="foo1Ex():*" icon="Method"/>
            </node>
            <node text="fooMine():*" icon="Method"/>
        </node>
    </node>
</structure>
