<!--- create a dummy query using queryNew --->
<cfset news = queryNew("id,title", "integer,varchar")>
<cfset queryAddRow(news)>
<cfset querySetCell(news, "id", "1")>
<cfset querySetCell(news, "title", "Dewey defeats Truman")>
<cfset queryAddRow(news)>
<cfset querySetCell(news, "id", "2")>
<cfset querySetCell(news, "title", "Men walk on Moon")>
<cfset writeDump(news)>

<!--- run QofQ (query of query) --->
<cfquery name="sortedNews" dbtype="query">
    SELECT id, title FROM news
    <cfqueryparam name="some other param here">
    ORDE<caret>R BY title DESC
</cfquery>
<cfset writeDump(sortedNews)>