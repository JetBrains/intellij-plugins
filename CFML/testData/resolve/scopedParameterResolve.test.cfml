<cfset arg = 10>
<cffunction name="foo">
<cfargument name="arg">
<cfset a = arguments.ar<caret>g + 5>
</cffunction>