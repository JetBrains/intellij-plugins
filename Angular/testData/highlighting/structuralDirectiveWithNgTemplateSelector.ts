import {Component, ContentChild, Directive, Input, TemplateRef} from "@angular/core";

@Directive({
             standalone: true,
             selector: 'ng-template[appTableHead]'
           })
export class TableHeadDirective {

  constructor() {
    console.log("appTableHead");
  }

}

interface Row<T extends object> {
  $implicit: T
}

@Directive({
             standalone: true,
             selector: 'ng-template[appTableRow]'
           })
export class TableRowDirective<T extends object> {
  @Input() appTableRow!: T[] | '';

  constructor() {
    console.log("TableRowDirective");
  }

  static ngTemplateContextGuard<TContext extends object>(
    <weak_warning descr="TS6133: 'directive' is declared but its value is never read.">directive</weak_warning>: TableRowDirective<TContext>,
    _context: unknown
  ): _context is Row<TContext> {
    return true;
  }

}

@Component({
             selector: 'app-table',
             templateUrl: './table.component.html',
             standalone: true,
             styleUrls: ['./table.component.scss']
           })
export class TableComponent< T extends object> {
  @Input() data!: T[];
  @ContentChild(TableHeadDirective, {read: TemplateRef}) tableHead!: TemplateRef<any>;
  @ContentChild(TableRowDirective, {read: TemplateRef}) tableRow!: TemplateRef<any>;
}

@Component({
 selector: 'app-root',
 standalone: true,
 imports: [
   TableComponent,
   TableHeadDirective,
   TableRowDirective
 ],
 template: `
    <app-table [data]="persons">
      <ng-template <error descr="Property appTableHead is not provided by any applicable directive on an embedded template">[appTableHead]</error>="12">
        <td <warning descr="Obsolete attribute">scope</warning>="col" class="py-3 px-6">Firstname</td>
      </ng-template>
      <ng-template [appTableHead] >
        <td <warning descr="Obsolete attribute">scope</warning>="col" class="py-3 px-6">Firstname</td>
      </ng-template>
      <ng-template appTableHead>
        <td <warning descr="Obsolete attribute">scope</warning>="col" class="py-3 px-6">Firstname</td>
      </ng-template>
      <ng-template [appTableRow]="persons" let-row>
        <td class="py-4 px-6">{{ row.firstName }}</td>
      </ng-template>
    </app-table>
  `
 })
export class AppComponent {
  persons = [
    {
      firstName: 'John'
    },
  ];
}
