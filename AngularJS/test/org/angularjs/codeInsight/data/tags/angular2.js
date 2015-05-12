@Directive({
    selector: '[non-bindable]',
    compileChildren: false
})
export class NonBindable {
}
