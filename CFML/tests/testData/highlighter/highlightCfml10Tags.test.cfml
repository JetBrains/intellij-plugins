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
  names = ["Ray","Adam","Scott","Todd","Dave"];
  function filter(n) {
    return len(n) < 4;
  }
  shortNames = arrayFilter(names, filter);
    writeDump(shortNames);
    writeDump(ArrayFindAll(["STRING","string"], "string"));
    writeDump(ArrayFindAllNoCase(["STRING","string"], "string"));
</cfscript>
