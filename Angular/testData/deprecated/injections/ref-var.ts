// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Directive} from "@angular/core";

@Component({
    selector: "main-comp",
    templateUrl: "./ref-var.html"
})
export class MainComponenet {

}

@Component({
    selector: "[comp]"
})
export class MyCustomComponent {

    myCompProp: string;

}

@Directive({
    selector: "[dir]",
    exportAs: "dir"
})
export class MyDirective {

    myDirectiveProp: string;

}