// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Directive} from "@angular/core";

@Component({})
export class NoProp {

}

@Component({})
export default class NoProp {

}

@Component({
    templateUrl: ""
})
export class Prop1 {

}


@Component({
    template: ""
})
export class Prop2 {

}


@Component({
    templateUrl: "bar"
})
export class PropBoth {

}

@Directive({

})
export class Dir {

}

@Component({selector: "foo"})
