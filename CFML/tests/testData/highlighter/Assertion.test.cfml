<cfcomponent>
  <cfscript>
    variables.eventDateTimeCEName = <weak_warning descr="Can't resolve">getEventDateTimeCEName</weak_warning>();
    variables.eventDateTimeViewName = <weak_warning descr="Can't resolve">getCEViewName</weak_warning>(variables.eventDateTimeCEName);
  </cfscript>
  <cfset ApplicationMetadata2 = <weak_warning descr="Can't resolve">GetApplicationMetadata2</weak_warning>() >
</cfcomponent>