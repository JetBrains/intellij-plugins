import {Component} from "@angular/core"

@Component({
    selector: 'my-customer',
    properties: {
        'id':'dependency'
    }
})
class Dependency {
    id:string;
}
