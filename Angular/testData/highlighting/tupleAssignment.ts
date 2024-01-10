import {Component, Input} from "@angular/core";

type Prefix = 'sm' | 'md' | 'lg';
type Icon = 'home' | 'settings' | 'user';
type IconProp = [Prefix, Icon];

@Component({
    selector: 'my-icon',
    standalone: true,
    template: `
        <my-icon [icon]="['sm', 'home']"></my-icon>
        <my-icon [icon]="<error descr="Type [\"foo\", \"home\"] is not assignable to type IconProp | undefined...  Type [\"foo\", \"home\"] is not assignable to type [\"sm\" | \"md\" | \"lg\", \"home\" | \"settings\" | \"user\"] | undefined    Type [\"foo\", \"home\"] is not assignable to type [\"sm\" | \"md\" | \"lg\", \"home\" | \"settings\" | \"user\"]">['foo', 'home']</error>"></my-icon>
    `
})
export class InnerComponent {
    @Input() icon: IconProp | undefined;
}
