class Greeter {
    constructor(public greeting: string) { }
    greet() {
        <error descr="TSLint: Use of debugger statements is forbidden (no-debugger)">debugger;</error>
        <error descr="TSLint: if statements must be braced (curly)">if (this.greeting.length > 1) return "<h2>" + this.greeting + "</h2>";</error>
        return "<h1>" + this.greeting + "</h1>";
    }
}<error descr="TSLint: Unnecessary semicolon (semicolon)">;</error>
var greeter = new Greeter(<error descr="TSLint: ' should be \" (quotemark)">'Hello, world!'</error>);
var str = greeter.greet();
document.body.innerHTML = str<error descr="TSLint: file should end with a newline (eofline)">;</error>