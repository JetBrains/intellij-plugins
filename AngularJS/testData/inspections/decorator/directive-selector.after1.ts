// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import {Component, Directive} from "@angular/core";

@Component({})
export class MyComponent {

}

@Component({
    selector: ""
})
export class MyComponent {

}

@Component({
    selector: "foo"
})
export class MyComponent {

}

@Component({
    selector: "foo:not(attr1,attr2)"
})
export class MyComponent {

}

@Directive({
    inputs: ["", ""],
    selector: "",
    outputs: [
        "",
        ""
    ]
})
export class MyDirective {

}

@Directive({
    inputs: ["", ""],
    outputs: [
        "",
        ""
    ]
})
export default class MyDirective {

}

@Directive({
    selector: ""
})
export class MyDirective {

}

@Directive({
    selector: "[foo])"
})
export class MyDirective {

}

@Directive({
    selector: "[foo]:not(a:not()"
})
export class MyDirective {

}

@Directive({
    selector: "[foo]:not(a:not()"
})
export default class MyDirective {

}

@Directive({})
export class MyDirective {

}

@Directive()
export class MyDirective {

}

@Directive()
export abstract class MyDirective {

}
