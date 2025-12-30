import type { ComponentInterface, EventEmitter } from '../../stencil-public-runtime';
import type { Color, StyleEventDetail } from '../../interface';
import type { SelectChangeEventDetail, SelectInterface, SelectCompareFn } from './select-interface';
/**
 * @virtualProp {"ios" | "md"} mode - The mode determines which platform styles to use.
 *
 * @slot label - The label text to associate with the select. Use the `labelPlacement` property to control where the label is placed relative to the select. Use this if you need to render a label with custom HTML.
 * @slot start - Content to display at the leading edge of the select.
 * @slot end - Content to display at the trailing edge of the select.
 *
 * @part placeholder - The text displayed in the select when there is no value.
 * @part text - The displayed value of the select.
 * @part icon - The select icon container.
 * @part container - The container for the selected text or placeholder.
 * @part label - The label text describing the select.
 */
export declare class Select implements ComponentInterface {
    private inputId;
    private overlay?;
    private focusEl?;
    private mutationO?;
    private inheritedAttributes;
    private nativeWrapperEl;
    private notchSpacerEl;
    private notchController?;
    el: HTMLIonSelectElement;
    isExpanded: boolean;
    /**
     * The text to display on the cancel button.
     */
    cancelText: string;
    /**
     * The color to use from your application's color palette.
     * Default options are: `"primary"`, `"secondary"`, `"tertiary"`, `"success"`, `"warning"`, `"danger"`, `"light"`, `"medium"`, and `"dark"`.
     * For more information on colors, see [theming](/docs/theming/basics).
     *
     * This property is only available when using the modern select syntax.
     */
    color?: Color;
    /**
     * This property allows developers to specify a custom function or property
     * name for comparing objects when determining the selected option in the
     * ion-select. When not specified, the default behavior will use strict
     * equality (===) for comparison.
     */
    compareWith?: string | SelectCompareFn | null;
    /**
     * If `true`, the user cannot interact with the select.
     */
    disabled: boolean;
    /**
     * The fill for the item. If `"solid"` the item will have a background. If
     * `"outline"` the item will be transparent with a border. Only available in `md` mode.
     */
    fill?: 'outline' | 'solid';
    /**
     * The interface the select should use: `action-sheet`, `popover`, `alert`, or `modal`.
     */
    interface: SelectInterface;
    /**
     * Any additional options that the `alert`, `action-sheet` or `popover` interface
     * can take. See the [ion-alert docs](./alert), the
     * [ion-action-sheet docs](./action-sheet), the
     * [ion-popover docs](./popover), and the [ion-modal docs](./modal) for the
     * create options for each interface.
     *
     * Note: `interfaceOptions` will not override `inputs` or `buttons` with the `alert` interface.
     */
    interfaceOptions: any;
    /**
     * How to pack the label and select within a line.
     * `justify` does not apply when the label and select
     * are on different lines when `labelPlacement` is set to
     * `"floating"` or `"stacked"`.
     * `"start"`: The label and select will appear on the left in LTR and
     * on the right in RTL.
     * `"end"`: The label and select will appear on the right in LTR and
     * on the left in RTL.
     * `"space-between"`: The label and select will appear on opposite
     * ends of the line with space between the two elements.
     */
    justify?: 'start' | 'end' | 'space-between';
    /**
     * The visible label associated with the select.
     *
     * Use this if you need to render a plaintext label.
     *
     * The `label` property will take priority over the `label` slot if both are used.
     */
    label?: string;
    /**
     * Where to place the label relative to the select.
     * `"start"`: The label will appear to the left of the select in LTR and to the right in RTL.
     * `"end"`: The label will appear to the right of the select in LTR and to the left in RTL.
     * `"floating"`: The label will appear smaller and above the select when the select is focused or it has a value. Otherwise it will appear on top of the select.
     * `"stacked"`: The label will appear smaller and above the select regardless even when the select is blurred or has no value.
     * `"fixed"`: The label has the same behavior as `"start"` except it also has a fixed width. Long text will be truncated with ellipses ("...").
     * When using `"floating"` or `"stacked"` we recommend initializing the select with either a `value` or a `placeholder`.
     */
    labelPlacement?: 'start' | 'end' | 'floating' | 'stacked' | 'fixed';
    /**
     * If `true`, the select can accept multiple values.
     */
    multiple: boolean;
    /**
     * The name of the control, which is submitted with the form data.
     */
    name: string;
    /**
     * The text to display on the ok button.
     */
    okText: string;
    /**
     * The text to display when the select is empty.
     */
    placeholder?: string;
    /**
     * The text to display instead of the selected option's value.
     */
    selectedText?: string | null;
    /**
     * The toggle icon to use. Defaults to `chevronExpand` for `ios` mode,
     * or `caretDownSharp` for `md` mode.
     */
    toggleIcon?: string;
    /**
     * The toggle icon to show when the select is open. If defined, the icon
     * rotation behavior in `md` mode will be disabled. If undefined, `toggleIcon`
     * will be used for when the select is both open and closed.
     */
    expandedIcon?: string;
    /**
     * The shape of the select. If "round" it will have an increased border radius.
     */
    shape?: 'round';
    /**
     * The value of the select.
     */
    value?: any | null;
    /**
     * Emitted when the value has changed.
     *
     * This event will not emit when programmatically setting the `value` property.
     */
    ionChange: EventEmitter<SelectChangeEventDetail>;
    /**
     * Emitted when the selection is cancelled.
     */
    ionCancel: EventEmitter<void>;
    /**
     * Emitted when the overlay is dismissed.
     */
    ionDismiss: EventEmitter<void>;
    /**
     * Emitted when the select has focus.
     */
    ionFocus: EventEmitter<void>;
    /**
     * Emitted when the select loses focus.
     */
    ionBlur: EventEmitter<void>;
    /**
     * Emitted when the styles change.
     * @internal
     */
    ionStyle: EventEmitter<StyleEventDetail>;
    protected styleChanged(): void;
    private setValue;
    connectedCallback(): Promise<void>;
    componentWillLoad(): void;
    componentDidLoad(): void;
    disconnectedCallback(): void;
    /**
     * Open the select overlay. The overlay is either an alert, action sheet, or popover,
     * depending on the `interface` property on the `ion-select`.
     *
     * @param event The user interface event that called the open.
     */
    open(event?: UIEvent): Promise<any>;
    private createOverlay;
    private updateOverlayOptions;
    private createActionSheetButtons;
    private createAlertInputs;
    private createOverlaySelectOptions;
    private openPopover;
    private openActionSheet;
    private openAlert;
    private openModal;
    /**
     * Close the select interface.
     */
    private close;
    private hasValue;
    private get childOpts();
    /**
     * Returns any plaintext associated with
     * the label (either prop or slot).
     * Note: This will not return any custom
     * HTML. Use the `hasLabel` getter if you
     * want to know if any slotted label content
     * was passed.
     */
    private get labelText();
    private getText;
    private setFocus;
    private emitStyle;
    private onClick;
    private onFocus;
    private onBlur;
    private renderLabel;
    componentDidRender(): void;
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
    /**
     * Renders either the placeholder
     * or the selected values based on
     * the state of the select.
     */
    private renderSelectText;
    /**
     * Renders the chevron icon
     * next to the select text.
     */
    private renderSelectIcon;
    private get ariaLabel();
    private renderListbox;
    render(): any;
}
