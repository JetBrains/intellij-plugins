/**
 * @license
 * Copyright Google Inc. All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { EventEmitter, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { FormControl, FormHooks } from '../model';
import { ControlContainer } from './control_container';
import { ControlValueAccessor } from './control_value_accessor';
import { NgControl } from './ng_control';
import { AsyncValidator, AsyncValidatorFn, Validator, ValidatorFn } from './validators';
export declare const formControlBinding: any;
/**
 * @description
 * Creates a `FormControl` instance from a domain model and binds it
 * to a form control element.
 *
 * The `FormControl` instance tracks the value, user interaction, and
 * validation status of the control and keeps the view synced with the model. If used
 * within a parent form, the directive also registers itself with the form as a child
 * control.
 *
 * This directive is used by itself or as part of a larger form. Use the
 * `ngModel` selector to activate it.
 *
 * It accepts a domain model as an optional `Input`. If you have a one-way binding
 * to `ngModel` with `[]` syntax, changing the value of the domain model in the component
 * class sets the value in the view. If you have a two-way binding with `[()]` syntax
 * (also known as 'banana-box syntax'), the value in the UI always syncs back to
 * the domain model in your class.
 *
 * To inspect the properties of the associated `FormControl` (like validity state),
 * export the directive into a local template variable using `ngModel` as the key (ex: `#myVar="ngModel"`).
 * You then access the control using the directive's `control` property,
 * but most properties used (like `valid` and `dirty`) fall through to the control anyway for direct access.
 * See a full list of properties directly available in `AbstractControlDirective`.
 *
 * @see `RadioControlValueAccessor`
 * @see `SelectControlValueAccessor`
 *
 * @usageNotes
 *
 * ### Using ngModel on a standalone control
 *
 * The following examples show a simple standalone control using `ngModel`:
 *
 * {@example forms/ts/simpleNgModel/simple_ng_model_example.ts region='Component'}
 *
 * When using the `ngModel` within `<form>` tags, you'll also need to supply a `name` attribute
 * so that the control can be registered with the parent form under that name.
 *
 * In the context of a parent form, it's often unnecessary to include one-way or two-way binding,
 * as the parent form syncs the value for you. You access its properties by exporting it into a
 * local template variable using `ngForm` such as (`#f="ngForm"`). Use the variable where
 * needed on form submission.
 *
 * If you do need to populate initial values into your form, using a one-way binding for
 * `ngModel` tends to be sufficient as long as you use the exported form's value rather
 * than the domain model's value on submit.
 *
 * ### Using ngModel within a form
 *
 * The following example shows controls using `ngModel` within a form:
 *
 * {@example forms/ts/simpleForm/simple_form_example.ts region='Component'}
 *
 * ### Using a standalone ngModel within a group
 *
 * The following example shows you how to use a standalone ngModel control
 * within a form. This controls the display of the form, but doesn't contain form data.
 *
 * ```html
 * <form>
 *   <input name="login" ngModel placeholder="Login">
 *   <input type="checkbox" ngModel [ngModelOptions]="{standalone: true}"> Show more options?
 * </form>
 * <!-- form value: {login: ''} -->
 * ```
 *
 * ### Setting the ngModel name attribute through options
 *
 * The following example shows you an alternate way to set the name attribute. The name attribute is used
 * within a custom form component, and the name `@Input` property serves a different purpose.
 *
 * ```html
 * <form>
 *   <my-person-control name="Nancy" ngModel [ngModelOptions]="{name: 'user'}">
 *   </my-person-control>
 * </form>
 * <!-- form value: {user: ''} -->
 * ```
 *
 * @ngModule FormsModule
 * @publicApi
 */
export declare class NgModel extends NgControl implements OnChanges, OnDestroy {
    readonly control: FormControl;
    /**
     * @description
     * Internal reference to the view model value.
     */
    viewModel: any;
    /**
     * @description
     * Tracks the name bound to the directive. The parent form
     * uses this name as a key to retrieve this control's value.
     */
    name: string;
    /**
     * @description
     * Tracks whether the control is disabled.
     */
    isDisabled: boolean;
    /**
     * @description
     * Tracks the value bound to this directive.
     */
    model: any;
    /**
     * @description
     * Tracks the configuration options for this `ngModel` instance.
     *
     * **name**: An alternative to setting the name attribute on the form control element. See
     * the [example](api/forms/NgModel#using-ngmodel-on-a-standalone-control) for using `NgModel`
     * as a standalone control.
     *
     * **standalone**: When set to true, the `ngModel` will not register itself with its parent form,
     * and acts as if it's not in the form. Defaults to false.
     *
     * **updateOn**: Defines the event upon which the form control value and validity update.
     * Defaults to 'change'. Possible values: `'change'` | `'blur'` | `'submit'`.
     *
     */
    options: {
        name?: string;
        standalone?: boolean;
        updateOn?: FormHooks;
    };
    /**
     * @description
     * Event emitter for producing the `ngModelChange` event after
     * the view model updates.
     */
    update: EventEmitter<{}>;
    constructor(parent: ControlContainer, validators: Array<Validator | ValidatorFn>, asyncValidators: Array<AsyncValidator | AsyncValidatorFn>, valueAccessors: ControlValueAccessor[]);
    /**
     * @description
     * A lifecycle method called when the directive's inputs change. For internal use
     * only.
     *
     * @param changes A object of key/value pairs for the set of changed inputs.
     */
    ngOnChanges(changes: SimpleChanges): void;
    /**
     * @description
     * Lifecycle method called before the directive's instance is destroyed. For internal
     * use only.
     */
    ngOnDestroy(): void;
    /**
     * @description
     * Returns an array that represents the path from the top-level form to this control.
     * Each index is the string name of the control on that level.
     */
    readonly path: string[];
    /**
     * @description
     * The top-level directive for this control if present, otherwise null.
     */
    readonly formDirective: any;
    /**
     * @description
     * Synchronous validator function composed of all the synchronous validators
     * registered with this directive.
     */
    readonly validator: ValidatorFn | null;
    /**
     * @description
     * Async validator function composed of all the async validators registered with this
     * directive.
     */
    readonly asyncValidator: AsyncValidatorFn | null;
    /**
     * @description
     * Sets the new value for the view model and emits an `ngModelChange` event.
     *
     * @param newValue The new value emitted by `ngModelChange`.
     */
    viewToModelUpdate(newValue: any): void;
    private _setUpControl;
    private _setUpdateStrategy;
    private _isStandalone;
    private _setUpStandalone;
    private _checkForErrors;
    private _checkParentType;
    private _checkName;
    private _updateValue;
    private _updateDisabled;
}
