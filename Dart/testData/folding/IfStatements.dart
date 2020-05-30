int f(int value) <fold text='{...}' expand='true'>{
  if (value == 1) <fold text='{...}' expand='true'>{
    return 1;
  }</fold>
  else if (value == 2) <fold text='{...}' expand='true'>{
    return 2;
  }</fold> else <fold text='{...}' expand='true'>{
    return 3;
  }</fold>
}</fold>

int g(int value) <fold text='{...}' expand='true'>{
  if (value == 1) return 1;
  else if (value == 2) return 2;
  else return 3;
}</fold>