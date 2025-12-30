import type { ComponentInterface, EventEmitter } from '../../stencil-public-runtime';
import type { AutocompleteTypes, Color, TextFieldTypes } from '../../interface';
import type { InputChangeEventDetail, InputInputEventDetail } from './input-interface';
/**
 * @virtualProp {"ios" | "md"} mode - The mode determines which platform styles to use.
 *
 * @slot label - The label text to associate with the input. Use the `labelPlacement` property to control where the label is placed relative to the input. Use this if you need to render a label with custom HTML. (EXPERIMENTAL)
 * @slot start - Content to display at the leading edge of the input. (EXPERIMENTAL)
 * @slot end - Content to display at the trailing edge of the input. (EXPERIMENTAL)
 */
export declare class Input implements ComponentInterface {
    private nativeInput?;
    private inputId;
    private helperTextId;
    private errorTextId;
    private inheritedAttributes;
    private isComposing;
    private slotMutationController?;
    private notchController?;
    private notchSpacerEl;
    private originalIonInput?;
    /**
     * `true` if the input was cleared as a result of the user typing
     * with `clearOnEdit` enabled.
     *
     * Resets when the input loses focus.
     */
    private didInputClearOnEdit;
    /**
     * The value of the input when the input is focused.
     */
    private focusedValue?;
    hasFocus: boolean;
    el: HTMLIonInputElement;
    /**
     * The color to use from your application's color palette.
     * Default options are: `"primary"`, `"secondary"`, `"tertiary"`, `"success"`, `"warning"`, `"danger"`, `"light"`, `"medium"`, and `"dark"`.
     * For more information on colors, see [theming](/docs/theming/basics).
     */
    color?: Color;
    /**
     * Indicates whether and how the text value should be automatically capitalized as it is entered/edited by the user.
     * Available options: `"off"`, `"none"`, `"on"`, `"sentences"`, `"words"`, `"characters"`.
     */
    autocapitalize: string;
    /**
     * Indicates whether the value of the control can be automatically completed by the browser.
     */
    autocomplete: AutocompleteTypes;
    /**
     * Whether auto correction should be enabled when the user is entering/editing the text value.
     */
    autocorrect: 'on' | 'off';
    /**
     * Sets the [`autofocus` attribute](https://developer.mozilla.org/en-US/docs/Web/HTML/Global_attributes/autofocus) on the native input element.
     *
     * This may not be sufficient for the element to be focused on page load. See [managing focus](/docs/developing/managing-focus) for more information.
     */
    autofocus: boolean;
    /**
     * If `true`, a clear icon will appear in the input when there is a value. Clicking it clears the input.
     */
    clearInput: boolean;
    /**
     * The icon to use for the clear button. Only applies when `clearInput` is set to `true`.
     */
    clearInputIcon?: string;
    /**
     * If `true`, the value will be cleared after focus upon edit. Defaults to `true` when `type` is `"password"`, `false` for all other types.
     */
    clearOnEdit?: boolean;
    /**
     * If `true`, a character counter will display the ratio of characters used and the total character limit. Developers must also set the `maxlength` property for the counter to be calculated correctly.
     */
    counter: boolean;
    /**
     * A callback used to format the counter text.
     * By default the counter text is set to "itemLength / maxLength".
     *
     * See https://ionicframework.com/docs/troubleshooting/runtime#accessing-this
     * if you need to access `this` from within the callback.
     */
    counterFormatter?: (inputLength: number, maxLength: number) => string;
    /**
     * Set the amount of time, in milliseconds, to wait to trigger the `ionInput` event after each keystroke.
     */
    debounce?: number;
    protected debounceChanged(): void;
    /**
     * If `true`, the user cannot interact with the input.
     */
    disabled: boolean;
    /**
     * A hint to the browser for which enter key to display.
     * Possible values: `"enter"`, `"done"`, `"go"`, `"next"`,
     * `"previous"`, `"search"`, and `"send"`.
     */
    enterkeyhint?: 'enter' | 'done' | 'go' | 'next' | 'previous' | 'search' | 'send';
    /**
     * Text that is placed under the input and displayed when an error is detected.
     */
    errorText?: string;
    /**
     * The fill for the item. If `"solid"` the item will have a background. If
     * `"outline"` the item will be transparent with a border. Only available in `md` mode.
     */
    fill?: 'outline' | 'solid';
    /**
     * A hint to the browser for which keyboard to display.
     * Possible values: `"none"`, `"text"`, `"tel"`, `"url"`,
     * `"email"`, `"numeric"`, `"decimal"`, and `"search"`.
     */
    inputmode?: 'none' | 'text' | 'tel' | 'url' | 'email' | 'numeric' | 'decimal' | 'search';
    /**
     * Text that is placed under the input and displayed when no error is detected.
     */
    helperText?: string;
    /**
     * The visible label associated with the input.
     *
     * Use this if you need to render a plaintext label.
     *
     * The `label` property will take priority over the `label` slot if both are used.
     */
    label?: string;
    /**
     * Where to place the label relative to the input.
     * `"start"`: The label will appear to the left of the input in LTR and to the right in RTL.
     * `"end"`: The label will appear to the right of the input in LTR and to the left in RTL.
     * `"floating"`: The label will appear smaller and above the input when the input is focused or it has a value. Otherwise it will appear on top of the input.
     * `"stacked"`: The label will appear smaller and above the input regardless even when the input is blurred or has no value.
     * `"fixed"`: The label has the same behavior as `"start"` except it also has a fixed width. Long text will be truncated with ellipses ("...").
     */
    labelPlacement: 'start' | 'end' | 'floating' | 'stacked' | 'fixed';
    /**
     * The maximum value, which must not be less than its minimum (min attribute) value.
     */
    max?: string | number;
    /**
     * If the value of the type attribute is `text`, `email`, `search`, `password`, `tel`, or `url`, this attribute specifies the maximum number of characters that the user can enter.
     */
    maxlength?: number;
    /**
     * The minimum value, which must not be greater than its maximum (max attribute) value.
     */
    min?: string | number;
    /**
     * If the value of the type attribute is `text`, `email`, `search`, `password`, `tel`, or `url`, this attribute specifies the minimum number of characters that the user can enter.
     */
    minlength?: number;
    /**
     * If `true`, the user can enter more than one value. This attribute applies when the type attribute is set to `"email"`, otherwise it is ignored.
     */
    multiple?: boolean;
    /**
     * The name of the control, which is submitted with the form data.
     */
    name: string;
    /**
     * A regular expression that the value is checked against. The pattern must match the entire value, not just some subset. Use the title attribute to describe the pattern to help the user. This attribute applies when the value of the type attribute is `"text"`, `"search"`, `"tel"`, `"url"`, `"email"`, `"date"`, or `"password"`, otherwise it is ignored. When the type attribute is `"date"`, `pattern` will only be used in browsers that do not support the `"date"` input type natively. See https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/date for more information.
     */
    pattern?: string;
    /**
     * Instructional text that shows before the input has a value.
     * This property applies only when the `type` property is set to `"email"`,
     * `"number"`, `"password"`, `"search"`, `"tel"`, `"text"`, or `"url"`, otherwise it is ignored.
     */
    placeholder?: string;
    /**
     * If `true`, the user cannot modify the value.
     */
    readonly: boolean;
    /**
     * If `true`, the user must fill in a value before submitting a form.
     */
    required: boolean;
    /**
     * The shape of the input. If "round" it will have an increased border radius.
     */
    shape?: 'round';
    /**
     * If `true`, the element will have its spelling and grammar checked.
     */
    spellcheck: boolean;
    /**
     * Works with the min and max attributes to limit the increments at which a value can be set.
     * Possible values are: `"any"` or a positive floating point number.
     */
    step?: string;
    /**
     * The type of control to display. The default type is text.
     */
    type: TextFieldTypes;
    /**
     * Whenever the type on the input changes we need
     * to update the internal type prop on the password
     * toggle so that that correct icon is shown.
     */
    onTypeChange(): void;
    /**
     * The value of the input.
     */
    value?: string | number | null;
    /**
     * The `ionInput` event is fired each time the user modifies the input's value.
     * Unlike the `ionChange` event, the `ionInput` event is fired for each alteration
     * to the input's value. This typically happens for each keystroke as the user types.
     *
     * For elements that accept text input (`type=text`, `type=tel`, etc.), the interface
     * is [`InputEvent`](https://developer.mozilla.org/en-US/docs/Web/API/InputEvent); for others,
     * the interface is [`Event`](https://developer.mozilla.org/en-US/docs/Web/API/Event). If
     * the input is cleared on edit, the type is `null`.
     */
    ionInput: EventEmitter<InputInputEventDetail>;
    /**
     * The `ionChange` event is fired when the user modifies the input's value.
     * Unlike the `ionInput` event, the `ionChange` event is only fired when changes
     * are committed, not as the user types.
     *
     * Depending on the way the users interacts with the element, the `ionChange`
     * event fires at a different moment:
     * - When the user commits the change explicitly (e.g. by selecting a date
     * from a date picker for `<ion-input type="date">`, pressing the "Enter" key, etc.).
     * - When the element loses focus after its value has changed: for elements
     * where the user's interaction is typing.
     *
     * This event will not emit when programmatically setting the `value` property.
     */
    ionChange: EventEmitter<InputChangeEventDetail>;
    /**
     * Emitted when the input loses focus.
     */
    ionBlur: EventEmitter<FocusEvent>;
    /**
     * Emitted when the input has focus.
     */
    ionFocus: EventEmitter<FocusEvent>;
    /**
     * Update the native input element when the value changes
     */
    protected valueChanged(): void;
    componentWillLoad(): void;
    connectedCallback(): void;
    componentDidLoad(): void;
    componentDidRender(): void;
    disconnectedCallback(): void;
    /**
     * Sets focus on the native `input` in `ion-input`. Use this method instead of the global
     * `input.focus()`.
     *
     * Developers who wish to focus an input when a page enters
     * should call `setFocus()` in the `ionViewDidEnter()` lifecycle method.
     *
     * Developers who wish to focus an input when an overlay is presented
     * should call `setFocus` after `didPresent` has resolved.
     *
     * See [managing focus](/docs/developing/managing-focus) for more information.
     */
    setFocus(): Promise<void>;
    /**
     * Returns the native `<input>` element used under the hood.
     */
    getInputElement(): Promise<HTMLInputElement>;
    /**
     * Emits an `ionChange` event.
     *
     * This API should be called for user committed changes.
     * This API should not be used for external value changes.
     */
    private emitValueChange;
    /**
     * Emits an `ionInput` event.
     */
    private emitInputChange;
    private shouldClearOnEdit;
    private getValue;
    private onInput;
    private onChange;
    private onBlur;
    private onFocus;
    private onKeydown;
    private checkClearOnEdit;
    private onCompositionStart;
    private onCompositionEnd;
    private clearTextInput;
    private hasValue;
    /**
     * Renders the helper text or error text values
     */
    private renderHintText;
    private getHintTextID;
    private renderCounter;
    /**
     * Responsible for rendering helper text,
     * error text, and counter. This element should only
     * be rendered if hint text is set or counter is enabled.
     */
    private renderBottomContent;
    private renderLabel;
    /**
     * Gets any content passed into the `label` slot,
     * not the <slot> definition.
     */
    private get labelSlot();
    /**
     * Returns `true` if label content is provided
     * either by a prop or a content. If you want
     * to get the plaintext value of the label use
     * the `labelText` getter instead.
     */
    private get hasLabel();
    /**
     * Renders the border container
     * when fill="outline".
     */
    private renderLabelContainer;
    render(): any;
}
