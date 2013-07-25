<cffunction name="myFun<caret>ction">
<cfif true>
    <cfinvoke method="myFunction">
<cfelse>
    <cfscript>
        myFunction();
    </cfscript>
</cfif>

</cffunction>

<cfinvoke method="myFunction">
<cfscript>
myFunction();
</cfscript>