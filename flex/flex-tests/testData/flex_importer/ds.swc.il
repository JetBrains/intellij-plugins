decompressed swf 60713 -> 149798
size [Rect 0 0 8000 6000]
frame rate 30
frame count 1
FileAttributes 4b 0%
SetBackgroundColor 3b 0%
DoABC2 878b 0%

abc name de/polygonal/ds/Compare
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 21 size 288 33 %
Cpool namespaces count 4 size 7 0 %
Cpool nssets count 0 size 1 0 %
Cpool names count 16 size 46 5 %
MethodInfo count 9 size 61 7 %
InstanceInfo size 7 0 %
ClassInfo size 26 3 %
ScriptInfo size 7 0 %
MethodBodies size 399 46 %
  script0

  class de.polygonal.ds::Compare extends Object
  {

    function de.polygonal.ds::Compare():void	/* disp_id -1*/

    static function compareNumberFall(Number,Number):int	/* disp_id 1*/

    static function compareNumberRise(Number,Number):int	/* disp_id 2*/

    static function compareStringCaseInSensitiveFall(String,String):int	/* disp_id 3*/

    static function compareStringCaseInSensitiveRise(String,String):int	/* disp_id 4*/

    static function compareStringCaseSensitiveFall(String,String):int	/* disp_id 5*/

    static function compareStringCaseSensitiveRise(String,String):int	/* disp_id 6*/

    static function de.polygonal.ds::Compare$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 3046b 2%

abc name de/polygonal/ds/pooling/LinkedObjectPool
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 61 size 555 18 %
Cpool namespaces count 7 size 13 0 %
Cpool nssets count 2 size 3 0 %
Cpool names count 48 size 141 4 %
MethodInfo count 15 size 87 2 %
InstanceInfo size 110 3 %
ClassInfo size 2 0 %
ScriptInfo size 7 0 %
MethodBodies size 2075 69 %
  script0

  class de.polygonal.ds.pooling::LinkedObjectPool extends Object
  {

    function de.polygonal.ds.pooling::LinkedObjectPool(int,Boolean):void	/* disp_id -1*/

    function toString():String	/* disp_id 0*/

    function put(Object):void	/* disp_id 0*/

    function purge():void	/* disp_id 0*/

    function getWasteCount():int	/* disp_id 0*/

    function getUsageCount():int	/* disp_id 0*/

    function getSize():int	/* disp_id 0*/

    function get():Object	/* disp_id 0*/

    function free():void	/* disp_id 0*/

    function allocate(Class,Object,de.polygonal.ds.pooling::Factory):void	/* disp_id 0*/
    var _usageCount:int	/* slot_id 0 */
    var _tail:de.polygonal.ds.pooling::ObjNode	/* slot_id 0 */
    var _initSize:int	/* slot_id 0 */
    var _head:de.polygonal.ds.pooling::ObjNode	/* slot_id 0 */
    var _growable:Boolean	/* slot_id 0 */

    function _grow():void	/* disp_id 0*/

    function _getInternal():Object	/* disp_id 0*/

    function _fill(Array):void	/* disp_id 0*/
    var _factory:de.polygonal.ds.pooling::Factory	/* slot_id 0 */
    var _fabricate:Function	/* slot_id 0 */
    var _emptyNode:de.polygonal.ds.pooling::ObjNode	/* slot_id 0 */
    var _currSize:int	/* slot_id 0 */
    var _allocNode:de.polygonal.ds.pooling::ObjNode	/* slot_id 0 */
    var _C:Class	/* slot_id 0 */

    static function de.polygonal.ds.pooling::LinkedObjectPool$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 203b 0%

abc name de/polygonal/ds/pooling/ObjNode
magic 2e0010
Cpool numbers size 3 1 %
Cpool strings count 8 size 55 32 %
Cpool namespaces count 3 size 5 2 %
Cpool nssets count 0 size 1 0 %
Cpool names count 6 size 16 9 %
MethodInfo count 3 size 13 7 %
InstanceInfo size 17 10 %
ClassInfo size 2 1 %
ScriptInfo size 7 4 %
MethodBodies size 43 25 %
  script0

  class de.polygonal.ds.pooling::ObjNode extends Object
  {

    function de.polygonal.ds.pooling::ObjNode():void	/* disp_id -1*/
    var val:Object	/* slot_id 0 */
    var next:de.polygonal.ds.pooling::ObjNode	/* slot_id 0 */

    static function de.polygonal.ds.pooling::ObjNode$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 514b 0%

abc name de/polygonal/ds/GraphArc
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 20 size 136 28 %
Cpool namespaces count 4 size 7 1 %
Cpool nssets count 0 size 1 0 %
Cpool names count 16 size 46 9 %
MethodInfo count 6 size 38 7 %
InstanceInfo size 39 8 %
ClassInfo size 2 0 %
ScriptInfo size 7 1 %
MethodBodies size 201 41 %
  script0

  class de.polygonal.ds::GraphArc extends Object
  {

    function de.polygonal.ds::GraphArc(de.polygonal.ds::GraphNode,Number):void	/* disp_id -1*/

    function val():Object	/* disp_id 0*/
    var prev:de.polygonal.ds::GraphArc	/* slot_id 0 */
    var node:de.polygonal.ds::GraphNode	/* slot_id 0 */
    var next:de.polygonal.ds::GraphArc	/* slot_id 0 */
    var cost:Number	/* slot_id 0 */

    function _unlink(de.polygonal.ds::GraphNode):void	/* disp_id 0*/

    function _link(de.polygonal.ds::GraphNode):void	/* disp_id 0*/

    static function de.polygonal.ds::GraphArc$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 569b 0%

abc name de/polygonal/ds/mem/MemoryAccess
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 23 size 179 33 %
Cpool namespaces count 4 size 7 1 %
Cpool nssets count 0 size 1 0 %
Cpool names count 20 size 58 10 %
MethodInfo count 8 size 40 7 %
InstanceInfo size 47 8 %
ClassInfo size 2 0 %
ScriptInfo size 7 1 %
MethodBodies size 183 34 %
  script0

  class de.polygonal.ds.mem::MemoryAccess extends Object
  {

    function de.polygonal.ds.mem::MemoryAccess(int):void	/* disp_id -1*/

    function resize(int):void	/* disp_id 0*/
    var offset:int	/* slot_id 0 */

    function free():void	/* disp_id 0*/

    function clear():void	/* disp_id 0*/
    var bytes:int	/* slot_id 0 */
    var _offset:int	/* slot_id 0 */
    var _bytes:int	/* slot_id 0 */

    function __offset():int	/* disp_id 0*/

    function __bytes():int	/* disp_id 0*/

    static function de.polygonal.ds.mem::MemoryAccess$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 1567b 1%

abc name de/polygonal/ds/mem/ByteMemory
magic 2e0010
Cpool numbers size 8 0 %
Cpool strings count 49 size 415 27 %
Cpool namespaces count 9 size 17 1 %
Cpool nssets count 2 size 3 0 %
Cpool names count 36 size 106 6 %
MethodInfo count 16 size 150 9 %
InstanceInfo size 45 2 %
ClassInfo size 26 1 %
ScriptInfo size 7 0 %
MethodBodies size 750 48 %
  script0

  class de.polygonal.ds.mem::ByteMemory extends de.polygonal.ds.mem::MemoryAccess
  {

    function de.polygonal.ds.mem::ByteMemory(int):void	/* disp_id -1*/

    function toString():String	/* disp_id 0*/

    function swp(int,int):void	/* disp_id 0*/
    var size:int	/* slot_id 0 */

    function set(int,int):void	/* disp_id 0*/

    function resize(int):void	/* disp_id 0*/

    function getAddr(int):int	/* disp_id 0*/

    function get(int):int	/* disp_id 0*/
    var _size:int	/* slot_id 0 */

    function __size():int	/* disp_id 0*/

    static function toByteArray(de.polygonal.ds.mem::ByteMemory,int,int):flash.utils::ByteArray	/* disp_id 1*/

    static function ofByteArray(flash.utils::ByteArray,int,int):de.polygonal.ds.mem::ByteMemory	/* disp_id 2*/

    static function toArray(de.polygonal.ds.mem::ByteMemory,int,int):Array	/* disp_id 3*/

    static function ofArray(Array,int,int):de.polygonal.ds.mem::ByteMemory	/* disp_id 4*/

    static function toVector(de.polygonal.ds.mem::ByteMemory,int,int):__AS3__.vec::Vector.<int>	/* disp_id 5*/

    static function ofVector(__AS3__.vec::Vector.<int>,int,int):de.polygonal.ds.mem::ByteMemory	/* disp_id 6*/

    static function de.polygonal.ds.mem::ByteMemory$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 223b 0%

abc name de/polygonal/ds/Itr
magic 2e0010
Cpool numbers size 3 1 %
Cpool strings count 11 size 81 40 %
Cpool namespaces count 4 size 7 3 %
Cpool nssets count 0 size 1 0 %
Cpool names count 8 size 22 11 %
MethodInfo count 6 size 25 12 %
InstanceInfo size 19 9 %
ClassInfo size 2 1 %
ScriptInfo size 7 3 %
MethodBodies size 27 13 %
  script0

  interface de.polygonal.ds::Itr extends *
  {

    function de.polygonal.ds::Itr():void	/* disp_id -1*/

    function de.polygonal.ds:Itr::reset():void	/* disp_id 0*/

    function de.polygonal.ds:Itr::next():Object	/* disp_id 0*/

    function de.polygonal.ds:Itr::hasNext():Boolean	/* disp_id 0*/

    static function de.polygonal.ds::Itr$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 313b 0%

abc name IntIter
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 14 size 83 27 %
Cpool namespaces count 3 size 5 1 %
Cpool nssets count 0 size 1 0 %
Cpool names count 12 size 34 11 %
MethodInfo count 5 size 30 9 %
InstanceInfo size 25 8 %
ClassInfo size 2 0 %
ScriptInfo size 7 2 %
MethodBodies size 106 35 %
  script0

  class IntIter extends Object
  {

    function IntIter(int,int):void	/* disp_id -1*/

    function next():int	/* disp_id 0*/
    var min:int	/* slot_id 0 */
    var max:int	/* slot_id 0 */

    function hasNext():Boolean	/* disp_id 0*/

    static function IntIter$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 408b 0%

abc name de/polygonal/ds/Collection
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 23 size 176 46 %
Cpool namespaces count 4 size 7 1 %
Cpool nssets count 0 size 1 0 %
Cpool names count 16 size 46 12 %
MethodInfo count 11 size 64 16 %
InstanceInfo size 39 10 %
ClassInfo size 2 0 %
ScriptInfo size 7 1 %
MethodBodies size 27 7 %
  script0

  interface de.polygonal.ds::Collection extends *
  {

    function de.polygonal.ds::Collection():void	/* disp_id -1*/

    function de.polygonal.ds:Collection::toVector(de.polygonal.ds::Vector):de.polygonal.ds::Vector	/* disp_id 0*/

    function de.polygonal.ds:Collection::toArray(Array):Array	/* disp_id 0*/

    function de.polygonal.ds:Collection::size():int	/* disp_id 0*/

    function de.polygonal.ds:Collection::iterator():Object	/* disp_id 0*/

    function de.polygonal.ds:Collection::isEmpty():Boolean	/* disp_id 0*/

    function de.polygonal.ds:Collection::contains(Object):Boolean	/* disp_id 0*/

    function de.polygonal.ds:Collection::clone(Boolean,Object):de.polygonal.ds::Collection	/* disp_id 0*/

    function de.polygonal.ds:Collection::clear():void	/* disp_id 0*/

    static function de.polygonal.ds::Collection$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 3862b 2%

abc name de/polygonal/ds/Graph
magic 2e0010
Cpool numbers size 8 0 %
Cpool strings count 79 size 648 16 %
Cpool namespaces count 7 size 13 0 %
Cpool nssets count 3 size 5 0 %
Cpool names count 63 size 186 4 %
MethodInfo count 27 size 201 5 %
InstanceInfo size 124 3 %
ClassInfo size 2 0 %
ScriptInfo size 7 0 %
MethodBodies size 2637 68 %
  script0

  class de.polygonal.ds::Graph extends Object
    implements de.polygonal.ds::Collection
  {

    function de.polygonal.ds::Graph(int):void	/* disp_id -1*/

    function toVector(de.polygonal.ds::Vector):de.polygonal.ds::Vector	/* disp_id 0*/

    function toString():String	/* disp_id 0*/

    function toArray(Array):Array	/* disp_id 0*/

    function size():int	/* disp_id 0*/

    function removeNode(de.polygonal.ds::GraphNode):void	/* disp_id 0*/

    function maxSize():int	/* disp_id 0*/

    function iterator():Object	/* disp_id 0*/

    function isEmpty():Boolean	/* disp_id 0*/

    function getNodeList():de.polygonal.ds::GraphNode	/* disp_id 0*/

    function findNode(Object):de.polygonal.ds::GraphNode	/* disp_id 0*/

    function contains(Object):Boolean	/* disp_id 0*/

    function clone(Boolean,Object):de.polygonal.ds::Collection	/* disp_id 0*/

    function clearMarks():void	/* disp_id 0*/

    function clear():void	/* disp_id 0*/

    function addSingleArc(de.polygonal.ds::GraphNode,de.polygonal.ds::GraphNode,Number):void	/* disp_id 0*/

    function addNode(Object):de.polygonal.ds::GraphNode	/* disp_id 0*/

    function addMutualArc(de.polygonal.ds::GraphNode,de.polygonal.ds::GraphNode,Number):void	/* disp_id 0*/
    var _stack:Array	/* slot_id 0 */
    var _size:int	/* slot_id 0 */
    var _que:Array	/* slot_id 0 */
    var _nodeList:de.polygonal.ds::GraphNode	/* slot_id 0 */

    function __unlink(Object,de.polygonal.ds::Graph):void	/* disp_id 0*/

    function __setMarked(Object,Boolean):void	/* disp_id 0*/

    function __next(Object):de.polygonal.ds::GraphNode	/* disp_id 0*/

    function __link(Object,de.polygonal.ds::Graph):void	/* disp_id 0*/

    function __getMarked(Object):Boolean	/* disp_id 0*/

    function DFS(Boolean,de.polygonal.ds::GraphNode,Object,*):void	/* disp_id 0*/

    function BFS(Boolean,de.polygonal.ds::GraphNode,Object,*):void	/* disp_id 0*/

    static function de.polygonal.ds::Graph$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 488b 0%

abc name de/polygonal/ds/GraphIterator
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 22 size 155 34 %
Cpool namespaces count 4 size 7 1 %
Cpool nssets count 2 size 3 0 %
Cpool names count 18 size 52 11 %
MethodInfo count 7 size 36 7 %
InstanceInfo size 34 7 %
ClassInfo size 2 0 %
ScriptInfo size 7 1 %
MethodBodies size 150 33 %
  script0

  class de.polygonal.ds::GraphIterator extends Object
    implements de.polygonal.ds::Itr
  {

    function de.polygonal.ds::GraphIterator(de.polygonal.ds::Graph):void	/* disp_id -1*/

    function reset():void	/* disp_id 0*/

    function next():Object	/* disp_id 0*/

    function hasNext():Boolean	/* disp_id 0*/
    var _node:de.polygonal.ds::GraphNode	/* slot_id 0 */
    var _f:de.polygonal.ds::Graph	/* slot_id 0 */

    function __nodeList(Object):de.polygonal.ds::GraphNode	/* disp_id 0*/

    static function de.polygonal.ds::GraphIterator$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 5948b 3%

abc name de/polygonal/ds/SLL
magic 2e0010
Cpool numbers size 9 0 %
Cpool strings count 91 size 759 12 %
Cpool namespaces count 7 size 13 0 %
Cpool nssets count 3 size 5 0 %
Cpool names count 76 size 225 3 %
MethodInfo count 41 size 263 4 %
InstanceInfo size 175 2 %
ClassInfo size 2 0 %
ScriptInfo size 7 0 %
MethodBodies size 4461 75 %
  script0

  class de.polygonal.ds::SLL extends Object
    implements de.polygonal.ds::Collection
  {

    function de.polygonal.ds::SLL(int):void	/* disp_id -1*/

    function toVector(de.polygonal.ds::Vector):de.polygonal.ds::Vector	/* disp_id 0*/

    function toString():String	/* disp_id 0*/

    function toArray(Array):Array	/* disp_id 0*/

    function tail():de.polygonal.ds::SLLNode	/* disp_id 0*/

    function sort(Function,Boolean):void	/* disp_id 0*/

    function size():int	/* disp_id 0*/

    function shuffle(de.polygonal.ds::Vector):void	/* disp_id 0*/

    function shiftUp():void	/* disp_id 0*/

    function reverse():void	/* disp_id 0*/

    function removeTail():Object	/* disp_id 0*/

    function removeHead():Object	/* disp_id 0*/

    function remove(de.polygonal.ds::SLLNode):de.polygonal.ds::SLLNode	/* disp_id 0*/

    function prepend(Object):de.polygonal.ds::SLLNode	/* disp_id 0*/

    function popDown():void	/* disp_id 0*/

    function nodeOf(Object,de.polygonal.ds::SLLNode):de.polygonal.ds::SLLNode	/* disp_id 0*/

    function merge(de.polygonal.ds::SLL):void	/* disp_id 0*/

    function maxSize():int	/* disp_id 0*/

    function join(String):String	/* disp_id 0*/

    function iterator():Object	/* disp_id 0*/

    function isEmpty():Boolean	/* disp_id 0*/

    function insertBefore(de.polygonal.ds::SLLNode,Object):de.polygonal.ds::SLLNode	/* disp_id 0*/

    function insertAfter(de.polygonal.ds::SLLNode,Object):de.polygonal.ds::SLLNode	/* disp_id 0*/

    function head():de.polygonal.ds::SLLNode	/* disp_id 0*/

    function getNodeAt(int):de.polygonal.ds::SLLNode	/* disp_id 0*/

    function contains(Object):Boolean	/* disp_id 0*/

    function concat(de.polygonal.ds::SLL):de.polygonal.ds::SLL	/* disp_id 0*/

    function clone(Boolean,Object):de.polygonal.ds::Collection	/* disp_id 0*/

    function clear():void	/* disp_id 0*/

    function assign(*,Array,int):void	/* disp_id 0*/

    function append(Object):de.polygonal.ds::SLLNode	/* disp_id 0*/

    function _valid(de.polygonal.ds::SLLNode):Boolean	/* disp_id 0*/
    var _tail:de.polygonal.ds::SLLNode	/* slot_id 0 */
    var _size:int	/* slot_id 0 */

    function _mergeSortComparable(de.polygonal.ds::SLLNode):de.polygonal.ds::SLLNode	/* disp_id 0*/

    function _mergeSort(de.polygonal.ds::SLLNode,Function):de.polygonal.ds::SLLNode	/* disp_id 0*/

    function _insertionSortComparable(de.polygonal.ds::SLLNode):de.polygonal.ds::SLLNode	/* disp_id 0*/

    function _insertionSort(de.polygonal.ds::SLLNode,Function):de.polygonal.ds::SLLNode	/* disp_id 0*/
    var _head:de.polygonal.ds::SLLNode	/* slot_id 0 */

    function _getNodeBefore(de.polygonal.ds::SLLNode):de.polygonal.ds::SLLNode	/* disp_id 0*/

    function _getNode(Object):de.polygonal.ds::SLLNode	/* disp_id 0*/

    function __insertAfter(Object,de.polygonal.ds::SLLNode):void	/* disp_id 0*/

    static function de.polygonal.ds::SLL$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 474b 0%

abc name de/polygonal/ds/SLLIterator
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 22 size 143 32 %
Cpool namespaces count 4 size 7 1 %
Cpool nssets count 2 size 3 0 %
Cpool names count 18 size 52 11 %
MethodInfo count 7 size 36 8 %
InstanceInfo size 34 7 %
ClassInfo size 2 0 %
ScriptInfo size 7 1 %
MethodBodies size 150 33 %
  script0

  class de.polygonal.ds::SLLIterator extends Object
    implements de.polygonal.ds::Itr
  {

    function de.polygonal.ds::SLLIterator(de.polygonal.ds::SLL):void	/* disp_id -1*/

    function reset():void	/* disp_id 0*/

    function next():Object	/* disp_id 0*/

    function hasNext():Boolean	/* disp_id 0*/
    var _walker:de.polygonal.ds::SLLNode	/* slot_id 0 */
    var _f:de.polygonal.ds::SLL	/* slot_id 0 */

    function __head(Object):de.polygonal.ds::SLLNode	/* disp_id 0*/

    static function de.polygonal.ds::SLLIterator$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 567b 0%

abc name de/polygonal/ds/BSTIterator
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 24 size 157 29 %
Cpool namespaces count 5 size 9 1 %
Cpool nssets count 3 size 5 0 %
Cpool names count 21 size 60 11 %
MethodInfo count 6 size 30 5 %
InstanceInfo size 35 6 %
ClassInfo size 2 0 %
ScriptInfo size 7 1 %
MethodBodies size 222 41 %
  script0

  class de.polygonal.ds::BSTIterator extends Object
    implements de.polygonal.ds::Itr
  {

    function de.polygonal.ds::BSTIterator(de.polygonal.ds::BinaryTreeNode):void	/* disp_id -1*/

    function reset():void	/* disp_id 0*/

    function next():Object	/* disp_id 0*/

    function hasNext():Boolean	/* disp_id 0*/
    var _top:int	/* slot_id 0 */
    var _stack:Array	/* slot_id 0 */
    var _node:de.polygonal.ds::BinaryTreeNode	/* slot_id 0 */

    static function de.polygonal.ds::BSTIterator$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 1956b 1%

abc name de/polygonal/ds/BST
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 53 size 418 21 %
Cpool namespaces count 7 size 13 0 %
Cpool nssets count 3 size 5 0 %
Cpool names count 44 size 129 6 %
MethodInfo count 19 size 112 5 %
InstanceInfo size 74 3 %
ClassInfo size 2 0 %
ScriptInfo size 7 0 %
MethodBodies size 1164 60 %
  script0

  class de.polygonal.ds::BST extends Object
    implements de.polygonal.ds::Collection
  {

    function de.polygonal.ds::BST():void	/* disp_id -1*/

    function toVector(de.polygonal.ds::Vector):de.polygonal.ds::Vector	/* disp_id 0*/

    function toString():String	/* disp_id 0*/

    function toArray(Array):Array	/* disp_id 0*/

    function size():int	/* disp_id 0*/

    function root():de.polygonal.ds::BinaryTreeNode	/* disp_id 0*/

    function removeNode(de.polygonal.ds::BinaryTreeNode):Boolean	/* disp_id 0*/

    function remove(de.polygonal.ds::Comparable):Boolean	/* disp_id 0*/

    function iterator():Object	/* disp_id 0*/

    function isEmpty():Boolean	/* disp_id 0*/

    function insert(de.polygonal.ds::Comparable):de.polygonal.ds::BinaryTreeNode	/* disp_id 0*/

    function find(de.polygonal.ds::Comparable):de.polygonal.ds::BinaryTreeNode	/* disp_id 0*/

    function contains(Object):Boolean	/* disp_id 0*/

    function clone(Boolean,Object):de.polygonal.ds::Collection	/* disp_id 0*/

    function clear():void	/* disp_id 0*/
    var _size:int	/* slot_id 0 */
    var _root:de.polygonal.ds::BinaryTreeNode	/* slot_id 0 */

    static function de.polygonal.ds::BST$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 2309b 1%

abc name de/polygonal/gl/color/RGB
magic 2e0010
Cpool numbers size 74 3 %
Cpool strings count 55 size 358 15 %
Cpool namespaces count 5 size 9 0 %
Cpool nssets count 0 size 1 0 %
Cpool names count 45 size 133 5 %
MethodInfo count 26 size 186 8 %
InstanceInfo size 58 2 %
ClassInfo size 98 4 %
ScriptInfo size 7 0 %
MethodBodies size 1350 59 %
  script0

  class de.polygonal.gl.color::RGB extends Object
  {

    function de.polygonal.gl.color::RGB(Number,Number,Number):void	/* disp_id -1*/

    function toString():String	/* disp_id 0*/

    function set24(int,int,int):void	/* disp_id 0*/

    function set(Number,Number,Number):void	/* disp_id 0*/
    var r:Number	/* slot_id 0 */

    function lerp(de.polygonal.gl.color::RGB,de.polygonal.gl.color::RGB,Number):void	/* disp_id 0*/

    function getR8():int	/* disp_id 0*/

    function getG8():int	/* disp_id 0*/

    function getB8():int	/* disp_id 0*/

    function get24():int	/* disp_id 0*/
    var g:Number	/* slot_id 0 */

    function copy(de.polygonal.gl.color::RGB):de.polygonal.gl.color::RGB	/* disp_id 0*/
    var b:Number	/* slot_id 0 */
    static var BLACK:int	/* slot_id 1 */
    static var WHITE:int	/* slot_id 2 */
    static var RED:int	/* slot_id 3 */
    static var GREEN:int	/* slot_id 4 */
    static var BLUE:int	/* slot_id 5 */
    static var CYAN:int	/* slot_id 6 */
    static var MAGENTA:int	/* slot_id 7 */
    static var YELLOW:int	/* slot_id 8 */

    static function setRGBi(int,int,int):int	/* disp_id 1*/

    static function setRGBf(Number,Number,Number):int	/* disp_id 2*/

    static function getR(int):int	/* disp_id 3*/

    static function getRf(int):Number	/* disp_id 4*/

    static function getG(int):int	/* disp_id 5*/

    static function getGf(int):Number	/* disp_id 6*/

    static function getB(int):int	/* disp_id 7*/

    static function getBf(int):Number	/* disp_id 8*/

    static function setR(int,int):int	/* disp_id 9*/

    static function setRf(int,Number):int	/* disp_id 10*/

    static function setG(int,int):int	/* disp_id 11*/

    static function setGf(int,Number):int	/* disp_id 12*/

    static function setB(int,int):int	/* disp_id 13*/

    static function setBf(int,Number):int	/* disp_id 14*/

    static function de.polygonal.gl.color::RGB$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 148b 0%

abc name de/polygonal/ds/Prioritizable
magic 2e0010
Cpool numbers size 3 2 %
Cpool strings count 5 size 37 32 %
Cpool namespaces count 3 size 5 4 %
Cpool nssets count 0 size 1 0 %
Cpool names count 3 size 7 6 %
MethodInfo count 3 size 13 11 %
InstanceInfo size 7 6 %
ClassInfo size 2 1 %
ScriptInfo size 7 6 %
MethodBodies size 27 23 %
  script0

  interface de.polygonal.ds::Prioritizable extends *
  {

    function de.polygonal.ds::Prioritizable():void	/* disp_id -1*/

    static function de.polygonal.ds::Prioritizable$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 745b 0%

abc name flash/Memory
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 31 size 254 34 %
Cpool namespaces count 5 size 9 1 %
Cpool nssets count 0 size 1 0 %
Cpool names count 24 size 70 9 %
MethodInfo count 17 size 107 14 %
InstanceInfo size 7 0 %
ClassInfo size 58 7 %
ScriptInfo size 7 0 %
MethodBodies size 207 28 %
  script0

  class flash::Memory extends Object
  {

    function flash::Memory():void	/* disp_id -1*/

    static function select(flash.utils::ByteArray):void	/* disp_id 1*/

    static function setByte(int,int):void	/* disp_id 2*/

    static function setI16(int,int):void	/* disp_id 3*/

    static function setI32(int,int):void	/* disp_id 4*/

    static function setFloat(int,Number):void	/* disp_id 5*/

    static function setDouble(int,Number):void	/* disp_id 6*/

    static function getByte(int):int	/* disp_id 7*/

    static function getUI16(int):int	/* disp_id 8*/

    static function getI32(int):int	/* disp_id 9*/

    static function getFloat(int):Number	/* disp_id 10*/

    static function getDouble(int):Number	/* disp_id 11*/

    static function signExtend1(int):int	/* disp_id 12*/

    static function signExtend8(int):int	/* disp_id 13*/

    static function signExtend16(int):int	/* disp_id 14*/

    static function flash::Memory$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 1777b 1%

abc name de/polygonal/ds/mem/IntMemory
magic 2e0010
Cpool numbers size 8 0 %
Cpool strings count 51 size 434 24 %
Cpool namespaces count 9 size 17 0 %
Cpool nssets count 2 size 3 0 %
Cpool names count 39 size 116 6 %
MethodInfo count 17 size 165 9 %
InstanceInfo size 45 2 %
ClassInfo size 30 1 %
ScriptInfo size 7 0 %
MethodBodies size 913 52 %
  script0

  class de.polygonal.ds.mem::IntMemory extends de.polygonal.ds.mem::MemoryAccess
  {

    function de.polygonal.ds.mem::IntMemory(int):void	/* disp_id -1*/

    function toString():String	/* disp_id 0*/

    function swp(int,int):void	/* disp_id 0*/
    var size:int	/* slot_id 0 */

    function set(int,int):void	/* disp_id 0*/

    function resize(int):void	/* disp_id 0*/

    function getAddr(int):int	/* disp_id 0*/

    function get(int):int	/* disp_id 0*/
    var _size:int	/* slot_id 0 */

    function __size():int	/* disp_id 0*/

    static function toByteArray(de.polygonal.ds.mem::IntMemory,int,int):flash.utils::ByteArray	/* disp_id 1*/

    static function ofByteArray(flash.utils::ByteArray,int,int):de.polygonal.ds.mem::IntMemory	/* disp_id 2*/

    static function toArray(de.polygonal.ds.mem::IntMemory,int,int):Array	/* disp_id 3*/

    static function ofArray(Array,int,int):de.polygonal.ds.mem::IntMemory	/* disp_id 4*/

    static function toVector(de.polygonal.ds.mem::IntMemory,int,int):__AS3__.vec::Vector.<int>	/* disp_id 5*/

    static function toUnsignedVector(de.polygonal.ds.mem::IntMemory,int,int):__AS3__.vec::Vector.<uint>	/* disp_id 6*/

    static function ofVector(__AS3__.vec::Vector.<int>,int,int):de.polygonal.ds.mem::IntMemory	/* disp_id 7*/

    static function de.polygonal.ds.mem::IntMemory$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 3674b 2%

abc name de/polygonal/core/math/Mathematics
magic 2e0010
Cpool numbers size 159 4 %
Cpool strings count 93 size 714 19 %
Cpool namespaces count 3 size 5 0 %
Cpool nssets count 0 size 1 0 %
Cpool names count 77 size 229 6 %
MethodInfo count 46 size 327 8 %
InstanceInfo size 7 0 %
ClassInfo size 294 8 %
ScriptInfo size 7 0 %
MethodBodies size 1887 51 %
  script0

  class de.polygonal.core.math::Mathematics extends Object
  {

    function de.polygonal.core.math::Mathematics():void	/* disp_id -1*/
    static var INT8_MIN:int	/* slot_id 1 */
    static var INT8_MAX:int	/* slot_id 2 */
    static var UINT8_MAX:int	/* slot_id 3 */
    static var INT16_MIN:int	/* slot_id 4 */
    static var INT16_MAX:int	/* slot_id 5 */
    static var UINT16_MAX:int	/* slot_id 6 */
    static var INT32_MIN:int	/* slot_id 7 */
    static var INT32_MAX:int	/* slot_id 8 */
    static var UINT32_MAX:int	/* slot_id 9 */
    static var NaN:Number	/* slot_id 10 */
    static var POSITIVE_INFINITY:Number	/* slot_id 11 */
    static var NEGATIVE_INFINITY:Number	/* slot_id 12 */
    static var FLOAT_MAX:Number	/* slot_id 13 */
    static var FLOAT_MIN:Number	/* slot_id 14 */
    static var DOUBLE_MIN:Number	/* slot_id 15 */
    static var DOUBLE_MAX:Number	/* slot_id 16 */
    static var RAD_DEG:Number	/* slot_id 17 */
    static var DEG_RAD:Number	/* slot_id 18 */
    static var LN2:Number	/* slot_id 19 */
    static var PIHALF:Number	/* slot_id 20 */
    static var PI:Number	/* slot_id 21 */
    static var PI2:Number	/* slot_id 22 */
    static var EPS:Number	/* slot_id 23 */
    static var EPS_SQ:Number	/* slot_id 24 */

    static function floatInt(Number):int	/* disp_id 1*/

    static function intFloat(int):Number	/* disp_id 2*/

    static function isFinite(Number):Boolean	/* disp_id 3*/

    static function isNaN(Number):Boolean	/* disp_id 4*/

    static function min(int,int):int	/* disp_id 5*/

    static function max(int,int):int	/* disp_id 6*/

    static function abs(int):int	/* disp_id 7*/

    static function sign(int):int	/* disp_id 8*/

    static function clamp(int,int,int):int	/* disp_id 9*/

    static function clampSymmetric(int,int):int	/* disp_id 10*/

    static function wrap(int,int,int):int	/* disp_id 11*/

    static function fmin(Number,Number):Number	/* disp_id 12*/

    static function fmax(Number,Number):Number	/* disp_id 13*/

    static function fabs(Number):Number	/* disp_id 14*/

    static function fsign(Number):int	/* disp_id 15*/

    static function fclamp(Number,Number,Number):Number	/* disp_id 16*/

    static function fclampSymmetric(Number,Number):Number	/* disp_id 17*/

    static function fwrap(Number,Number,Number):Number	/* disp_id 18*/

    static function equalSign(int,int):Boolean	/* disp_id 19*/

    static function isEven(int):Boolean	/* disp_id 20*/

    static function isOdd(int):Boolean	/* disp_id 21*/

    static function isPow2(int):Boolean	/* disp_id 22*/

    static function lerp(Number,Number,Number):Number	/* disp_id 23*/

    static function slerp(Number,Number,Number):Number	/* disp_id 24*/

    static function nextPow2(int):int	/* disp_id 25*/

    static function exp(int,int):int	/* disp_id 26*/

    static function roundTo(Number,Number):Number	/* disp_id 27*/

    static function round(Number):int	/* disp_id 28*/

    static function ceil(Number):int	/* disp_id 29*/

    static function floor(Number):int	/* disp_id 30*/

    static function sqr(Number):Number	/* disp_id 31*/

    static function invSqrt(Number):Number	/* disp_id 32*/

    static function wrapPI(Number):Number	/* disp_id 33*/

    static function compareAbsolute(Number,Number,Number):Boolean	/* disp_id 34*/

    static function snap(Number,Number):Number	/* disp_id 35*/

    static function inRange(Number,Number,Number):Boolean	/* disp_id 36*/

    static function frand():Number	/* disp_id 37*/

    static function irand():int	/* disp_id 38*/

    static function frandRange(Number,Number):Number	/* disp_id 39*/

    static function frandRangeSymmetric(Number):Number	/* disp_id 40*/

    static function irandRange(int,int):int	/* disp_id 41*/

    static function irandRangeSymmetric(int):Number	/* disp_id 42*/

    static function almostEqual2sComplement(Number,Number,int):Boolean	/* disp_id 43*/

    static function de.polygonal.core.math::Mathematics$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 2134b 1%

abc name de/polygonal/ds/TreeBuilder
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 48 size 408 19 %
Cpool namespaces count 4 size 7 0 %
Cpool nssets count 0 size 1 0 %
Cpool names count 39 size 115 5 %
MethodInfo count 26 size 126 5 %
InstanceInfo size 109 5 %
ClassInfo size 2 0 %
ScriptInfo size 7 0 %
MethodBodies size 1319 62 %
  script0

  class de.polygonal.ds::TreeBuilder extends Object
  {

    function de.polygonal.ds::TreeBuilder(de.polygonal.ds::TreeNode):void	/* disp_id -1*/

    function valid():Boolean	/* disp_id 0*/

    function up():Boolean	/* disp_id 0*/

    function toString():String	/* disp_id 0*/

    function setVal(Object):void	/* disp_id 0*/

    function root():void	/* disp_id 0*/

    function removeChild():Boolean	/* disp_id 0*/

    function prevChild():Boolean	/* disp_id 0*/

    function prependChild(Object):de.polygonal.ds::TreeNode	/* disp_id 0*/

    function nextChild():Boolean	/* disp_id 0*/

    function insertBeforeChild(Object):de.polygonal.ds::TreeNode	/* disp_id 0*/

    function insertAfterChild(Object):de.polygonal.ds::TreeNode	/* disp_id 0*/

    function getVal():Object	/* disp_id 0*/

    function getNode():de.polygonal.ds::TreeNode	/* disp_id 0*/

    function getChildVal():Object	/* disp_id 0*/

    function getChildNode():de.polygonal.ds::TreeNode	/* disp_id 0*/

    function down():Boolean	/* disp_id 0*/

    function childValid():Boolean	/* disp_id 0*/

    function childStart():Boolean	/* disp_id 0*/

    function childEnd():Boolean	/* disp_id 0*/

    function appendChild(Object):de.polygonal.ds::TreeNode	/* disp_id 0*/

    function _reset():void	/* disp_id 0*/
    var _node:de.polygonal.ds::TreeNode	/* slot_id 0 */

    function _getTail(de.polygonal.ds::TreeNode):de.polygonal.ds::TreeNode	/* disp_id 0*/

    function _createChildNode(Object,Boolean):de.polygonal.ds::TreeNode	/* disp_id 0*/
    var _child:de.polygonal.ds::TreeNode	/* slot_id 0 */

    static function de.polygonal.ds::TreeBuilder$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 1639b 1%

abc name de/polygonal/ds/mem/FloatMemory
magic 2e0010
Cpool numbers size 8 0 %
Cpool strings count 50 size 425 26 %
Cpool namespaces count 9 size 17 1 %
Cpool nssets count 2 size 3 0 %
Cpool names count 37 size 109 6 %
MethodInfo count 16 size 150 9 %
InstanceInfo size 45 2 %
ClassInfo size 26 1 %
ScriptInfo size 7 0 %
MethodBodies size 808 50 %
  script0

  class de.polygonal.ds.mem::FloatMemory extends de.polygonal.ds.mem::MemoryAccess
  {

    function de.polygonal.ds.mem::FloatMemory(int):void	/* disp_id -1*/

    function toString():String	/* disp_id 0*/

    function swp(int,int):void	/* disp_id 0*/
    var size:int	/* slot_id 0 */

    function set(int,Number):void	/* disp_id 0*/

    function resize(int):void	/* disp_id 0*/

    function getAddr(int):int	/* disp_id 0*/

    function get(int):Number	/* disp_id 0*/
    var _size:int	/* slot_id 0 */

    function __size():int	/* disp_id 0*/

    static function toByteArray(de.polygonal.ds.mem::FloatMemory,int,int):flash.utils::ByteArray	/* disp_id 1*/

    static function ofByteArray(flash.utils::ByteArray,int,int):de.polygonal.ds.mem::FloatMemory	/* disp_id 2*/

    static function toArray(de.polygonal.ds.mem::FloatMemory,int,int):Array	/* disp_id 3*/

    static function ofArray(Array,int,int):de.polygonal.ds.mem::FloatMemory	/* disp_id 4*/

    static function toVector(de.polygonal.ds.mem::FloatMemory,int,int):__AS3__.vec::Vector.<Number>	/* disp_id 5*/

    static function ofVector(__AS3__.vec::Vector.<Number>,int,int):de.polygonal.ds.mem::FloatMemory	/* disp_id 6*/

    static function de.polygonal.ds.mem::FloatMemory$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 1342b 0%

abc name haxe/io/Bytes
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 43 size 322 24 %
Cpool namespaces count 6 size 11 0 %
Cpool nssets count 2 size 3 0 %
Cpool names count 34 size 99 7 %
MethodInfo count 14 size 96 7 %
InstanceInfo size 49 3 %
ClassInfo size 14 1 %
ScriptInfo size 7 0 %
MethodBodies size 715 54 %
  script0

  class haxe.io::Bytes extends Object
  {

    function haxe.io::Bytes(int,flash.utils::ByteArray):void	/* disp_id -1*/

    function toString():String	/* disp_id 0*/

    function sub(int,int):haxe.io::Bytes	/* disp_id 0*/

    function set(int,int):void	/* disp_id 0*/

    function readString(int,int):String	/* disp_id 0*/
    var length:int	/* slot_id 0 */

    function getData():flash.utils::ByteArray	/* disp_id 0*/

    function get(int):int	/* disp_id 0*/

    function compare(haxe.io::Bytes):int	/* disp_id 0*/

    function blit(int,haxe.io::Bytes,int,int):void	/* disp_id 0*/
    var b:flash.utils::ByteArray	/* slot_id 0 */

    static function alloc(int):haxe.io::Bytes	/* disp_id 1*/

    static function ofString(String):haxe.io::Bytes	/* disp_id 2*/

    static function ofData(flash.utils::ByteArray):haxe.io::Bytes	/* disp_id 3*/

    static function haxe.io::Bytes$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 2827b 1%

abc name flash/Boot
magic 2e0010
Cpool numbers size 5 0 %
Cpool strings count 117 size 903 32 %
Cpool namespaces count 8 size 15 0 %
Cpool nssets count 2 size 3 0 %
Cpool names count 82 size 243 8 %
MethodInfo count 21 size 120 4 %
InstanceInfo size 7 0 %
ClassInfo size 59 2 %
ScriptInfo size 7 0 %
MethodBodies size 1445 51 %
  script0

  dynamic class flash::Boot extends flash.display::MovieClip
  {

    function flash::Boot(flash.display::MovieClip):void	/* disp_id -1*/
    static var init:Function	/* slot_id 1 */
    static var tf:flash.text::TextField	/* slot_id 2 */
    static var lines:Array	/* slot_id 3 */
    static var lastError:Error	/* slot_id 4 */
    static var skip_constructor:Boolean	/* slot_id 5 */

    static function enum_to_string(Object):String	/* disp_id 1*/

    static function __instanceof(*,*):Boolean	/* disp_id 2*/

    static function __clear_trace():void	/* disp_id 3*/

    static function __set_trace_color(uint):void	/* disp_id 4*/

    static function getTrace():flash.text::TextField	/* disp_id 5*/

    static function __trace(*,Object):void	/* disp_id 6*/

    static function __string_rec(*,String):String	/* disp_id 7*/

    static function __unprotect__(String):String	/* disp_id 8*/

    static function flash::Boot$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 1785b 1%

abc name de/polygonal/ds/mem/ShortMemory
magic 2e0010
Cpool numbers size 8 0 %
Cpool strings count 51 size 440 25 %
Cpool namespaces count 9 size 17 0 %
Cpool nssets count 2 size 3 0 %
Cpool names count 39 size 116 6 %
MethodInfo count 17 size 165 9 %
InstanceInfo size 45 2 %
ClassInfo size 30 1 %
ScriptInfo size 7 0 %
MethodBodies size 913 52 %
  script0

  class de.polygonal.ds.mem::ShortMemory extends de.polygonal.ds.mem::MemoryAccess
  {

    function de.polygonal.ds.mem::ShortMemory(int):void	/* disp_id -1*/

    function toString():String	/* disp_id 0*/

    function swp(int,int):void	/* disp_id 0*/
    var size:int	/* slot_id 0 */

    function set(int,int):void	/* disp_id 0*/

    function resize(int):void	/* disp_id 0*/

    function getAddr(int):int	/* disp_id 0*/

    function get(int):int	/* disp_id 0*/
    var _size:int	/* slot_id 0 */

    function __size():int	/* disp_id 0*/

    static function toByteArray(de.polygonal.ds.mem::ShortMemory,int,int):flash.utils::ByteArray	/* disp_id 1*/

    static function ofByteArray(flash.utils::ByteArray,int,int):de.polygonal.ds.mem::ShortMemory	/* disp_id 2*/

    static function toArray(de.polygonal.ds.mem::ShortMemory,int,int):Array	/* disp_id 3*/

    static function ofArray(Array,int,int):de.polygonal.ds.mem::ShortMemory	/* disp_id 4*/

    static function toVector(de.polygonal.ds.mem::ShortMemory,int,int):__AS3__.vec::Vector.<int>	/* disp_id 5*/

    static function toUnsignedVector(de.polygonal.ds.mem::ShortMemory,int,int):__AS3__.vec::Vector.<uint>	/* disp_id 6*/

    static function ofVector(__AS3__.vec::Vector.<int>,int,int):de.polygonal.ds.mem::ShortMemory	/* disp_id 7*/

    static function de.polygonal.ds.mem::ShortMemory$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 5897b 3%

abc name de/polygonal/core/fmt/Sprintf
magic 2e0010
Cpool numbers size 101 1 %
Cpool strings count 96 size 729 12 %
Cpool namespaces count 7 size 13 0 %
Cpool nssets count 2 size 3 0 %
Cpool names count 68 size 202 3 %
MethodInfo count 11 size 77 1 %
InstanceInfo size 32 0 %
ClassInfo size 144 2 %
ScriptInfo size 7 0 %
MethodBodies size 4550 77 %
  script0

  class de.polygonal.core.fmt::Sprintf extends Object
  {

    function de.polygonal.core.fmt::Sprintf():void	/* disp_id -1*/

    function cca(String,int):int	/* disp_id 0*/

    function _rpad(String,String,int):String	/* disp_id 0*/

    function _padNumber(String,Number,int,int):String	/* disp_id 0*/

    function _lpad(String,String,int):String	/* disp_id 0*/

    function _format(String,Array):String	/* disp_id 0*/
    var _bits:__AS3__.vec::Vector.<int>	/* slot_id 0 */
    static var BIT_MINUS:int	/* slot_id 1 */
    static var BIT_PLUS:int	/* slot_id 2 */
    static var BIT_SPACE:int	/* slot_id 3 */
    static var BIT_SHARP:int	/* slot_id 4 */
    static var BIT_ZERO:int	/* slot_id 5 */
    static var BIT_h:int	/* slot_id 6 */
    static var BIT_l:int	/* slot_id 7 */
    static var BIT_L:int	/* slot_id 8 */
    static var BIT_c:int	/* slot_id 9 */
    static var BIT_d:int	/* slot_id 10 */
    static var BIT_i:int	/* slot_id 11 */
    static var BIT_e:int	/* slot_id 12 */
    static var BIT_E:int	/* slot_id 13 */
    static var BIT_f:int	/* slot_id 14 */
    static var BIT_g:int	/* slot_id 15 */
    static var BIT_G:int	/* slot_id 16 */
    static var BIT_o:int	/* slot_id 17 */
    static var BIT_s:int	/* slot_id 18 */
    static var BIT_u:int	/* slot_id 19 */
    static var BIT_x:int	/* slot_id 20 */
    static var BIT_X:int	/* slot_id 21 */
    static var BIT_p:int	/* slot_id 22 */
    static var BIT_n:int	/* slot_id 23 */
    static var BIT_b:int	/* slot_id 24 */
    static var MASK_SPECIFIERS:int	/* slot_id 25 */
    static var _instance:de.polygonal.core.fmt::Sprintf	/* slot_id 26 */

    static function get():de.polygonal.core.fmt::Sprintf	/* disp_id 1*/

    static function free():void	/* disp_id 2*/

    static function format(String,Array):String	/* disp_id 3*/

    static function de.polygonal.core.fmt::Sprintf$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 1630b 1%

abc name de/polygonal/ds/GraphNode
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 48 size 426 26 %
Cpool namespaces count 5 size 9 0 %
Cpool nssets count 0 size 1 0 %
Cpool names count 37 size 109 6 %
MethodInfo count 19 size 112 7 %
InstanceInfo size 96 6 %
ClassInfo size 2 0 %
ScriptInfo size 7 0 %
MethodBodies size 830 51 %
  script0

  class de.polygonal.ds::GraphNode extends Object
  {

    function de.polygonal.ds::GraphNode(Object):void	/* disp_id -1*/
    var val:Object	/* slot_id 0 */

    function toString():String	/* disp_id 0*/

    function removeSingleArcs():void	/* disp_id 0*/

    function removeMutualArcs():void	/* disp_id 0*/

    function removeArc(de.polygonal.ds::GraphNode):void	/* disp_id 0*/
    var prev:de.polygonal.ds::GraphNode	/* slot_id 0 */
    var next:de.polygonal.ds::GraphNode	/* slot_id 0 */

    function iterator():Object	/* disp_id 0*/

    function isMutuallyConnected(de.polygonal.ds::GraphNode):Boolean	/* disp_id 0*/

    function isMarked():Boolean	/* disp_id 0*/

    function isConnected(de.polygonal.ds::GraphNode):Boolean	/* disp_id 0*/

    function getArc(de.polygonal.ds::GraphNode):de.polygonal.ds::GraphArc	/* disp_id 0*/
    var arcList:de.polygonal.ds::GraphArc	/* slot_id 0 */

    function addArc(de.polygonal.ds::GraphNode,Number):void	/* disp_id 0*/

    function _unlink(de.polygonal.ds::Graph):void	/* disp_id 0*/
    var _marked:Boolean	/* slot_id 0 */

    function _link(de.polygonal.ds::Graph):void	/* disp_id 0*/

    function __unlink(Object,de.polygonal.ds::GraphNode):void	/* disp_id 0*/

    function __setNodeList(Object,de.polygonal.ds::GraphNode):void	/* disp_id 0*/

    function __link(Object,de.polygonal.ds::GraphNode):void	/* disp_id 0*/

    function __getNodeList(Object):de.polygonal.ds::GraphNode	/* disp_id 0*/

    static function de.polygonal.ds::GraphNode$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 473b 0%

abc name de/polygonal/ds/GraphNodeIterator
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 21 size 158 36 %
Cpool namespaces count 4 size 7 1 %
Cpool nssets count 2 size 3 0 %
Cpool names count 18 size 52 11 %
MethodInfo count 6 size 30 6 %
InstanceInfo size 30 6 %
ClassInfo size 2 0 %
ScriptInfo size 7 1 %
MethodBodies size 138 31 %
  script0

  class de.polygonal.ds::GraphNodeIterator extends Object
    implements de.polygonal.ds::Itr
  {

    function de.polygonal.ds::GraphNodeIterator(de.polygonal.ds::GraphNode):void	/* disp_id -1*/

    function reset():void	/* disp_id 0*/

    function next():Object	/* disp_id 0*/

    function hasNext():Boolean	/* disp_id 0*/
    var _node:de.polygonal.ds::GraphNode	/* slot_id 0 */
    var _arcList:de.polygonal.ds::GraphArc	/* slot_id 0 */

    static function de.polygonal.ds::GraphNodeIterator$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 3858b 2%

abc name de/polygonal/ds/Array3
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 89 size 595 15 %
Cpool namespaces count 7 size 13 0 %
Cpool nssets count 3 size 5 0 %
Cpool names count 65 size 192 5 %
MethodInfo count 35 size 271 7 %
InstanceInfo size 156 4 %
ClassInfo size 2 0 %
ScriptInfo size 7 0 %
MethodBodies size 2582 67 %
  script0

  class de.polygonal.ds::Array3 extends Object
    implements de.polygonal.ds::Collection
  {

    function de.polygonal.ds::Array3(int,int,int):void	/* disp_id -1*/

    function walk(Function):void	/* disp_id 0*/

    function toVector(de.polygonal.ds::Vector):de.polygonal.ds::Vector	/* disp_id 0*/

    function toString():String	/* disp_id 0*/

    function toArray(Array):Array	/* disp_id 0*/

    function swap(int,int,int,int,int,int):void	/* disp_id 0*/

    function size():int	/* disp_id 0*/

    function shuffle(de.polygonal.ds::Vector):void	/* disp_id 0*/

    function setW(int):void	/* disp_id 0*/

    function setRow(int,int,Array):void	/* disp_id 0*/

    function setPile(int,int,Array):void	/* disp_id 0*/

    function setH(int):void	/* disp_id 0*/

    function setD(int):void	/* disp_id 0*/

    function setCol(int,int,Array):void	/* disp_id 0*/

    function set(int,int,int,Object):void	/* disp_id 0*/

    function resize(int,int,int):void	/* disp_id 0*/

    function iterator():Object	/* disp_id 0*/

    function isEmpty():Boolean	/* disp_id 0*/

    function getW():int	/* disp_id 0*/

    function getRow(int,int,Array):void	/* disp_id 0*/

    function getPile(int,int,Array):void	/* disp_id 0*/

    function getLayer(int,de.polygonal.ds::Array2):de.polygonal.ds::Array2	/* disp_id 0*/

    function getH():int	/* disp_id 0*/

    function getD():int	/* disp_id 0*/

    function getCol(int,int,Array):void	/* disp_id 0*/

    function getArray():Array	/* disp_id 0*/

    function get(int,int,int):Object	/* disp_id 0*/

    function contains(Object):Boolean	/* disp_id 0*/

    function clone(Boolean,Object):de.polygonal.ds::Collection	/* disp_id 0*/

    function clear():void	/* disp_id 0*/

    function assign(*,Array):void	/* disp_id 0*/
    var _w:int	/* slot_id 0 */
    var _h:int	/* slot_id 0 */
    var _d:int	/* slot_id 0 */
    var _a:Array	/* slot_id 0 */

    function __set(int,Object):void	/* disp_id 0*/

    function __get(int):Object	/* disp_id 0*/

    static function de.polygonal.ds::Array3$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 582b 0%

abc name de/polygonal/ds/Array3Iterator
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 27 size 155 28 %
Cpool namespaces count 5 size 9 1 %
Cpool nssets count 3 size 5 0 %
Cpool names count 24 size 69 12 %
MethodInfo count 8 size 42 7 %
InstanceInfo size 48 8 %
ClassInfo size 2 0 %
ScriptInfo size 7 1 %
MethodBodies size 202 36 %
  script0

  class de.polygonal.ds::Array3Iterator extends Object
    implements de.polygonal.ds::Itr
  {

    function de.polygonal.ds::Array3Iterator(de.polygonal.ds::Array3):void	/* disp_id -1*/

    function reset():void	/* disp_id 0*/

    function next():Object	/* disp_id 0*/

    function hasNext():Boolean	/* disp_id 0*/
    var _s:int	/* slot_id 0 */
    var _i:int	/* slot_id 0 */
    var _f:de.polygonal.ds::Array3	/* slot_id 0 */
    var _a:Array	/* slot_id 0 */

    function __size(Object):int	/* disp_id 0*/

    function __a(Object):Array	/* disp_id 0*/

    static function de.polygonal.ds::Array3Iterator$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 1081b 0%

abc name flash/Lib
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 50 size 388 36 %
Cpool namespaces count 10 size 19 1 %
Cpool nssets count 2 size 3 0 %
Cpool names count 36 size 106 9 %
MethodInfo count 12 size 77 7 %
InstanceInfo size 7 0 %
ClassInfo size 43 4 %
ScriptInfo size 7 0 %
MethodBodies size 409 38 %
  script0

  class flash::Lib extends Object
  {

    function flash::Lib():void	/* disp_id -1*/
    static var current:flash.display::MovieClip	/* slot_id 1 */

    static function getTimer():int	/* disp_id 1*/

    static function eval(String):*	/* disp_id 2*/

    static function getURL(flash.net::URLRequest,String):void	/* disp_id 3*/

    static function fscommand(String,String):void	/* disp_id 4*/

    static function trace(*):void	/* disp_id 5*/

    static function attach(String):flash.display::MovieClip	/* disp_id 6*/

    static function as(*,Class):Object	/* disp_id 7*/

    static function vectorOfArray(Array):__AS3__.vec::Vector.<Object>	/* disp_id 8*/

    static function vectorConvert(__AS3__.vec::Vector.<Object>):__AS3__.vec::Vector.<Object>	/* disp_id 9*/

    static function flash::Lib$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 7474b 4%

abc name de/polygonal/ds/mem/MemoryManager
magic 2e0010
Cpool numbers size 33 0 %
Cpool strings count 119 size 1085 14 %
Cpool namespaces count 13 size 25 0 %
Cpool nssets count 2 size 3 0 %
Cpool names count 100 size 297 3 %
MethodInfo count 27 size 155 2 %
InstanceInfo size 100 1 %
ClassInfo size 95 1 %
ScriptInfo size 7 0 %
MethodBodies size 5631 75 %
  script0

  class de.polygonal.ds.mem::MemoryManager extends Object
  {

    function de.polygonal.ds.mem::MemoryManager():void	/* disp_id -1*/

    function _wipe(int,int,int):void	/* disp_id 0*/

    function _realloc(de.polygonal.ds.mem::MemoryAccess,int):void	/* disp_id 0*/

    function _pack():void	/* disp_id 0*/

    function _mergeRight(de.polygonal.ds.mem._MemoryManager::MemoryArea):void	/* disp_id 0*/

    function _mergeLeft(de.polygonal.ds.mem._MemoryManager::MemoryArea):void	/* disp_id 0*/

    function _memmove(int,int,int,int):void	/* disp_id 0*/
    var _map:de.polygonal.ds::HashMap	/* slot_id 0 */

    function _malloc(de.polygonal.ds.mem::MemoryAccess,int):void	/* disp_id 0*/
    var _intervalList:de.polygonal.ds.mem._MemoryManager::MemoryArea	/* slot_id 0 */

    function _grow(int):void	/* disp_id 0*/

    function _free():void	/* disp_id 0*/

    function _findEmptySpace(int):de.polygonal.ds.mem._MemoryManager::MemoryArea	/* disp_id 0*/

    function _defrag():void	/* disp_id 0*/

    function _dealloc(de.polygonal.ds.mem::MemoryAccess):void	/* disp_id 0*/
    var _bytesUsed:int	/* slot_id 0 */
    var _bytesTotal:int	/* slot_id 0 */
    var _bytesRaw:int	/* slot_id 0 */
    var _bytes:flash.utils::ByteArray	/* slot_id 0 */
    var _buffer:flash.utils::ByteArray	/* slot_id 0 */
    var _blockSizeShift:int	/* slot_id 0 */
    var _blockSizeBytes:int	/* slot_id 0 */
    static var _I:de.polygonal.ds.mem::MemoryManager	/* slot_id 1 */
    static var get:de.polygonal.ds.mem::MemoryManager	/* slot_id 2 */

    static function __get():de.polygonal.ds.mem::MemoryManager	/* disp_id 1*/
    static var RESERVE_BYTES:int	/* slot_id 3 */
    static var MEMORY_LIMIT_BYTES:int	/* slot_id 4 */
    static var BLOCK_SIZE_BYTES:int	/* slot_id 5 */
    static var RAW_BYTES:int	/* slot_id 6 */

    static function free():void	/* disp_id 2*/
    static var bytesTotal:int	/* slot_id 7 */

    static function __bytesTotal():int	/* disp_id 3*/
    static var bytesUsed:int	/* slot_id 8 */

    static function __bytesUsed():int	/* disp_id 4*/
    static var bytesFree:int	/* slot_id 9 */

    static function __bytesFree():int	/* disp_id 5*/

    static function malloc(de.polygonal.ds.mem::MemoryAccess,int):void	/* disp_id 6*/

    static function dealloc(de.polygonal.ds.mem::MemoryAccess):void	/* disp_id 7*/

    static function realloc(de.polygonal.ds.mem::MemoryAccess,int):void	/* disp_id 8*/

    static function defrag():void	/* disp_id 9*/

    static function pack():void	/* disp_id 10*/

    static function draw(flash.display::Graphics,flash.geom::Rectangle):void	/* disp_id 11*/

    static function toString():String	/* disp_id 12*/

    static function de.polygonal.ds.mem::MemoryManager$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 1281b 0%

abc name de/polygonal/ds/mem/_MemoryManager/MemoryArea
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 42 size 385 31 %
Cpool namespaces count 6 size 11 0 %
Cpool nssets count 0 size 1 0 %
Cpool names count 34 size 100 8 %
MethodInfo count 14 size 82 6 %
InstanceInfo size 96 7 %
ClassInfo size 2 0 %
ScriptInfo size 7 0 %
MethodBodies size 539 43 %
  script0

  class de.polygonal.ds.mem._MemoryManager::MemoryArea extends Object
  {

    function de.polygonal.ds.mem._MemoryManager::MemoryArea(de.polygonal.ds.mem::MemoryManager,int):void	/* disp_id -1*/

    function wipe():void	/* disp_id 0*/

    function toString():String	/* disp_id 0*/
    var size:int	/* slot_id 0 */

    function shrinkRight(int):void	/* disp_id 0*/

    function shrinkLeft(int):void	/* disp_id 0*/

    function shiftRight(int):void	/* disp_id 0*/

    function shiftLeft(int):void	/* disp_id 0*/

    function setOffset():void	/* disp_id 0*/

    function setByteOffset(Object,int):void	/* disp_id 0*/
    var prev:de.polygonal.ds.mem._MemoryManager::MemoryArea	/* slot_id 0 */
    var offset:int	/* slot_id 0 */
    var next:de.polygonal.ds.mem._MemoryManager::MemoryArea	/* slot_id 0 */
    var manager:de.polygonal.ds.mem::MemoryManager	/* slot_id 0 */
    var isEmpty:Boolean	/* slot_id 0 */

    function expandRight(int):void	/* disp_id 0*/

    function expandLeft(int):void	/* disp_id 0*/
    var e:int	/* slot_id 0 */

    function copy():de.polygonal.ds.mem._MemoryManager::MemoryArea	/* disp_id 0*/
    var b:int	/* slot_id 0 */
    var access:de.polygonal.ds.mem::MemoryAccess	/* slot_id 0 */

    static function de.polygonal.ds.mem._MemoryManager::MemoryArea$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 1443b 0%

abc name de/polygonal/ds/pooling/ObjectPool
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 48 size 401 28 %
Cpool namespaces count 8 size 15 1 %
Cpool nssets count 2 size 3 0 %
Cpool names count 38 size 112 7 %
MethodInfo count 12 size 73 5 %
InstanceInfo size 68 4 %
ClassInfo size 2 0 %
ScriptInfo size 7 0 %
MethodBodies size 715 50 %
  script0

  class de.polygonal.ds.pooling::ObjectPool extends Object
  {

    function de.polygonal.ds.pooling::ObjectPool(int):void	/* disp_id -1*/

    function toString():String	/* disp_id 0*/

    function size():int	/* disp_id 0*/

    function put(int):void	/* disp_id 0*/

    function next():int	/* disp_id 0*/

    function iterator():Object	/* disp_id 0*/

    function get(int):Object	/* disp_id 0*/

    function free():void	/* disp_id 0*/

    function allocate(Class,Object,de.polygonal.ds.pooling::Factory):void	/* disp_id 0*/
    var _size:int	/* slot_id 0 */
    var _pool:__AS3__.vec::Vector.<de.polygonal.ds.pooling::ObjectPoolItem>	/* slot_id 0 */
    var _last:int	/* slot_id 0 */
    var _item:de.polygonal.ds.pooling::ObjectPoolItem	/* slot_id 0 */
    var _free:int	/* slot_id 0 */

    function _fill(Array):void	/* disp_id 0*/

    static function de.polygonal.ds.pooling::ObjectPool$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 415b 0%

abc name de/polygonal/ds/pooling/ObjectPoolItem
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 18 size 178 47 %
Cpool namespaces count 4 size 7 1 %
Cpool nssets count 0 size 1 0 %
Cpool names count 14 size 40 10 %
MethodInfo count 4 size 17 4 %
InstanceInfo size 26 6 %
ClassInfo size 2 0 %
ScriptInfo size 7 1 %
MethodBodies size 86 23 %
  script0

  class de.polygonal.ds.pooling::ObjectPoolItem extends Object
  {

    function de.polygonal.ds.pooling::ObjectPoolItem():void	/* disp_id -1*/

    function toString():String	/* disp_id 0*/
    var obj:Object	/* slot_id 0 */
    var next:int	/* slot_id 0 */
    var id:int	/* slot_id 0 */

    static function de.polygonal.ds.pooling::ObjectPoolItem$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 577b 0%

abc name de/polygonal/ds/pooling/ObjectPoolIterator
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 27 size 200 37 %
Cpool namespaces count 7 size 13 2 %
Cpool nssets count 3 size 5 0 %
Cpool names count 23 size 67 12 %
MethodInfo count 6 size 30 5 %
InstanceInfo size 40 7 %
ClassInfo size 2 0 %
ScriptInfo size 7 1 %
MethodBodies size 158 29 %
  script0

  class de.polygonal.ds.pooling::ObjectPoolIterator extends Object
    implements de.polygonal.ds::Itr
  {

    function de.polygonal.ds.pooling::ObjectPoolIterator(Object):void	/* disp_id -1*/

    function reset():void	/* disp_id 0*/

    function next():Object	/* disp_id 0*/

    function hasNext():Boolean	/* disp_id 0*/
    var _v:__AS3__.vec::Vector.<de.polygonal.ds.pooling::ObjectPoolItem>	/* slot_id 0 */
    var _s:int	/* slot_id 0 */
    var _i:int	/* slot_id 0 */
    var _f:Object	/* slot_id 0 */

    static function de.polygonal.ds.pooling::ObjectPoolIterator$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 936b 0%

abc name de/polygonal/gl/color/ColorConversion
magic 2e0010
Cpool numbers size 21 2 %
Cpool strings count 18 size 98 10 %
Cpool namespaces count 3 size 5 0 %
Cpool nssets count 0 size 1 0 %
Cpool names count 14 size 40 4 %
MethodInfo count 5 size 35 3 %
InstanceInfo size 7 0 %
ClassInfo size 10 1 %
ScriptInfo size 7 0 %
MethodBodies size 665 74 %
  script0

  class de.polygonal.gl.color::ColorConversion extends Object
  {

    function de.polygonal.gl.color::ColorConversion():void	/* disp_id -1*/

    static function RGBtoHSV(de.polygonal.gl.color::RGB,de.polygonal.gl.color::HSV):de.polygonal.gl.color::HSV	/* disp_id 1*/

    static function HSVtoRGB(de.polygonal.gl.color::HSV,de.polygonal.gl.color::RGB):de.polygonal.gl.color::RGB	/* disp_id 2*/

    static function de.polygonal.gl.color::ColorConversion$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 807b 0%

abc name haxe/Int32
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 34 size 175 22 %
Cpool namespaces count 4 size 7 0 %
Cpool nssets count 0 size 1 0 %
Cpool names count 27 size 79 9 %
MethodInfo count 21 size 147 18 %
InstanceInfo size 7 0 %
ClassInfo size 74 9 %
ScriptInfo size 7 0 %
MethodBodies size 287 36 %
  script0

  class haxe::Int32 extends Object
  {

    function haxe::Int32():void	/* disp_id -1*/

    static function make(int,int):int	/* disp_id 1*/

    static function ofInt(int):int	/* disp_id 2*/

    static function toInt(int):int	/* disp_id 3*/

    static function toNativeInt(int):int	/* disp_id 4*/

    static function add(int,int):int	/* disp_id 5*/

    static function sub(int,int):int	/* disp_id 6*/

    static function mul(int,int):int	/* disp_id 7*/

    static function div(int,int):int	/* disp_id 8*/

    static function mod(int,int):int	/* disp_id 9*/

    static function shl(int,int):int	/* disp_id 10*/

    static function shr(int,int):int	/* disp_id 11*/

    static function ushr(int,int):int	/* disp_id 12*/

    static function and(int,int):int	/* disp_id 13*/

    static function or(int,int):int	/* disp_id 14*/

    static function xor(int,int):int	/* disp_id 15*/

    static function neg(int):int	/* disp_id 16*/

    static function complement(int):int	/* disp_id 17*/

    static function compare(int,int):int	/* disp_id 18*/

    static function haxe::Int32$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 677b 0%

abc name ValueType
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 31 size 208 31 %
Cpool namespaces count 3 size 5 0 %
Cpool nssets count 0 size 1 0 %
Cpool names count 27 size 79 11 %
MethodInfo count 6 size 35 5 %
InstanceInfo size 32 4 %
ClassInfo size 56 8 %
ScriptInfo size 7 1 %
MethodBodies size 232 34 %
  script0

  final class ValueType extends Object
  {

    function ValueType(String,int,*):void	/* disp_id -1*/
    var tag:String	/* slot_id 0 */
    var index:int	/* slot_id 0 */
    var params:Array	/* slot_id 0 */
    const __enum__:Boolean = "true"	/* slot_id 0 */

    function toString():String	/* disp_id 0*/
    static const __isenum:Boolean = "true"	/* slot_id 11 */
    static var __constructs__:*	/* slot_id 10 */
    static var TUnknown:ValueType	/* slot_id 9 */
    static var TObject:ValueType	/* slot_id 8 */
    static var TNull:ValueType	/* slot_id 7 */
    static var TInt:ValueType	/* slot_id 6 */
    static var TFunction:ValueType	/* slot_id 5 */
    static var TFloat:ValueType	/* slot_id 4 */

    static function TEnum(Class):ValueType	/* disp_id 3*/

    static function TClass(Class):ValueType	/* disp_id 2*/
    static var TBool:ValueType	/* slot_id 1 */

    static function ValueType$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 4083b 2%

abc name Type
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 108 size 1031 25 %
Cpool namespaces count 6 size 11 0 %
Cpool nssets count 2 size 3 0 %
Cpool names count 73 size 216 5 %
MethodInfo count 23 size 153 3 %
InstanceInfo size 7 0 %
ClassInfo size 82 2 %
ScriptInfo size 7 0 %
MethodBodies size 2556 62 %
  script0

  class Type extends Object
  {

    function Type():void	/* disp_id -1*/

    static function getClass(Object):Class	/* disp_id 1*/

    static function getEnum(*):Class	/* disp_id 2*/

    static function getSuperClass(Class):Class	/* disp_id 3*/

    static function getClassName(Class):String	/* disp_id 4*/

    static function getEnumName(Class):String	/* disp_id 5*/

    static function resolveClass(String):Class	/* disp_id 6*/

    static function resolveEnum(String):Class	/* disp_id 7*/

    static function createInstance(Class,Array):Object	/* disp_id 8*/

    static function createEmptyInstance(Class):Object	/* disp_id 9*/

    static function createEnum(Class,String,Array):Object	/* disp_id 10*/

    static function createEnumIndex(Class,int,Array):Object	/* disp_id 11*/

    static function describe(*,Boolean):Array	/* disp_id 12*/

    static function getInstanceFields(Class):Array	/* disp_id 13*/

    static function getClassFields(Class):Array	/* disp_id 14*/

    static function getEnumConstructs(Class):Array	/* disp_id 15*/

    static function typeof(*):ValueType	/* disp_id 16*/

    static function enumEq(Object,Object):Boolean	/* disp_id 17*/

    static function enumConstructor(*):String	/* disp_id 18*/

    static function enumParameters(*):Array	/* disp_id 19*/

    static function enumIndex(*):int	/* disp_id 20*/

    static function Type$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 1145b 0%

abc name Reflect
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 42 size 308 27 %
Cpool namespaces count 4 size 7 0 %
Cpool nssets count 2 size 3 0 %
Cpool names count 29 size 84 7 %
MethodInfo count 16 size 107 9 %
InstanceInfo size 7 0 %
ClassInfo size 50 4 %
ScriptInfo size 7 0 %
MethodBodies size 552 48 %
  script0

  class Reflect extends Object
  {

    function Reflect():void	/* disp_id -1*/

    static function hasField(*,String):Boolean	/* disp_id 1*/

    static function field(*,String):*	/* disp_id 2*/

    static function setField(*,String,*):void	/* disp_id 3*/

    static function callMethod(*,*,Array):*	/* disp_id 4*/

    static function fields(*):Array	/* disp_id 5*/

    static function isFunction(*):Boolean	/* disp_id 6*/

    static function compare(Object,Object):int	/* disp_id 7*/

    static function compareMethods(*,*):Boolean	/* disp_id 8*/

    static function isObject(*):Boolean	/* disp_id 9*/

    static function deleteField(*,String):Boolean	/* disp_id 10*/

    static function copy(Object):Object	/* disp_id 11*/

    static function makeVarArgs(Function):*	/* disp_id 12*/

    static function Reflect$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 2128b 1%

abc name de/polygonal/ds/BitVector
magic 2e0010
Cpool numbers size 17 0 %
Cpool strings count 49 size 408 19 %
Cpool namespaces count 8 size 15 0 %
Cpool nssets count 2 size 3 0 %
Cpool names count 40 size 118 5 %
MethodInfo count 16 size 90 4 %
InstanceInfo size 74 3 %
ClassInfo size 2 0 %
ScriptInfo size 7 0 %
MethodBodies size 1359 64 %
  script0

  class de.polygonal.ds::BitVector extends Object
  {

    function de.polygonal.ds::BitVector(int):void	/* disp_id -1*/

    function toString():String	/* disp_id 0*/

    function toBytes(Boolean):flash.utils::ByteArray	/* disp_id 0*/

    function size():int	/* disp_id 0*/

    function setAll():void	/* disp_id 0*/

    function set(int):void	/* disp_id 0*/

    function resize(int):void	/* disp_id 0*/

    function ofBytes(flash.utils::ByteArray,Boolean):void	/* disp_id 0*/

    function has(int):Boolean	/* disp_id 0*/

    function clrAll():void	/* disp_id 0*/

    function clr(int):void	/* disp_id 0*/

    function clone():de.polygonal.ds::BitVector	/* disp_id 0*/

    function capacity():int	/* disp_id 0*/

    function bucketSize():int	/* disp_id 0*/
    var _bits:__AS3__.vec::Vector.<int>	/* slot_id 0 */
    var _bitSize:int	/* slot_id 0 */
    var _arrSize:int	/* slot_id 0 */

    static function de.polygonal.ds::BitVector$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 219b 0%

abc name de/polygonal/ds/Visitable
magic 2e0010
Cpool numbers size 3 1 %
Cpool strings count 10 size 92 48 %
Cpool namespaces count 4 size 7 3 %
Cpool nssets count 0 size 1 0 %
Cpool names count 5 size 13 6 %
MethodInfo count 4 size 21 11 %
InstanceInfo size 11 5 %
ClassInfo size 2 1 %
ScriptInfo size 7 3 %
MethodBodies size 27 14 %
  script0

  interface de.polygonal.ds::Visitable extends *
  {

    function de.polygonal.ds::Visitable():void	/* disp_id -1*/

    function de.polygonal.ds:Visitable::visit(Boolean,*):Boolean	/* disp_id 0*/

    static function de.polygonal.ds::Visitable$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 1100b 0%

abc name de/polygonal/ds/DLLNode
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 39 size 303 28 %
Cpool namespaces count 5 size 9 0 %
Cpool nssets count 0 size 1 0 %
Cpool names count 31 size 91 8 %
MethodInfo count 17 size 90 8 %
InstanceInfo size 83 7 %
ClassInfo size 2 0 %
ScriptInfo size 7 0 %
MethodBodies size 478 44 %
  script0

  class de.polygonal.ds::DLLNode extends Object
  {

    function de.polygonal.ds::DLLNode(Object,de.polygonal.ds::DLL):void	/* disp_id -1*/
    var val:Object	/* slot_id 0 */

    function toString():String	/* disp_id 0*/

    function remove():de.polygonal.ds::DLLNode	/* disp_id 0*/

    function prevVal():Object	/* disp_id 0*/
    var prev:de.polygonal.ds::DLLNode	/* slot_id 0 */

    function prependTo(de.polygonal.ds::DLLNode):de.polygonal.ds::DLLNode	/* disp_id 0*/

    function prepend(de.polygonal.ds::DLLNode):de.polygonal.ds::DLLNode	/* disp_id 0*/

    function nextVal():Object	/* disp_id 0*/
    var next:de.polygonal.ds::DLLNode	/* slot_id 0 */

    function hasPrev():Boolean	/* disp_id 0*/

    function hasNext():Boolean	/* disp_id 0*/

    function getList():de.polygonal.ds::DLL	/* disp_id 0*/

    function appendTo(de.polygonal.ds::DLLNode):de.polygonal.ds::DLLNode	/* disp_id 0*/

    function append(de.polygonal.ds::DLLNode):de.polygonal.ds::DLLNode	/* disp_id 0*/

    function _unlink():de.polygonal.ds::DLLNode	/* disp_id 0*/
    var _list:de.polygonal.ds::DLL	/* slot_id 0 */

    function _insertBefore(de.polygonal.ds::DLLNode):void	/* disp_id 0*/

    function _insertAfter(de.polygonal.ds::DLLNode):void	/* disp_id 0*/

    static function de.polygonal.ds::DLLNode$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 477b 0%

abc name de/polygonal/ds/HeapIterator
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 22 size 132 29 %
Cpool namespaces count 5 size 9 2 %
Cpool nssets count 3 size 5 1 %
Cpool names count 19 size 54 12 %
MethodInfo count 6 size 30 6 %
InstanceInfo size 40 9 %
ClassInfo size 2 0 %
ScriptInfo size 7 1 %
MethodBodies size 157 35 %
  script0

  class de.polygonal.ds::HeapIterator extends Object
    implements de.polygonal.ds::Itr
  {

    function de.polygonal.ds::HeapIterator(Object):void	/* disp_id -1*/

    function reset():void	/* disp_id 0*/

    function next():Object	/* disp_id 0*/

    function hasNext():Boolean	/* disp_id 0*/
    var _s:int	/* slot_id 0 */
    var _i:int	/* slot_id 0 */
    var _f:Object	/* slot_id 0 */
    var _a:Array	/* slot_id 0 */

    static function de.polygonal.ds::HeapIterator$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 2554b 1%

abc name de/polygonal/ds/Heap
magic 2e0010
Cpool numbers size 8 0 %
Cpool strings count 55 size 455 17 %
Cpool namespaces count 7 size 13 0 %
Cpool nssets count 3 size 5 0 %
Cpool names count 45 size 132 5 %
MethodInfo count 20 size 119 4 %
InstanceInfo size 96 3 %
ClassInfo size 2 0 %
ScriptInfo size 7 0 %
MethodBodies size 1687 66 %
  script0

  class de.polygonal.ds::Heap extends Object
    implements de.polygonal.ds::Collection
  {

    function de.polygonal.ds::Heap(Boolean,int):void	/* disp_id -1*/

    function toVector(de.polygonal.ds::Vector):de.polygonal.ds::Vector	/* disp_id 0*/

    function toString():String	/* disp_id 0*/

    function toArray(Array):Array	/* disp_id 0*/

    function size():int	/* disp_id 0*/

    function remove(de.polygonal.ds::Comparable):void	/* disp_id 0*/

    function maxSize():int	/* disp_id 0*/

    function iterator():Object	/* disp_id 0*/

    function isEmpty():Boolean	/* disp_id 0*/

    function front():de.polygonal.ds::Comparable	/* disp_id 0*/

    function enqueue(de.polygonal.ds::Comparable):void	/* disp_id 0*/

    function dequeue():de.polygonal.ds::Comparable	/* disp_id 0*/

    function contains(Object):Boolean	/* disp_id 0*/

    function compact():void	/* disp_id 0*/

    function clone(Boolean,Object):de.polygonal.ds::Collection	/* disp_id 0*/

    function clear():void	/* disp_id 0*/
    var _size:int	/* slot_id 0 */
    var _removable:Boolean	/* slot_id 0 */
    var _position:de.polygonal.ds::HashMap	/* slot_id 0 */
    var _a:Array	/* slot_id 0 */

    function __set(int,de.polygonal.ds::Comparable):void	/* disp_id 0*/

    function __get(int):de.polygonal.ds::Comparable	/* disp_id 0*/

    static function de.polygonal.ds::Heap$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 2452b 1%

abc name de/polygonal/gl/text/ASCII
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 114 size 605 24 %
Cpool namespaces count 3 size 5 0 %
Cpool nssets count 0 size 1 0 %
Cpool names count 112 size 334 13 %
MethodInfo count 11 size 61 2 %
InstanceInfo size 7 0 %
ClassInfo size 524 21 %
ScriptInfo size 7 0 %
MethodBodies size 869 35 %
  script0

  class de.polygonal.gl.text::ASCII extends Object
  {

    function de.polygonal.gl.text::ASCII():void	/* disp_id -1*/

    static function isASCII(int):Boolean	/* disp_id 1*/

    static function isDigit(int):Boolean	/* disp_id 2*/

    static function isWhite(int):Boolean	/* disp_id 3*/

    static function isPrintable(int):Boolean	/* disp_id 4*/

    static function isUpperCaseAlphabetic(int):Boolean	/* disp_id 5*/

    static function isLowerCaseAlphabetic(int):Boolean	/* disp_id 6*/

    static function isAlphabetic(int):Boolean	/* disp_id 7*/

    static function isAlphaNumeric(int):Boolean	/* disp_id 8*/
    static var BACKSPACE:int	/* slot_id 1 */
    static var TAB:int	/* slot_id 2 */
    static var NEWLINE:int	/* slot_id 3 */
    static var SPACE:int	/* slot_id 4 */
    static var EXCLAM:int	/* slot_id 5 */
    static var QUOTEDBL:int	/* slot_id 6 */
    static var NUMBERSIGN:int	/* slot_id 7 */
    static var DOLLAR:int	/* slot_id 8 */
    static var PERCENT:int	/* slot_id 9 */
    static var AMPERSAND:int	/* slot_id 10 */
    static var QUOTESINGLE:int	/* slot_id 11 */
    static var PARENLEFT:int	/* slot_id 12 */
    static var PARENRIGHT:int	/* slot_id 13 */
    static var ASTERISK:int	/* slot_id 14 */
    static var PLUS:int	/* slot_id 15 */
    static var COMMA:int	/* slot_id 16 */
    static var HYPHEN:int	/* slot_id 17 */
    static var PERIOD:int	/* slot_id 18 */
    static var SLASH:int	/* slot_id 19 */
    static var ZERO:int	/* slot_id 20 */
    static var ONE:int	/* slot_id 21 */
    static var TWO:int	/* slot_id 22 */
    static var THREE:int	/* slot_id 23 */
    static var FOUR:int	/* slot_id 24 */
    static var FIVE:int	/* slot_id 25 */
    static var SIX:int	/* slot_id 26 */
    static var SEVEN:int	/* slot_id 27 */
    static var EIGHT:int	/* slot_id 28 */
    static var NINE:int	/* slot_id 29 */
    static var COLON:int	/* slot_id 30 */
    static var SEMICOLON:int	/* slot_id 31 */
    static var LESS:int	/* slot_id 32 */
    static var EQUAL:int	/* slot_id 33 */
    static var GREATER:int	/* slot_id 34 */
    static var QUESTION:int	/* slot_id 35 */
    static var AT:int	/* slot_id 36 */
    static var A:int	/* slot_id 37 */
    static var B:int	/* slot_id 38 */
    static var C:int	/* slot_id 39 */
    static var D:int	/* slot_id 40 */
    static var E:int	/* slot_id 41 */
    static var F:int	/* slot_id 42 */
    static var G:int	/* slot_id 43 */
    static var H:int	/* slot_id 44 */
    static var I:int	/* slot_id 45 */
    static var J:int	/* slot_id 46 */
    static var K:int	/* slot_id 47 */
    static var L:int	/* slot_id 48 */
    static var M:int	/* slot_id 49 */
    static var N:int	/* slot_id 50 */
    static var O:int	/* slot_id 51 */
    static var P:int	/* slot_id 52 */
    static var Q:int	/* slot_id 53 */
    static var R:int	/* slot_id 54 */
    static var S:int	/* slot_id 55 */
    static var T:int	/* slot_id 56 */
    static var U:int	/* slot_id 57 */
    static var V:int	/* slot_id 58 */
    static var W:int	/* slot_id 59 */
    static var X:int	/* slot_id 60 */
    static var Y:int	/* slot_id 61 */
    static var Z:int	/* slot_id 62 */
    static var BRACKETLEFT:int	/* slot_id 63 */
    static var BACKSLASH:int	/* slot_id 64 */
    static var BRACKETRIGHT:int	/* slot_id 65 */
    static var CIRCUM:int	/* slot_id 66 */
    static var UNDERSCORE:int	/* slot_id 67 */
    static var GRAVE:int	/* slot_id 68 */
    static var a:int	/* slot_id 69 */
    static var b:int	/* slot_id 70 */
    static var c:int	/* slot_id 71 */
    static var d:int	/* slot_id 72 */
    static var e:int	/* slot_id 73 */
    static var f:int	/* slot_id 74 */
    static var g:int	/* slot_id 75 */
    static var h:int	/* slot_id 76 */
    static var i:int	/* slot_id 77 */
    static var j:int	/* slot_id 78 */
    static var k:int	/* slot_id 79 */
    static var l:int	/* slot_id 80 */
    static var m:int	/* slot_id 81 */
    static var n:int	/* slot_id 82 */
    static var o:int	/* slot_id 83 */
    static var p:int	/* slot_id 84 */
    static var q:int	/* slot_id 85 */
    static var r:int	/* slot_id 86 */
    static var s:int	/* slot_id 87 */
    static var t:int	/* slot_id 88 */
    static var u:int	/* slot_id 89 */
    static var v:int	/* slot_id 90 */
    static var w:int	/* slot_id 91 */
    static var x:int	/* slot_id 92 */
    static var y:int	/* slot_id 93 */
    static var z:int	/* slot_id 94 */
    static var BRACELEFT:int	/* slot_id 95 */
    static var BAR:int	/* slot_id 96 */
    static var BRACERIGTH:int	/* slot_id 97 */
    static var TILDE:int	/* slot_id 98 */

    static function de.polygonal.gl.text::ASCII$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 362b 0%

abc name de/polygonal/gl/color/HSV
magic 2e0010
Cpool numbers size 11 3 %
Cpool strings count 14 size 85 25 %
Cpool namespaces count 4 size 7 2 %
Cpool nssets count 0 size 1 0 %
Cpool names count 11 size 31 9 %
MethodInfo count 4 size 36 10 %
InstanceInfo size 26 7 %
ClassInfo size 2 0 %
ScriptInfo size 7 2 %
MethodBodies size 121 36 %
  script0

  class de.polygonal.gl.color::HSV extends Object
  {

    function de.polygonal.gl.color::HSV(Number,Number,Number):void	/* disp_id -1*/
    var v:Number	/* slot_id 0 */

    function set(Number,Number,Number):void	/* disp_id 0*/
    var s:Number	/* slot_id 0 */
    var h:Number	/* slot_id 0 */

    static function de.polygonal.gl.color::HSV$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 590b 0%

abc name de/polygonal/ds/PriorityQueueIterator
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 26 size 180 32 %
Cpool namespaces count 5 size 9 1 %
Cpool nssets count 3 size 5 0 %
Cpool names count 23 size 66 12 %
MethodInfo count 8 size 42 7 %
InstanceInfo size 48 8 %
ClassInfo size 2 0 %
ScriptInfo size 7 1 %
MethodBodies size 181 33 %
  script0

  class de.polygonal.ds::PriorityQueueIterator extends Object
    implements de.polygonal.ds::Itr
  {

    function de.polygonal.ds::PriorityQueueIterator(de.polygonal.ds::PriorityQueue):void	/* disp_id -1*/

    function reset():void	/* disp_id 0*/

    function next():Object	/* disp_id 0*/

    function hasNext():Boolean	/* disp_id 0*/
    var _s:int	/* slot_id 0 */
    var _i:int	/* slot_id 0 */
    var _f:de.polygonal.ds::PriorityQueue	/* slot_id 0 */
    var _a:Array	/* slot_id 0 */

    function __size(Object):int	/* disp_id 0*/

    function __a(Object):Array	/* disp_id 0*/

    static function de.polygonal.ds::PriorityQueueIterator$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 4481b 2%

abc name de/polygonal/ds/PriorityQueue
magic 2e0010
Cpool numbers size 8 0 %
Cpool strings count 58 size 514 11 %
Cpool namespaces count 7 size 13 0 %
Cpool nssets count 3 size 5 0 %
Cpool names count 46 size 135 3 %
MethodInfo count 23 size 139 3 %
InstanceInfo size 103 2 %
ClassInfo size 2 0 %
ScriptInfo size 7 0 %
MethodBodies size 3516 79 %
  script0

  class de.polygonal.ds::PriorityQueue extends Object
    implements de.polygonal.ds::Collection
  {

    function de.polygonal.ds::PriorityQueue(Boolean,int):void	/* disp_id -1*/

    function toVector(de.polygonal.ds::Vector):de.polygonal.ds::Vector	/* disp_id 0*/

    function toString():String	/* disp_id 0*/

    function toArray(Array):Array	/* disp_id 0*/

    function size():int	/* disp_id 0*/

    function reprioritize(de.polygonal.ds::Prioritizable,int):void	/* disp_id 0*/

    function remove(de.polygonal.ds::Prioritizable):void	/* disp_id 0*/

    function maxSize():int	/* disp_id 0*/

    function iterator():Object	/* disp_id 0*/

    function isEmpty():Boolean	/* disp_id 0*/

    function front():de.polygonal.ds::Prioritizable	/* disp_id 0*/

    function enqueue(de.polygonal.ds::Prioritizable):void	/* disp_id 0*/

    function dequeue():de.polygonal.ds::Prioritizable	/* disp_id 0*/

    function contains(Object):Boolean	/* disp_id 0*/

    function compact():void	/* disp_id 0*/

    function clone(Boolean,Object):de.polygonal.ds::Collection	/* disp_id 0*/

    function clear():void	/* disp_id 0*/

    function _walkUp(int):void	/* disp_id 0*/

    function _walkDown(int):void	/* disp_id 0*/
    var _size:int	/* slot_id 0 */
    var _inverse:Boolean	/* slot_id 0 */
    var _a:Array	/* slot_id 0 */

    function __set(int,de.polygonal.ds::Prioritizable):void	/* disp_id 0*/

    function __get(int):de.polygonal.ds::Prioritizable	/* disp_id 0*/

    static function de.polygonal.ds::PriorityQueue$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 1994b 1%

abc name de/polygonal/ds/Bits
magic 2e0010
Cpool numbers size 104 5 %
Cpool strings count 65 size 431 21 %
Cpool namespaces count 3 size 5 0 %
Cpool nssets count 0 size 1 0 %
Cpool names count 56 size 166 8 %
MethodInfo count 20 size 141 7 %
InstanceInfo size 7 0 %
ClassInfo size 235 11 %
ScriptInfo size 7 0 %
MethodBodies size 867 44 %
  script0

  class de.polygonal.ds::Bits extends Object
  {

    function de.polygonal.ds::Bits():void	/* disp_id -1*/
    static var BIT_01:int	/* slot_id 1 */
    static var BIT_02:int	/* slot_id 2 */
    static var BIT_03:int	/* slot_id 3 */
    static var BIT_04:int	/* slot_id 4 */
    static var BIT_05:int	/* slot_id 5 */
    static var BIT_06:int	/* slot_id 6 */
    static var BIT_07:int	/* slot_id 7 */
    static var BIT_08:int	/* slot_id 8 */
    static var BIT_09:int	/* slot_id 9 */
    static var BIT_10:int	/* slot_id 10 */
    static var BIT_11:int	/* slot_id 11 */
    static var BIT_12:int	/* slot_id 12 */
    static var BIT_13:int	/* slot_id 13 */
    static var BIT_14:int	/* slot_id 14 */
    static var BIT_15:int	/* slot_id 15 */
    static var BIT_16:int	/* slot_id 16 */
    static var BIT_17:int	/* slot_id 17 */
    static var BIT_18:int	/* slot_id 18 */
    static var BIT_19:int	/* slot_id 19 */
    static var BIT_20:int	/* slot_id 20 */
    static var BIT_21:int	/* slot_id 21 */
    static var BIT_22:int	/* slot_id 22 */
    static var BIT_23:int	/* slot_id 23 */
    static var BIT_24:int	/* slot_id 24 */
    static var BIT_25:int	/* slot_id 25 */
    static var BIT_26:int	/* slot_id 26 */
    static var BIT_27:int	/* slot_id 27 */
    static var BIT_28:int	/* slot_id 28 */
    static var BIT_29:int	/* slot_id 29 */
    static var BIT_30:int	/* slot_id 30 */
    static var BIT_31:int	/* slot_id 31 */
    static var BIT_32:int	/* slot_id 32 */
    static var ALL:int	/* slot_id 33 */

    static function hasBits(int,int):Boolean	/* disp_id 1*/

    static function hasAllBits(int,int):Boolean	/* disp_id 2*/

    static function setBits(int,int):int	/* disp_id 3*/

    static function clrBits(int,int):int	/* disp_id 4*/

    static function invBits(int,int):int	/* disp_id 5*/

    static function mask(int):int	/* disp_id 6*/

    static function ones(int):int	/* disp_id 7*/

    static function ntz(int):int	/* disp_id 8*/

    static function nlz(int):int	/* disp_id 9*/

    static function msb(int):int	/* disp_id 10*/

    static function reverse(int):int	/* disp_id 11*/

    static function hasBitAt(int,int):Boolean	/* disp_id 12*/

    static function setBitAt(int,int):int	/* disp_id 13*/

    static function clrBitAt(int,int):int	/* disp_id 14*/

    static function invBitAt(int,int):int	/* disp_id 15*/

    static function setBitsIf(int,Boolean,int):int	/* disp_id 16*/

    static function setBitsRange(int,int,int):int	/* disp_id 17*/

    static function de.polygonal.ds::Bits$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 214b 0%

abc name de/polygonal/ds/pooling/Factory
magic 2e0010
Cpool numbers size 3 1 %
Cpool strings count 8 size 85 47 %
Cpool namespaces count 4 size 7 3 %
Cpool nssets count 0 size 1 0 %
Cpool names count 5 size 13 7 %
MethodInfo count 4 size 17 9 %
InstanceInfo size 11 6 %
ClassInfo size 2 1 %
ScriptInfo size 7 3 %
MethodBodies size 27 15 %
  script0

  interface de.polygonal.ds.pooling::Factory extends *
  {

    function de.polygonal.ds.pooling::Factory():void	/* disp_id -1*/

    function de.polygonal.ds.pooling:Factory::create():Object	/* disp_id 0*/

    static function de.polygonal.ds.pooling::Factory$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 635b 0%

abc name de/polygonal/ds/SLLNode
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 30 size 227 37 %
Cpool namespaces count 5 size 9 1 %
Cpool nssets count 0 size 1 0 %
Cpool names count 22 size 64 10 %
MethodInfo count 9 size 48 7 %
InstanceInfo size 46 7 %
ClassInfo size 2 0 %
ScriptInfo size 7 1 %
MethodBodies size 195 32 %
  script0

  class de.polygonal.ds::SLLNode extends Object
  {

    function de.polygonal.ds::SLLNode(Object,de.polygonal.ds::SLL):void	/* disp_id -1*/
    var val:Object	/* slot_id 0 */

    function toString():String	/* disp_id 0*/

    function remove():de.polygonal.ds::SLLNode	/* disp_id 0*/

    function nextVal():Object	/* disp_id 0*/
    var next:de.polygonal.ds::SLLNode	/* slot_id 0 */

    function hasNext():Boolean	/* disp_id 0*/

    function getList():de.polygonal.ds::SLL	/* disp_id 0*/
    var _list:de.polygonal.ds::SLL	/* slot_id 0 */

    function _insertAfter(de.polygonal.ds::SLLNode):void	/* disp_id 0*/

    static function de.polygonal.ds::SLLNode$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 887b 0%

abc name de/polygonal/ds/ArrayedDeque
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 27 size 178 20 %
Cpool namespaces count 5 size 9 1 %
Cpool nssets count 2 size 3 0 %
Cpool names count 22 size 63 7 %
MethodInfo count 12 size 64 7 %
InstanceInfo size 63 7 %
ClassInfo size 2 0 %
ScriptInfo size 7 0 %
MethodBodies size 457 53 %
  script0

  class de.polygonal.ds::ArrayedDeque extends Object
  {

    function de.polygonal.ds::ArrayedDeque(int):void	/* disp_id -1*/

    function pushFront(Object):void	/* disp_id 0*/

    function pushBack(Object):void	/* disp_id 0*/

    function popFront():Object	/* disp_id 0*/

    function popBack():Object	/* disp_id 0*/

    function front():Object	/* disp_id 0*/

    function clear():void	/* disp_id 0*/

    function back():Object	/* disp_id 0*/
    var _size:int	/* slot_id 0 */
    var _front:int	/* slot_id 0 */
    var _count:int	/* slot_id 0 */
    var _a:Array	/* slot_id 0 */

    function __set(int,Object):void	/* disp_id 0*/

    function __get(int):Object	/* disp_id 0*/

    static function de.polygonal.ds::ArrayedDeque$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 233b 0%

abc name de/polygonal/ds/Stack
magic 2e0010
Cpool numbers size 3 1 %
Cpool strings count 12 size 84 40 %
Cpool namespaces count 4 size 7 3 %
Cpool nssets count 2 size 3 1 %
Cpool names count 8 size 22 10 %
MethodInfo count 6 size 27 13 %
InstanceInfo size 20 9 %
ClassInfo size 2 0 %
ScriptInfo size 7 3 %
MethodBodies size 27 13 %
  script0

  interface de.polygonal.ds::Stack extends *
    implements de.polygonal.ds::Collection
  {

    function de.polygonal.ds::Stack():void	/* disp_id -1*/

    function de.polygonal.ds:Stack::top():Object	/* disp_id 0*/

    function de.polygonal.ds:Stack::push(Object):void	/* disp_id 0*/

    function de.polygonal.ds:Stack::pop():Object	/* disp_id 0*/

    static function de.polygonal.ds::Stack$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 2314b 1%

abc name de/polygonal/ds/ArrayedStack
magic 2e0010
Cpool numbers size 9 0 %
Cpool strings count 67 size 520 22 %
Cpool namespaces count 7 size 13 0 %
Cpool nssets count 3 size 5 0 %
Cpool names count 54 size 159 6 %
MethodInfo count 26 size 169 7 %
InstanceInfo size 110 4 %
ClassInfo size 2 0 %
ScriptInfo size 7 0 %
MethodBodies size 1282 56 %
  script0

  class de.polygonal.ds::ArrayedStack extends Object
    implements de.polygonal.ds::Stack
  {

    function de.polygonal.ds::ArrayedStack(int):void	/* disp_id -1*/

    function walk(Function):void	/* disp_id 0*/

    function top():Object	/* disp_id 0*/

    function toVector(de.polygonal.ds::Vector):de.polygonal.ds::Vector	/* disp_id 0*/

    function toString():String	/* disp_id 0*/

    function toArray(Array):Array	/* disp_id 0*/

    function swp(int,int):void	/* disp_id 0*/

    function size():int	/* disp_id 0*/

    function shuffle(de.polygonal.ds::Vector):void	/* disp_id 0*/

    function set(int,Object):void	/* disp_id 0*/

    function push(Object):void	/* disp_id 0*/

    function pop():Object	/* disp_id 0*/

    function maxSize():int	/* disp_id 0*/

    function iterator():Object	/* disp_id 0*/

    function isEmpty():Boolean	/* disp_id 0*/

    function get(int):Object	/* disp_id 0*/

    function cpy(int,int):void	/* disp_id 0*/

    function contains(Object):Boolean	/* disp_id 0*/

    function compact():void	/* disp_id 0*/

    function clone(Boolean,Object):de.polygonal.ds::Collection	/* disp_id 0*/

    function clear():void	/* disp_id 0*/

    function assign(*,Array,int):void	/* disp_id 0*/
    var _top:int	/* slot_id 0 */
    var _a:Array	/* slot_id 0 */

    function __set(int,Object):void	/* disp_id 0*/

    function __get(int):Object	/* disp_id 0*/

    static function de.polygonal.ds::ArrayedStack$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 549b 0%

abc name de/polygonal/ds/ArrayedStackIterator
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 24 size 159 31 %
Cpool namespaces count 5 size 9 1 %
Cpool nssets count 3 size 5 0 %
Cpool names count 21 size 60 11 %
MethodInfo count 8 size 42 8 %
InstanceInfo size 43 8 %
ClassInfo size 2 0 %
ScriptInfo size 7 1 %
MethodBodies size 173 34 %
  script0

  class de.polygonal.ds::ArrayedStackIterator extends Object
    implements de.polygonal.ds::Itr
  {

    function de.polygonal.ds::ArrayedStackIterator(de.polygonal.ds::ArrayedStack):void	/* disp_id -1*/

    function reset():void	/* disp_id 0*/

    function next():Object	/* disp_id 0*/

    function hasNext():Boolean	/* disp_id 0*/
    var _i:int	/* slot_id 0 */
    var _f:de.polygonal.ds::ArrayedStack	/* slot_id 0 */
    var _a:Array	/* slot_id 0 */

    function __top(Object):int	/* disp_id 0*/

    function __a(Object):Array	/* disp_id 0*/

    static function de.polygonal.ds::ArrayedStackIterator$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 5489b 3%

abc name de/polygonal/ds/TreeNode
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 89 size 803 14 %
Cpool namespaces count 6 size 11 0 %
Cpool nssets count 3 size 5 0 %
Cpool names count 71 size 210 3 %
MethodInfo count 44 size 267 4 %
InstanceInfo size 190 3 %
ClassInfo size 2 0 %
ScriptInfo size 7 0 %
MethodBodies size 3957 72 %
  script0

  class de.polygonal.ds::TreeNode extends Object
    implements de.polygonal.ds::Collection
  {

    function de.polygonal.ds::TreeNode(Object,de.polygonal.ds::TreeNode):void	/* disp_id -1*/
    var val:Object	/* slot_id 0 */

    function toVector(de.polygonal.ds::Vector):de.polygonal.ds::Vector	/* disp_id 0*/

    function toString():String	/* disp_id 0*/

    function toArray(Array):Array	/* disp_id 0*/

    function size():int	/* disp_id 0*/
    var prev:de.polygonal.ds::TreeNode	/* slot_id 0 */

    function preorder(Function,Boolean,*):void	/* disp_id 0*/

    function postorder(Function,Boolean,*):void	/* disp_id 0*/
    var parent:de.polygonal.ds::TreeNode	/* slot_id 0 */

    function numSiblings():int	/* disp_id 0*/

    function numChildren():int	/* disp_id 0*/
    var next:de.polygonal.ds::TreeNode	/* slot_id 0 */

    function iterator():Object	/* disp_id 0*/

    function isRoot():Boolean	/* disp_id 0*/

    function isLeaf():Boolean	/* disp_id 0*/

    function isEmpty():Boolean	/* disp_id 0*/

    function isChild():Boolean	/* disp_id 0*/

    function height():int	/* disp_id 0*/

    function hasSiblings():Boolean	/* disp_id 0*/

    function hasPrevSibling():Boolean	/* disp_id 0*/

    function hasParent():Boolean	/* disp_id 0*/

    function hasNextSibling():Boolean	/* disp_id 0*/

    function hasChildren():Boolean	/* disp_id 0*/

    function getRoot():de.polygonal.ds::TreeNode	/* disp_id 0*/

    function getLastSibling():de.polygonal.ds::TreeNode	/* disp_id 0*/

    function getLastChild():de.polygonal.ds::TreeNode	/* disp_id 0*/

    function getFirstSibling():de.polygonal.ds::TreeNode	/* disp_id 0*/

    function getChildIndex():int	/* disp_id 0*/

    function getBuilder():de.polygonal.ds::TreeBuilder	/* disp_id 0*/

    function findTail(de.polygonal.ds::TreeNode):de.polygonal.ds::TreeNode	/* disp_id 0*/

    function find(Object):de.polygonal.ds::TreeNode	/* disp_id 0*/

    function depth():int	/* disp_id 0*/

    function contains(Object):Boolean	/* disp_id 0*/

    function clone(Boolean,Object):de.polygonal.ds::Collection	/* disp_id 0*/

    function clear():void	/* disp_id 0*/
    var children:de.polygonal.ds::TreeNode	/* slot_id 0 */

    function _valid(de.polygonal.ds::TreeNode):Boolean	/* disp_id 0*/
    var _timestamp:int	/* slot_id 0 */

    function _preOrderInternalVisitable(de.polygonal.ds::TreeNode,*):Boolean	/* disp_id 0*/

    function _preOrderInternal(de.polygonal.ds::TreeNode,Function,*):Boolean	/* disp_id 0*/

    function _postOrderInternalVisitable(de.polygonal.ds::TreeNode,*):Boolean	/* disp_id 0*/

    function _postOrderInternal(de.polygonal.ds::TreeNode,Function,*):Boolean	/* disp_id 0*/

    function _findHead(de.polygonal.ds::TreeNode):de.polygonal.ds::TreeNode	/* disp_id 0*/

    function _describe():String	/* disp_id 0*/

    static function de.polygonal.ds::TreeNode$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 589b 0%

abc name de/polygonal/ds/TreeIterator
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 25 size 196 35 %
Cpool namespaces count 6 size 11 1 %
Cpool nssets count 3 size 5 0 %
Cpool names count 21 size 60 10 %
MethodInfo count 6 size 30 5 %
InstanceInfo size 35 6 %
ClassInfo size 2 0 %
ScriptInfo size 7 1 %
MethodBodies size 202 36 %
  script0

  class de.polygonal.ds::TreeIterator extends Object
    implements de.polygonal.ds::Itr
  {

    function de.polygonal.ds::TreeIterator(de.polygonal.ds::TreeNode):void	/* disp_id -1*/

    function reset():void	/* disp_id 0*/

    function next():Object	/* disp_id 0*/

    function hasNext():Boolean	/* disp_id 0*/
    var _top:int	/* slot_id 0 */
    var _stack:Array	/* slot_id 0 */
    var _node:de.polygonal.ds::TreeNode	/* slot_id 0 */

    static function de.polygonal.ds::TreeIterator$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 5175b 3%

abc name de/polygonal/ds/BinaryTreeNode
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 71 size 714 13 %
Cpool namespaces count 7 size 13 0 %
Cpool nssets count 3 size 5 0 %
Cpool names count 60 size 177 3 %
MethodInfo count 33 size 240 4 %
InstanceInfo size 145 2 %
ClassInfo size 2 0 %
ScriptInfo size 7 0 %
MethodBodies size 3829 74 %
  script0

  class de.polygonal.ds::BinaryTreeNode extends Object
    implements de.polygonal.ds::Collection
  {

    function de.polygonal.ds::BinaryTreeNode(Object):void	/* disp_id -1*/
    var val:Object	/* slot_id 0 */

    function toVector(de.polygonal.ds::Vector):de.polygonal.ds::Vector	/* disp_id 0*/

    function toString():String	/* disp_id 0*/

    function toArray(Array):Array	/* disp_id 0*/

    function size():int	/* disp_id 0*/

    function setR(Object):void	/* disp_id 0*/

    function setL(Object):void	/* disp_id 0*/
    var r:de.polygonal.ds::BinaryTreeNode	/* slot_id 0 */

    function preorder(Object,Boolean,*):void	/* disp_id 0*/

    function postorder(Object,Boolean,*):void	/* disp_id 0*/
    var p:de.polygonal.ds::BinaryTreeNode	/* slot_id 0 */
    var l:de.polygonal.ds::BinaryTreeNode	/* slot_id 0 */

    function iterator():Object	/* disp_id 0*/

    function isRoot():Boolean	/* disp_id 0*/

    function isR():Boolean	/* disp_id 0*/

    function isL():Boolean	/* disp_id 0*/

    function isEmpty():Boolean	/* disp_id 0*/

    function inorder(Object,Boolean,*):void	/* disp_id 0*/

    function height():int	/* disp_id 0*/

    function hasR():Boolean	/* disp_id 0*/

    function hasL():Boolean	/* disp_id 0*/

    function depth():int	/* disp_id 0*/

    function contains(Object):Boolean	/* disp_id 0*/

    function clone(Boolean,Object):de.polygonal.ds::Collection	/* disp_id 0*/

    function clear():void	/* disp_id 0*/
    var _timestamp:int	/* slot_id 0 */

    function _preorderRecursiveVisitable(de.polygonal.ds::BinaryTreeNode,*):Boolean	/* disp_id 0*/

    function _preorderRecursive(de.polygonal.ds::BinaryTreeNode,Function,*):Boolean	/* disp_id 0*/

    function _postorderRecursiveVisitable(de.polygonal.ds::BinaryTreeNode,*):Boolean	/* disp_id 0*/

    function _postorderRecursive(de.polygonal.ds::BinaryTreeNode,Function,*):Boolean	/* disp_id 0*/

    function _inorderRecursiveVisitable(de.polygonal.ds::BinaryTreeNode,*):Boolean	/* disp_id 0*/

    function _inorderRecursive(de.polygonal.ds::BinaryTreeNode,Function,*):Boolean	/* disp_id 0*/

    function _heightRecursive(de.polygonal.ds::BinaryTreeNode):int	/* disp_id 0*/

    static function de.polygonal.ds::BinaryTreeNode$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 589b 0%

abc name de/polygonal/ds/BinaryTreeNodeIterator
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 24 size 168 30 %
Cpool namespaces count 5 size 9 1 %
Cpool nssets count 3 size 5 0 %
Cpool names count 21 size 60 10 %
MethodInfo count 6 size 30 5 %
InstanceInfo size 35 6 %
ClassInfo size 2 0 %
ScriptInfo size 7 1 %
MethodBodies size 222 40 %
  script0

  class de.polygonal.ds::BinaryTreeNodeIterator extends Object
    implements de.polygonal.ds::Itr
  {

    function de.polygonal.ds::BinaryTreeNodeIterator(de.polygonal.ds::BinaryTreeNode):void	/* disp_id -1*/

    function reset():void	/* disp_id 0*/

    function next():Object	/* disp_id 0*/

    function hasNext():Boolean	/* disp_id 0*/
    var _top:int	/* slot_id 0 */
    var _stack:Array	/* slot_id 0 */
    var _node:de.polygonal.ds::BinaryTreeNode	/* slot_id 0 */

    static function de.polygonal.ds::BinaryTreeNodeIterator$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 1876b 1%

abc name de/polygonal/ds/Set
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 55 size 451 24 %
Cpool namespaces count 8 size 15 0 %
Cpool nssets count 3 size 5 0 %
Cpool names count 45 size 132 7 %
MethodInfo count 19 size 122 6 %
InstanceInfo size 87 4 %
ClassInfo size 2 0 %
ScriptInfo size 7 0 %
MethodBodies size 1023 55 %
  script0

  class de.polygonal.ds::Set extends Object
    implements de.polygonal.ds::Collection
  {

    function de.polygonal.ds::Set(Boolean):void	/* disp_id -1*/

    function toVector(de.polygonal.ds::Vector):de.polygonal.ds::Vector	/* disp_id 0*/

    function toString():String	/* disp_id 0*/

    function toArray(Array):Array	/* disp_id 0*/

    function size():int	/* disp_id 0*/

    function setIfAbsent(Object):Boolean	/* disp_id 0*/

    function setAll(de.polygonal.ds::Set,Boolean,Object):void	/* disp_id 0*/

    function set(Object):void	/* disp_id 0*/

    function removeIfExists(Object):Boolean	/* disp_id 0*/

    function remove(Object):void	/* disp_id 0*/

    function iterator():Object	/* disp_id 0*/

    function isEmpty():Boolean	/* disp_id 0*/

    function has(Object):Boolean	/* disp_id 0*/

    function contains(Object):Boolean	/* disp_id 0*/

    function clone(Boolean,Object):de.polygonal.ds::Collection	/* disp_id 0*/

    function clear():void	/* disp_id 0*/
    var _weak:Boolean	/* slot_id 0 */
    var _size:int	/* slot_id 0 */
    var _set:flash.utils::Dictionary	/* slot_id 0 */

    function _isNullAllowed(Object):Boolean	/* disp_id 0*/

    static function de.polygonal.ds::Set$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 627b 0%

abc name de/polygonal/ds/SetIterator
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 29 size 214 35 %
Cpool namespaces count 7 size 13 2 %
Cpool nssets count 3 size 5 0 %
Cpool names count 24 size 69 11 %
MethodInfo count 7 size 36 6 %
InstanceInfo size 44 7 %
ClassInfo size 2 0 %
ScriptInfo size 7 1 %
MethodBodies size 197 33 %
  script0

  class de.polygonal.ds::SetIterator extends Object
    implements de.polygonal.ds::Itr
  {

    function de.polygonal.ds::SetIterator(de.polygonal.ds::Set):void	/* disp_id -1*/

    function reset():void	/* disp_id 0*/

    function next():Object	/* disp_id 0*/

    function hasNext():Boolean	/* disp_id 0*/
    var _values:Array	/* slot_id 0 */
    var _s:int	/* slot_id 0 */
    var _i:int	/* slot_id 0 */
    var _f:de.polygonal.ds::Set	/* slot_id 0 */

    function __set(Object):flash.utils::Dictionary	/* disp_id 0*/

    static function de.polygonal.ds::SetIterator$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 170b 0%

abc name de/polygonal/ds/LinkedDeque
magic 2e0010
Cpool numbers size 3 2 %
Cpool strings count 6 size 42 30 %
Cpool namespaces count 3 size 5 3 %
Cpool nssets count 0 size 1 0 %
Cpool names count 4 size 10 7 %
MethodInfo count 3 size 13 9 %
InstanceInfo size 7 5 %
ClassInfo size 2 1 %
ScriptInfo size 7 5 %
MethodBodies size 43 31 %
  script0

  class de.polygonal.ds::LinkedDeque extends Object
  {

    function de.polygonal.ds::LinkedDeque():void	/* disp_id -1*/

    static function de.polygonal.ds::LinkedDeque$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 478b 0%

abc name Std
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 26 size 148 31 %
Cpool namespaces count 3 size 5 1 %
Cpool nssets count 0 size 1 0 %
Cpool names count 20 size 58 12 %
MethodInfo count 9 size 51 10 %
InstanceInfo size 7 1 %
ClassInfo size 26 5 %
ScriptInfo size 7 1 %
MethodBodies size 159 33 %
  script0

  class Std extends Object
  {

    function Std():void	/* disp_id -1*/

    static function is(*,*):Boolean	/* disp_id 1*/

    static function string(*):String	/* disp_id 2*/

    static function _int(Number):int	/* disp_id 3*/

    static function parseInt(String):Object	/* disp_id 4*/

    static function parseFloat(String):Number	/* disp_id 5*/

    static function random(int):int	/* disp_id 6*/

    static function Std$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 302b 0%

abc name de/polygonal/ds/Deque
magic 2e0010
Cpool numbers size 3 1 %
Cpool strings count 15 size 118 42 %
Cpool namespaces count 4 size 7 2 %
Cpool nssets count 2 size 3 1 %
Cpool names count 11 size 31 11 %
MethodInfo count 9 size 41 14 %
InstanceInfo size 32 11 %
ClassInfo size 2 0 %
ScriptInfo size 7 2 %
MethodBodies size 27 9 %
  script0

  interface de.polygonal.ds::Deque extends *
    implements de.polygonal.ds::Collection
  {

    function de.polygonal.ds::Deque():void	/* disp_id -1*/

    function de.polygonal.ds:Deque::pushFront(Object):void	/* disp_id 0*/

    function de.polygonal.ds:Deque::pushBack(Object):void	/* disp_id 0*/

    function de.polygonal.ds:Deque::popFront():Object	/* disp_id 0*/

    function de.polygonal.ds:Deque::popBack():Object	/* disp_id 0*/

    function de.polygonal.ds:Deque::front():Object	/* disp_id 0*/

    function de.polygonal.ds:Deque::back():Object	/* disp_id 0*/

    static function de.polygonal.ds::Deque$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 2398b 1%

abc name de/polygonal/ds/LinkedStack
magic 2e0010
Cpool numbers size 9 0 %
Cpool strings count 58 size 478 20 %
Cpool namespaces count 7 size 13 0 %
Cpool nssets count 3 size 5 0 %
Cpool names count 48 size 141 5 %
MethodInfo count 18 size 115 4 %
InstanceInfo size 78 3 %
ClassInfo size 2 0 %
ScriptInfo size 7 0 %
MethodBodies size 1513 63 %
  script0

  class de.polygonal.ds::LinkedStack extends Object
    implements de.polygonal.ds::Stack
  {

    function de.polygonal.ds::LinkedStack(int):void	/* disp_id -1*/

    function top():Object	/* disp_id 0*/

    function toVector(de.polygonal.ds::Vector):de.polygonal.ds::Vector	/* disp_id 0*/

    function toString():String	/* disp_id 0*/

    function toArray(Array):Array	/* disp_id 0*/

    function size():int	/* disp_id 0*/

    function shuffle(de.polygonal.ds::Vector):void	/* disp_id 0*/

    function push(Object):void	/* disp_id 0*/

    function pop():Object	/* disp_id 0*/

    function maxSize():int	/* disp_id 0*/

    function iterator():Object	/* disp_id 0*/

    function isEmpty():Boolean	/* disp_id 0*/

    function contains(Object):Boolean	/* disp_id 0*/

    function clone(Boolean,Object):de.polygonal.ds::Collection	/* disp_id 0*/

    function clear():void	/* disp_id 0*/

    function assign(*,Array,int):void	/* disp_id 0*/
    var _top:int	/* slot_id 0 */
    var _head:de.polygonal.ds::LinkedStackNode	/* slot_id 0 */

    static function de.polygonal.ds::LinkedStack$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 274b 0%

abc name de/polygonal/ds/LinkedStackNode
magic 2e0010
Cpool numbers size 3 1 %
Cpool strings count 12 size 82 34 %
Cpool namespaces count 3 size 5 2 %
Cpool nssets count 0 size 1 0 %
Cpool names count 10 size 28 11 %
MethodInfo count 4 size 17 7 %
InstanceInfo size 21 8 %
ClassInfo size 2 0 %
ScriptInfo size 7 2 %
MethodBodies size 67 28 %
  script0

  class de.polygonal.ds::LinkedStackNode extends Object
  {

    function de.polygonal.ds::LinkedStackNode():void	/* disp_id -1*/
    var val:Object	/* slot_id 0 */

    function toString():String	/* disp_id 0*/
    var next:de.polygonal.ds::LinkedStackNode	/* slot_id 0 */

    static function de.polygonal.ds::LinkedStackNode$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 508b 0%

abc name de/polygonal/ds/LinkedStackIterator
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 22 size 167 35 %
Cpool namespaces count 4 size 7 1 %
Cpool nssets count 2 size 3 0 %
Cpool names count 18 size 52 11 %
MethodInfo count 7 size 36 7 %
InstanceInfo size 34 7 %
ClassInfo size 2 0 %
ScriptInfo size 7 1 %
MethodBodies size 152 32 %
  script0

  class de.polygonal.ds::LinkedStackIterator extends Object
    implements de.polygonal.ds::Itr
  {

    function de.polygonal.ds::LinkedStackIterator(de.polygonal.ds::LinkedStack):void	/* disp_id -1*/

    function reset():void	/* disp_id 0*/

    function next():Object	/* disp_id 0*/

    function hasNext():Boolean	/* disp_id 0*/
    var _walker:de.polygonal.ds::LinkedStackNode	/* slot_id 0 */
    var _f:de.polygonal.ds::LinkedStack	/* slot_id 0 */

    function __head(Object):de.polygonal.ds::LinkedStackNode	/* disp_id 0*/

    static function de.polygonal.ds::LinkedStackIterator$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 1425b 0%

abc name de/polygonal/ds/mem/BitMemory
magic 2e0010
Cpool numbers size 8 0 %
Cpool strings count 45 size 379 27 %
Cpool namespaces count 8 size 15 1 %
Cpool nssets count 2 size 3 0 %
Cpool names count 34 size 99 7 %
MethodInfo count 15 size 91 6 %
InstanceInfo size 53 3 %
ClassInfo size 14 1 %
ScriptInfo size 7 0 %
MethodBodies size 717 51 %
  script0

  class de.polygonal.ds.mem::BitMemory extends de.polygonal.ds.mem::MemoryAccess
  {

    function de.polygonal.ds.mem::BitMemory(int):void	/* disp_id -1*/

    function toString():String	/* disp_id 0*/
    var size:int	/* slot_id 0 */

    function setAll():void	/* disp_id 0*/

    function set(int):void	/* disp_id 0*/

    function resize(int):void	/* disp_id 0*/

    function has(int):Boolean	/* disp_id 0*/

    function getAddr(int):int	/* disp_id 0*/

    function clrAll():void	/* disp_id 0*/

    function clr(int):void	/* disp_id 0*/
    var _size:int	/* slot_id 0 */

    function __size():int	/* disp_id 0*/

    static function toByteArray(de.polygonal.ds.mem::BitMemory):flash.utils::ByteArray	/* disp_id 1*/

    static function ofByteArray(flash.utils::ByteArray,int,int):de.polygonal.ds.mem::BitMemory	/* disp_id 2*/

    static function toBitVector(de.polygonal.ds.mem::BitMemory):de.polygonal.ds::BitVector	/* disp_id 3*/

    static function de.polygonal.ds.mem::BitMemory$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 4571b 3%

abc name de/polygonal/ds/Array2
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 87 size 617 13 %
Cpool namespaces count 7 size 13 0 %
Cpool nssets count 3 size 5 0 %
Cpool names count 67 size 198 4 %
MethodInfo count 39 size 255 5 %
InstanceInfo size 167 3 %
ClassInfo size 2 0 %
ScriptInfo size 7 0 %
MethodBodies size 3272 72 %
  script0

  class de.polygonal.ds::Array2 extends Object
    implements de.polygonal.ds::Collection
  {

    function de.polygonal.ds::Array2(int,int):void	/* disp_id -1*/

    function walk(Function):void	/* disp_id 0*/

    function transpose():void	/* disp_id 0*/

    function toVector(de.polygonal.ds::Vector):de.polygonal.ds::Vector	/* disp_id 0*/

    function toString():String	/* disp_id 0*/

    function toArray(Array):Array	/* disp_id 0*/

    function swap(int,int,int,int):void	/* disp_id 0*/

    function size():int	/* disp_id 0*/

    function shuffle(de.polygonal.ds::Vector):void	/* disp_id 0*/

    function shiftW():void	/* disp_id 0*/

    function shiftS():void	/* disp_id 0*/

    function shiftN():void	/* disp_id 0*/

    function shiftE():void	/* disp_id 0*/

    function setW(int):void	/* disp_id 0*/

    function setRow(int,Array):void	/* disp_id 0*/

    function setH(int):void	/* disp_id 0*/

    function setCol(int,Array):void	/* disp_id 0*/

    function set(int,int,Object):void	/* disp_id 0*/

    function resize(int,int):void	/* disp_id 0*/

    function prependRow(Array):void	/* disp_id 0*/

    function prependCol(Array):void	/* disp_id 0*/

    function iterator():Object	/* disp_id 0*/

    function isEmpty():Boolean	/* disp_id 0*/

    function getW():int	/* disp_id 0*/

    function getRow(int,Array):Array	/* disp_id 0*/

    function getH():int	/* disp_id 0*/

    function getCol(int,Array):Array	/* disp_id 0*/

    function getArray():Array	/* disp_id 0*/

    function get(int,int):Object	/* disp_id 0*/

    function contains(Object):Boolean	/* disp_id 0*/

    function clone(Boolean,Object):de.polygonal.ds::Collection	/* disp_id 0*/

    function clear():void	/* disp_id 0*/

    function assign(*,Array):void	/* disp_id 0*/

    function appendRow(Array):void	/* disp_id 0*/

    function appendCol(Array):void	/* disp_id 0*/
    var _w:int	/* slot_id 0 */
    var _h:int	/* slot_id 0 */
    var _a:Array	/* slot_id 0 */

    function __set(int,Object):void	/* disp_id 0*/

    function __get(int):Object	/* disp_id 0*/

    static function de.polygonal.ds::Array2$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 566b 0%

abc name de/polygonal/ds/Array2Iterator
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 26 size 152 28 %
Cpool namespaces count 5 size 9 1 %
Cpool nssets count 3 size 5 0 %
Cpool names count 23 size 66 12 %
MethodInfo count 8 size 42 7 %
InstanceInfo size 48 9 %
ClassInfo size 2 0 %
ScriptInfo size 7 1 %
MethodBodies size 192 36 %
  script0

  class de.polygonal.ds::Array2Iterator extends Object
    implements de.polygonal.ds::Itr
  {

    function de.polygonal.ds::Array2Iterator(de.polygonal.ds::Array2):void	/* disp_id -1*/

    function reset():void	/* disp_id 0*/

    function next():Object	/* disp_id 0*/

    function hasNext():Boolean	/* disp_id 0*/
    var _s:int	/* slot_id 0 */
    var _i:int	/* slot_id 0 */
    var _f:de.polygonal.ds::Array2	/* slot_id 0 */
    var _a:Array	/* slot_id 0 */

    function __size(Object):int	/* disp_id 0*/

    function __a(Object):Array	/* disp_id 0*/

    static function de.polygonal.ds::Array2Iterator$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 257b 0%

abc name de/polygonal/ds/Queue
magic 2e0010
Cpool numbers size 3 1 %
Cpool strings count 13 size 97 41 %
Cpool namespaces count 4 size 7 3 %
Cpool nssets count 2 size 3 1 %
Cpool names count 9 size 25 10 %
MethodInfo count 7 size 31 13 %
InstanceInfo size 24 10 %
ClassInfo size 2 0 %
ScriptInfo size 7 3 %
MethodBodies size 27 11 %
  script0

  interface de.polygonal.ds::Queue extends *
    implements de.polygonal.ds::Collection
  {

    function de.polygonal.ds::Queue():void	/* disp_id -1*/

    function de.polygonal.ds:Queue::peek():Object	/* disp_id 0*/

    function de.polygonal.ds:Queue::enqueue(Object):void	/* disp_id 0*/

    function de.polygonal.ds:Queue::dequeue():Object	/* disp_id 0*/

    function de.polygonal.ds:Queue::back():Object	/* disp_id 0*/

    static function de.polygonal.ds::Queue$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 2750b 1%

abc name de/polygonal/ds/ArrayedQueue
magic 2e0010
Cpool numbers size 4 0 %
Cpool strings count 71 size 559 20 %
Cpool namespaces count 7 size 13 0 %
Cpool nssets count 3 size 5 0 %
Cpool names count 57 size 168 6 %
MethodInfo count 28 size 177 6 %
InstanceInfo size 133 4 %
ClassInfo size 2 0 %
ScriptInfo size 7 0 %
MethodBodies size 1644 60 %
  script0

  class de.polygonal.ds::ArrayedQueue extends Object
    implements de.polygonal.ds::Queue
  {

    function de.polygonal.ds::ArrayedQueue(int):void	/* disp_id -1*/

    function walk(Function):void	/* disp_id 0*/

    function toVector(de.polygonal.ds::Vector):de.polygonal.ds::Vector	/* disp_id 0*/

    function toString():String	/* disp_id 0*/

    function toArray(Array):Array	/* disp_id 0*/

    function swp(int,int):void	/* disp_id 0*/

    function size():int	/* disp_id 0*/

    function shuffle(de.polygonal.ds::Vector):void	/* disp_id 0*/

    function set(int,Object):void	/* disp_id 0*/

    function peek():Object	/* disp_id 0*/

    function iterator():Object	/* disp_id 0*/

    function isFull():Boolean	/* disp_id 0*/

    function isEmpty():Boolean	/* disp_id 0*/

    function get(int):Object	/* disp_id 0*/

    function enqueue(Object):void	/* disp_id 0*/

    function dispose():void	/* disp_id 0*/

    function dequeue():Object	/* disp_id 0*/

    function cpy(int,int):void	/* disp_id 0*/

    function contains(Object):Boolean	/* disp_id 0*/

    function clone(Boolean,Object):de.polygonal.ds::Collection	/* disp_id 0*/

    function clear():void	/* disp_id 0*/

    function capacity():int	/* disp_id 0*/

    function back():Object	/* disp_id 0*/

    function assign(*,Array,int):void	/* disp_id 0*/
    var _size:int	/* slot_id 0 */
    var _mask:int	/* slot_id 0 */
    var _front:int	/* slot_id 0 */
    var _count:int	/* slot_id 0 */
    var _a:Array	/* slot_id 0 */

    function __set(int,Object):void	/* disp_id 0*/

    function __get(int):Object	/* disp_id 0*/

    static function de.polygonal.ds::ArrayedQueue$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 689b 0%

abc name de/polygonal/ds/ArrayedQueueIterator
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 28 size 191 29 %
Cpool namespaces count 5 size 9 1 %
Cpool nssets count 3 size 5 0 %
Cpool names count 25 size 72 11 %
MethodInfo count 10 size 54 8 %
InstanceInfo size 66 10 %
ClassInfo size 2 0 %
ScriptInfo size 7 1 %
MethodBodies size 234 36 %
  script0

  class de.polygonal.ds::ArrayedQueueIterator extends Object
    implements de.polygonal.ds::Itr
  {

    function de.polygonal.ds::ArrayedQueueIterator(de.polygonal.ds::ArrayedQueue):void	/* disp_id -1*/

    function reset():void	/* disp_id 0*/

    function next():Object	/* disp_id 0*/

    function hasNext():Boolean	/* disp_id 0*/
    var _mask:int	/* slot_id 0 */
    var _i:int	/* slot_id 0 */
    var _front:int	/* slot_id 0 */
    var _f:de.polygonal.ds::ArrayedQueue	/* slot_id 0 */
    var _count:int	/* slot_id 0 */
    var _a:Array	/* slot_id 0 */

    function __mask(Object):int	/* disp_id 0*/

    function __front(Object):int	/* disp_id 0*/

    function __count(Object):int	/* disp_id 0*/

    function __a(Object):Array	/* disp_id 0*/

    static function de.polygonal.ds::ArrayedQueueIterator$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 1921b 1%

abc name de/polygonal/core/fmt/NumberFormat
magic 2e0010
Cpool numbers size 14 0 %
Cpool strings count 46 size 343 18 %
Cpool namespaces count 4 size 7 0 %
Cpool nssets count 0 size 1 0 %
Cpool names count 21 size 61 3 %
MethodInfo count 11 size 76 4 %
InstanceInfo size 7 0 %
ClassInfo size 34 1 %
ScriptInfo size 7 0 %
MethodBodies size 1327 70 %
  script0

  class de.polygonal.core.fmt::NumberFormat extends Object
  {

    function de.polygonal.core.fmt::NumberFormat():void	/* disp_id -1*/

    static function toBin(int):String	/* disp_id 1*/

    static function toHex(int):String	/* disp_id 2*/

    static function toOct(int):String	/* disp_id 3*/

    static function toRadix(int,int):String	/* disp_id 4*/

    static function toFixed(Number,int):String	/* disp_id 5*/

    static function toMMSS(int):String	/* disp_id 6*/

    static function groupDigits(int,String):String	/* disp_id 7*/

    static function centToEuro(int,String,String):String	/* disp_id 8*/

    static function de.polygonal.core.fmt::NumberFormat$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 356b 0%

abc name Void
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 19 size 129 37 %
Cpool namespaces count 3 size 5 1 %
Cpool nssets count 0 size 1 0 %
Cpool names count 17 size 49 14 %
MethodInfo count 4 size 23 6 %
InstanceInfo size 32 9 %
ClassInfo size 13 3 %
ScriptInfo size 7 2 %
MethodBodies size 80 23 %
  script0

  final class Void extends Object
  {

    function Void(String,int,*):void	/* disp_id -1*/
    var tag:String	/* slot_id 0 */
    var index:int	/* slot_id 0 */
    var params:Array	/* slot_id 0 */
    const __enum__:Boolean = "true"	/* slot_id 0 */

    function toString():String	/* disp_id 0*/
    static const __isenum:Boolean = "true"	/* slot_id 2 */
    static var __constructs__:*	/* slot_id 1 */

    static function Void$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 128b 0%

abc name Dynamic
magic 2e0010
Cpool numbers size 3 2 %
Cpool strings count 5 size 22 18 %
Cpool namespaces count 2 size 3 2 %
Cpool nssets count 0 size 1 0 %
Cpool names count 4 size 10 8 %
MethodInfo count 3 size 13 11 %
InstanceInfo size 7 6 %
ClassInfo size 2 1 %
ScriptInfo size 7 6 %
MethodBodies size 43 37 %
  script0

  class Dynamic extends Object
  {

    function Dynamic():void	/* disp_id -1*/

    static function Dynamic$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 521b 0%

abc name haxe/io/Error
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 25 size 178 35 %
Cpool namespaces count 4 size 7 1 %
Cpool nssets count 0 size 1 0 %
Cpool names count 21 size 61 12 %
MethodInfo count 5 size 29 5 %
InstanceInfo size 32 6 %
ClassInfo size 32 6 %
ScriptInfo size 7 1 %
MethodBodies size 148 29 %
  script0

  final class haxe.io::Error extends Object
  {

    function haxe.io::Error(String,int,*):void	/* disp_id -1*/
    var tag:String	/* slot_id 0 */
    var index:int	/* slot_id 0 */
    var params:Array	/* slot_id 0 */
    const __enum__:Boolean = "true"	/* slot_id 0 */

    function toString():String	/* disp_id 0*/
    static const __isenum:Boolean = "true"	/* slot_id 6 */
    static var __constructs__:*	/* slot_id 5 */
    static var Overflow:haxe.io::Error	/* slot_id 4 */
    static var OutsideBounds:haxe.io::Error	/* slot_id 3 */

    static function Custom(*):haxe.io::Error	/* disp_id 2*/
    static var Blocked:haxe.io::Error	/* slot_id 1 */

    static function haxe.io::Error$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 2448b 1%

abc name de/polygonal/ds/LinkedQueue
magic 2e0010
Cpool numbers size 9 0 %
Cpool strings count 60 size 490 20 %
Cpool namespaces count 7 size 13 0 %
Cpool nssets count 3 size 5 0 %
Cpool names count 49 size 144 5 %
MethodInfo count 19 size 119 4 %
InstanceInfo size 87 3 %
ClassInfo size 2 0 %
ScriptInfo size 7 0 %
MethodBodies size 1535 63 %
  script0

  class de.polygonal.ds::LinkedQueue extends Object
    implements de.polygonal.ds::Queue
  {

    function de.polygonal.ds::LinkedQueue(int):void	/* disp_id -1*/

    function toVector(de.polygonal.ds::Vector):de.polygonal.ds::Vector	/* disp_id 0*/

    function toString():String	/* disp_id 0*/

    function toArray(Array):Array	/* disp_id 0*/

    function size():int	/* disp_id 0*/

    function shuffle(de.polygonal.ds::Vector):void	/* disp_id 0*/

    function peek():Object	/* disp_id 0*/

    function maxSize():int	/* disp_id 0*/

    function iterator():Object	/* disp_id 0*/

    function isEmpty():Boolean	/* disp_id 0*/

    function enqueue(Object):void	/* disp_id 0*/

    function dequeue():Object	/* disp_id 0*/

    function contains(Object):Boolean	/* disp_id 0*/

    function clone(Boolean,Object):de.polygonal.ds::Collection	/* disp_id 0*/

    function clear():void	/* disp_id 0*/

    function back():Object	/* disp_id 0*/

    function assign(*,Array,int):void	/* disp_id 0*/
    var _tail:de.polygonal.ds::LinkedQueueNode	/* slot_id 0 */
    var _size:int	/* slot_id 0 */
    var _head:de.polygonal.ds::LinkedQueueNode	/* slot_id 0 */

    static function de.polygonal.ds::LinkedQueue$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 312b 0%

abc name de/polygonal/ds/LinkedQueueNode
magic 2e0010
Cpool numbers size 3 1 %
Cpool strings count 14 size 101 36 %
Cpool namespaces count 4 size 7 2 %
Cpool nssets count 0 size 1 0 %
Cpool names count 10 size 28 10 %
MethodInfo count 4 size 22 7 %
InstanceInfo size 21 7 %
ClassInfo size 2 0 %
ScriptInfo size 7 2 %
MethodBodies size 79 28 %
  script0

  class de.polygonal.ds::LinkedQueueNode extends Object
  {

    function de.polygonal.ds::LinkedQueueNode(Object):void	/* disp_id -1*/
    var val:Object	/* slot_id 0 */

    function toString():String	/* disp_id 0*/
    var next:de.polygonal.ds::LinkedQueueNode	/* slot_id 0 */

    static function de.polygonal.ds::LinkedQueueNode$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 506b 0%

abc name de/polygonal/ds/LinkedQueueIterator
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 22 size 167 35 %
Cpool namespaces count 4 size 7 1 %
Cpool nssets count 2 size 3 0 %
Cpool names count 18 size 52 11 %
MethodInfo count 7 size 36 7 %
InstanceInfo size 34 7 %
ClassInfo size 2 0 %
ScriptInfo size 7 1 %
MethodBodies size 150 32 %
  script0

  class de.polygonal.ds::LinkedQueueIterator extends Object
    implements de.polygonal.ds::Itr
  {

    function de.polygonal.ds::LinkedQueueIterator(de.polygonal.ds::LinkedQueue):void	/* disp_id -1*/

    function reset():void	/* disp_id 0*/

    function next():Object	/* disp_id 0*/

    function hasNext():Boolean	/* disp_id 0*/
    var _walker:de.polygonal.ds::LinkedQueueNode	/* slot_id 0 */
    var _f:de.polygonal.ds::LinkedQueue	/* slot_id 0 */

    function __head(Object):de.polygonal.ds::LinkedQueueNode	/* disp_id 0*/

    static function de.polygonal.ds::LinkedQueueIterator$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 215b 0%

abc name de/polygonal/ds/Comparable
magic 2e0010
Cpool numbers size 3 1 %
Cpool strings count 10 size 86 46 %
Cpool namespaces count 4 size 7 3 %
Cpool nssets count 0 size 1 0 %
Cpool names count 6 size 16 8 %
MethodInfo count 4 size 19 10 %
InstanceInfo size 11 5 %
ClassInfo size 2 1 %
ScriptInfo size 7 3 %
MethodBodies size 27 14 %
  script0

  interface de.polygonal.ds::Comparable extends *
  {

    function de.polygonal.ds::Comparable():void	/* disp_id -1*/

    function de.polygonal.ds:Comparable::compare(Object):int	/* disp_id 0*/

    static function de.polygonal.ds::Comparable$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 1643b 1%

abc name de/polygonal/ds/mem/DoubleMemory
magic 2e0010
Cpool numbers size 8 0 %
Cpool strings count 50 size 428 26 %
Cpool namespaces count 9 size 17 1 %
Cpool nssets count 2 size 3 0 %
Cpool names count 37 size 109 6 %
MethodInfo count 16 size 150 9 %
InstanceInfo size 45 2 %
ClassInfo size 26 1 %
ScriptInfo size 7 0 %
MethodBodies size 808 50 %
  script0

  class de.polygonal.ds.mem::DoubleMemory extends de.polygonal.ds.mem::MemoryAccess
  {

    function de.polygonal.ds.mem::DoubleMemory(int):void	/* disp_id -1*/

    function toString():String	/* disp_id 0*/

    function swp(int,int):void	/* disp_id 0*/
    var size:int	/* slot_id 0 */

    function set(int,Number):void	/* disp_id 0*/

    function resize(int):void	/* disp_id 0*/

    function getAddr(int):int	/* disp_id 0*/

    function get(int):Number	/* disp_id 0*/
    var _size:int	/* slot_id 0 */

    function __size():int	/* disp_id 0*/

    static function toByteArray(de.polygonal.ds.mem::DoubleMemory,int,int):flash.utils::ByteArray	/* disp_id 1*/

    static function ofByteArray(flash.utils::ByteArray,int,int):de.polygonal.ds.mem::DoubleMemory	/* disp_id 2*/

    static function toArray(de.polygonal.ds.mem::DoubleMemory,int,int):Array	/* disp_id 3*/

    static function ofArray(Array,int,int):de.polygonal.ds.mem::DoubleMemory	/* disp_id 4*/

    static function toVector(de.polygonal.ds.mem::DoubleMemory,int,int):__AS3__.vec::Vector.<Number>	/* disp_id 5*/

    static function ofVector(__AS3__.vec::Vector.<Number>,int,int):de.polygonal.ds.mem::DoubleMemory	/* disp_id 6*/

    static function de.polygonal.ds.mem::DoubleMemory$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 1601b 1%

abc name de/polygonal/ds/ArrayConvert
magic 2e0010
Cpool numbers size 8 0 %
Cpool strings count 45 size 346 22 %
Cpool namespaces count 4 size 7 0 %
Cpool nssets count 2 size 3 0 %
Cpool names count 39 size 114 7 %
MethodInfo count 12 size 107 6 %
InstanceInfo size 7 0 %
ClassInfo size 38 2 %
ScriptInfo size 7 0 %
MethodBodies size 926 59 %
  script0

  class de.polygonal.ds::ArrayConvert extends Object
  {

    function de.polygonal.ds::ArrayConvert():void	/* disp_id -1*/

    static function toArray2(Array,int,int):de.polygonal.ds::Array2	/* disp_id 1*/

    static function toArray3(Array,int,int,int):de.polygonal.ds::Array3	/* disp_id 2*/

    static function toArrayedQueue(Array):de.polygonal.ds::ArrayedQueue	/* disp_id 3*/

    static function toArrayedStack(Array,int):de.polygonal.ds::ArrayedStack	/* disp_id 4*/

    static function toDLL(Array,int):de.polygonal.ds::DLL	/* disp_id 5*/

    static function toLinkedQueue(Array,int):de.polygonal.ds::LinkedQueue	/* disp_id 6*/

    static function toLinkedStack(Array,int):de.polygonal.ds::LinkedStack	/* disp_id 7*/

    static function toSLL(Array,int):de.polygonal.ds::SLL	/* disp_id 8*/

    static function toVector(Array,int):de.polygonal.ds::Vector	/* disp_id 9*/

    static function de.polygonal.ds::ArrayConvert$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 7542b 5%

abc name de/polygonal/ds/DLL
magic 2e0010
Cpool numbers size 9 0 %
Cpool strings count 103 size 886 11 %
Cpool namespaces count 7 size 13 0 %
Cpool nssets count 3 size 5 0 %
Cpool names count 87 size 258 3 %
MethodInfo count 47 size 302 4 %
InstanceInfo size 204 2 %
ClassInfo size 2 0 %
ScriptInfo size 7 0 %
MethodBodies size 5827 77 %
  script0

  class de.polygonal.ds::DLL extends Object
    implements de.polygonal.ds::Collection
  {

    function de.polygonal.ds::DLL(int):void	/* disp_id -1*/

    function toVector(de.polygonal.ds::Vector):de.polygonal.ds::Vector	/* disp_id 0*/

    function toString():String	/* disp_id 0*/

    function toArray(Array):Array	/* disp_id 0*/

    function tail():de.polygonal.ds::DLLNode	/* disp_id 0*/

    function sort(Function,Boolean):void	/* disp_id 0*/

    function size():int	/* disp_id 0*/

    function shuffle(de.polygonal.ds::Vector):void	/* disp_id 0*/

    function shiftUp():void	/* disp_id 0*/

    function reverse():void	/* disp_id 0*/

    function removeTail():Object	/* disp_id 0*/

    function removeHead():Object	/* disp_id 0*/

    function remove(de.polygonal.ds::DLLNode):de.polygonal.ds::DLLNode	/* disp_id 0*/

    function prepend(Object):de.polygonal.ds::DLLNode	/* disp_id 0*/

    function popDown():void	/* disp_id 0*/

    function open():void	/* disp_id 0*/

    function nodeOf(Object,de.polygonal.ds::DLLNode):de.polygonal.ds::DLLNode	/* disp_id 0*/

    function merge(de.polygonal.ds::DLL):void	/* disp_id 0*/

    function maxSize():int	/* disp_id 0*/

    function lastNodeOf(Object,de.polygonal.ds::DLLNode):de.polygonal.ds::DLLNode	/* disp_id 0*/

    function join(String):String	/* disp_id 0*/

    function iterator():Object	/* disp_id 0*/

    function isEmpty():Boolean	/* disp_id 0*/

    function isCircular():Boolean	/* disp_id 0*/

    function insertBefore(de.polygonal.ds::DLLNode,Object):de.polygonal.ds::DLLNode	/* disp_id 0*/

    function insertAfter(de.polygonal.ds::DLLNode,Object):de.polygonal.ds::DLLNode	/* disp_id 0*/

    function head():de.polygonal.ds::DLLNode	/* disp_id 0*/

    function getNodeAt(int):de.polygonal.ds::DLLNode	/* disp_id 0*/

    function contains(Object):Boolean	/* disp_id 0*/

    function concat(de.polygonal.ds::DLL):de.polygonal.ds::DLL	/* disp_id 0*/

    function close():void	/* disp_id 0*/

    function clone(Boolean,Object):de.polygonal.ds::Collection	/* disp_id 0*/

    function clear():void	/* disp_id 0*/

    function assign(*,Array,int):void	/* disp_id 0*/

    function append(Object):de.polygonal.ds::DLLNode	/* disp_id 0*/

    function _valid(de.polygonal.ds::DLLNode):Boolean	/* disp_id 0*/
    var _tail:de.polygonal.ds::DLLNode	/* slot_id 0 */
    var _size:int	/* slot_id 0 */

    function _mergeSortComparable(de.polygonal.ds::DLLNode):de.polygonal.ds::DLLNode	/* disp_id 0*/

    function _mergeSort(de.polygonal.ds::DLLNode,Function):de.polygonal.ds::DLLNode	/* disp_id 0*/

    function _insertionSortComparable(de.polygonal.ds::DLLNode):de.polygonal.ds::DLLNode	/* disp_id 0*/

    function _insertionSort(de.polygonal.ds::DLLNode,Function):de.polygonal.ds::DLLNode	/* disp_id 0*/
    var _head:de.polygonal.ds::DLLNode	/* slot_id 0 */

    function _getNode(Object):de.polygonal.ds::DLLNode	/* disp_id 0*/
    var _circular:Boolean	/* slot_id 0 */

    function __unlink(Object):void	/* disp_id 0*/

    function __list(Object,de.polygonal.ds::DLL):void	/* disp_id 0*/

    function __insertBefore(Object,de.polygonal.ds::DLLNode):void	/* disp_id 0*/

    function __insertAfter(Object,de.polygonal.ds::DLLNode):void	/* disp_id 0*/

    static function de.polygonal.ds::DLL$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 474b 0%

abc name de/polygonal/ds/DLLIterator
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 22 size 143 32 %
Cpool namespaces count 4 size 7 1 %
Cpool nssets count 2 size 3 0 %
Cpool names count 18 size 52 11 %
MethodInfo count 7 size 36 8 %
InstanceInfo size 34 7 %
ClassInfo size 2 0 %
ScriptInfo size 7 1 %
MethodBodies size 150 33 %
  script0

  class de.polygonal.ds::DLLIterator extends Object
    implements de.polygonal.ds::Itr
  {

    function de.polygonal.ds::DLLIterator(de.polygonal.ds::DLL):void	/* disp_id -1*/

    function reset():void	/* disp_id 0*/

    function next():Object	/* disp_id 0*/

    function hasNext():Boolean	/* disp_id 0*/
    var _walker:de.polygonal.ds::DLLNode	/* slot_id 0 */
    var _f:de.polygonal.ds::DLL	/* slot_id 0 */

    function __head(Object):de.polygonal.ds::DLLNode	/* disp_id 0*/

    static function de.polygonal.ds::DLLIterator$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 586b 0%

abc name de/polygonal/ds/CircularDLLIterator
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 27 size 174 31 %
Cpool namespaces count 4 size 7 1 %
Cpool nssets count 2 size 3 0 %
Cpool names count 23 size 67 12 %
MethodInfo count 8 size 42 7 %
InstanceInfo size 48 8 %
ClassInfo size 2 0 %
ScriptInfo size 7 1 %
MethodBodies size 188 34 %
  script0

  class de.polygonal.ds::CircularDLLIterator extends Object
    implements de.polygonal.ds::Itr
  {

    function de.polygonal.ds::CircularDLLIterator(de.polygonal.ds::DLL):void	/* disp_id -1*/

    function reset():void	/* disp_id 0*/

    function next():Object	/* disp_id 0*/

    function hasNext():Boolean	/* disp_id 0*/
    var _walker:de.polygonal.ds::DLLNode	/* slot_id 0 */
    var _s:int	/* slot_id 0 */
    var _i:int	/* slot_id 0 */
    var _f:de.polygonal.ds::DLL	/* slot_id 0 */

    function __size(Object):int	/* disp_id 0*/

    function __head(Object):de.polygonal.ds::DLLNode	/* disp_id 0*/

    static function de.polygonal.ds::CircularDLLIterator$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 2392b 1%

abc name de/polygonal/ds/HashMap
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 61 size 522 22 %
Cpool namespaces count 8 size 15 0 %
Cpool nssets count 3 size 5 0 %
Cpool names count 45 size 132 5 %
MethodInfo count 22 size 158 6 %
InstanceInfo size 99 4 %
ClassInfo size 2 0 %
ScriptInfo size 7 0 %
MethodBodies size 1416 59 %
  script0

  class de.polygonal.ds::HashMap extends Object
    implements de.polygonal.ds::Collection
  {

    function de.polygonal.ds::HashMap(Boolean):void	/* disp_id -1*/

    function valuesToVector(de.polygonal.ds::Vector):de.polygonal.ds::Vector	/* disp_id 0*/

    function valuesToArray(Array):Array	/* disp_id 0*/

    function toVector(de.polygonal.ds::Vector):de.polygonal.ds::Vector	/* disp_id 0*/

    function toString():String	/* disp_id 0*/

    function toArray(Array):Array	/* disp_id 0*/

    function size():int	/* disp_id 0*/

    function setIfAbsent(Object,Object):Boolean	/* disp_id 0*/

    function setAll(de.polygonal.ds::HashMap,Boolean,Boolean,Object,Object):void	/* disp_id 0*/

    function set(Object,Object):void	/* disp_id 0*/

    function removeIfExists(Object):Boolean	/* disp_id 0*/

    function remove(Object):void	/* disp_id 0*/

    function remap(Object,Object):void	/* disp_id 0*/

    function iterator():Object	/* disp_id 0*/

    function isEmpty():Boolean	/* disp_id 0*/

    function has(Object):Boolean	/* disp_id 0*/

    function get(Object):Object	/* disp_id 0*/

    function contains(Object):Boolean	/* disp_id 0*/

    function clone(Boolean,Object):de.polygonal.ds::Collection	/* disp_id 0*/

    function clear():void	/* disp_id 0*/
    var _weak:Boolean	/* slot_id 0 */
    var _size:int	/* slot_id 0 */
    var _map:flash.utils::Dictionary	/* slot_id 0 */

    static function de.polygonal.ds::HashMap$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 637b 0%

abc name de/polygonal/ds/HashMapIterator
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 29 size 220 36 %
Cpool namespaces count 7 size 13 2 %
Cpool nssets count 3 size 5 0 %
Cpool names count 24 size 69 11 %
MethodInfo count 7 size 36 5 %
InstanceInfo size 44 7 %
ClassInfo size 2 0 %
ScriptInfo size 7 1 %
MethodBodies size 197 32 %
  script0

  class de.polygonal.ds::HashMapIterator extends Object
    implements de.polygonal.ds::Itr
  {

    function de.polygonal.ds::HashMapIterator(de.polygonal.ds::HashMap):void	/* disp_id -1*/

    function reset():void	/* disp_id 0*/

    function next():Object	/* disp_id 0*/

    function hasNext():Boolean	/* disp_id 0*/
    var _s:int	/* slot_id 0 */
    var _keys:Array	/* slot_id 0 */
    var _i:int	/* slot_id 0 */
    var _f:de.polygonal.ds::HashMap	/* slot_id 0 */

    function __map(Object):flash.utils::Dictionary	/* disp_id 0*/

    static function de.polygonal.ds::HashMapIterator$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 195b 0%

abc name de/polygonal/ds/Cloneable
magic 2e0010
Cpool numbers size 3 1 %
Cpool strings count 8 size 72 43 %
Cpool namespaces count 4 size 7 4 %
Cpool nssets count 0 size 1 0 %
Cpool names count 5 size 13 7 %
MethodInfo count 4 size 17 10 %
InstanceInfo size 11 6 %
ClassInfo size 2 1 %
ScriptInfo size 7 4 %
MethodBodies size 27 16 %
  script0

  interface de.polygonal.ds::Cloneable extends *
  {

    function de.polygonal.ds::Cloneable():void	/* disp_id -1*/

    function de.polygonal.ds:Cloneable::clone():Object	/* disp_id 0*/

    static function de.polygonal.ds::Cloneable$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 5492b 3%

abc name de/polygonal/ds/Vector
magic 2e0010
Cpool numbers size 9 0 %
Cpool strings count 93 size 753 13 %
Cpool namespaces count 7 size 13 0 %
Cpool nssets count 3 size 5 0 %
Cpool names count 75 size 222 4 %
MethodInfo count 45 size 314 5 %
InstanceInfo size 186 3 %
ClassInfo size 2 0 %
ScriptInfo size 7 0 %
MethodBodies size 3949 72 %
  script0

  class de.polygonal.ds::Vector extends Object
    implements de.polygonal.ds::Collection
  {

    function de.polygonal.ds::Vector(int):void	/* disp_id -1*/

    function toVector(de.polygonal.ds::Vector):de.polygonal.ds::Vector	/* disp_id 0*/

    function toString():String	/* disp_id 0*/

    function toArray(Array):Array	/* disp_id 0*/

    function swp(int,int):void	/* disp_id 0*/

    function sort(Function,Boolean):void	/* disp_id 0*/

    function size():int	/* disp_id 0*/

    function shuffle(de.polygonal.ds::Vector):void	/* disp_id 0*/

    function set(int,Object):void	/* disp_id 0*/

    function reverse():void	/* disp_id 0*/

    function removeRange(int,int,de.polygonal.ds::Vector):de.polygonal.ds::Vector	/* disp_id 0*/

    function removeAt(int):Object	/* disp_id 0*/

    function pushFront(Object):void	/* disp_id 0*/

    function pushBack(Object):void	/* disp_id 0*/

    function popFront():Object	/* disp_id 0*/

    function popBack():Object	/* disp_id 0*/

    function move(int,int,int):void	/* disp_id 0*/

    function maxSize():int	/* disp_id 0*/

    function lastIndexOf(Object,int):int	/* disp_id 0*/

    function join(String):String	/* disp_id 0*/

    function iterator():Object	/* disp_id 0*/

    function isEmpty():Boolean	/* disp_id 0*/

    function insertAt(int,Object):void	/* disp_id 0*/

    function indexOf(Object,int):int	/* disp_id 0*/

    function getArray():Array	/* disp_id 0*/

    function get(int):Object	/* disp_id 0*/

    function front():Object	/* disp_id 0*/

    function erase(Object):Boolean	/* disp_id 0*/

    function cpy(int,int):void	/* disp_id 0*/

    function contains(Object):Boolean	/* disp_id 0*/

    function concat(de.polygonal.ds::Vector,Boolean):de.polygonal.ds::Vector	/* disp_id 0*/

    function compact():void	/* disp_id 0*/

    function clone(Boolean,Object):de.polygonal.ds::Collection	/* disp_id 0*/

    function clear():void	/* disp_id 0*/

    function back():Object	/* disp_id 0*/

    function assign(*,Array,int):void	/* disp_id 0*/
    var _size:int	/* slot_id 0 */

    function _quickSortComparable(int,int):void	/* disp_id 0*/

    function _quickSort(int,int,Function):void	/* disp_id 0*/

    function _insertionSortComparable():void	/* disp_id 0*/

    function _insertionSort(Function):void	/* disp_id 0*/
    var _a:Array	/* slot_id 0 */

    function __set(int,Object):void	/* disp_id 0*/

    function __get(int):Object	/* disp_id 0*/

    function __cpy(int,int):void	/* disp_id 0*/

    static function de.polygonal.ds::Vector$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

DoABC2 549b 0%

abc name de/polygonal/ds/VectorIterator
magic 2e0010
Cpool numbers size 3 0 %
Cpool strings count 25 size 152 29 %
Cpool namespaces count 5 size 9 1 %
Cpool nssets count 3 size 5 0 %
Cpool names count 22 size 63 12 %
MethodInfo count 8 size 42 8 %
InstanceInfo size 48 9 %
ClassInfo size 2 0 %
ScriptInfo size 7 1 %
MethodBodies size 178 34 %
  script0

  class de.polygonal.ds::VectorIterator extends Object
    implements de.polygonal.ds::Itr
  {

    function de.polygonal.ds::VectorIterator(de.polygonal.ds::Vector):void	/* disp_id -1*/

    function reset():void	/* disp_id 0*/

    function next():Object	/* disp_id 0*/

    function hasNext():Boolean	/* disp_id 0*/
    var _s:int	/* slot_id 0 */
    var _i:int	/* slot_id 0 */
    var _f:de.polygonal.ds::Vector	/* slot_id 0 */
    var _a:Array	/* slot_id 0 */

    function __size(Object):int	/* disp_id 0*/

    function __a(Object):Array	/* disp_id 0*/

    static function de.polygonal.ds::VectorIterator$cinit():void	/* disp_id 0*/
  }

  function script0$init():void	/* disp_id 0*/
OPCODE	SIZE	% OF 0

SymbolClass 15b 0%
ShowFrame 0b 0%
End 0b 0%
