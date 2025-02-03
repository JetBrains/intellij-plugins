<cfscript>
  array2 = [1, 2, 3, 4, 5, 6, 7, 8];
  newArray = arrayMap(array2, function(item, index, arrayp) {});
  arrayReduce(array2, function(result, item) {
    result = result?:0;
    return result + item;
  }, 1);
  deserialize("foo:bar", "json");
  deserializeXml("<foo/>", true);
  serialize(array2, "xml");
  serializeXml(array2);
  InvalidateOauthAccesstoken("any_token_value","facebook");
  IsValidOauthAccesstoken("any_token_value","facebook");

  cityList = "San Jose,New york, Boston, Las Vegas";

  function printCity(String city)
  {
    writeOutput("<br>Current city: " & city);
  }

  ListEach(cityList ,printCity);
  list = "red, blue, green, yellow";
  ucaseList = listMap(list, function(item)
  {
    return ucase(item);
  });


  writeDump(ucaseList);
  writeOutput("<br>");


  concatValues = listReduce(list, function(result, item, index, mylist, delimiter)
  {
    result = result?:"";
    if(index == 1)
      delimiter = "";
    result&= delimiter & item;
    return result;
  });
  writeDump(concatValues);

  person = {fname="John", lname="Doe"};

  ucaseStruct = structMap(person, function(key, value)
  {
    return ucase(value);
  });

  writeDump(ucaseStruct);

  concatValues = structReduce(person, function(result, key, value)
  {
    result = result?:"";
    result&= value & " ";
    return result;
  });


  writeDump(concatValues);
</cfscript>
<cffunction access="remote" name="canDeSerialize" returntype="boolean">

  <cfargument name="type" type="string"/>

<!--- If you need to deserialize only XML using the custom serializer. --->
  <cfif #type# eq "XML">

    <cfreturn true>

  <cfelse>

    <cfreturn false>

  </cfif>

</cffunction>
<cffunction access="remote" name="canSerialize" returntype="boolean">

  <cfargument name="type" type="string"/>

<!--- If you need to serialize only XML using the custom serializer. --->
  <cfif #type# eq "XML">

    <cfreturn true>

  <cfelse>

    <cfreturn false>

  </cfif>

</cffunction>

<!---- Prints ,.-_ ---->
<cfset x=encodeForXMLAttribute(",.-_")>
<cfoutput>#x#</cfoutput>


<cfscript>
  salt="A41n9t0Q";
  password = "Password@123";
  PBKDFalgorithm = "PBKDF2WithSHA224";
  dataToEncrypt= "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua";
  encryptionAlgorithm = "AES";
  derivedKey = GeneratePBKDFKey(PBKDFalgorithm ,password ,salt,4096,128);
  writeOutput("Generated PBKDFKey (Base 64) : " & derivedKey);
  encryptedData = encrypt(dataToEncrypt, derivedKey, encryptionAlgorithm, "BASE64");
  writeoutput("Data After Encryption using PBKDF2: " & encryptedData);
</cfscript>

<cfscript>
  salt="A41n9t0Q";
  password = "Password@123";
  PBKDFalgorithm = "PBKDF2WithSHA224";
  derivedKey = GeneratePBKDFKey(PBKDFalgorithm ,password ,salt,4096,128);
  decryptedData = decrypt(encryptedData, derivedKey, encryptionAlgorithm, "BASE64");
  writeoutput("Data After Decryption using PBKDF2: " & decryptedData);
  inputHtml = "";
</cfscript>
<!---- Prints &#x27;or 1&#x3d;1---->

<cfset x=encodeForXPath("'or 1=1",false)>
<cfoutput>#x#</cfoutput>

<cfset  isSafe = isSafeHTML(inputHTML)>
<cfset  SafeHTML = getSafeHTML(inputHTML, "", true)>
<cfoutput> is Safe   : #isSafe#  Safe HTML : #SafeHTML#  </cfoutput>

<cfimage action="read" name="myImage" source="../../MasterImages/sky.jpg">
<cfset getMetaData = ImageGetMetadata(myImage) >
<cfoutput>
  Image Height : #getMetaData["Image Height"]#<BR>
Image Width  : #getMetaData["Image Width"]#<BR>
Exif Version : #getMetaData["Exif Version"]#<BR>
</cfoutput>

<cfclientsettings enableDeviceAPI=true>
<cfclient>


  <cffunction access="public" name="createfile" returntype="void" >

  </cffunction>


</cfclient>
File name : <input id="filename" type="text"/>

<button onclick="invokeCFClientFunction('createfile',null)">Create a file</button>
<br> <b>Result :</b><div id="result"></div>


<script type="text/javascript">
  function showresult(obj)
  {
    return JSON.stringify(obj);
  }
</script>

<cfset qoptions = {result="myresult", datasource="artGallery",  fetchclientinfo="yes"}>
<cfset myquery = QueryExecute("select *  from art where ARTID < 5", [] ,qoptions)>
<cfdump var="#myQuery#" >
<cfquery name="myQuery" result="myresult" datasource="artGallery" fetchclientinfo="yes" >
     select *  from art where ARTID >
     <cfqueryparam value="2" cfsqltype="CF_SQL_INTEGER">
</cfquery>

<cfdump var="#myQuery#" >

<cfset data = QueryGetRow(myQuery, 1) >

<cfdump var="#data#" >

<cfset colList = "col1,col2,col3,col4,col5,col6,col7,col8,co9,col0">
<cfset rowCount = 100>
<cfset qryObj = QueryNew("#colList#")>
<cfset QueryAddRow(qryObj, #rowCount#)>

<cfloop from="1" to="#rowCount#" index="r">
  <cfloop from="1" to="#ListLen(colList)#" index="c">
    <cfset QuerySetCell(qryObj, #ListGetAt(colList,c)#, "some random text r#r#c#c#", r)>
  </cfloop>
</cfloop>


<cfset xlObj =spreadsheetNew("testsheet", true)>

<cfset spreadsheetAddRows(xlObj, "#qryObj#")>

<cfset SpreadSheetAddAutofilter(xlObj, "E5:J50")>
<cfset SpreadSheetAddPagebreaks(xlObj, "50,90", "4,9")>

<cfset spreadsheetwrite(xlObj, "#Expandpath("./")#test_autofilter.xlsx", true)>

<cfhtmltopdf>
  This is a test <cfoutput>#now()#</cfoutput>
</cfhtmltopdf>

<cfhtmltopdf destination="myPDF.pdf" source="http://somesite.com" overwrite="true">
  <cfhtmltopdfitem type="header">
    Page: _PAGENUMBER of _LASTPAGENUMBER
  </cfhtmltopdfitem>
  <cfhtmltopdfitem type="pagebreak" />
  <cfhtmltopdfitem type="footer" image="test.jpg">
  </cfhtmltopdfitem>
</cfhtmltopdf>

<cf_socialplugin type = "subscribe"
    url = "profile to subscribe"
    width = ""
    colorscheme = "dark|light"
    showfaces = "true|false"
    layout = "standard|button_count|box_count"
    style = ""
    extraoptions = ""
    >

<cfoauth
    type = ""
    clientid = ""
    scope = ""
    state = ""
    authendpoint = ""
    secretkey = ""
    accesstokenendpoint = ""
    result = ""
    redirecturi = ""
    urlparams = ""
    >

<cfimapfilter
    name = "filter type"
    value = "filter value">