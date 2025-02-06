<cfscript><fold text='...' expand='true'>
  if (true) <fold text='{...}' expand='true'>{
  }</fold>

  while (true) <fold text='{...}' expand='true'>{
  }</fold>

  switch(fruit) <fold text='{...}' expand='true'>{
    case "apple":
         WriteOutput("I like Apples");
         break;
    case "orange":
         WriteOutput("I like Oranges");
         break;
    default: 
         WriteOutput("I like fruit");
   }</fold>
</fold></cfscript>