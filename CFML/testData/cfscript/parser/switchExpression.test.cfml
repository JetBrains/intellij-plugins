<cfscript>
   switch(arg){
    case 'html':
      f();
      break;
    case 'xml': {
      f();
      g();
      break;
    }
    default:
      g();
      break;
   }
</cfscript>