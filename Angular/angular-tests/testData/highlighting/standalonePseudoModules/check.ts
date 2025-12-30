import {Component} from '@angular/core';
import {IGX_DIALOG_DIRECTIVES} from "igniteui-angular";
import {IgxAccordionComponent, IgxExpansionPanelComponent, IgxDropDownComponent, IgxDropDownItemComponent, IgxExpansionPanelIconDirective} from "igniteui-angular"

export const IGX_DROP_DOWN_DIRECTIVES = [IgxDropDownComponent, IgxDropDownItemComponent];

const IGX_ACCORDION_DIRECTIVES = [IgxAccordionComponent, IgxExpansionPanelComponent] as const;

const IGX_ACCORDION_DIRECTIVES_UNUSED = [IgxExpansionPanelIconDirective, IgxExpansionPanelComponent] as const;

export const IGX_ACCORDION_DIRECTIVES_UNUSED_MODULE = [IgxExpansionPanelIconDirective, IgxExpansionPanelComponent];

@Component({
   selector: 'app-root',
   standalone: true,
   imports: [
     IGX_DIALOG_DIRECTIVES,
     IGX_DROP_DOWN_DIRECTIVES,
     <error descr="None of the declarations provided by IGX_ACCORDION_DIRECTIVES_UNUSED_MODULE are used in a component template">IGX_ACCORDION_DIRECTIVES_UNUSED_MODULE</error>,
     IGX_ACCORDION_DIRECTIVES,
     IGX_ACCORDION_DIRECTIVES_UNUSED,
   ],
   template: `
    <igx-dialog></igx-dialog>
    <igx-accordion></igx-accordion>
    <igx-drop-down><igx-drop-down-item></igx-drop-down-item></igx-drop-down>
    <<warning descr="Unknown html tag foo">foo</warning>></<warning descr="Unknown html tag foo">foo</warning>>
   `
})
export class AppComponent {
}
