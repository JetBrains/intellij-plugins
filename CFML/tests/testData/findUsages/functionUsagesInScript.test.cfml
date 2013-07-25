<cfscript>
     fo<caret>o(1,2,3);
     function foo( a, b, c )
    {
        a+=12;
        b++;
        writeOutput(a);

    }
    foo(2,3,4);
    a = foo(1,2,2);

</cfscript>