public class Inherited2 extends Inherited2Base {
    function foo() {}

    override function fooBase1() {}

    protected var field : String;

    protected override function get prop() : String { return "fooo"; }

}
<structure activeProviders="SHOW_INHERITED">
    <node text="Inherited2.as">
        <node text="Inherited2" icon="AS_CLASS_ICON">
            <node text="foo():*" icon="METHOD_ICON"/>
            <node text="fooBase1():*" icon="METHOD_ICON"/>
            <node text="field:String" icon="FIELD_ICON"/>
            <node text="prop:String" icon="PROPERTY_READ_ICON"/>
            <node text="fooBase2():*" icon="METHOD_ICON" inherited="true"/>
            <node text="fieldBase:String" icon="FIELD_ICON" inherited="true"/>
        </node>
    </node>
</structure>

<structure activeProviders="SHOW_INHERITED" activeFilters="SHOW_FIELDS">
    <node text="Inherited2.as">
        <node text="Inherited2" icon="AS_CLASS_ICON">
            <node text="foo():*" icon="METHOD_ICON"/>
            <node text="fooBase1():*" icon="METHOD_ICON"/>
            <node text="prop:String" icon="PROPERTY_READ_ICON"/>
            <node text="fooBase2():*" icon="METHOD_ICON" inherited="true"/>
        </node>
    </node>
</structure>

<structure activeProviders="SHOW_INHERITED" activeGroupers="SHOW_CLASSES">
    <node text="Inherited2.as">
        <node text="Inherited2" icon="AS_CLASS_ICON">
            <node text="Inherited2Base" icon="(GROUP)">
                <node text="fooBase1():*" icon="METHOD_ICON"/>
                <node text="prop:String" icon="PROPERTY_READ_ICON"/>
                <node text="fooBase2():*" icon="METHOD_ICON" inherited="true"/>
                <node text="fieldBase:String" icon="FIELD_ICON" inherited="true"/>
            </node>
            <node text="foo():*" icon="METHOD_ICON"/>
            <node text="field:String" icon="FIELD_ICON"/>
        </node>
    </node>
</structure>

<structure activeProviders="SHOW_INHERITED" activeGroupers="SHOW_CLASSES" activeFilters="SHOW_FIELDS">
    <node text="Inherited2.as">
        <node text="Inherited2" icon="AS_CLASS_ICON">
            <node text="Inherited2Base" icon="(GROUP)">
                <node text="fooBase1():*" icon="METHOD_ICON"/>
                <node text="prop:String" icon="PROPERTY_READ_ICON"/>
                <node text="fooBase2():*" icon="METHOD_ICON" inherited="true"/>
            </node>
            <node text="foo():*" icon="METHOD_ICON"/>
        </node>
    </node>
</structure>

<structure activeFilters="HIDE_INHERITED">
    <node text="Inherited2.as">
        <node text="Inherited2" icon="AS_CLASS_ICON">
            <node text="foo():*" icon="METHOD_ICON"/>
            <node text="fooBase1():*" icon="METHOD_ICON"/>
            <node text="field:String" icon="FIELD_ICON"/>
            <node text="prop:String" icon="PROPERTY_READ_ICON"/>
        </node>
    </node>
</structure>

<structure activeFilters="HIDE_INHERITED,SHOW_FIELDS">
    <node text="Inherited2.as">
        <node text="Inherited2" icon="AS_CLASS_ICON">
            <node text="foo():*" icon="METHOD_ICON"/>
            <node text="fooBase1():*" icon="METHOD_ICON"/>
            <node text="prop:String" icon="PROPERTY_READ_ICON"/>
        </node>
    </node>
</structure>

<structure activeFilters="HIDE_INHERITED" activeGroupers="SHOW_CLASSES">
    <node text="Inherited2.as">
        <node text="Inherited2" icon="AS_CLASS_ICON">
            <node text="Inherited2Base" icon="(GROUP)">
                <node text="fooBase1():*" icon="METHOD_ICON"/>
                <node text="prop:String" icon="PROPERTY_READ_ICON"/>
            </node>
            <node text="foo():*" icon="METHOD_ICON"/>
            <node text="field:String" icon="FIELD_ICON"/>
        </node>
    </node>
</structure>

<structure activeFilters="HIDE_INHERITED,SHOW_FIELDS" activeGroupers="SHOW_CLASSES">
    <node text="Inherited2.as">
        <node text="Inherited2" icon="AS_CLASS_ICON">
            <node text="Inherited2Base" icon="(GROUP)">
                <node text="fooBase1():*" icon="METHOD_ICON"/>
                <node text="prop:String" icon="PROPERTY_READ_ICON"/>
            </node>
            <node text="foo():*" icon="METHOD_ICON"/>
        </node>
    </node>
</structure>
