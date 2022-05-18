main() <fold text='{...}' expand='true'>{
  // For
  for (final i in ['a', 'b']) { singleLine(); }
  for (final i in ['a', 'b']) <fold text='{...}' expand='true'>{
    final x = i;
  }</fold>

  for (var i = 0; i <= 2; i++) <fold text='{...}' expand='true'>{
    final x = i;
  }</fold>

  /// While
  while(false) <fold text='{...}' expand='true'>{
    final x = 'x';
  }</fold>

  /// Do while
  do <fold text='{...}' expand='true'>{
    final x = 'x';
  }</fold> while(false);
}</fold>
