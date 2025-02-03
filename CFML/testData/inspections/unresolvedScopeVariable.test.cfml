<cfset test=structKeyExists(form, "blah")> <!--- form should be considered defined --->
<cfset url.test="1">
<cfset variables.test2="2">
<cfset form.test3="3">
<cfset request.atest4=12>
<cfoutput>#<weak_warning descr="Can't resolve">atest4</weak_warning>#</cfoutput>