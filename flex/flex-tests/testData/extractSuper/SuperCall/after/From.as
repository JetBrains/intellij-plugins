package {
 class From extends FromSuper {
     function bar() {}
 }

class UsageBase {
     function UsageBase(p1:FromSuper, p2:From) {
         p1.foo();
         p2.bar();
     }
 }

 class Usage extends UsageBase {
     function Usage(p1:FromSuper, p2:From) {
         super(p1, p2);
     }
 }


}
