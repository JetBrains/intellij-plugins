function trace() {}
var numbers:Array = new Array(1,2,3);
var letters:Array = new Array("a","b","c");

trace(numbers.concat(letters));

function myFunction(obj:Object):void {}
numbers.every(myFunction, this)
numbers.filter(myFunction, this)
numbers.forEach(myFunction, this)
numbers.indexOf(1, 2)
numbers.lastIndexOf(1, 2)
numbers.join(' ')
numbers.map(myFunction)
numbers.some(myFunction)
numbers.pop()
numbers.push(4)
numbers.reverse()
numbers.shift()
numbers.unshift(1)
numbers.slice()
numbers.sort()
numbers.sortOn('1')
numbers.splice()

trace(Array.NUMERIC)
trace(Array.RETURNINDEXEDARRAY)
trace(Array.UNIQUESORT)