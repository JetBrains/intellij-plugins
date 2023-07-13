import {Component} from "@angular/core"

@Component({
    selector: 'my_customer',
    properties: {
        'id':'dependency'
    }
})
class Dependency {
    id:string;
}
