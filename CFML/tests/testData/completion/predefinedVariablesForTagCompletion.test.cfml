<cfquery
    name="GetParks" datasource="cfdocexamples"
    cachedwithin="#CreateTimeSpan(0, 6, 0, 0)#">
    SELECT PARKNAME, REGION, STATE
    FROM Parks
    ORDER BY ParkName, State
</cfquery>

<cfoutput>
  #GetParks.<caret>#
</cfoutput>


