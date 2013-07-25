<cfscript>
    obj1=new MyComponentToResolve();
    obj1.foo();
    obj2=createObject('component', 'MyComponentToResolve').foo();
    obj3=createObject('component', 'MyComponentToResolve').init().<caret>;
    obj2=new MyComponentToResolve().foo();

</cfscript>
