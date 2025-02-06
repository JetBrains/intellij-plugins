<cfscript>
    obj1=new MyComponentToResolve();
    obj1.foo();
    obj2=createObject('component', 'MyComponentToResolve').foo();
    obj3=createObject('component', 'MyComponentToResolve').init().fo<caret>o();
    obj2=new MyComponentToResolve().foo();

</cfscript>
