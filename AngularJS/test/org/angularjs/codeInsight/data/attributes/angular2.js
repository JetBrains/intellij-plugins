@Directive({
    selector: '[ng-non-bindable]',
    compileChildren: false
})
export class NonBindable {
}

@Directive({selector: '[ng-for][ng-for-of]', properties: ['ngForOf'], lifecycle: [onCheck]})
export class NgFor {
}