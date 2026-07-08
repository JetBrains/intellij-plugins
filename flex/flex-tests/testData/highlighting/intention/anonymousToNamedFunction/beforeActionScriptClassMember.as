// "Convert to named function" "true"

class ClassA {
    var one = 1
    var two = 2
    
    const handler = function<caret> () {
        console.log(one)
        console.log(two)
    }
    
    function main() {
        handler();
    }
}