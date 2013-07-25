<cfcomponent name="myComponent">
    <cffunction name="myFunction">
        <cfargument name="arg">
        <cfset v = arguments.arg + arg>
        <cfscript>
            v = arguments.a<caret>rg + arg;
        </cfscript>
    </cffunction>
</cfcomponent>