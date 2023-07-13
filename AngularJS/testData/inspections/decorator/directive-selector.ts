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
    selector: "foo:not(attr1<error descr="Multiple selectors in :not are not supported">,</error>attr2)"
})
export class MyComponent {

}

@Directive(<error descr="Directive is missing a required 'selector' property">{
    inputs: ["", ""],
    outputs: [
        "",
        ""
    ]
}</error>)
export class MyDirective {

}

@Directive(<error descr="Directive is missing a required 'selector' property">{
    inputs: ["", ""],
    outputs: [
        "",
        ""
    ]
}</error>)
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
    selector: "[foo]:not(a<error descr="Nested :not is not allowed in selectors">:not(</error>)"
})
export class MyDirective {

}

@Directive({
    selector: "[foo]:not(a<error descr="Nested :not is not allowed in selectors">:not(</error>)"
})
export default class MyDirective {

}

@Directive(<error descr="Directive is missing a required 'selector' property">{}</error>)
export class MyDirective {

}

@Directive()
export class MyDirective {

}

@Directive()
export abstract class MyDirective {

}
