magic 2e0010
Cpool numbers size 74 0 %
Cpool strings count 170 size 1850 15 %
Cpool namespaces count 43 size 88 0 %
Cpool nssets count 16 size 114 0 %
Cpool names count 339 size 1079 8 %
MethodInfo count 251 size 1734 14 %
InstanceInfo size 556 4 %
ClassInfo size 555 4 %
ScriptInfo size 135 1 %
MethodBodies size 6134 49 %
script0

final class String extends Object
{

  function String(*):*	/* disp_id -1*/
  {
    // local_count=2 max_scope=1 max_stack=1 code_len=6
    0       getlocal0     	
    1       pushscope     	
    2       getlocal0     	
    3       constructsuper	(0)
    5       returnvoid    	
  }

  function http://adobe.com/AS3/2006/builtin::match(*):Array	/* disp_id 16*/
  {
    // local_count=2 max_scope=1 max_stack=3 code_len=12
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	_match
    5       getlocal0     	
    6       getlocal1     	
    7       callproperty  	_match (2)
    11      returnvalue   	
  }

  native function private::_charAt(int):String	/* disp_id 10*/

  native function http://adobe.com/AS3/2006/builtin::indexOf(String,Number):int	/* disp_id 7*/

  native function private::_indexOf(String,int):int	/* disp_id 6*/

  native function private::_substr(int,int):String	/* disp_id 24*/

  native function http://adobe.com/AS3/2006/builtin::substring(Number,Number):String	/* disp_id 23*/

  native function http://adobe.com/AS3/2006/builtin::slice(Number,Number):String	/* disp_id 20*/

  native function http://adobe.com/AS3/2006/builtin::lastIndexOf(String,Number):int	/* disp_id 9*/

  native function private::_lastIndexOf(String,int):int	/* disp_id 8*/

  native function private::_charCodeAt(int):Number	/* disp_id 12*/

  native function http://adobe.com/AS3/2006/builtin::toLowerCase():String	/* disp_id 26*/

  native function private::_substring(int,int):String	/* disp_id 22*/

  function http://adobe.com/AS3/2006/builtin::split(*,*):Array	/* disp_id 21*/
  {
    // local_count=3 max_scope=1 max_stack=4 code_len=25
    0       getlocal0     	
    1       pushscope     	
    2       getlocal2     	
    3       getglobalscope	
    4       getslot       	17
    6       ifne          	L1

    10      pushdouble    	NaN
    12      coerce_a      	
    13      setlocal2     	
    
    L1: 
    14      findpropstrict	_split
    17      getlocal0     	
    18      getlocal1     	
    19      getlocal2     	
    20      callproperty  	_split (3)
    24      returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::concat():String	/* disp_id 14*/
  {
    // local_count=5 max_scope=1 max_stack=4 code_len=50
    0       getlocal0     	
    1       pushscope     	
    2       getlocal0     	
    3       coerce_s      	
    4       setlocal2     	
    5       pushbyte      	0
    7       convert_u     	
    8       setlocal3     	
    9       getlocal1     	
    10      getproperty   	length
    13      convert_u     	
    14      setlocal      	4
    16      jump          	L1

    
    L2: 
    20      label         	
    21      getlocal2     	
    22      findpropstrict	String
    25      getlocal1     	
    26      getlocal3     	
    27      getproperty   	null
    30      callproperty  	String (1)
    34      add           	
    35      coerce_s      	
    36      setlocal2     	
    37      getlocal3     	
    38      increment     	
    39      convert_u     	
    40      setlocal3     	
    
    L1: 
    41      getlocal3     	
    42      getlocal      	4
    44      iflt          	L2

    48      getlocal2     	
    49      returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::toLocaleLowerCase():String	/* disp_id 27*/
  {
    // local_count=1 max_scope=1 max_stack=1 code_len=8
    0       getlocal0     	
    1       pushscope     	
    2       getlocal0     	
    3       callproperty  	http://adobe.com/AS3/2006/builtin::toLowerCase (0)
    7       returnvalue   	
  }

  native function private::_slice(int,int):String	/* disp_id 19*/

  native function get length():int	/* disp_id 5*/

  function http://adobe.com/AS3/2006/builtin::search(*):int	/* disp_id 18*/
  {
    // local_count=2 max_scope=1 max_stack=3 code_len=12
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	_search
    5       getlocal0     	
    6       getlocal1     	
    7       callproperty  	_search (2)
    11      returnvalue   	
  }

  native function http://adobe.com/AS3/2006/builtin::charAt(Number):String	/* disp_id 11*/

  native function http://adobe.com/AS3/2006/builtin::localeCompare(String):int	/* disp_id 15*/

  function http://adobe.com/AS3/2006/builtin::toString():String	/* disp_id 30*/
  {
    // local_count=1 max_scope=1 max_stack=1 code_len=4
    0       getlocal0     	
    1       pushscope     	
    2       getlocal0     	
    3       returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::valueOf():String	/* disp_id 31*/
  {
    // local_count=1 max_scope=1 max_stack=1 code_len=4
    0       getlocal0     	
    1       pushscope     	
    2       getlocal0     	
    3       returnvalue   	
  }

  native function http://adobe.com/AS3/2006/builtin::substr(Number,Number):String	/* disp_id 25*/

  function http://adobe.com/AS3/2006/builtin::replace(*,*):String	/* disp_id 17*/
  {
    // local_count=3 max_scope=1 max_stack=4 code_len=13
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	_replace
    5       getlocal0     	
    6       getlocal1     	
    7       getlocal2     	
    8       callproperty  	_replace (3)
    12      returnvalue   	
  }

  native function http://adobe.com/AS3/2006/builtin::charCodeAt(Number):Number	/* disp_id 13*/

  native function http://adobe.com/AS3/2006/builtin::toUpperCase():String	/* disp_id 28*/

  function http://adobe.com/AS3/2006/builtin::toLocaleUpperCase():String	/* disp_id 29*/
  {
    // local_count=1 max_scope=1 max_stack=1 code_len=8
    0       getlocal0     	
    1       pushscope     	
    2       getlocal0     	
    3       callproperty  	http://adobe.com/AS3/2006/builtin::toUpperCase (0)
    7       returnvalue   	
  }

  static native function private::_replace(String,*,*):String	/* disp_id 5*/
  static const length:int = 1	/* slot_id 1 */

  static native function private::_match(String,*):Array	/* disp_id 4*/

  static native function private::_search(String,*):int	/* disp_id 6*/

  static native function private::_split(String,*,uint):Array	/* disp_id 7*/

  static native function http://adobe.com/AS3/2006/builtin::fromCharCode():String	/* disp_id 3*/

  static function String$cinit():*	/* disp_id 0*/
  {
    // local_count=2 max_scope=1 max_stack=4 code_len=262
    0       getlocal0     	
    1       pushscope     	
    2       findproperty  	length
    4       pushbyte      	1
    6       initproperty  	length
    8       getlocal0     	
    9       newfunction   	var undefined():*	/* disp_id 0*/
    12      setproperty   	fromCharCode
    15      findpropstrict	prototype
    18      getproperty   	prototype
    21      newfunction   	var undefined(String,Number):int	/* disp_id 0*/
    24      setproperty   	indexOf
    27      findpropstrict	prototype
    30      getproperty   	prototype
    33      newfunction   	var undefined(String,Number):int	/* disp_id 0*/
    36      setproperty   	lastIndexOf
    39      findpropstrict	prototype
    42      getproperty   	prototype
    45      newfunction   	var undefined(Number):String	/* disp_id 0*/
    48      setproperty   	charAt
    51      findpropstrict	prototype
    54      getproperty   	prototype
    57      newfunction   	var undefined(Number):Number	/* disp_id 0*/
    60      setproperty   	charCodeAt
    63      findpropstrict	prototype
    66      getproperty   	prototype
    69      newfunction   	var undefined():String	/* disp_id 0*/
    72      setproperty   	concat
    75      findpropstrict	prototype
    78      getproperty   	prototype
    81      newfunction   	var undefined(String):int	/* disp_id 0*/
    84      setproperty   	localeCompare
    87      findpropstrict	prototype
    90      getproperty   	prototype
    93      newfunction   	var undefined(*):Array	/* disp_id 0*/
    96      setproperty   	match
    99      findpropstrict	prototype
    102     getproperty   	prototype
    105     newfunction   	var undefined(*,*):String	/* disp_id 0*/
    108     setproperty   	replace
    111     findpropstrict	prototype
    114     getproperty   	prototype
    117     newfunction   	var undefined(*):int	/* disp_id 0*/
    120     setproperty   	search
    123     findpropstrict	prototype
    126     getproperty   	prototype
    129     newfunction   	var undefined(Number,Number):String	/* disp_id 0*/
    132     setproperty   	slice
    135     findpropstrict	prototype
    138     getproperty   	prototype
    141     newfunction   	var undefined(*,*):Array	/* disp_id 0*/
    144     setproperty   	split
    147     findpropstrict	prototype
    150     getproperty   	prototype
    153     newfunction   	var undefined(Number,Number):String	/* disp_id 0*/
    156     setproperty   	substring
    159     findpropstrict	prototype
    162     getproperty   	prototype
    165     newfunction   	var undefined(Number,Number):String	/* disp_id 0*/
    168     setproperty   	substr
    171     findpropstrict	prototype
    174     getproperty   	prototype
    177     findpropstrict	prototype
    180     getproperty   	prototype
    183     newfunction   	var undefined():String	/* disp_id 0*/
    186     dup           	
    187     setlocal1     	
    188     setproperty   	toLocaleLowerCase
    191     getlocal1     	
    192     kill          	1
    194     setproperty   	toLowerCase
    197     findpropstrict	prototype
    200     getproperty   	prototype
    203     findpropstrict	prototype
    206     getproperty   	prototype
    209     newfunction   	var undefined():String	/* disp_id 0*/
    212     dup           	
    213     setlocal1     	
    214     setproperty   	toLocaleUpperCase
    217     getlocal1     	
    218     kill          	1
    220     setproperty   	toUpperCase
    223     findpropstrict	prototype
    226     getproperty   	prototype
    229     newfunction   	var undefined():String	/* disp_id 0*/
    232     setproperty   	toString
    235     findpropstrict	prototype
    238     getproperty   	prototype
    241     newfunction   	var undefined():*	/* disp_id 0*/
    244     setproperty   	valueOf
    247     findpropstrict	_dontEnumPrototype
    250     findpropstrict	prototype
    253     getproperty   	prototype
    256     callproperty  	_dontEnumPrototype (1)
    260     pop           	
    261     returnvoid    	
  }
}
const AS3:* = "http://adobe.com/AS3/2006/builtin"	/* slot_id 1 */

final class Boolean extends Object
{

  function Boolean(*):*	/* disp_id -1*/
  {
    // local_count=2 max_scope=1 max_stack=1 code_len=6
    0       getlocal0     	
    1       pushscope     	
    2       getlocal0     	
    3       constructsuper	(0)
    5       returnvoid    	
  }

  function http://adobe.com/AS3/2006/builtin::valueOf():Boolean	/* disp_id 6*/
  {
    // local_count=1 max_scope=1 max_stack=1 code_len=4
    0       getlocal0     	
    1       pushscope     	
    2       getlocal0     	
    3       returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::toString():String	/* disp_id 5*/
  {
    // local_count=1 max_scope=1 max_stack=1 code_len=18
    0       getlocal0     	
    1       pushscope     	
    2       getlocal0     	
    3       iffalse       	L1

    7       pushstring    	"true"
    9       coerce_s      	
    10      jump          	L2
    
    L1: 
    14      pushstring    	"false"
    16      coerce_s      	
    
    L2: 
    17      returnvalue   	
  }
  static const length:int = 1	/* slot_id 1 */

  static function Boolean$cinit():*	/* disp_id 0*/
  {
    // local_count=1 max_scope=1 max_stack=2 code_len=35
    0       getlocal0     	
    1       pushscope     	
    2       findproperty  	length
    4       pushbyte      	1
    6       initproperty  	length
    8       findpropstrict	prototype
    10      getproperty   	prototype
    12      newfunction   	var undefined():String	/* disp_id 0*/
    14      setproperty   	toString
    16      findpropstrict	prototype
    18      getproperty   	prototype
    20      newfunction   	var undefined():*	/* disp_id 0*/
    22      setproperty   	valueOf
    24      findpropstrict	_dontEnumPrototype
    26      findpropstrict	prototype
    28      getproperty   	prototype
    30      callproperty  	_dontEnumPrototype (1)
    33      pop           	
    34      returnvoid    	
  }
}

native function isNaN(Number):Boolean	/* disp_id 5*/

final class Namespace extends Object
{

  function Namespace(*,*):*	/* disp_id -1*/
  {
    // local_count=3 max_scope=1 max_stack=1 code_len=6
    0       getlocal0     	
    1       pushscope     	
    2       getlocal0     	
    3       constructsuper	(0)
    5       returnvoid    	
  }

  function http://adobe.com/AS3/2006/builtin::valueOf():String	/* disp_id 7*/
  {
    // local_count=1 max_scope=1 max_stack=1 code_len=7
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	uri
    4       getproperty   	uri
    6       returnvalue   	
  }

  native function get prefix():*	/* disp_id 5*/

  function http://adobe.com/AS3/2006/builtin::toString():String	/* disp_id 8*/
  {
    // local_count=1 max_scope=1 max_stack=1 code_len=7
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	uri
    4       getproperty   	uri
    6       returnvalue   	
  }

  native function get uri():String	/* disp_id 6*/
  static const length:* = 2	/* slot_id 1 */

  static function Namespace$cinit():*	/* disp_id 0*/
  {
    // local_count=1 max_scope=1 max_stack=2 code_len=35
    0       getlocal0     	
    1       pushscope     	
    2       findproperty  	length
    4       pushbyte      	2
    6       initproperty  	length
    8       findpropstrict	prototype
    10      getproperty   	prototype
    12      newfunction   	var undefined():String	/* disp_id 0*/
    14      setproperty   	valueOf
    16      findpropstrict	prototype
    18      getproperty   	prototype
    20      newfunction   	var undefined():String	/* disp_id 0*/
    22      setproperty   	toString
    24      findpropstrict	_dontEnumPrototype
    26      findpropstrict	prototype
    28      getproperty   	prototype
    30      callproperty  	_dontEnumPrototype (1)
    33      pop           	
    34      returnvoid    	
  }
}

native function parseFloat(String):Number	/* disp_id 8*/
const Infinity:Number = Infinity	/* slot_id 16 */

final class decimal extends Object
{

  function decimal(*):*	/* disp_id -1*/
  {
    // local_count=2 max_scope=1 max_stack=1 code_len=6
    0       getlocal0     	
    1       pushscope     	
    2       getlocal0     	
    3       constructsuper	(0)
    5       returnvoid    	
  }

  function http://adobe.com/AS3/2006/builtin::toPrecision(*):String	/* disp_id 8*/
  {
    // local_count=2 max_scope=1 max_stack=4 code_len=39
    0       getlocal0     	
    1       pushscope     	
    2       getlocal1     	
    3       getglobalscope	
    4       getslot       	17
    6       ifne          	L1

    10      getlocal0     	
    11      callproperty  	toString (0)
    15      returnvalue   	
    
    L1: 
    16      findpropstrict	_convert
    19      getlocal0     	
    20      findpropstrict	int
    23      getlocal1     	
    24      callproperty  	int (1)
    28      findpropstrict	DTOSTR_PRECISION
    31      getproperty   	DTOSTR_PRECISION
    34      callproperty  	_convert (3)
    38      returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::valueOf():decimal	/* disp_id 5*/
  {
    // local_count=1 max_scope=1 max_stack=1 code_len=4
    0       getlocal0     	
    1       pushscope     	
    2       getlocal0     	
    3       returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::toFixed(*):String	/* disp_id 9*/
  {
    // local_count=2 max_scope=1 max_stack=4 code_len=25
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	_convert
    5       getlocal0     	
    6       findpropstrict	int
    9       getlocal1     	
    10      callproperty  	int (1)
    14      findpropstrict	DTOSTR_FIXED
    17      getproperty   	DTOSTR_FIXED
    20      callproperty  	_convert (3)
    24      returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::toExponential(*):String	/* disp_id 7*/
  {
    // local_count=2 max_scope=1 max_stack=4 code_len=25
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	_convert
    5       getlocal0     	
    6       findpropstrict	int
    9       getlocal1     	
    10      callproperty  	int (1)
    14      findpropstrict	DTOSTR_EXPONENTIAL
    17      getproperty   	DTOSTR_EXPONENTIAL
    20      callproperty  	_convert (3)
    24      returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::toString(*):String	/* disp_id 6*/
  {
    // local_count=2 max_scope=1 max_stack=3 code_len=12
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	_toString
    5       getlocal0     	
    6       getlocal1     	
    7       callproperty  	_toString (2)
    11      returnvalue   	
  }

  static native function log(decimal):decimal	/* disp_id 11*/

  static native function ceil(decimal):decimal	/* disp_id 8*/
  static const private::DTOSTR_FIXED:int = 1	/* slot_id 1 */

  static native function private::_toString(decimal,int):String	/* disp_id 3*/
  static const private::DTOSTR_PRECISION:int = 2	/* slot_id 2 */

  static native function min(decimal,decimal):decimal	/* disp_id 17*/
  static const ROUND_DOWN:int = 5	/* slot_id 9 */
  static const ROUND_HALF_EVEN:int = 3	/* slot_id 7 */
  static const ROUND_HALF_UP:int = 2	/* slot_id 6 */
  static const private::DTOSTR_EXPONENTIAL:int = 3	/* slot_id 3 */

  static native function private::_min(decimal,decimal):decimal	/* disp_id 5*/
  static const ROUND_FLOOR:int = 6	/* slot_id 10 */
  static const ROUND_HALF_DOWN:int = 4	/* slot_id 8 */

  static native function pow(decimal,decimal):decimal	/* disp_id 15*/

  static native function sqrt(decimal):decimal	/* disp_id 14*/

  static native function log10(decimal):decimal	/* disp_id 12*/
  static const ROUND_UP:int = 1	/* slot_id 5 */

  static native function private::_max(decimal,decimal):decimal	/* disp_id 6*/

  static native function max(decimal,decimal):decimal	/* disp_id 16*/
  static const length:int = 1	/* slot_id 11 */
  static const ROUND_CEILING:int = 0	/* slot_id 4 */

  static native function abs(decimal):decimal	/* disp_id 7*/

  static native function private::_convert(decimal,int,int):String	/* disp_id 4*/

  static native function exp(decimal):decimal	/* disp_id 9*/

  static native function floor(decimal):decimal	/* disp_id 10*/

  static native function round(decimal,int):decimal	/* disp_id 13*/

  static function decimal$cinit():*	/* disp_id 0*/
  {
    // local_count=2 max_scope=2 max_stack=5 code_len=182
    0       getlocal0     	
    1       pushscope     	
    2       findproperty  	private::DTOSTR_FIXED
    5       pushbyte      	1
    7       initproperty  	private::DTOSTR_FIXED
    10      findproperty  	private::DTOSTR_PRECISION
    13      pushbyte      	2
    15      initproperty  	private::DTOSTR_PRECISION
    18      findproperty  	private::DTOSTR_EXPONENTIAL
    21      pushbyte      	3
    23      initproperty  	private::DTOSTR_EXPONENTIAL
    26      findproperty  	ROUND_CEILING
    29      pushbyte      	0
    31      initproperty  	ROUND_CEILING
    34      findproperty  	ROUND_UP
    37      pushbyte      	1
    39      initproperty  	ROUND_UP
    42      findproperty  	ROUND_HALF_UP
    45      pushbyte      	2
    47      initproperty  	ROUND_HALF_UP
    50      findproperty  	ROUND_HALF_EVEN
    53      pushbyte      	3
    55      initproperty  	ROUND_HALF_EVEN
    58      findproperty  	ROUND_HALF_DOWN
    61      pushbyte      	4
    63      initproperty  	ROUND_HALF_DOWN
    66      findproperty  	ROUND_DOWN
    69      pushbyte      	5
    71      initproperty  	ROUND_DOWN
    74      findproperty  	ROUND_FLOOR
    77      pushbyte      	6
    79      initproperty  	ROUND_FLOOR
    82      findproperty  	length
    84      pushbyte      	1
    86      initproperty  	length
    88      findpropstrict	prototype
    91      getproperty   	prototype
    94      findpropstrict	prototype
    97      getproperty   	prototype
    100     newobject     	{0}
    102     pushwith      	
    103     newfunction   	var undefined(*):String	/* disp_id 0*/
    105     dup           	
    106     getscopeobject	1
    108     swap          	
    109     setproperty   	toString
    111     popscope      	
    112     dup           	
    113     setlocal1     	
    114     setproperty   	toString
    117     getlocal1     	
    118     kill          	1
    120     setproperty   	toLocaleString
    123     findpropstrict	prototype
    126     getproperty   	prototype
    129     newfunction   	var undefined():*	/* disp_id 0*/
    131     setproperty   	valueOf
    134     findpropstrict	prototype
    137     getproperty   	prototype
    140     newfunction   	var undefined(*):String	/* disp_id 0*/
    142     setproperty   	toExponential
    145     findpropstrict	prototype
    148     getproperty   	prototype
    151     newfunction   	var undefined(*):String	/* disp_id 0*/
    153     setproperty   	toPrecision
    156     findpropstrict	prototype
    159     getproperty   	prototype
    162     newfunction   	var undefined(*):String	/* disp_id 0*/
    164     setproperty   	toFixed
    167     findpropstrict	_dontEnumPrototype
    170     findpropstrict	prototype
    173     getproperty   	prototype
    176     callproperty  	_dontEnumPrototype (1)
    180     pop           	
    181     returnvoid    	
  }
}

final class double extends Object
{

  function double(*):*	/* disp_id -1*/
  {
    // local_count=2 max_scope=1 max_stack=1 code_len=6
    0       getlocal0     	
    1       pushscope     	
    2       getlocal0     	
    3       constructsuper	(0)
    5       returnvoid    	
  }

  function http://adobe.com/AS3/2006/builtin::toPrecision(*):String	/* disp_id 8*/
  {
    // local_count=2 max_scope=1 max_stack=2 code_len=15
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	Number
    5       getlocal0     	
    6       callproperty  	Number (1)
    10      getlocal1     	
    11      callproperty  	http://adobe.com/AS3/2006/builtin::toPrecision (1)
    14      returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::valueOf():double	/* disp_id 6*/
  {
    // local_count=1 max_scope=1 max_stack=1 code_len=4
    0       getlocal0     	
    1       pushscope     	
    2       getlocal0     	
    3       returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::toFixed(*):String	/* disp_id 9*/
  {
    // local_count=2 max_scope=1 max_stack=2 code_len=15
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	Number
    5       getlocal0     	
    6       callproperty  	Number (1)
    10      getlocal1     	
    11      callproperty  	http://adobe.com/AS3/2006/builtin::toFixed (1)
    14      returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::toExponential(*):String	/* disp_id 7*/
  {
    // local_count=2 max_scope=1 max_stack=2 code_len=15
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	Number
    5       getlocal0     	
    6       callproperty  	Number (1)
    10      getlocal1     	
    11      callproperty  	http://adobe.com/AS3/2006/builtin::toExponential (1)
    14      returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::toString(*):String	/* disp_id 5*/
  {
    // local_count=2 max_scope=1 max_stack=2 code_len=15
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	Number
    5       getlocal0     	
    6       callproperty  	Number (1)
    10      getlocal1     	
    11      callproperty  	http://adobe.com/AS3/2006/builtin::toString (1)
    14      returnvalue   	
  }
  static const MIN_VALUE:double = 4.9E-324	/* slot_id 5 */
  static const length:int = 1	/* slot_id 1 */
  static const POSITIVE_INFINITY:double = Infinity	/* slot_id 4 */
  static const NEGATIVE_INFINITY:double = -Infinity	/* slot_id 3 */
  static const MAX_VALUE:double = NaN	/* slot_id 6 */

  static native function private::_toString(double,int):String	/* disp_id 3*/
  static const NaN:double = NaN	/* slot_id 2 */

  static function double$cinit():*	/* disp_id 0*/
  {
    // local_count=2 max_scope=1 max_stack=4 code_len=109
    0       getlocal0     	
    1       pushscope     	
    2       findproperty  	length
    4       pushbyte      	1
    6       initproperty  	length
    8       findproperty  	NaN
    10      pushbyte      	0
    12      pushbyte      	0
    14      divide        	
    15      initproperty  	NaN
    17      findproperty  	NEGATIVE_INFINITY
    19      pushbyte      	-1
    21      pushbyte      	0
    23      divide        	
    24      initproperty  	NEGATIVE_INFINITY
    26      findproperty  	POSITIVE_INFINITY
    28      pushbyte      	1
    30      pushbyte      	0
    32      divide        	
    33      initproperty  	POSITIVE_INFINITY
    35      findproperty  	MIN_VALUE
    37      pushdouble    	4.9E-324
    39      initproperty  	MIN_VALUE
    41      findproperty  	MAX_VALUE
    43      pushdouble    	NaN
    45      initproperty  	MAX_VALUE
    47      findpropstrict	prototype
    49      getproperty   	prototype
    51      findpropstrict	prototype
    53      getproperty   	prototype
    55      newfunction   	var undefined(*):String	/* disp_id 0*/
    57      dup           	
    58      setlocal1     	
    59      setproperty   	toString
    61      getlocal1     	
    62      kill          	1
    64      setproperty   	toLocaleString
    66      findpropstrict	prototype
    68      getproperty   	prototype
    70      newfunction   	var undefined():*	/* disp_id 0*/
    72      setproperty   	valueOf
    74      findpropstrict	prototype
    76      getproperty   	prototype
    78      newfunction   	var undefined(*):String	/* disp_id 0*/
    80      setproperty   	toExponential
    82      findpropstrict	prototype
    84      getproperty   	prototype
    86      newfunction   	var undefined(*):String	/* disp_id 0*/
    88      setproperty   	toPrecision
    90      findpropstrict	prototype
    92      getproperty   	prototype
    94      newfunction   	var undefined(*):String	/* disp_id 0*/
    96      setproperty   	toFixed
    98      findpropstrict	_dontEnumPrototype
    100     findpropstrict	prototype
    102     getproperty   	prototype
    104     callproperty  	_dontEnumPrototype (1)
    107     pop           	
    108     returnvoid    	
  }
}

final class uint extends Object
{

  function uint(*):*	/* disp_id -1*/
  {
    // local_count=2 max_scope=1 max_stack=1 code_len=6
    0       getlocal0     	
    1       pushscope     	
    2       getlocal0     	
    3       constructsuper	(0)
    5       returnvoid    	
  }

  function http://adobe.com/AS3/2006/builtin::toPrecision(*):String	/* disp_id 8*/
  {
    // local_count=2 max_scope=1 max_stack=2 code_len=13
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	Number
    4       getlocal0     	
    5       callproperty  	Number (1)
    8       getlocal1     	
    9       callproperty  	http://adobe.com/AS3/2006/builtin::toPrecision (1)
    12      returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::valueOf():uint	/* disp_id 6*/
  {
    // local_count=1 max_scope=1 max_stack=1 code_len=4
    0       getlocal0     	
    1       pushscope     	
    2       getlocal0     	
    3       returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::toFixed(*):String	/* disp_id 9*/
  {
    // local_count=2 max_scope=1 max_stack=2 code_len=13
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	Number
    4       getlocal0     	
    5       callproperty  	Number (1)
    8       getlocal1     	
    9       callproperty  	http://adobe.com/AS3/2006/builtin::toFixed (1)
    12      returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::toExponential(*):String	/* disp_id 7*/
  {
    // local_count=2 max_scope=1 max_stack=2 code_len=13
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	Number
    4       getlocal0     	
    5       callproperty  	Number (1)
    8       getlocal1     	
    9       callproperty  	http://adobe.com/AS3/2006/builtin::toExponential (1)
    12      returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::toString(*):String	/* disp_id 5*/
  {
    // local_count=2 max_scope=1 max_stack=2 code_len=13
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	Number
    4       getlocal0     	
    5       callproperty  	Number (1)
    8       getlocal1     	
    9       callproperty  	http://adobe.com/AS3/2006/builtin::toString (1)
    12      returnvalue   	
  }
  static const MIN_VALUE:uint = 0	/* slot_id 1 */
  static const length:int = 1	/* slot_id 3 */
  static const MAX_VALUE:uint = NaN	/* slot_id 2 */

  static function uint$cinit():*	/* disp_id 0*/
  {
    // local_count=2 max_scope=2 max_stack=5 code_len=92
    0       getlocal0     	
    1       pushscope     	
    2       findproperty  	MIN_VALUE
    4       pushbyte      	0
    6       initproperty  	MIN_VALUE
    8       findproperty  	MAX_VALUE
    10      pushdouble    	NaN
    12      initproperty  	MAX_VALUE
    14      findproperty  	length
    16      pushbyte      	1
    18      initproperty  	length
    20      findpropstrict	prototype
    22      getproperty   	prototype
    24      findpropstrict	prototype
    26      getproperty   	prototype
    28      newobject     	{0}
    30      pushwith      	
    31      newfunction   	var undefined(*):String	/* disp_id 0*/
    33      dup           	
    34      getscopeobject	1
    36      swap          	
    37      setproperty   	toString
    39      popscope      	
    40      dup           	
    41      setlocal1     	
    42      setproperty   	toString
    44      getlocal1     	
    45      kill          	1
    47      setproperty   	toLocaleString
    49      findpropstrict	prototype
    51      getproperty   	prototype
    53      newfunction   	var undefined():*	/* disp_id 0*/
    55      setproperty   	valueOf
    57      findpropstrict	prototype
    59      getproperty   	prototype
    61      newfunction   	var undefined(*):String	/* disp_id 0*/
    63      setproperty   	toExponential
    65      findpropstrict	prototype
    67      getproperty   	prototype
    69      newfunction   	var undefined(*):String	/* disp_id 0*/
    71      setproperty   	toPrecision
    73      findpropstrict	prototype
    75      getproperty   	prototype
    77      newfunction   	var undefined(*):String	/* disp_id 0*/
    79      setproperty   	toFixed
    81      findpropstrict	_dontEnumPrototype
    83      findpropstrict	prototype
    85      getproperty   	prototype
    87      callproperty  	_dontEnumPrototype (1)
    90      pop           	
    91      returnvoid    	
  }
}

native function isFinite(Number):Boolean	/* disp_id 6*/

dynamic class Object extends *
{

  function Object():*	/* disp_id -1*/
  {
    // local_count=1 max_scope=1 max_stack=1 code_len=3
    0       getlocal0     	
    1       pushscope     	
    2       returnvoid    	
  }

  function http://adobe.com/AS3/2006/builtin::hasOwnProperty(*):Boolean	/* disp_id 2*/
  {
    // local_count=2 max_scope=1 max_stack=3 code_len=10
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	_hasOwnProperty
    4       getlocal0     	
    5       getlocal1     	
    6       callproperty  	_hasOwnProperty (2)
    9       returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::propertyIsEnumerable(*):Boolean	/* disp_id 3*/
  {
    // local_count=2 max_scope=1 max_stack=3 code_len=10
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	_propertyIsEnumerable
    4       getlocal0     	
    5       getlocal1     	
    6       callproperty  	_propertyIsEnumerable (2)
    9       returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::isPrototypeOf(*):Boolean	/* disp_id 1*/
  {
    // local_count=2 max_scope=1 max_stack=3 code_len=10
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	_isPrototypeOf
    4       getlocal0     	
    5       getlocal1     	
    6       callproperty  	_isPrototypeOf (2)
    9       returnvalue   	
  }

  static native function private::_hasOwnProperty(*,String):Boolean	/* disp_id 3*/

  static function Object::_dontEnumPrototype(Object):void	/* disp_id 8*/
  {
    // local_count=5 max_scope=1 max_stack=4 code_len=44
    0       getlocal0     	
    1       pushscope     	
    2       pushnull      	
    3       coerce_s      	
    4       setlocal2     	
    5       pushbyte      	0
    7       setlocal3     	
    8       getlocal1     	
    9       coerce_a      	
    10      setlocal      	4
    12      jump          	L1

    
    L2: 
    16      label         	
    17      getlocal      	4
    19      getlocal3     	
    20      nextname      	
    21      coerce_s      	
    22      setlocal2     	
    23      findpropstrict	_setPropertyIsEnumerable
    25      getlocal1     	
    26      getlocal2     	
    27      pushfalse     	
    28      callproperty  	_setPropertyIsEnumerable (3)
    31      pop           	
    
    L1: 
    32      hasnext2      	4 3
    35      iftrue        	L2

    39      kill          	4
    41      kill          	3
    43      returnvoid    	
  }

  static native function private::_propertyIsEnumerable(*,String):Boolean	/* disp_id 4*/

  static native function private::_isPrototypeOf(*,*):Boolean	/* disp_id 6*/

  static native function Object::_setPropertyIsEnumerable(*,String,Boolean):void	/* disp_id 5*/

  static native function private::_toString(*):String	/* disp_id 7*/
  static const length:int = 1	/* slot_id 1 */

  static function init():*	/* disp_id 9*/
  {
    activation {
    }
    // local_count=3 max_scope=2 max_stack=4 code_len=76
    0       getlocal0     	
    1       pushscope     	
    2       newactivation 	
    3       dup           	
    4       setlocal1     	
    5       pushscope     	
    6       findpropstrict	prototype
    8       getproperty   	prototype
    10      newfunction   	var undefined(*):Boolean	/* disp_id 0*/
    12      setproperty   	hasOwnProperty
    14      findpropstrict	prototype
    16      getproperty   	prototype
    18      newfunction   	var undefined(*):*	/* disp_id 0*/
    20      setproperty   	propertyIsEnumerable
    22      findpropstrict	prototype
    24      getproperty   	prototype
    26      newfunction   	var undefined(String,Boolean):void	/* disp_id 0*/
    28      setproperty   	setPropertyIsEnumerable
    30      findpropstrict	prototype
    32      getproperty   	prototype
    34      newfunction   	var undefined(*):Boolean	/* disp_id 0*/
    36      setproperty   	isPrototypeOf
    38      findpropstrict	prototype
    40      getproperty   	prototype
    42      findpropstrict	prototype
    44      getproperty   	prototype
    46      newfunction   	var undefined():String	/* disp_id 0*/
    48      dup           	
    49      setlocal2     	
    50      setproperty   	toLocaleString
    52      getlocal2     	
    53      kill          	2
    55      setproperty   	toString
    57      findpropstrict	prototype
    59      getproperty   	prototype
    61      newfunction   	var undefined():*	/* disp_id 0*/
    63      setproperty   	valueOf
    65      findpropstrict	_dontEnumPrototype
    67      findpropstrict	prototype
    69      getproperty   	prototype
    71      callproperty  	_dontEnumPrototype (1)
    74      pop           	
    75      returnvoid    	
  }

  static function Object$cinit():*	/* disp_id 0*/
  {
    // local_count=1 max_scope=1 max_stack=2 code_len=9
    0       getlocal0     	
    1       pushscope     	
    2       findproperty  	length
    4       pushbyte      	1
    6       initproperty  	length
    8       returnvoid    	
  }
}

native function unescape(String):String	/* disp_id 10*/

native function escape(String):String	/* disp_id 9*/

native function decodeURI(String):String	/* disp_id 1*/

dynamic class Class extends Object
{

  function Class():*	/* disp_id -1*/
  {
    // local_count=1 max_scope=1 max_stack=1 code_len=6
    0       getlocal0     	
    1       pushscope     	
    2       getlocal0     	
    3       constructsuper	(0)
    5       returnvoid    	
  }

  native function get prototype():*	/* disp_id 5*/
  static const length:int = 1	/* slot_id 1 */

  static function Class$cinit():*	/* disp_id 0*/
  {
    // local_count=1 max_scope=1 max_stack=2 code_len=9
    0       getlocal0     	
    1       pushscope     	
    2       findproperty  	length
    4       pushbyte      	1
    6       initproperty  	length
    8       returnvoid    	
  }
}
const undefined:*	/* slot_id 17 */

final class Number extends Object
{

  function Number(*):*	/* disp_id -1*/
  {
    // local_count=2 max_scope=1 max_stack=1 code_len=6
    0       getlocal0     	
    1       pushscope     	
    2       getlocal0     	
    3       constructsuper	(0)
    5       returnvoid    	
  }

  function http://adobe.com/AS3/2006/builtin::toPrecision(*):String	/* disp_id 8*/
  {
    // local_count=2 max_scope=1 max_stack=4 code_len=32
    0       getlocal0     	
    1       pushscope     	
    2       getlocal1     	
    3       getglobalscope	
    4       getslot       	17
    6       ifne          	L1

    10      getlocal0     	
    11      callproperty  	toString (0)
    14      returnvalue   	
    
    L1: 
    15      findpropstrict	_convert
    17      getlocal0     	
    18      findpropstrict	int
    20      getlocal1     	
    21      callproperty  	int (1)
    24      findpropstrict	DTOSTR_PRECISION
    26      getproperty   	DTOSTR_PRECISION
    28      callproperty  	_convert (3)
    31      returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::valueOf():Number	/* disp_id 6*/
  {
    // local_count=1 max_scope=1 max_stack=1 code_len=4
    0       getlocal0     	
    1       pushscope     	
    2       getlocal0     	
    3       returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::toFixed(*):String	/* disp_id 9*/
  {
    // local_count=2 max_scope=1 max_stack=4 code_len=19
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	_convert
    4       getlocal0     	
    5       findpropstrict	int
    7       getlocal1     	
    8       callproperty  	int (1)
    11      findpropstrict	DTOSTR_FIXED
    13      getproperty   	DTOSTR_FIXED
    15      callproperty  	_convert (3)
    18      returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::toExponential(*):String	/* disp_id 7*/
  {
    // local_count=2 max_scope=1 max_stack=4 code_len=19
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	_convert
    4       getlocal0     	
    5       findpropstrict	int
    7       getlocal1     	
    8       callproperty  	int (1)
    11      findpropstrict	DTOSTR_EXPONENTIAL
    13      getproperty   	DTOSTR_EXPONENTIAL
    15      callproperty  	_convert (3)
    18      returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::toString(*):String	/* disp_id 5*/
  {
    // local_count=2 max_scope=1 max_stack=3 code_len=10
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	_toString
    4       getlocal0     	
    5       getlocal1     	
    6       callproperty  	_toString (2)
    9       returnvalue   	
  }
  static const MIN_VALUE:Number = 4.9E-324	/* slot_id 5 */
  static const length:int = 1	/* slot_id 1 */
  static const private::DTOSTR_FIXED:int = 1	/* slot_id 7 */
  static const POSITIVE_INFINITY:Number = Infinity	/* slot_id 4 */
  static const private::DTOSTR_PRECISION:int = 2	/* slot_id 8 */
  static const NEGATIVE_INFINITY:Number = -Infinity	/* slot_id 3 */

  static native function private::_convert(Number,int,int):String	/* disp_id 4*/
  static const MAX_VALUE:Number = NaN	/* slot_id 6 */
  static const private::DTOSTR_EXPONENTIAL:int = 3	/* slot_id 9 */

  static native function private::_toString(Number,int):String	/* disp_id 3*/
  static const NaN:Number = NaN	/* slot_id 2 */

  static function Number$cinit():*	/* disp_id 0*/
  {
    // local_count=2 max_scope=1 max_stack=4 code_len=127
    0       getlocal0     	
    1       pushscope     	
    2       findproperty  	length
    4       pushbyte      	1
    6       initproperty  	length
    8       findproperty  	NaN
    10      pushbyte      	0
    12      pushbyte      	0
    14      divide        	
    15      initproperty  	NaN
    17      findproperty  	NEGATIVE_INFINITY
    19      pushbyte      	-1
    21      pushbyte      	0
    23      divide        	
    24      initproperty  	NEGATIVE_INFINITY
    26      findproperty  	POSITIVE_INFINITY
    28      pushbyte      	1
    30      pushbyte      	0
    32      divide        	
    33      initproperty  	POSITIVE_INFINITY
    35      findproperty  	MIN_VALUE
    37      pushdouble    	4.9E-324
    39      initproperty  	MIN_VALUE
    41      findproperty  	MAX_VALUE
    43      pushdouble    	NaN
    45      initproperty  	MAX_VALUE
    47      findproperty  	private::DTOSTR_FIXED
    49      pushbyte      	1
    51      initproperty  	private::DTOSTR_FIXED
    53      findproperty  	private::DTOSTR_PRECISION
    55      pushbyte      	2
    57      initproperty  	private::DTOSTR_PRECISION
    59      findproperty  	private::DTOSTR_EXPONENTIAL
    61      pushbyte      	3
    63      initproperty  	private::DTOSTR_EXPONENTIAL
    65      findpropstrict	prototype
    67      getproperty   	prototype
    69      findpropstrict	prototype
    71      getproperty   	prototype
    73      newfunction   	var undefined(*):String	/* disp_id 0*/
    75      dup           	
    76      setlocal1     	
    77      setproperty   	toString
    79      getlocal1     	
    80      kill          	1
    82      setproperty   	toLocaleString
    84      findpropstrict	prototype
    86      getproperty   	prototype
    88      newfunction   	var undefined():*	/* disp_id 0*/
    90      setproperty   	valueOf
    92      findpropstrict	prototype
    94      getproperty   	prototype
    96      newfunction   	var undefined(*):String	/* disp_id 0*/
    98      setproperty   	toExponential
    100     findpropstrict	prototype
    102     getproperty   	prototype
    104     newfunction   	var undefined(*):String	/* disp_id 0*/
    106     setproperty   	toPrecision
    108     findpropstrict	prototype
    110     getproperty   	prototype
    112     newfunction   	var undefined(*):String	/* disp_id 0*/
    114     setproperty   	toFixed
    116     findpropstrict	_dontEnumPrototype
    118     findpropstrict	prototype
    120     getproperty   	prototype
    122     callproperty  	_dontEnumPrototype (1)
    125     pop           	
    126     returnvoid    	
  }
}
const NaN:Number = NaN	/* slot_id 15 */

native function encodeURIComponent(String):String	/* disp_id 4*/

final class int extends Object
{

  function int(*):*	/* disp_id -1*/
  {
    // local_count=2 max_scope=1 max_stack=1 code_len=6
    0       getlocal0     	
    1       pushscope     	
    2       getlocal0     	
    3       constructsuper	(0)
    5       returnvoid    	
  }

  function http://adobe.com/AS3/2006/builtin::toPrecision(*):String	/* disp_id 8*/
  {
    // local_count=2 max_scope=1 max_stack=2 code_len=13
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	Number
    4       getlocal0     	
    5       callproperty  	Number (1)
    8       getlocal1     	
    9       callproperty  	http://adobe.com/AS3/2006/builtin::toPrecision (1)
    12      returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::valueOf():int	/* disp_id 6*/
  {
    // local_count=1 max_scope=1 max_stack=1 code_len=4
    0       getlocal0     	
    1       pushscope     	
    2       getlocal0     	
    3       returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::toFixed(*):String	/* disp_id 9*/
  {
    // local_count=2 max_scope=1 max_stack=2 code_len=13
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	Number
    4       getlocal0     	
    5       callproperty  	Number (1)
    8       getlocal1     	
    9       callproperty  	http://adobe.com/AS3/2006/builtin::toFixed (1)
    12      returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::toExponential(*):String	/* disp_id 7*/
  {
    // local_count=2 max_scope=1 max_stack=2 code_len=13
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	Number
    4       getlocal0     	
    5       callproperty  	Number (1)
    8       getlocal1     	
    9       callproperty  	http://adobe.com/AS3/2006/builtin::toExponential (1)
    12      returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::toString(*):String	/* disp_id 5*/
  {
    // local_count=2 max_scope=1 max_stack=2 code_len=13
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	Number
    4       getlocal0     	
    5       callproperty  	Number (1)
    8       getlocal1     	
    9       callproperty  	http://adobe.com/AS3/2006/builtin::toString (1)
    12      returnvalue   	
  }
  static const MIN_VALUE:int = -2.147483648E9	/* slot_id 1 */
  static const length:int = 1	/* slot_id 3 */
  static const MAX_VALUE:int = 2147483647	/* slot_id 2 */

  static function int$cinit():*	/* disp_id 0*/
  {
    // local_count=2 max_scope=2 max_stack=5 code_len=92
    0       getlocal0     	
    1       pushscope     	
    2       findproperty  	MIN_VALUE
    4       pushdouble    	-2.147483648E9
    6       initproperty  	MIN_VALUE
    8       findproperty  	MAX_VALUE
    10      pushint       	2147483647	// 0x7fffffff
    12      initproperty  	MAX_VALUE
    14      findproperty  	length
    16      pushbyte      	1
    18      initproperty  	length
    20      findpropstrict	prototype
    22      getproperty   	prototype
    24      findpropstrict	prototype
    26      getproperty   	prototype
    28      newobject     	{0}
    30      pushwith      	
    31      newfunction   	var undefined(*):String	/* disp_id 0*/
    33      dup           	
    34      getscopeobject	1
    36      swap          	
    37      setproperty   	toString
    39      popscope      	
    40      dup           	
    41      setlocal1     	
    42      setproperty   	toString
    44      getlocal1     	
    45      kill          	1
    47      setproperty   	toLocaleString
    49      findpropstrict	prototype
    51      getproperty   	prototype
    53      newfunction   	var undefined():*	/* disp_id 0*/
    55      setproperty   	valueOf
    57      findpropstrict	prototype
    59      getproperty   	prototype
    61      newfunction   	var undefined(*):String	/* disp_id 0*/
    63      setproperty   	toExponential
    65      findpropstrict	prototype
    67      getproperty   	prototype
    69      newfunction   	var undefined(*):String	/* disp_id 0*/
    71      setproperty   	toPrecision
    73      findpropstrict	prototype
    75      getproperty   	prototype
    77      newfunction   	var undefined(*):String	/* disp_id 0*/
    79      setproperty   	toFixed
    81      findpropstrict	_dontEnumPrototype
    83      findpropstrict	prototype
    85      getproperty   	prototype
    87      callproperty  	_dontEnumPrototype (1)
    90      pop           	
    91      returnvoid    	
  }
}

dynamic class Function extends Object
{

  function Function():*	/* disp_id -1*/
  {
    // local_count=1 max_scope=1 max_stack=1 code_len=6
    0       getlocal0     	
    1       pushscope     	
    2       getlocal0     	
    3       constructsuper	(0)
    5       returnvoid    	
  }

  native function get prototype():*	/* disp_id 5*/

  native function set prototype(*):*	/* disp_id 6*/

  native function http://adobe.com/AS3/2006/builtin::call(*):*	/* disp_id 8*/

  native function get length():int	/* disp_id 7*/

  native function http://adobe.com/AS3/2006/builtin::apply(*,*):*	/* disp_id 9*/

  static function private::emptyCtor():*	/* disp_id 3*/
  {
    activation {
    }
    // local_count=2 max_scope=2 max_stack=2 code_len=9
    0       getlocal0     	
    1       pushscope     	
    2       newactivation 	
    3       dup           	
    4       setlocal1     	
    5       pushscope     	
    6       newfunction   	var undefined():*	/* disp_id 0*/
    8       returnvalue   	
  }
  static const length:int = 1	/* slot_id 1 */

  static function Function$cinit():*	/* disp_id 0*/
  {
    // local_count=2 max_scope=1 max_stack=4 code_len=54
    0       getlocal0     	
    1       pushscope     	
    2       findproperty  	length
    4       pushbyte      	1
    6       initproperty  	length
    8       findpropstrict	prototype
    10      getproperty   	prototype
    12      findpropstrict	prototype
    14      getproperty   	prototype
    16      newfunction   	var undefined():String	/* disp_id 0*/
    18      dup           	
    19      setlocal1     	
    20      setproperty   	toString
    22      getlocal1     	
    23      kill          	1
    25      setproperty   	toLocaleString
    27      findpropstrict	prototype
    29      getproperty   	prototype
    31      newfunction   	var undefined(*):*	/* disp_id 0*/
    33      setproperty   	call
    35      findpropstrict	prototype
    37      getproperty   	prototype
    39      newfunction   	var undefined(*,*):*	/* disp_id 0*/
    41      setproperty   	apply
    43      findpropstrict	_dontEnumPrototype
    45      findpropstrict	prototype
    47      getproperty   	prototype
    49      callproperty  	_dontEnumPrototype (1)
    52      pop           	
    53      returnvoid    	
  }
}

dynamic class Array extends Object
{

  function Array():*	/* disp_id -1*/
  {
    // local_count=6 max_scope=1 max_stack=4 code_len=145
    0       getlocal0     	
    1       pushscope     	
    2       pushnan       	
    3       setlocal3     	
    4       pushbyte      	0
    6       convert_u     	
    7       setlocal      	4
    9       pushbyte      	0
    11      convert_u     	
    12      setlocal      	5
    14      getlocal0     	
    15      constructsuper	(0)
    17      getlocal1     	
    18      getproperty   	length
    21      convert_u     	
    22      setlocal2     	
    23      getlocal2     	
    24      pushbyte      	1
    26      equals        	
    27      convert_b     	
    28      dup           	
    29      convert_b     	
    30      iffalse       	L1

    34      pop           	
    35      getlocal1     	
    36      pushbyte      	0
    38      getproperty   	null
    41      getglobalscope	
    42      getslot       	8
    44      istypelate    	
    45      convert_b     	
    
    L1: 
    46      iffalse       	L2

    50      getlocal1     	
    51      pushbyte      	0
    53      getproperty   	null
    56      convert_d     	
    57      setlocal3     	
    58      getlocal3     	
    59      convert_u     	
    60      setlocal      	4
    62      getlocal      	4
    64      getlocal3     	
    65      ifeq          	L3

    69      findpropstrict	Error
    72      getproperty   	Error
    75      findpropstrict	RangeError
    78      getproperty   	RangeError
    81      pushshort     	1005
    84      getlocal3     	
    85      callproperty  	throwError (3)
    89      pop           	
    
    L3: 
    90      findproperty  	length
    93      getlocal      	4
    95      initproperty  	length
    98      jump          	L4
    
    L2: 
    102     findproperty  	length
    105     getlocal2     	
    106     initproperty  	length
    109     pushbyte      	0
    111     convert_u     	
    112     setlocal      	5
    114     jump          	L5

    
    L6: 
    118     label         	
    119     getlocal0     	
    120     getlocal      	5
    122     getlocal1     	
    123     getlocal      	5
    125     getproperty   	null
    128     initproperty  	null
    131     getlocal      	5
    133     increment     	
    134     convert_u     	
    135     setlocal      	5
    
    L5: 
    137     getlocal      	5
    139     getlocal2     	
    140     iflt          	L6
    
    L4: 
    144     returnvoid    	
  }

  function http://adobe.com/AS3/2006/builtin::reverse():Array	/* disp_id 10*/
  {
    // local_count=1 max_scope=1 max_stack=2 code_len=11
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	_reverse
    5       getlocal0     	
    6       callproperty  	_reverse (1)
    10      returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::map(Function,*):Array	/* disp_id 23*/
  {
    // local_count=3 max_scope=1 max_stack=4 code_len=13
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	_map
    5       getlocal0     	
    6       getlocal1     	
    7       getlocal2     	
    8       callproperty  	_map (3)
    12      returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::shift():*	/* disp_id 12*/
  {
    // local_count=1 max_scope=1 max_stack=2 code_len=11
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	_shift
    5       getlocal0     	
    6       callproperty  	_shift (1)
    10      returnvalue   	
  }

  native function get length():uint	/* disp_id 5*/

  native function http://adobe.com/AS3/2006/builtin::unshift():uint	/* disp_id 14*/

  function http://adobe.com/AS3/2006/builtin::join(*):String	/* disp_id 7*/
  {
    // local_count=2 max_scope=1 max_stack=3 code_len=12
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	_join
    5       getlocal0     	
    6       getlocal1     	
    7       callproperty  	_join (2)
    11      returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::lastIndexOf(*,*):int	/* disp_id 19*/
  {
    // local_count=3 max_scope=1 max_stack=5 code_len=20
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	_lastIndexOf
    5       getlocal0     	
    6       getlocal1     	
    7       findpropstrict	int
    10      getlocal2     	
    11      callproperty  	int (1)
    15      callproperty  	_lastIndexOf (3)
    19      returnvalue   	
  }

  native function set length(uint):*	/* disp_id 6*/

  function http://adobe.com/AS3/2006/builtin::indexOf(*,*):int	/* disp_id 18*/
  {
    // local_count=3 max_scope=1 max_stack=5 code_len=20
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	_indexOf
    5       getlocal0     	
    6       getlocal1     	
    7       findpropstrict	int
    10      getlocal2     	
    11      callproperty  	int (1)
    15      callproperty  	_indexOf (3)
    19      returnvalue   	
  }

  native function http://adobe.com/AS3/2006/builtin::pop():*	/* disp_id 8*/

  function http://adobe.com/AS3/2006/builtin::slice(*,*):Array	/* disp_id 13*/
  {
    // local_count=3 max_scope=1 max_stack=5 code_len=27
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	_slice
    5       getlocal0     	
    6       findpropstrict	Number
    9       getlocal1     	
    10      callproperty  	Number (1)
    14      findpropstrict	Number
    17      getlocal2     	
    18      callproperty  	Number (1)
    22      callproperty  	_slice (3)
    26      returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::concat():Array	/* disp_id 11*/
  {
    // local_count=2 max_scope=1 max_stack=3 code_len=12
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	_concat
    5       getlocal0     	
    6       getlocal1     	
    7       callproperty  	_concat (2)
    11      returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::some(Function,*):Boolean	/* disp_id 24*/
  {
    // local_count=3 max_scope=1 max_stack=4 code_len=13
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	_some
    5       getlocal0     	
    6       getlocal1     	
    7       getlocal2     	
    8       callproperty  	_some (3)
    12      returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::filter(Function,*):Array	/* disp_id 21*/
  {
    // local_count=3 max_scope=1 max_stack=4 code_len=13
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	_filter
    5       getlocal0     	
    6       getlocal1     	
    7       getlocal2     	
    8       callproperty  	_filter (3)
    12      returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::forEach(Function,*):void	/* disp_id 22*/
  {
    // local_count=3 max_scope=1 max_stack=4 code_len=14
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	_forEach
    5       getlocal0     	
    6       getlocal1     	
    7       getlocal2     	
    8       callproperty  	_forEach (3)
    12      pop           	
    13      returnvoid    	
  }

  native function http://adobe.com/AS3/2006/builtin::push():uint	/* disp_id 9*/

  function http://adobe.com/AS3/2006/builtin::every(Function,*):Boolean	/* disp_id 20*/
  {
    // local_count=3 max_scope=1 max_stack=4 code_len=13
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	_every
    5       getlocal0     	
    6       getlocal1     	
    7       getlocal2     	
    8       callproperty  	_every (3)
    12      returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::splice():*	/* disp_id 15*/
  {
    // local_count=2 max_scope=1 max_stack=3 code_len=25
    0       getlocal0     	
    1       pushscope     	
    2       getlocal1     	
    3       getproperty   	length
    6       not           	
    7       iffalse       	L1

    11      getglobalscope	
    12      getslot       	17
    14      returnvalue   	
    
    L1: 
    15      findpropstrict	_splice
    18      getlocal0     	
    19      getlocal1     	
    20      callproperty  	_splice (2)
    24      returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::sortOn(*,*):*	/* disp_id 17*/
  {
    // local_count=4 max_scope=1 max_stack=4 code_len=13
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	_sortOn
    5       getlocal0     	
    6       getlocal1     	
    7       getlocal2     	
    8       callproperty  	_sortOn (3)
    12      returnvalue   	
  }

  function http://adobe.com/AS3/2006/builtin::sort():*	/* disp_id 16*/
  {
    // local_count=2 max_scope=1 max_stack=3 code_len=12
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	_sort
    5       getlocal0     	
    6       getlocal1     	
    7       callproperty  	_sort (2)
    11      returnvalue   	
  }
  static const CASEINSENSITIVE:uint = 1	/* slot_id 1 */

  static native function private::_shift(*):*	/* disp_id 7*/

  static function private::_join(*,*):String	/* disp_id 3*/
  {
    // local_count=8 max_scope=1 max_stack=2 code_len=121
    0       getlocal0     	
    1       pushscope     	
    2       pushundefined 	
    3       coerce_a      	
    4       setlocal      	7
    6       getlocal2     	
    7       getglobalscope	
    8       getslot       	17
    10      ifstrictne    	L1

    14      pushstring    	","
    17      coerce_s      	
    18      jump          	L2
    
    L1: 
    22      findpropstrict	String
    25      getlocal2     	
    26      callproperty  	String (1)
    30      coerce_s      	
    
    L2: 
    31      coerce_s      	
    32      setlocal3     	
    33      pushstring    	""
    35      coerce_s      	
    36      setlocal      	4
    38      pushbyte      	0
    40      convert_u     	
    41      setlocal      	5
    43      findpropstrict	uint
    46      getlocal1     	
    47      getproperty   	length
    50      callproperty  	uint (1)
    54      convert_u     	
    55      setlocal      	6
    57      jump          	L3

    
    L4: 
    61      label         	
    62      getlocal1     	
    63      getlocal      	5
    65      getproperty   	null
    68      coerce_a      	
    69      setlocal      	7
    71      getlocal      	7
    73      pushnull      	
    74      ifeq          	L5

    78      getlocal      	4
    80      getlocal      	7
    82      add           	
    83      coerce_s      	
    84      setlocal      	4
    
    L5: 
    86      getlocal      	5
    88      pushbyte      	1
    90      add           	
    91      getlocal      	6
    93      ifnlt         	L6

    97      getlocal      	4
    99      getlocal3     	
    100     add           	
    101     coerce_s      	
    102     setlocal      	4
    
    L6: 
    104     getlocal      	5
    106     increment     	
    107     convert_u     	
    108     setlocal      	5
    
    L3: 
    110     getlocal      	5
    112     getlocal      	6
    114     iflt          	L4

    118     getlocal      	4
    120     returnvalue   	
  }

  static native function private::_indexOf(*,*,int):int	/* disp_id 12*/

  static native function private::_pop(*):*	/* disp_id 4*/

  static native function private::_slice(*,Number,Number):Array	/* disp_id 8*/

  static native function private::_lastIndexOf(*,*,int):int	/* disp_id 13*/

  static native function private::_filter(*,Function,*):Array	/* disp_id 15*/

  static native function private::_splice(*,Array):Array	/* disp_id 9*/

  static native function private::_every(*,Function,*):Boolean	/* disp_id 14*/

  static native function private::_map(*,Function,*):Array	/* disp_id 17*/

  static native function private::_reverse(*):*	/* disp_id 5*/
  static const length:int = 1	/* slot_id 6 */

  static native function private::_forEach(*,Function,*):void	/* disp_id 16*/
  static const NUMERIC:uint = 16	/* slot_id 5 */
  static const UNIQUESORT:uint = 4	/* slot_id 3 */

  static native function private::_concat(*,Array):Array	/* disp_id 6*/

  static native function private::_some(*,Function,*):Boolean	/* disp_id 18*/
  static const RETURNINDEXEDARRAY:uint = 8	/* slot_id 4 */

  static native function private::_sortOn(*,*,*):*	/* disp_id 11*/

  static native function private::_sort(*,Array):*	/* disp_id 10*/
  static const DESCENDING:uint = 2	/* slot_id 2 */

  static function Array$cinit():*	/* disp_id 0*/
  {
    // local_count=1 max_scope=1 max_stack=2 code_len=303
    0       getlocal0     	
    1       pushscope     	
    2       findproperty  	CASEINSENSITIVE
    5       pushbyte      	1
    7       initproperty  	CASEINSENSITIVE
    10      findproperty  	DESCENDING
    13      pushbyte      	2
    15      initproperty  	DESCENDING
    18      findproperty  	UNIQUESORT
    21      pushbyte      	4
    23      initproperty  	UNIQUESORT
    26      findproperty  	RETURNINDEXEDARRAY
    29      pushbyte      	8
    31      initproperty  	RETURNINDEXEDARRAY
    34      findproperty  	NUMERIC
    37      pushbyte      	16
    39      initproperty  	NUMERIC
    42      findproperty  	length
    44      pushbyte      	1
    46      initproperty  	length
    48      findpropstrict	prototype
    51      getproperty   	prototype
    54      newfunction   	var undefined(*):String	/* disp_id 0*/
    57      setproperty   	join
    60      findpropstrict	prototype
    63      getproperty   	prototype
    66      newfunction   	var undefined():*	/* disp_id 0*/
    69      setproperty   	pop
    72      findpropstrict	prototype
    75      getproperty   	prototype
    78      newfunction   	var undefined():String	/* disp_id 0*/
    81      setproperty   	toString
    84      findpropstrict	prototype
    87      getproperty   	prototype
    90      newfunction   	var undefined():String	/* disp_id 0*/
    93      setproperty   	toLocaleString
    96      findpropstrict	prototype
    99      getproperty   	prototype
    102     newfunction   	var undefined():uint	/* disp_id 0*/
    105     setproperty   	push
    108     findpropstrict	prototype
    111     getproperty   	prototype
    114     newfunction   	var undefined():*	/* disp_id 0*/
    117     setproperty   	reverse
    120     findpropstrict	prototype
    123     getproperty   	prototype
    126     newfunction   	var undefined():Array	/* disp_id 0*/
    129     setproperty   	concat
    132     findpropstrict	prototype
    135     getproperty   	prototype
    138     newfunction   	var undefined():*	/* disp_id 0*/
    141     setproperty   	shift
    144     findpropstrict	prototype
    147     getproperty   	prototype
    150     newfunction   	var undefined(*,*):Array	/* disp_id 0*/
    153     setproperty   	slice
    156     findpropstrict	prototype
    159     getproperty   	prototype
    162     newfunction   	var undefined():uint	/* disp_id 0*/
    165     setproperty   	unshift
    168     findpropstrict	prototype
    171     getproperty   	prototype
    174     newfunction   	var undefined():*	/* disp_id 0*/
    177     setproperty   	splice
    180     findpropstrict	prototype
    183     getproperty   	prototype
    186     newfunction   	var undefined():*	/* disp_id 0*/
    189     setproperty   	sort
    192     findpropstrict	prototype
    195     getproperty   	prototype
    198     newfunction   	var undefined(*,*):*	/* disp_id 0*/
    201     setproperty   	sortOn
    204     findpropstrict	prototype
    207     getproperty   	prototype
    210     newfunction   	var undefined(*,*):int	/* disp_id 0*/
    213     setproperty   	indexOf
    216     findpropstrict	prototype
    219     getproperty   	prototype
    222     newfunction   	var undefined(*,*):int	/* disp_id 0*/
    225     setproperty   	lastIndexOf
    228     findpropstrict	prototype
    231     getproperty   	prototype
    234     newfunction   	var undefined(Function,*):Boolean	/* disp_id 0*/
    237     setproperty   	every
    240     findpropstrict	prototype
    243     getproperty   	prototype
    246     newfunction   	var undefined(Function,*):Array	/* disp_id 0*/
    249     setproperty   	filter
    252     findpropstrict	prototype
    255     getproperty   	prototype
    258     newfunction   	var undefined(Function,*):void	/* disp_id 0*/
    261     setproperty   	forEach
    264     findpropstrict	prototype
    267     getproperty   	prototype
    270     newfunction   	var undefined(Function,*):Array	/* disp_id 0*/
    273     setproperty   	map
    276     findpropstrict	prototype
    279     getproperty   	prototype
    282     newfunction   	var undefined(Function,*):Boolean	/* disp_id 0*/
    285     setproperty   	some
    288     findpropstrict	_dontEnumPrototype
    291     findpropstrict	prototype
    294     getproperty   	prototype
    297     callproperty  	_dontEnumPrototype (1)
    301     pop           	
    302     returnvoid    	
  }
}

final class private::MethodClosure extends Function
{

  function private::MethodClosure():*	/* disp_id -1*/
  {
    // local_count=1 max_scope=1 max_stack=1 code_len=6
    0       getlocal0     	
    1       pushscope     	
    2       getlocal0     	
    3       constructsuper	(0)
    5       returnvoid    	
  }

  function get prototype():*	/* disp_id 5*/
  {
    // local_count=1 max_scope=1 max_stack=1 code_len=4
    0       getlocal0     	
    1       pushscope     	
    2       pushnull      	
    3       returnvalue   	
  }

  function set prototype(*):*	/* disp_id 6*/
  {
    // local_count=2 max_scope=1 max_stack=5 code_len=28
    0       getlocal0     	
    1       pushscope     	
    2       findpropstrict	Error
    5       getproperty   	Error
    8       findpropstrict	ReferenceError
    11      getproperty   	ReferenceError
    14      pushshort     	1074
    17      pushstring    	"prototype"
    19      pushstring    	"MethodClosure"
    22      callproperty  	throwError (4)
    26      pop           	
    27      returnvoid    	
  }

  native function get length():int	/* disp_id 7*/

  static function private::MethodClosure$cinit():*	/* disp_id 0*/
  {
    // local_count=1 max_scope=1 max_stack=1 code_len=3
    0       getlocal0     	
    1       pushscope     	
    2       returnvoid    	
  }
}

native function parseInt(String,int):Number	/* disp_id 7*/

native function encodeURI(String):String	/* disp_id 3*/

native function decodeURIComponent(String):String	/* disp_id 2*/

function script0$init():*	/* disp_id 0*/
{
  // local_count=2 max_scope=3 max_stack=3 code_len=236
  0         getlocal0     	
  1         pushscope     	
  2         getscopeobject	0
  4         pushnull      	
  5         newclass      	Object
  7         initproperty  	Object
  9         getscopeobject	0
  11        findpropstrict	Object
  13        getproperty   	Object
  15        pushscope     	
  16        getglobalscope	
  17        getslot       	2
  19        newclass      	Class
  21        popscope      	
  22        initproperty  	Class
  24        getscopeobject	0
  26        findpropstrict	Object
  28        getproperty   	Object
  30        pushscope     	
  31        getglobalscope	
  32        getslot       	2
  34        newclass      	Function
  36        popscope      	
  37        initproperty  	Function
  39        getscopeobject	0
  41        findpropstrict	Object
  43        getproperty   	Object
  45        pushscope     	
  46        findpropstrict	Function
  48        getproperty   	Function
  50        pushscope     	
  51        getglobalscope	
  52        getslot       	4
  54        newclass      	private::MethodClosure
  56        popscope      	
  57        popscope      	
  58        initproperty  	private::MethodClosure
  61        getscopeobject	0
  63        findpropstrict	Object
  65        getproperty   	Object
  67        pushscope     	
  68        getglobalscope	
  69        getslot       	2
  71        newclass      	Namespace
  73        popscope      	
  74        initproperty  	Namespace
  76        getscopeobject	0
  78        findpropstrict	Object
  80        getproperty   	Object
  82        pushscope     	
  83        getglobalscope	
  84        getslot       	2
  86        newclass      	Boolean
  88        popscope      	
  89        initproperty  	Boolean
  91        getscopeobject	0
  93        findpropstrict	Object
  95        getproperty   	Object
  97        pushscope     	
  98        getglobalscope	
  99        getslot       	2
  101       newclass      	Number
  103       popscope      	
  104       initproperty  	Number
  106       getscopeobject	0
  108       findpropstrict	Object
  110       getproperty   	Object
  112       pushscope     	
  113       getglobalscope	
  114       getslot       	2
  116       newclass      	int
  118       popscope      	
  119       initproperty  	int
  121       getscopeobject	0
  123       findpropstrict	Object
  125       getproperty   	Object
  127       pushscope     	
  128       getglobalscope	
  129       getslot       	2
  131       newclass      	uint
  133       popscope      	
  134       initproperty  	uint
  136       getscopeobject	0
  138       findpropstrict	Object
  140       getproperty   	Object
  142       pushscope     	
  143       getglobalscope	
  144       getslot       	2
  146       newclass      	double
  148       popscope      	
  149       initproperty  	double
  151       getscopeobject	0
  153       findpropstrict	Object
  155       getproperty   	Object
  157       pushscope     	
  158       getglobalscope	
  159       getslot       	2
  161       newclass      	decimal
  163       popscope      	
  164       initproperty  	decimal
  167       getscopeobject	0
  169       findpropstrict	Object
  171       getproperty   	Object
  173       pushscope     	
  174       getglobalscope	
  175       getslot       	2
  177       newclass      	String
  179       popscope      	
  180       initproperty  	String
  182       getscopeobject	0
  184       findpropstrict	Object
  186       getproperty   	Object
  188       pushscope     	
  189       getglobalscope	
  190       getslot       	2
  192       newclass      	Array
  194       popscope      	
  195       initproperty  	Array
  198       getglobalscope	
  199       getslot       	2
  201       callproperty  	init (0)
  205       coerce_a      	
  206       setlocal1     	
  207       getscopeobject	0
  209       pushbyte      	0
  211       pushbyte      	0
  213       divide        	
  214       initproperty  	NaN
  216       getscopeobject	0
  218       pushbyte      	1
  220       pushbyte      	0
  222       divide        	
  223       initproperty  	Infinity
  226       getscopeobject	0
  228       pushundefined 	
  229       initproperty  	undefined
  232       getlocal1     	
  233       returnvalue   	
  234       kill          	1
}

var undefined(*):Boolean	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=2 code_len=6
  0         getlocal0     	
  1         getlocal1     	
  2         callproperty  	http://adobe.com/AS3/2006/builtin::hasOwnProperty (1)
  5         returnvalue   	
}

var undefined(*):*	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=2 code_len=6
  0         getlocal0     	
  1         getlocal1     	
  2         callproperty  	http://adobe.com/AS3/2006/builtin::propertyIsEnumerable (1)
  5         returnvalue   	
}

var undefined(String,Boolean):void	/* disp_id 0*/
{
  // local_count=3 max_scope=0 max_stack=4 code_len=10
  0         findpropstrict	_setPropertyIsEnumerable
  2         getlocal0     	
  3         getlocal1     	
  4         getlocal2     	
  5         callproperty  	_setPropertyIsEnumerable (3)
  8         pop           	
  9         returnvoid    	
}

var undefined(*):Boolean	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=2 code_len=6
  0         getlocal0     	
  1         getlocal1     	
  2         callproperty  	http://adobe.com/AS3/2006/builtin::isPrototypeOf (1)
  5         returnvalue   	
}

var undefined():String	/* disp_id 0*/
{
  // local_count=1 max_scope=0 max_stack=2 code_len=7
  0         findpropstrict	_toString
  2         getlocal0     	
  3         callproperty  	_toString (1)
  6         returnvalue   	
}

var undefined():*	/* disp_id 0*/
{
  // local_count=1 max_scope=0 max_stack=1 code_len=2
  0         getlocal0     	
  1         returnvalue   	
}

var undefined():String	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=1 code_len=6
  0         getlocal0     	
  1         coerce_a      	
  2         setlocal1     	
  3         pushstring    	"function Function() {}"
  5         returnvalue   	
}

var undefined(*):*	/* disp_id 0*/
{
  // local_count=4 max_scope=0 max_stack=3 code_len=11
  0         getlocal0     	
  1         coerce        	Function
  3         setlocal3     	
  4         getlocal3     	
  5         getlocal1     	
  6         getlocal2     	
  7         callproperty  	http://adobe.com/AS3/2006/builtin::apply (2)
  10        returnvalue   	
}

var undefined(*,*):*	/* disp_id 0*/
{
  // local_count=4 max_scope=0 max_stack=3 code_len=11
  0         getlocal0     	
  1         coerce        	Function
  3         setlocal3     	
  4         getlocal3     	
  5         getlocal1     	
  6         getlocal2     	
  7         callproperty  	http://adobe.com/AS3/2006/builtin::apply (2)
  10        returnvalue   	
}

var undefined():*	/* disp_id 0*/
{
  // local_count=1 max_scope=0 max_stack=0 code_len=1
  0         returnvoid    	
}

var undefined():String	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=2 code_len=20
  0         getlocal0     	
  1         findpropstrict	prototype
  3         getproperty   	prototype
  5         ifstrictne    	L1

  9         pushstring    	""
  11        returnvalue   	
  
  L1: 
  12        getlocal0     	
  13        coerce        	Namespace
  15        setlocal1     	
  16        getlocal1     	
  17        getproperty   	uri
  19        returnvalue   	
}

var undefined():String	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=2 code_len=20
  0         getlocal0     	
  1         findpropstrict	prototype
  3         getproperty   	prototype
  5         ifstrictne    	L1

  9         pushstring    	""
  11        returnvalue   	
  
  L1: 
  12        getlocal0     	
  13        coerce        	Namespace
  15        setlocal1     	
  16        getlocal1     	
  17        getproperty   	uri
  19        returnvalue   	
}

var undefined():String	/* disp_id 0*/
{
  // local_count=1 max_scope=0 max_stack=4 code_len=56
  0         getlocal0     	
  1         findpropstrict	prototype
  3         getproperty   	prototype
  5         ifstrictne    	L1

  9         pushstring    	"false"
  11        returnvalue   	
  
  L1: 
  12        getlocal0     	
  13        getglobalscope	
  14        getslot       	7
  16        istypelate    	
  17        not           	
  18        iffalse       	L2

  22        findpropstrict	Error
  24        getproperty   	Error
  26        findpropstrict	TypeError
  28        getproperty   	TypeError
  30        pushshort     	1004
  33        pushstring    	"Boolean.prototype.toString"
  35        callproperty  	throwError (3)
  38        pop           	
  
  L2: 
  39        getlocal0     	
  40        convert_b     	
  41        iffalse       	L3

  45        pushstring    	"true"
  47        coerce_s      	
  48        jump          	L4
  
  L3: 
  52        pushstring    	"false"
  54        coerce_s      	
  
  L4: 
  55        returnvalue   	
}

var undefined():*	/* disp_id 0*/
{
  // local_count=1 max_scope=0 max_stack=4 code_len=40
  0         getlocal0     	
  1         findpropstrict	prototype
  3         getproperty   	prototype
  5         ifstrictne    	L1

  9         pushfalse     	
  10        returnvalue   	
  
  L1: 
  11        getlocal0     	
  12        getglobalscope	
  13        getslot       	7
  15        istypelate    	
  16        not           	
  17        iffalse       	L2

  21        findpropstrict	Error
  23        getproperty   	Error
  25        findpropstrict	TypeError
  27        getproperty   	TypeError
  29        pushshort     	1004
  32        pushstring    	"Boolean.prototype.valueOf"
  34        callproperty  	throwError (3)
  37        pop           	
  
  L2: 
  38        getlocal0     	
  39        returnvalue   	
}

var undefined(*):String	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=4 code_len=47
  0         getlocal0     	
  1         findpropstrict	prototype
  3         getproperty   	prototype
  5         ifstrictne    	L1

  9         pushstring    	"0"
  11        returnvalue   	
  
  L1: 
  12        getlocal0     	
  13        getglobalscope	
  14        getslot       	8
  16        istypelate    	
  17        not           	
  18        iffalse       	L2

  22        findpropstrict	Error
  24        getproperty   	Error
  26        findpropstrict	TypeError
  28        getproperty   	TypeError
  30        pushshort     	1004
  33        pushstring    	"Number.prototype.toString"
  35        callproperty  	throwError (3)
  38        pop           	
  
  L2: 
  39        findpropstrict	_toString
  41        getlocal0     	
  42        getlocal1     	
  43        callproperty  	_toString (2)
  46        returnvalue   	
}

var undefined():*	/* disp_id 0*/
{
  // local_count=1 max_scope=0 max_stack=4 code_len=41
  0         getlocal0     	
  1         findpropstrict	prototype
  3         getproperty   	prototype
  5         ifstrictne    	L1

  9         pushbyte      	0
  11        returnvalue   	
  
  L1: 
  12        getlocal0     	
  13        getglobalscope	
  14        getslot       	8
  16        istypelate    	
  17        not           	
  18        iffalse       	L2

  22        findpropstrict	Error
  24        getproperty   	Error
  26        findpropstrict	TypeError
  28        getproperty   	TypeError
  30        pushshort     	1004
  33        pushstring    	"Number.prototype.valueOf"
  35        callproperty  	throwError (3)
  38        pop           	
  
  L2: 
  39        getlocal0     	
  40        returnvalue   	
}

var undefined(*):String	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=4 code_len=22
  0         findpropstrict	_convert
  2         findpropstrict	Number
  4         getlocal0     	
  5         callproperty  	Number (1)
  8         findpropstrict	int
  10        getlocal1     	
  11        callproperty  	int (1)
  14        findpropstrict	DTOSTR_EXPONENTIAL
  16        getproperty   	DTOSTR_EXPONENTIAL
  18        callproperty  	_convert (3)
  21        returnvalue   	
}

var undefined(*):String	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=4 code_len=35
  0         getlocal1     	
  1         getglobalscope	
  2         getslot       	17
  4         ifne          	L1

  8         getlocal0     	
  9         callproperty  	toString (0)
  12        returnvalue   	
  
  L1: 
  13        findpropstrict	_convert
  15        findpropstrict	Number
  17        getlocal0     	
  18        callproperty  	Number (1)
  21        findpropstrict	int
  23        getlocal1     	
  24        callproperty  	int (1)
  27        findpropstrict	DTOSTR_PRECISION
  29        getproperty   	DTOSTR_PRECISION
  31        callproperty  	_convert (3)
  34        returnvalue   	
}

var undefined(*):String	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=4 code_len=22
  0         findpropstrict	_convert
  2         findpropstrict	Number
  4         getlocal0     	
  5         callproperty  	Number (1)
  8         findpropstrict	int
  10        getlocal1     	
  11        callproperty  	int (1)
  14        findpropstrict	DTOSTR_FIXED
  16        getproperty   	DTOSTR_FIXED
  18        callproperty  	_convert (3)
  21        returnvalue   	
}

var undefined(*):String	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=4 code_len=50
  0         getlocal0     	
  1         findpropstrict	prototype
  3         getproperty   	prototype
  5         ifstrictne    	L1

  9         pushstring    	"0"
  11        returnvalue   	
  
  L1: 
  12        getlocal0     	
  13        getglobalscope	
  14        getslot       	9
  16        istypelate    	
  17        not           	
  18        iffalse       	L2

  22        findpropstrict	Error
  24        getproperty   	Error
  26        findpropstrict	TypeError
  28        getproperty   	TypeError
  30        pushshort     	1004
  33        pushstring    	"int.prototype.toString"
  35        callproperty  	throwError (3)
  38        pop           	
  
  L2: 
  39        findpropstrict	Number
  41        getlocal0     	
  42        callproperty  	Number (1)
  45        getlocal1     	
  46        callproperty  	toString (1)
  49        returnvalue   	
}

var undefined():*	/* disp_id 0*/
{
  // local_count=1 max_scope=0 max_stack=4 code_len=41
  0         getlocal0     	
  1         findpropstrict	prototype
  3         getproperty   	prototype
  5         ifstrictne    	L1

  9         pushbyte      	0
  11        returnvalue   	
  
  L1: 
  12        getlocal0     	
  13        getglobalscope	
  14        getslot       	9
  16        istypelate    	
  17        not           	
  18        iffalse       	L2

  22        findpropstrict	Error
  24        getproperty   	Error
  26        findpropstrict	TypeError
  28        getproperty   	TypeError
  30        pushshort     	1004
  33        pushstring    	"int.prototype.valueOf"
  35        callproperty  	throwError (3)
  38        pop           	
  
  L2: 
  39        getlocal0     	
  40        returnvalue   	
}

var undefined(*):String	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=2 code_len=11
  0         findpropstrict	Number
  2         getlocal0     	
  3         callproperty  	Number (1)
  6         getlocal1     	
  7         callproperty  	http://adobe.com/AS3/2006/builtin::toExponential (1)
  10        returnvalue   	
}

var undefined(*):String	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=2 code_len=11
  0         findpropstrict	Number
  2         getlocal0     	
  3         callproperty  	Number (1)
  6         getlocal1     	
  7         callproperty  	http://adobe.com/AS3/2006/builtin::toPrecision (1)
  10        returnvalue   	
}

var undefined(*):String	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=2 code_len=11
  0         findpropstrict	Number
  2         getlocal0     	
  3         callproperty  	Number (1)
  6         getlocal1     	
  7         callproperty  	http://adobe.com/AS3/2006/builtin::toFixed (1)
  10        returnvalue   	
}

var undefined(*):String	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=4 code_len=50
  0         getlocal0     	
  1         findpropstrict	prototype
  3         getproperty   	prototype
  5         ifstrictne    	L1

  9         pushstring    	"0"
  11        returnvalue   	
  
  L1: 
  12        getlocal0     	
  13        getglobalscope	
  14        getslot       	8
  16        istypelate    	
  17        not           	
  18        iffalse       	L2

  22        findpropstrict	Error
  24        getproperty   	Error
  26        findpropstrict	TypeError
  28        getproperty   	TypeError
  30        pushshort     	1004
  33        pushstring    	"uint.prototype.toString"
  35        callproperty  	throwError (3)
  38        pop           	
  
  L2: 
  39        findpropstrict	Number
  41        getlocal0     	
  42        callproperty  	Number (1)
  45        getlocal1     	
  46        callproperty  	toString (1)
  49        returnvalue   	
}

var undefined():*	/* disp_id 0*/
{
  // local_count=1 max_scope=0 max_stack=4 code_len=41
  0         getlocal0     	
  1         findpropstrict	prototype
  3         getproperty   	prototype
  5         ifstrictne    	L1

  9         pushbyte      	0
  11        returnvalue   	
  
  L1: 
  12        getlocal0     	
  13        getglobalscope	
  14        getslot       	10
  16        istypelate    	
  17        not           	
  18        iffalse       	L2

  22        findpropstrict	Error
  24        getproperty   	Error
  26        findpropstrict	TypeError
  28        getproperty   	TypeError
  30        pushshort     	1004
  33        pushstring    	"uint.prototype.valueOf"
  35        callproperty  	throwError (3)
  38        pop           	
  
  L2: 
  39        getlocal0     	
  40        returnvalue   	
}

var undefined(*):String	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=2 code_len=11
  0         findpropstrict	Number
  2         getlocal0     	
  3         callproperty  	Number (1)
  6         getlocal1     	
  7         callproperty  	http://adobe.com/AS3/2006/builtin::toExponential (1)
  10        returnvalue   	
}

var undefined(*):String	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=2 code_len=11
  0         findpropstrict	Number
  2         getlocal0     	
  3         callproperty  	Number (1)
  6         getlocal1     	
  7         callproperty  	http://adobe.com/AS3/2006/builtin::toPrecision (1)
  10        returnvalue   	
}

var undefined(*):String	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=2 code_len=11
  0         findpropstrict	Number
  2         getlocal0     	
  3         callproperty  	Number (1)
  6         getlocal1     	
  7         callproperty  	http://adobe.com/AS3/2006/builtin::toFixed (1)
  10        returnvalue   	
}

var undefined(*):String	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=4 code_len=55
  0         getlocal0     	
  1         findpropstrict	prototype
  3         getproperty   	prototype
  5         ifstrictne    	L1

  9         pushstring    	"0"
  11        returnvalue   	
  
  L1: 
  12        getlocal0     	
  13        getglobalscope	
  14        getslot       	11
  16        istypelate    	
  17        not           	
  18        iffalse       	L2

  22        findpropstrict	Error
  24        getproperty   	Error
  26        findpropstrict	TypeError
  29        getproperty   	TypeError
  32        pushshort     	1004
  35        pushstring    	"double.prototype.toString"
  37        callproperty  	throwError (3)
  41        pop           	
  
  L2: 
  42        findpropstrict	Number
  45        getlocal0     	
  46        callproperty  	Number (1)
  50        getlocal1     	
  51        callproperty  	toString (1)
  54        returnvalue   	
}

var undefined():*	/* disp_id 0*/
{
  // local_count=1 max_scope=0 max_stack=4 code_len=44
  0         getlocal0     	
  1         findpropstrict	prototype
  3         getproperty   	prototype
  5         ifstrictne    	L1

  9         pushbyte      	0
  11        returnvalue   	
  
  L1: 
  12        getlocal0     	
  13        getglobalscope	
  14        getslot       	11
  16        istypelate    	
  17        not           	
  18        iffalse       	L2

  22        findpropstrict	Error
  24        getproperty   	Error
  26        findpropstrict	TypeError
  29        getproperty   	TypeError
  32        pushshort     	1004
  35        pushstring    	"double.prototype.valueOf"
  37        callproperty  	throwError (3)
  41        pop           	
  
  L2: 
  42        getlocal0     	
  43        returnvalue   	
}

var undefined(*):String	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=2 code_len=13
  0         findpropstrict	Number
  3         getlocal0     	
  4         callproperty  	Number (1)
  8         getlocal1     	
  9         callproperty  	http://adobe.com/AS3/2006/builtin::toExponential (1)
  12        returnvalue   	
}

var undefined(*):String	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=2 code_len=13
  0         findpropstrict	Number
  3         getlocal0     	
  4         callproperty  	Number (1)
  8         getlocal1     	
  9         callproperty  	http://adobe.com/AS3/2006/builtin::toPrecision (1)
  12        returnvalue   	
}

var undefined(*):String	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=2 code_len=13
  0         findpropstrict	Number
  3         getlocal0     	
  4         callproperty  	Number (1)
  8         getlocal1     	
  9         callproperty  	http://adobe.com/AS3/2006/builtin::toFixed (1)
  12        returnvalue   	
}

var undefined(*):String	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=4 code_len=56
  0         getlocal0     	
  1         findpropstrict	prototype
  4         getproperty   	prototype
  7         ifstrictne    	L1

  11        pushstring    	"0"
  13        returnvalue   	
  
  L1: 
  14        getlocal0     	
  15        getglobalscope	
  16        getslot       	12
  18        istypelate    	
  19        not           	
  20        iffalse       	L2

  24        findpropstrict	Error
  27        getproperty   	Error
  30        findpropstrict	TypeError
  33        getproperty   	TypeError
  36        pushshort     	1004
  39        pushstring    	"decimal.prototype.toString"
  41        callproperty  	throwError (3)
  45        pop           	
  
  L2: 
  46        findpropstrict	_toString
  49        getlocal0     	
  50        getlocal1     	
  51        callproperty  	_toString (2)
  55        returnvalue   	
}

var undefined():*	/* disp_id 0*/
{
  // local_count=1 max_scope=0 max_stack=4 code_len=48
  0         getlocal0     	
  1         findpropstrict	prototype
  4         getproperty   	prototype
  7         ifstrictne    	L1

  11        pushbyte      	0
  13        returnvalue   	
  
  L1: 
  14        getlocal0     	
  15        getglobalscope	
  16        getslot       	12
  18        istypelate    	
  19        not           	
  20        iffalse       	L2

  24        findpropstrict	Error
  27        getproperty   	Error
  30        findpropstrict	TypeError
  33        getproperty   	TypeError
  36        pushshort     	1004
  39        pushstring    	"decimal.prototype.valueOf"
  41        callproperty  	throwError (3)
  45        pop           	
  
  L2: 
  46        getlocal0     	
  47        returnvalue   	
}

var undefined(*):String	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=4 code_len=30
  0         findpropstrict	_convert
  3         findpropstrict	decimal
  6         getlocal0     	
  7         callproperty  	decimal (1)
  11        findpropstrict	int
  14        getlocal1     	
  15        callproperty  	int (1)
  19        findpropstrict	DTOSTR_EXPONENTIAL
  22        getproperty   	DTOSTR_EXPONENTIAL
  25        callproperty  	_convert (3)
  29        returnvalue   	
}

var undefined(*):String	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=4 code_len=44
  0         getlocal1     	
  1         getglobalscope	
  2         getslot       	17
  4         ifne          	L1

  8         getlocal0     	
  9         callproperty  	toString (0)
  13        returnvalue   	
  
  L1: 
  14        findpropstrict	_convert
  17        findpropstrict	decimal
  20        getlocal0     	
  21        callproperty  	decimal (1)
  25        findpropstrict	int
  28        getlocal1     	
  29        callproperty  	int (1)
  33        findpropstrict	DTOSTR_PRECISION
  36        getproperty   	DTOSTR_PRECISION
  39        callproperty  	_convert (3)
  43        returnvalue   	
}

var undefined(*):String	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=4 code_len=30
  0         findpropstrict	_convert
  3         findpropstrict	decimal
  6         getlocal0     	
  7         callproperty  	decimal (1)
  11        findpropstrict	int
  14        getlocal1     	
  15        callproperty  	int (1)
  19        findpropstrict	DTOSTR_FIXED
  22        getproperty   	DTOSTR_FIXED
  25        callproperty  	_convert (3)
  29        returnvalue   	
}

var undefined():*	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=3 code_len=15
  0         findpropstrict	http://adobe.com/AS3/2006/builtin::fromCharCode
  3         getproperty   	http://adobe.com/AS3/2006/builtin::fromCharCode
  6         getglobalscope	
  7         getslot       	13
  9         getlocal1     	
  10        callproperty  	apply (2)
  14        returnvalue   	
}

var undefined(String,Number):int	/* disp_id 0*/
{
  // local_count=3 max_scope=0 max_stack=3 code_len=15
  0         findpropstrict	String
  3         getlocal0     	
  4         callproperty  	String (1)
  8         getlocal1     	
  9         getlocal2     	
  10        callproperty  	http://adobe.com/AS3/2006/builtin::indexOf (2)
  14        returnvalue   	
}

var undefined(String,Number):int	/* disp_id 0*/
{
  // local_count=3 max_scope=0 max_stack=3 code_len=15
  0         findpropstrict	String
  3         getlocal0     	
  4         callproperty  	String (1)
  8         getlocal1     	
  9         getlocal2     	
  10        callproperty  	http://adobe.com/AS3/2006/builtin::lastIndexOf (2)
  14        returnvalue   	
}

var undefined(Number):String	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=2 code_len=14
  0         findpropstrict	String
  3         getlocal0     	
  4         callproperty  	String (1)
  8         getlocal1     	
  9         callproperty  	http://adobe.com/AS3/2006/builtin::charAt (1)
  13        returnvalue   	
}

var undefined(Number):Number	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=2 code_len=14
  0         findpropstrict	String
  3         getlocal0     	
  4         callproperty  	String (1)
  8         getlocal1     	
  9         callproperty  	http://adobe.com/AS3/2006/builtin::charCodeAt (1)
  13        returnvalue   	
}

var undefined():String	/* disp_id 0*/
{
  // local_count=5 max_scope=0 max_stack=4 code_len=55
  0         findpropstrict	String
  3         getlocal0     	
  4         callproperty  	String (1)
  8         coerce_s      	
  9         setlocal2     	
  10        pushbyte      	0
  12        convert_u     	
  13        setlocal3     	
  14        getlocal1     	
  15        getproperty   	length
  18        convert_u     	
  19        setlocal      	4
  21        jump          	L1

  
  L2: 
  25        label         	
  26        getlocal2     	
  27        findpropstrict	String
  30        getlocal1     	
  31        getlocal3     	
  32        getproperty   	null
  35        callproperty  	String (1)
  39        add           	
  40        coerce_s      	
  41        setlocal2     	
  42        getlocal3     	
  43        increment     	
  44        convert_u     	
  45        setlocal3     	
  
  L1: 
  46        getlocal3     	
  47        getlocal      	4
  49        iflt          	L2

  53        getlocal2     	
  54        returnvalue   	
}

var undefined(String):int	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=2 code_len=14
  0         findpropstrict	String
  3         getlocal0     	
  4         callproperty  	String (1)
  8         getlocal1     	
  9         callproperty  	http://adobe.com/AS3/2006/builtin::localeCompare (1)
  13        returnvalue   	
}

var undefined(*):Array	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=3 code_len=17
  0         findpropstrict	_match
  3         findpropstrict	String
  6         getlocal0     	
  7         callproperty  	String (1)
  11        getlocal1     	
  12        callproperty  	_match (2)
  16        returnvalue   	
}

var undefined(*,*):String	/* disp_id 0*/
{
  // local_count=3 max_scope=0 max_stack=4 code_len=18
  0         findpropstrict	_replace
  3         findpropstrict	String
  6         getlocal0     	
  7         callproperty  	String (1)
  11        getlocal1     	
  12        getlocal2     	
  13        callproperty  	_replace (3)
  17        returnvalue   	
}

var undefined(*):int	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=3 code_len=17
  0         findpropstrict	_search
  3         findpropstrict	String
  6         getlocal0     	
  7         callproperty  	String (1)
  11        getlocal1     	
  12        callproperty  	_search (2)
  16        returnvalue   	
}

var undefined(Number,Number):String	/* disp_id 0*/
{
  // local_count=3 max_scope=0 max_stack=3 code_len=15
  0         findpropstrict	String
  3         getlocal0     	
  4         callproperty  	String (1)
  8         getlocal1     	
  9         getlocal2     	
  10        callproperty  	http://adobe.com/AS3/2006/builtin::slice (2)
  14        returnvalue   	
}

var undefined(*,*):Array	/* disp_id 0*/
{
  // local_count=3 max_scope=0 max_stack=4 code_len=30
  0         getlocal2     	
  1         getglobalscope	
  2         getslot       	17
  4         ifne          	L1

  8         pushdouble    	NaN
  10        coerce_a      	
  11        setlocal2     	
  
  L1: 
  12        findpropstrict	_split
  15        findpropstrict	String
  18        getlocal0     	
  19        callproperty  	String (1)
  23        getlocal1     	
  24        getlocal2     	
  25        callproperty  	_split (3)
  29        returnvalue   	
}

var undefined(Number,Number):String	/* disp_id 0*/
{
  // local_count=3 max_scope=0 max_stack=3 code_len=15
  0         findpropstrict	String
  3         getlocal0     	
  4         callproperty  	String (1)
  8         getlocal1     	
  9         getlocal2     	
  10        callproperty  	http://adobe.com/AS3/2006/builtin::substring (2)
  14        returnvalue   	
}

var undefined(Number,Number):String	/* disp_id 0*/
{
  // local_count=3 max_scope=0 max_stack=3 code_len=15
  0         findpropstrict	String
  3         getlocal0     	
  4         callproperty  	String (1)
  8         getlocal1     	
  9         getlocal2     	
  10        callproperty  	http://adobe.com/AS3/2006/builtin::substr (2)
  14        returnvalue   	
}

var undefined():String	/* disp_id 0*/
{
  // local_count=1 max_scope=0 max_stack=2 code_len=13
  0         findpropstrict	String
  3         getlocal0     	
  4         callproperty  	String (1)
  8         callproperty  	http://adobe.com/AS3/2006/builtin::toLowerCase (0)
  12        returnvalue   	
}

var undefined():String	/* disp_id 0*/
{
  // local_count=1 max_scope=0 max_stack=2 code_len=13
  0         findpropstrict	String
  3         getlocal0     	
  4         callproperty  	String (1)
  8         callproperty  	http://adobe.com/AS3/2006/builtin::toUpperCase (0)
  12        returnvalue   	
}

var undefined():String	/* disp_id 0*/
{
  // local_count=1 max_scope=0 max_stack=4 code_len=48
  0         getlocal0     	
  1         findpropstrict	prototype
  4         getproperty   	prototype
  7         ifstrictne    	L1

  11        pushstring    	""
  13        returnvalue   	
  
  L1: 
  14        getlocal0     	
  15        getglobalscope	
  16        getslot       	13
  18        istypelate    	
  19        not           	
  20        iffalse       	L2

  24        findpropstrict	Error
  27        getproperty   	Error
  30        findpropstrict	TypeError
  33        getproperty   	TypeError
  36        pushshort     	1004
  39        pushstring    	"String.prototype.toString"
  41        callproperty  	throwError (3)
  45        pop           	
  
  L2: 
  46        getlocal0     	
  47        returnvalue   	
}

var undefined():*	/* disp_id 0*/
{
  // local_count=1 max_scope=0 max_stack=4 code_len=48
  0         getlocal0     	
  1         findpropstrict	prototype
  4         getproperty   	prototype
  7         ifstrictne    	L1

  11        pushstring    	""
  13        returnvalue   	
  
  L1: 
  14        getlocal0     	
  15        getglobalscope	
  16        getslot       	13
  18        istypelate    	
  19        not           	
  20        iffalse       	L2

  24        findpropstrict	Error
  27        getproperty   	Error
  30        findpropstrict	TypeError
  33        getproperty   	TypeError
  36        pushshort     	1004
  39        pushstring    	"String.prototype.valueOf"
  41        callproperty  	throwError (3)
  45        pop           	
  
  L2: 
  46        getlocal0     	
  47        returnvalue   	
}

var undefined(*):String	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=3 code_len=10
  0         findpropstrict	_join
  3         getlocal0     	
  4         getlocal1     	
  5         callproperty  	_join (2)
  9         returnvalue   	
}

var undefined():*	/* disp_id 0*/
{
  // local_count=1 max_scope=0 max_stack=2 code_len=9
  0         findpropstrict	_pop
  3         getlocal0     	
  4         callproperty  	_pop (1)
  8         returnvalue   	
}

var undefined():String	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=3 code_len=17
  0         getlocal0     	
  1         coerce        	Array
  4         setlocal1     	
  5         findpropstrict	_join
  8         getlocal1     	
  9         pushstring    	","
  12        callproperty  	_join (2)
  16        returnvalue   	
}

var undefined():String	/* disp_id 0*/
{
  // local_count=6 max_scope=0 max_stack=2 code_len=84
  0         pushundefined 	
  1         coerce_a      	
  2         setlocal      	5
  4         getlocal0     	
  5         coerce        	Array
  8         setlocal1     	
  9         pushstring    	""
  11        coerce_s      	
  12        setlocal2     	
  13        pushbyte      	0
  15        convert_u     	
  16        setlocal3     	
  17        getlocal1     	
  18        getproperty   	length
  21        convert_u     	
  22        setlocal      	4
  24        jump          	L1

  
  L2: 
  28        label         	
  29        getlocal1     	
  30        getlocal3     	
  31        getproperty   	null
  34        coerce_a      	
  35        setlocal      	5
  37        getlocal      	5
  39        pushnull      	
  40        ifeq          	L3

  44        getlocal2     	
  45        getlocal      	5
  47        callproperty  	toLocaleString (0)
  51        add           	
  52        coerce_s      	
  53        setlocal2     	
  
  L3: 
  54        getlocal3     	
  55        pushbyte      	1
  57        add           	
  58        getlocal      	4
  60        ifnlt         	L4

  64        getlocal2     	
  65        pushstring    	","
  68        add           	
  69        coerce_s      	
  70        setlocal2     	
  
  L4: 
  71        getlocal3     	
  72        increment     	
  73        convert_u     	
  74        setlocal3     	
  
  L1: 
  75        getlocal3     	
  76        getlocal      	4
  78        iflt          	L2

  82        getlocal2     	
  83        returnvalue   	
}

var undefined():uint	/* disp_id 0*/
{
  // local_count=6 max_scope=0 max_stack=4 code_len=68
  0         findpropstrict	uint
  3         getlocal0     	
  4         getproperty   	length
  7         callproperty  	uint (1)
  11        convert_u     	
  12        setlocal2     	
  13        pushbyte      	0
  15        convert_u     	
  16        setlocal3     	
  17        getlocal1     	
  18        getproperty   	length
  21        convert_u     	
  22        setlocal      	4
  24        jump          	L1

  
  L2: 
  28        label         	
  29        getlocal0     	
  30        getlocal2     	
  31        convert_d     	
  32        dup           	
  33        increment     	
  34        convert_u     	
  35        setlocal2     	
  36        setlocal      	5
  38        getlocal      	5
  40        getlocal1     	
  41        getlocal3     	
  42        getproperty   	null
  45        setproperty   	null
  48        kill          	5
  50        getlocal3     	
  51        increment     	
  52        convert_u     	
  53        setlocal3     	
  
  L1: 
  54        getlocal3     	
  55        getlocal      	4
  57        iflt          	L2

  61        getlocal0     	
  62        getlocal2     	
  63        setproperty   	length
  66        getlocal2     	
  67        returnvalue   	
}

var undefined():*	/* disp_id 0*/
{
  // local_count=1 max_scope=0 max_stack=2 code_len=9
  0         findpropstrict	_reverse
  3         getlocal0     	
  4         callproperty  	_reverse (1)
  8         returnvalue   	
}

var undefined():Array	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=3 code_len=10
  0         findpropstrict	_concat
  3         getlocal0     	
  4         getlocal1     	
  5         callproperty  	_concat (2)
  9         returnvalue   	
}

var undefined():*	/* disp_id 0*/
{
  // local_count=1 max_scope=0 max_stack=2 code_len=9
  0         findpropstrict	_shift
  3         getlocal0     	
  4         callproperty  	_shift (1)
  8         returnvalue   	
}

var undefined(*,*):Array	/* disp_id 0*/
{
  // local_count=3 max_scope=0 max_stack=5 code_len=25
  0         findpropstrict	_slice
  3         getlocal0     	
  4         findpropstrict	Number
  7         getlocal1     	
  8         callproperty  	Number (1)
  12        findpropstrict	Number
  15        getlocal2     	
  16        callproperty  	Number (1)
  20        callproperty  	_slice (3)
  24        returnvalue   	
}

var undefined():uint	/* disp_id 0*/
{
  // local_count=8 max_scope=0 max_stack=4 code_len=149
  0         pushbyte      	0
  2         convert_u     	
  3         setlocal      	4
  5         pushbyte      	0
  7         convert_u     	
  8         setlocal      	6
  10        findpropstrict	uint
  13        getlocal0     	
  14        getproperty   	length
  17        callproperty  	uint (1)
  21        convert_u     	
  22        setlocal2     	
  23        getlocal1     	
  24        getproperty   	length
  27        convert_u     	
  28        setlocal3     	
  29        getlocal2     	
  30        convert_u     	
  31        setlocal      	4
  33        jump          	L1

  
  L2: 
  37        label         	
  38        getlocal      	4
  40        decrement     	
  41        convert_u     	
  42        setlocal      	4
  44        getlocal      	4
  46        getlocal3     	
  47        add           	
  48        convert_u     	
  49        setlocal      	6
  51        getlocal      	4
  53        getlocal0     	
  54        in            	
  55        iffalse       	L3

  59        getlocal0     	
  60        getlocal      	6
  62        getlocal0     	
  63        getlocal      	4
  65        getproperty   	null
  68        setproperty   	null
  71        jump          	L1
  
  L3: 
  75        getlocal0     	
  76        getlocal      	6
  78        deleteproperty	null
  81        pop           	
  
  L1: 
  82        getlocal      	4
  84        pushbyte      	0
  86        ifgt          	L2

  90        pushbyte      	0
  92        convert_u     	
  93        setlocal      	5
  95        jump          	L4

  
  L5: 
  99        label         	
  100       getlocal0     	
  101       getlocal      	4
  103       convert_d     	
  104       dup           	
  105       increment     	
  106       convert_u     	
  107       setlocal      	4
  109       setlocal      	7
  111       getlocal      	7
  113       getlocal1     	
  114       getlocal      	5
  116       getproperty   	null
  119       setproperty   	null
  122       kill          	7
  124       getlocal      	5
  126       increment     	
  127       convert_u     	
  128       setlocal      	5
  
  L4: 
  130       getlocal      	5
  132       getlocal3     	
  133       iflt          	L5

  137       getlocal2     	
  138       getlocal3     	
  139       add           	
  140       convert_u     	
  141       setlocal2     	
  142       getlocal0     	
  143       getlocal2     	
  144       setproperty   	length
  147       getlocal2     	
  148       returnvalue   	
}

var undefined():*	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=3 code_len=23
  0         getlocal1     	
  1         getproperty   	length
  4         not           	
  5         iffalse       	L1

  9         getglobalscope	
  10        getslot       	17
  12        returnvalue   	
  
  L1: 
  13        findpropstrict	_splice
  16        getlocal0     	
  17        getlocal1     	
  18        callproperty  	_splice (2)
  22        returnvalue   	
}

var undefined():*	/* disp_id 0*/
{
  // local_count=2 max_scope=0 max_stack=3 code_len=10
  0         findpropstrict	_sort
  3         getlocal0     	
  4         getlocal1     	
  5         callproperty  	_sort (2)
  9         returnvalue   	
}

var undefined(*,*):*	/* disp_id 0*/
{
  // local_count=4 max_scope=0 max_stack=4 code_len=11
  0         findpropstrict	_sortOn
  3         getlocal0     	
  4         getlocal1     	
  5         getlocal2     	
  6         callproperty  	_sortOn (3)
  10        returnvalue   	
}

var undefined(*,*):int	/* disp_id 0*/
{
  // local_count=3 max_scope=0 max_stack=5 code_len=18
  0         findpropstrict	_indexOf
  3         getlocal0     	
  4         getlocal1     	
  5         findpropstrict	int
  8         getlocal2     	
  9         callproperty  	int (1)
  13        callproperty  	_indexOf (3)
  17        returnvalue   	
}

var undefined(*,*):int	/* disp_id 0*/
{
  // local_count=3 max_scope=0 max_stack=5 code_len=18
  0         findpropstrict	_lastIndexOf
  3         getlocal0     	
  4         getlocal1     	
  5         findpropstrict	int
  8         getlocal2     	
  9         callproperty  	int (1)
  13        callproperty  	_lastIndexOf (3)
  17        returnvalue   	
}

var undefined(Function,*):Boolean	/* disp_id 0*/
{
  // local_count=3 max_scope=0 max_stack=4 code_len=11
  0         findpropstrict	_every
  3         getlocal0     	
  4         getlocal1     	
  5         getlocal2     	
  6         callproperty  	_every (3)
  10        returnvalue   	
}

var undefined(Function,*):Array	/* disp_id 0*/
{
  // local_count=3 max_scope=0 max_stack=4 code_len=11
  0         findpropstrict	_filter
  3         getlocal0     	
  4         getlocal1     	
  5         getlocal2     	
  6         callproperty  	_filter (3)
  10        returnvalue   	
}

var undefined(Function,*):void	/* disp_id 0*/
{
  // local_count=3 max_scope=0 max_stack=4 code_len=12
  0         findpropstrict	_forEach
  3         getlocal0     	
  4         getlocal1     	
  5         getlocal2     	
  6         callproperty  	_forEach (3)
  10        pop           	
  11        returnvoid    	
}

var undefined(Function,*):Array	/* disp_id 0*/
{
  // local_count=3 max_scope=0 max_stack=4 code_len=11
  0         findpropstrict	_map
  3         getlocal0     	
  4         getlocal1     	
  5         getlocal2     	
  6         callproperty  	_map (3)
  10        returnvalue   	
}

var undefined(Function,*):Boolean	/* disp_id 0*/
{
  // local_count=3 max_scope=0 max_stack=4 code_len=11
  0         findpropstrict	_some
  3         getlocal0     	
  4         getlocal1     	
  5         getlocal2     	
  6         callproperty  	_some (3)
  10        returnvalue   	
}
OPCODE	SIZE	% OF 4720
findpropstrict	800	16%
callproperty  	746	15%
getproperty   	482	10%
getlocal0     	274	5%
setproperty   	237	5%
newfunction   	192	4%
returnvalue   	155	3%
initproperty  	149	3%
pushbyte      	140	2%
getlocal1     	128	2%
findproperty  	109	2%
pushscope     	104	2%
iffalse       	84	1%
pushstring    	76	1%
getlocal      	76	1%
getslot       	76	1%
ifstrictne    	68	1%
setlocal      	60	1%
jump          	56	1%
getlocal2     	53	1%
pushshort     	48	1%
getglobalscope	38	0%
getscopeobject	38	0%
convert_u     	34	0%
pop           	33	0%
returnvoid    	33	0%
kill          	28	0%
iflt          	28	0%
newclass      	26	0%
ifne          	24	0%
constructsuper	24	0%
getlocal3     	23	0%
coerce_s      	19	0%
dup           	17	0%
setlocal2     	17	0%
popscope      	16	0%
pushdouble    	16	0%
not           	16	0%
setlocal1     	16	0%
istypelate    	15	0%
setlocal3     	15	0%
coerce        	14	0%
ifeq          	12	0%
add           	10	0%
label         	9	0%
coerce_a      	9	0%
increment     	9	0%
ifnlt         	8	0%
divide        	8	0%
newobject     	6	0%
pushnull      	5	0%
iftrue        	4	0%
ifgt          	4	0%
convert_b     	4	0%
pushwith      	3	0%
pushundefined 	3	0%
swap          	3	0%
hasnext2      	3	0%
deleteproperty	3	0%
convert_d     	3	0%
pushfalse     	2	0%
pushint       	2	0%
newactivation 	2	0%
nextname      	1	0%
pushnan       	1	0%
decrement     	1	0%
equals        	1	0%
in            	1	0%
