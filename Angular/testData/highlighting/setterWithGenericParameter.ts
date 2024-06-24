import {Component, Input} from "@angular/core";

export type myVariationSectionType =
  'default' | 'plus' | 'gift' | 'classic-no-padding' | 'blue-gray-left';

@Component({})
export abstract class CustomizableComponent<T extends string> {

  get variation(): boolean {
    return false;
  }

  @Input() set variation(<warning descr="Unused parameter val"><weak_warning descr="TS6133: 'val' is declared but its value is never read.">val</weak_warning></warning>: T) {
  }
}

@Component({
  selector: 'voo-section',
  template: '',
  standalone: true
})
export class VooSectionComponent extends CustomizableComponent<myVariationSectionType> {

}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [VooSectionComponent],
  template: `
    <voo-section [variation]="'gift'">Voo</voo-section>
    <voo-section [variation]="<error descr="Type \"gifts\" is not assignable to type \"default\" | \"plus\" | \"gift\" | \"classic-no-padding\" | \"blue-gray-left\"...  Type \"gifts\" is not assignable to type \"blue-gray-left\"    Type \"gifts\" is not assignable to type \"classic-no-padding\"">'gifts'</error>">Voo</voo-section>
  `
})
export class AppComponent {

}
