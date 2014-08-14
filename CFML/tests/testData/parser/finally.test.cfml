<cfscript>
  try{
    throw "message";
  }catch(Any e){
    rethrow;
  }finally{
  }
</cfscript>
