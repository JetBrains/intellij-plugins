 <?php
 function bar($a) {
   baz($a);
 }
 function baz($b) {
   foo($b);
 }
 function foo($param) {
   echo $param;
 }
 bar($_POST['anyKey']);
 bar('safeString');

 function foo1($param) {
   bar($param);
   baz($param);
   foo($param);
 }
 foo1($_POST['anyKey']);