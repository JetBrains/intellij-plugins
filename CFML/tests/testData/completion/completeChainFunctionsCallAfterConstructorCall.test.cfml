<cfscript>
    obj1=new MyComponentToResolve();
    obj1.foo();
    obj2=createObject('component', 'MyComponentToResolve').foo();
    obj3=createObject('component', 'MyComponentToResolve').init().foo();
    obj2=new MyComponentToResolve().<caret>;

</cfscript>
