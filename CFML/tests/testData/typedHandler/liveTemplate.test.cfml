<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>cftable tag example: how to build a table in coldfusion (HTML Table)</title>
</head>

<body>
<h2 style="color:Crimson">cftable Tag Example: HTML Table</h2>

<cfquery name="qAuthors" datasource="cfbookclub">
 SELECT AuthorID, FirstName, LastName FROM AUTHORS
</cfquery>

<cftable
 query="qAuthors"
    border="yes"
    maxrows="8"
    startrow="1"
    colspacing="1"
    colheaders="yes"
    htmltable="yes"
    headerlines="1"
    >
 <cfcol header="Author ID" align="left" text="#AuthorID#">
 <cfcol header="First Name" align="left" text="#FirstName#">
 <cfcol header="Last Name" align="left" text="#LastName#">
</cftable>
</body>
</html>
cfd<caret>