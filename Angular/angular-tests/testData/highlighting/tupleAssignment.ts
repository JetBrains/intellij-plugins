import {Component, Input} from "@angular/core";

type Prefix = 'sm' | 'md' | 'lg';
type Icon = 'home' | 'settings' | 'user';
type IconProp = [Prefix, Icon];

@Component({
    selector: 'my-icon',
    standalone: true,
    template: `
        <my-icon [icon]="['sm', 'home']"></my-icon>
        <my-icon [icon]="[<error descr="TS2322: Type '\"foo\"' is not assignable to type 'Prefix'.">'foo'</error>, 'home']"></my-icon>
    `
})
export class InnerComponent {
    @Input() icon: IconProp | undefined;
}
