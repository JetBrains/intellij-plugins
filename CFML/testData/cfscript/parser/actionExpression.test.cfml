<cfscript>
    function foo( )
    {
        lock scope="Application" type="exclusive" timeout="10"
        {
            //asd
        }
        TRANSACTION = "12";
        writeOutput(TRANSACTION);
        TRANSACTION action="begin" {
            //   transaction code
        }
    }
</cfscript>