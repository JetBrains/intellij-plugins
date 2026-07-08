// "Convert to named function" "true"
class SomeInterface {
    public function g(list:Array):Boolean {
        return list.some(foo)

        function foo(item:String, index:int, array:Array):Boolean {
            return list.length > 3;
        }
    }
}