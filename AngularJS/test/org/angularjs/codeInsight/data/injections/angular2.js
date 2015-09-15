@Directive({
    selector: '[ng-non-bindable]',
    compileChildren: false
})
export class NonBindable {
}
