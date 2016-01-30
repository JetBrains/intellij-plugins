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

