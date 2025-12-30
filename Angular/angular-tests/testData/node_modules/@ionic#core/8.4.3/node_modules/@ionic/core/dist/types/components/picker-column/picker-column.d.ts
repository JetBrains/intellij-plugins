import type { ComponentInterface, EventEmitter } from '../../stencil-public-runtime';
import type { Color } from '../../interface';
import type { PickerColumnChangeEventDetail, PickerColumnValue } from './picker-column-interfaces';
/**
 * @virtualProp {"ios" | "md"} mode - The mode determines which platform styles to use.
 *
 * @slot prefix - Content to show on the left side of the picker options.
 * @slot suffix - Content to show on the right side of the picker options.
 */
export declare class PickerColumn implements ComponentInterface {
    private scrollEl?;
    private destroyScrollListener?;
    private isScrolling;
    private scrollEndCallback?;
    private isColumnVisible;
    private parentEl?;
    private canExitInputMode;
    private assistiveFocusable?;
    private updateValueTextOnScroll;
    ariaLabel: string | null;
    ariaLabelChanged(newValue: string): void;
    isActive: boolean;
    el: HTMLIonPickerColumnElement;
    /**
     * If `true`, the user cannot interact with the picker.
     */
    disabled: boolean;
    /**
     * The selected option in the picker.
     */
    value?: string | number;
    /**
     * The color to use from your application's color palette.
     * Default options are: `"primary"`, `"secondary"`, `"tertiary"`, `"success"`, `"warning"`, `"danger"`, `"light"`, `"medium"`, and `"dark"`.
     * For more information on colors, see [theming](/docs/theming/basics).
     */
    color?: Color;
    /**
     * If `true`, tapping the picker will
     * reveal a number input keyboard that lets
     * the user type in values for each picker
     * column. This is useful when working
     * with time pickers.
     *
     * @internal
     */
    numericInput: boolean;
    /**
     * Emitted when the value has changed.
     *
     * This event will not emit when programmatically setting the `value` property.
     */
    ionChange: EventEmitter<PickerColumnChangeEventDetail>;
    valueChange(): void;
    /**
     * Only setup scroll listeners
     * when the picker is visible, otherwise
     * the container will have a scroll
     * height of 0px.
     */
    componentWillLoad(): void;
    componentDidRender(): void;
    /** @internal  */
    scrollActiveItemIntoView(smooth?: boolean): Promise<void>;
    /**
     * Sets the value prop and fires the ionChange event.
     * This is used when we need to fire ionChange from
     * user-generated events that cannot be caught with normal
     * input/change event listeners.
     * @internal
     */
    setValue(value: PickerColumnValue): Promise<void>;
    /**
     * Sets focus on the scrollable container within the picker column.
     * Use this method instead of the global `pickerColumn.focus()`.
     */
    setFocus(): Promise<void>;
    connectedCallback(): void;
    private centerPickerItemInView;
    private setPickerItemActiveState;
    /**
     * When ionInputModeChange is emitted, each column
     * needs to check if it is the one being made available
     * for text entry.
     */
    private inputModeChange;
    /**
     * Setting isActive will cause a re-render.
     * As a result, we do not want to cause the
     * re-render mid scroll as this will cause
     * the picker column to jump back to
     * whatever value was selected at the
     * start of the scroll interaction.
     */
    private setInputModeActive;
    /**
     * When the column scrolls, the component
     * needs to determine which item is centered
     * in the view and will emit an ionChange with
     * the item object.
     */
    private initializeScrollListener;
    /**
     * Tells the parent picker to
     * exit text entry mode. This is only called
     * when the selected item changes during scroll, so
     * we know that the user likely wants to scroll
     * instead of type.
     */
    private exitInputMode;
    get activeItem(): HTMLIonPickerColumnOptionElement | undefined;
    /**
     * Find the next enabled option after the active option.
     * @param stride - How many options to "jump" over in order to select the next option.
     * This can be used to implement PageUp/PageDown behaviors where pressing these keys
     * scrolls the picker by more than 1 option. For example, a stride of 5 means select
     * the enabled option 5 options after the active one. Note that the actual option selected
     * may be past the stride if the option at the stride is disabled.
     */
    private findNextOption;
    /**
     * Find the next enabled option after the active option.
     * @param stride - How many options to "jump" over in order to select the next option.
     * This can be used to implement PageUp/PageDown behaviors where pressing these keys
     * scrolls the picker by more than 1 option. For example, a stride of 5 means select
     * the enabled option 5 options before the active one. Note that the actual option selected
     *  may be past the stride if the option at the stride is disabled.
     */
    private findPreviousOption;
    private onKeyDown;
    /**
     * Utility to generate the correct text for aria-valuetext.
     */
    private getOptionValueText;
    /**
     * Render an element that overlays the column. This element is for assistive
     * tech to allow users to navigate the column up/down. This element should receive
     * focus as it listens for synthesized keyboard events as required by the
     * slider role: https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Roles/slider_role
     */
    private renderAssistiveFocusable;
    render(): any;
}
