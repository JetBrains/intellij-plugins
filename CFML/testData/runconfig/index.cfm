<html>

<cfajaximport params="#{googlemapkey='YourGoogleMapsAPIKeyHere'}#" />
<cfparam name="URL.overview" default="false" type="boolean" />

<head>
</head>
<body style="padding:20px;">

<cfform method="GET" action="#CGI.SCRIPT_NAME#">
  <cfinput name="overview" type="checkbox" value="1" checked="#URL.overview#" onchange="this.form.submit();" />
  <label for="overview">overview</label>
  <cfinput name="submitBtn" type="submit" value="Submit" />
</cfform>

<cfmap name="map01"
    centerAddress="St.Petersburg, RU"
    overview="#URL.overview#" />

</body>
</html>