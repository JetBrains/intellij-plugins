import {Component} from '@angular/core';
import {IGX_DIALOG_DIRECTIVES} from "igniteui-angular";
import {IgxAccordionComponent, IgxExpansionPanelComponent, IgxDropDownComponent, IgxDropDownItemComponent} from "igniteui-angular"

export const IGX_DROP_DOWN_DIRECTIVES = [IgxDropDownComponent, IgxDropDownItemComponent];

const IGX_ACCORDION_DIRECTIVES = [IgxAccordionComponent, IgxExpansionPanelComponent] as const;

@Component({
   selector: 'app-root',
   standalone: true,
   imports: [IGX_DIALOG_DIRECTIVES, IGX_ACCORDION_DIRECTIVES, IGX_DROP_DOWN_DIRECTIVES],
   template: `
    <igx-dialog></igx-dialog>
    <igx-accordion></igx-accordion>
    <igx-drop-down><igx-drop-down-item></igx-drop-down-item></igx-drop-down>
    <<warning descr="Unknown html tag foo">foo</warning>></<warning descr="Unknown html tag foo">foo</warning>>
   `
})
export class AppComponent {
}
