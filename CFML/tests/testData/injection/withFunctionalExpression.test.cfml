<cfquery datasource="test">
    <cfinclude template="test.cfm"/>
    SELECT * <!---Common injection--->
    FROM Users u <!---Common injection--->
    WHERE u.UserID = #createODBCDate(now())# <!---Common injection check for Functional Expression escape here (IDEA-205092) --->
</cfquery>