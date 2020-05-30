import {Component, Directive} from "@angular/core"

@Directive({
    selector: '[my-customer]',
    properties: {
        'id':'dependency'
    },
    templateUrl: 'custom.html',
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

