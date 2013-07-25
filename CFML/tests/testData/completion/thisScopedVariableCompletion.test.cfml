<cfcomponent output="no">
<cfscript>
    variables.foo = 1;
    this.bar = 1;
</cfscript>
<cffunction name="myMethod" returnType="void">
    <cfscript>
        variables.foo = 12;
        this.b<caret>
    </cfscript>
</cffunction>
</cfcomponent>
