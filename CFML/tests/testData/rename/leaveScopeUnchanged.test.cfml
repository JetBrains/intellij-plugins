<cfcomponent name="myComponent">
    <cfset cookie.my<caret>Variable = 10>
    <cffunction name="myFunction">
        <cfset cookie.myVariable2 = cookie.myVariable + myVariable + 10>
    </cffunction>
</cfcomponent>