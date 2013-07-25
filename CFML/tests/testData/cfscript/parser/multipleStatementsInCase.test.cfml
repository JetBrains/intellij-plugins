<cfscript>
  var a = 0;
  switch(a) {
    case 'A':
    case 'B':
      cmd = 0;
      cmd = 1;
      break;
    case 'C':
      if(true)
          cmd = 0;
      else
          cmd = 1;
      cmd++;
    default:
      a = 0;
  }
  a = 1;
</cfscript>
