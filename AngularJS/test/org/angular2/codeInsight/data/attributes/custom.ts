// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Directive} from "@angular/core"

@Directive({
    selector: '[my-customer]',
    properties: {
        'id':'dependency'
    },
    templateUrl: '',
    styleUrls: [''],
})
class Dependency {
    id:string;
}

@Component({
    selector: 'some-tag',
    properties: {
        'id':'dependency'
    }
})
class Dependency {
    id:string;
}

