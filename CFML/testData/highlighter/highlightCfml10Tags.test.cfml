<script type="text/javascript">
  function mymessagehandler(aevent, atoken)
  {
    var message = ColdFusion.JSON.encode(atoken);
    var txt=document.getElementById("myDiv");
    txt.innerHTML +=message  +"<br>";
  }
</script>
<cfexchangefolder action="getInfo" name="result2">
<cfexchangefolder action="getInfo" name="result">
<cfexchangefolder action="delete" connection="conn1" uid="#result2#" deletetype="harddelete">
<cfexchangeconversation action="get" folderid="#result#" name="conversations" connection="conn1">
  <cfexchangefilter name="topic" value="testcfexchnage3">
  <cfexchangefilter name="categories" value="Yellow Category">
</cfexchangeconversation>
<cfwebsocket name="mycfwebsocketobject"  onmessage="mymessagehandler" subscribeto="stocks" >
<cfdiv id="myDiv"></cfdiv>

<cfscript>
  array2 = [1, 2, 3, 4, 5, 6, 7, 8];
  newArray = arraySlice(array2, 2, 3);//returns 2,3,4
  newArray = arraySlice(array2, 4);//returns 4,5,6, 7, 8
  newArray = arraySlice(array2, -5, 3);//returns 4,5,6

  cityArray = ["San Jose","New york","Boston", "Las Vegas"];

  function printArrayCity(city, index)
  {
    writeOutput("<br>" & city & "   is at index " &  index);
  }

  ArrayEach(cityArray ,printArrayCity);
</cfscript>

<cfscript>
  callStackGet();
  callStackDump();

  names = ["Ray","Adam","Scott","Todd","Dave"];

  shortNames = arrayFilter(names, function (n) {
    return len(n) < 4;
  });
    writeDump(shortNames);
    writeDump(ArrayFindAll(["STRING","string"], "string"));
    writeDump(ArrayFindAllNoCase(["STRING","string"], "string"));
    writeDump(getSystemFreeMemory());
    writeDump(getSystemTotalMemory());

    sessionInvalidate();
</cfscript>

<cftry>
  <cfset totalRAMSpace = getTotalSpace("ram:")>
  <cfset freeRAMSpace = getFreeSpace("ram:")>
  <cfset totalDiskSpace = getTotalSpace("c:")>
  <cfset freeDiskSpace = getFreeSpace("c:")>
  <cfoutput>
    Total Hard Disk Space = #DecimalFormat(totalDiskSpace / (1024 * 1024 * 1024))# GB
  <br>Free Hard Disk Space = #DecimalFormat(freeDiskSpace / (1024 * 1024 * 1024))# GB
  <br>Total Application RAM Memory = #DecimalFormat(totalRAMSpace / (1024 * 1024))# MB
  <br>Free Application RAM Memory = #DecimalFormat(freeRAMSpace / (1024 * 1024))# MB
  <br>
    #canonicalize("%26lt; %26lt; %2526lt%253B %2526lt%253B %2526lt%253B",true,true, true)#
  </cfoutput>

  <cfcatch type="any">
    <cfoutput>
      #cfcatch.message#
      <br>#cfcatch.detail#
      <br>
    </cfoutput>
  </cfcatch>
</cftry>
<h2>HMAC Test</h2>
<cfset x=hmac("Hi There","key1","HMACRIPEMD160")>
<cfoutput>#x#</cfoutput>

<h1>ImageCreateCaptcha Method</h1>
<cfset funcimg1 = ImageCreateCaptcha(35,400,"loner")>
<cfimage action="writetoBrowser" source="#funcimg1#">
<cfset funcimg2 = ImageCreateCaptcha(35,400,"loner","high")>
<cfimage action="writetoBrowser" source="#funcimg2#">
<cfset funcimg3 = ImageCreateCaptcha(35,400,"loner","high","serif,sansserif", "24")>
<cfimage action="writetoBrowser" source="#funcimg3#">

<cfset myImage=ImageNew("",200,110)>
<!--- Set the drawing color to green. --->
<cfset ImageSetDrawingColor(myImage,"green")>
<!--- Turn on antialiasing to improve image quality. --->
<cfset ImageSetAntialiasing(myImage,"on")>
<!--- Draw a filled green oval on the image. --->
<cfset ImageDrawOval(myImage,5,5,190,100,"yes")>
<!--- Display the image in a browser. --->
<cfoutput>PNG image<br></cfoutput>
<cfset ImageWrite(myImage,"#expandpath('.')#/png.png")>
<cfset myImage = ImageRead("#expandpath('.')#/png.png")>
<cfimage source="#myImage#" action="writeToBrowser" >
<cfset x =ImageMakeColorTransparent(myImage,"green")>
<cfimage source="#x#" action="writeToBrowser" >
<cfset x =ImageMakeTranslucent(myImage,100)>
<cfimage source="#x#" action="writeToBrowser" >

<cfscript>
  obj = createObject("webservice","http://somedomain/test.cfc?wsdl");
//Invoke foo
  result = invoke(obj,"foo",{arg1="ColdFusion"});

//Invoke bar with two arguments, passing the second argument as null.
  result = invoke(obj, "bar", {arg1="ColdFusion", arg2=javacast("null", "")});

  adder = function(x,y){
  return x + y;
};

  isClosure(adder);
  getApplicationMetadata();
  FilegetMimeType(expandPath("/folder1/test.pdf"));
  getCPUUsage();

  myList = "one,two,three,four,five,one,five,three";
  newList = listremoveduplicates(myList);

  EmpObjs = EntityLoad("Employee",{lastname="Bond"});
  for(EmpObj in EmpObjs)
  {
    ormindex(EmpObj);
  }

  ORMIndexPurge("Employee");

  objs = ORMSearch("datecheck:[#dateformat(dateadd("d",5,now()),"yyyymmdd")# TO #dateformat(dateadd("d",35,now()),"yyyymmdd")#]","C2",[],{maxresults=2});
  ORMSearchOffline("ch*","Employee",["FirstName","LastName"],["FirstName"],{sort="salary",maxresults=5,offset=2});

  SessionRotate();
</cfscript>

<cfset metaData = sessionGetMetaData() >

<cfset todayDateTime = Now()>
<cfoutput>
  <ul>
  <li>#LSDateTimeFormat(todayDateTime, "yyyy.MM.dd G 'at' HH:nn:ss z")#
    <li>#LSDateTimeFormat(todayDateTime, "yyMMddHHnnssZ", "English (UK)", "GMT")#
    <li>#DateTimeFormat(todayDateTime, "yyyy.MM.dd G 'at' HH:nn:ss z")#
    <li>#DateTimeFormat(todayDateTime, "yyMMddHHnnssZ", "English (UK)")#
  </ul>
</cfoutput>

<cfset obj2 = structNew()>
<cfset obj2.name = "xyz">
<cfoutput>Starting to write to cache..</cfoutput>
<cfset cachePut("obj2",obj2)>
<br/>
<cfoutput>Done!!</cfoutput>

<cfoutput>Trying to fetch cached item...</cfoutput>
<cfset obj = cacheGet("obj2")>
<cfoutput>#cacheIdExists("obj2","OBJECT")#</cfoutput>

<!--- Defining properties for the struct --->
<cfset defaultCacheProps = StructNew()>
<cfset defaultCacheProps.CLEARONFLUSH = "true">
<cfset defaultCacheProps.DISKEXPIRYTHREADINTERVALSECONDS = "3600">
<cfset defaultCacheProps.DISKPERSISTENT = "false">
<cfset defaultCacheProps.DISKSPOOLBUFFERSIZEMB = "30">
<cfset defaultCacheProps.ETERNAL = "false">
<cfset defaultCacheProps.MAXELEMENTSINMEMORY = "5">
<cfset defaultCacheProps.MAXELEMENTSONDISK = "10">
<cfset defaultCacheProps.MEMORYEVICTIONPOLICY = "LRU">
<cfset defaultCacheProps.OBJECTTYPE = "OBJECT">
<cfset defaultCacheProps.OVERFLOWTODISK = "true">
<cfset defaultCacheProps.TIMETOLIVESECONDS = "5">
<cfset defaultCacheProps.TIMETOIDLESECONDS = "30">
<cfset cacheRegionNew("testregion",#defaultCacheProps#,false)>

<cfif #cacheRegionExists("testregion")# EQ "YES">
  <cfset cacheRegionRemove("testregion")>
  <cfif #cacheRegionExists("testregion")# EQ "NO">
    <cfoutput>Region is deleted<br></cfoutput>
  </cfif>
<cfelse>
  <cfset cacheRegionNew("testregion")>
  <cfset cacheRegionRemove("testregion")>
  <cfif #cacheRegionExists("testregion")# EQ "NO">
    <cfoutput>Region is deleted<br></cfoutput>
  </cfif>
</cfif>

<cfset csrfToken=CSRFGenerateToken() />
<cfform method="post" action="sayHello.cfm">
  <cfinput name="userName" type="text" >
  <cfinput name="token" value="#csrfToken#" type="hidden" >
  <cfinput name="submit"  value="Say Hello!!" type="submit" >
</cfform>

<cfset validate = CSRFverifyToken(csrfToken)>
<cfoutput >#validate#</cfoutput>
<cfoutput >#DecodeForHTML("encoded user name")#</cfoutput>

<cfset string2 = "http://www.adobe.com/">
<cfset urlencoded = encodeforURL(string2)>
<cfset urldecoded = decodefromUrl(urlEncoded)>
<cfoutput>
  String: #string2# <br/>
URL Encoded: #urlencoded#<br/>
URL Decoded: #urldecoded#<br/>
</cfoutput>
<cfoutput >#encodeForHTML("form.string")#</cfoutput>
<cfoutput >#encodeForXML("form.string")#</cfoutput>

<style>
  .myDiv
  {
    background-color : #encodeForCSS(form.bgcolor)#;
    /* Encode the input to avoid any malicious code execution.*/
  }
</style>

<table width="#encodeForHTMLAttribute('form.width')#" border="1" bgcolor="RED">
  <tr>
    <td>
      Enter the value in the below text field.
    </td>
  </tr>
</table>

<cfoutput >
  <script type="text/javascript">
      alert('Hello #encodeForJavascript('form.userName')# !!!');
// For security purpose, encode the user-generated input, so that it does not execute malicious codes.
  </script>
</cfoutput>

<cfoutput>
  #reescape("*.{}[]exam?ple")#
</cfoutput>

<cfset RestInitApplication("C:/ColdFusion10/cfusion/wwwroot/restexample/", "restexample")>
<cfset RestDeleteApplication("C:/ColdFusion10/cfusion/wwwroot/restexample/")>

<cfset sql = "SELECT * from art where artid = ?">
<cfquery name="q" datasource="cfartgallery" cachedwithin="#CreateTimeSpan(0, 6, 0, 0)#">
  SELECT * from art where artid = <cfqueryPARAM value = "1" CFSQLType = 'CF_SQL_INTEGER'>
</cfquery>
<cfset a = arrayNew(1)>
<cfset a[1] = 1>
<cfset removeCachedQuery(sql,"cfartgallery", a)>

<cfset var response={
  status=201,
  content="<customer id="&a&"><name>"&a&"</name></customer>",
headers={
location="http://localhost:8500/rest/CustomerService/customers/123"
}
}>
<cfset restSetResponse(response)>

<cfscript>
  if(isdefined("form.publish"))
    WsPublish(#"form.channel"#, #"form.message"#);
</cfscript>
<cfform method="post">
  <cfselect name="channel">
    <option>
      stocks
    </option>
    <option>
      news
    </option>
    <option>
      products
    </option>
  </cfselect>
  Message:
  <input id="message" name="message" type="text">
  <cfinput id="publish" name="publish" value="publish" type="submit">
</cfform>