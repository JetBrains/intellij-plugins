import {Directive} from "@angular/core"

@Directive({
    selector: 'my-customer',
    properties: {
        'id':'dependency'
    }
})
class Dependency {
    id:string;
}
