// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.


import {MyModule1} from "./non-angular.b";
import {Component, Directive, NgModule} from "foo/core";

@Directive({
})
export class MyDir1 {

}

@Component({
    entryComponents: [
        MyDir1
    ]
})
export class Component1 {

}

@NgModule({
    imports: [
        Component1
    ],
    exports: [MyModule1]
})
export class MyModule3 {

}

const THE_IMPORT = [
    MyModule3
]

@NgModule({
    imports: [
        THE_IMPORT,
        MyModule2
    ]
})
export class MyModule2 {

}

export const MY_IMPORTS = [
    MyModule2
]

@NgModule({
    imports: [MyModule3],
    exports: [
      MyModule4,
      Component1
    ]
})
export class MyModule4 {
}

@Component({})
class Component3 {
}
@Component({
    selector: "foo:not(attr1,attr2)"
})

export class MyComponent {

}

@Directive({
    inputs: ["", ""],
    outputs: [
        "",
        ""
    ]
})
export class MyDirective {

}

@Component({
  template: "foo",
  templateUrl: "bar"
})
export class PropBoth {

}
