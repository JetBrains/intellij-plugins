import {Component} from '@angular/core'
import {AbstractControl, FormArray, FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms'

type NonUndefined<T> = T extends undefined ? never : T;

export type FormControlsOf<T extends Record<string, any>> = {
    [K in keyof T]: NonUndefined<T[K]> extends AbstractControl ? T[K] : NonUndefined<T[K]> extends (infer R)[]
        ? FormArray<FormControl<R>>
        : NonUndefined<T[K]> extends Record<any, any>
            ? FormGroup<FormControlsOf<T[K]>>
            : FormControl<T[K]>
}

export type CustomLinkData = {
    text: string | null
    url: string | null
}

@Component({
    selector: '',
    standalone: true,
    imports: [ReactiveFormsModule],
    template: `<input [formControl]="formGroup.controls.text" 
                      <error descr="TS2322: Type 'FormControl<string | null>' is not assignable to type 'string | number | null'.">[formControlName]</error>="formGroup.controls.text">`
})

export class CustomLinkEditorComponent {
    protected readonly formGroup = new FormGroup<FormControlsOf<CustomLinkData>>({
        text: new FormControl(''),
        url: new FormControl('')
    })
}
