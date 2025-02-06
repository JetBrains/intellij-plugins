<cfcomponent>
  <cfscript>
    variables.eventDateTimeCEName = <weak_warning descr="Can't resolve">getEventDateTimeCEName</weak_warning>();
    variables.eventDateTimeViewName = <weak_warning descr="Can't resolve">getCEViewName</weak_warning>(variables.eventDateTimeCEName);
    function foo() {}
  </cfscript>
  <cfset ApplicationMetadata2 = <weak_warning descr="Can't resolve">GetApplicationMetadata2</weak_warning>() >
  <cffunction name="bar">
    <cfscript>
      foo();
    </cfscript>
  </cffunction>
</cfcomponent>