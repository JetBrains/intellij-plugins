import {Directive} from "@angular/core"

@Directive({
    selector: `  [other-attr] `,
    template: `this is other-attr ({{ value }}) `
})
export class OtherAttrComponent {
    value: string = "other ";
}
