import { ElementRef, ChangeDetectorRef, EventEmitter } from '@angular/core';
declare type StencilComponents<T extends keyof StencilElementInterfaces> = StencilElementInterfaces[T];
export declare interface IonApp extends StencilComponents<'IonApp'> {
}
export declare class IonApp {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonAvatar extends StencilComponents<'IonAvatar'> {
}
export declare class IonAvatar {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonBackButton extends StencilComponents<'IonBackButton'> {
}
export declare class IonBackButton {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonBackdrop extends StencilComponents<'IonBackdrop'> {
}
export declare class IonBackdrop {
    ionBackdropTap: EventEmitter<CustomEvent>;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonBadge extends StencilComponents<'IonBadge'> {
}
export declare class IonBadge {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonButton extends StencilComponents<'IonButton'> {
}
export declare class IonButton {
    ionFocus: EventEmitter<CustomEvent>;
    ionBlur: EventEmitter<CustomEvent>;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonButtons extends StencilComponents<'IonButtons'> {
}
export declare class IonButtons {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonCard extends StencilComponents<'IonCard'> {
}
export declare class IonCard {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonCardContent extends StencilComponents<'IonCardContent'> {
}
export declare class IonCardContent {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonCardHeader extends StencilComponents<'IonCardHeader'> {
}
export declare class IonCardHeader {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonCardSubtitle extends StencilComponents<'IonCardSubtitle'> {
}
export declare class IonCardSubtitle {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonCardTitle extends StencilComponents<'IonCardTitle'> {
}
export declare class IonCardTitle {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonCheckbox extends StencilComponents<'IonCheckbox'> {
}
export declare class IonCheckbox {
    ionChange: EventEmitter<CustomEvent>;
    ionFocus: EventEmitter<CustomEvent>;
    ionBlur: EventEmitter<CustomEvent>;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonChip extends StencilComponents<'IonChip'> {
}
export declare class IonChip {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonCol extends StencilComponents<'IonCol'> {
}
export declare class IonCol {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonContent extends StencilComponents<'IonContent'> {
}
export declare class IonContent {
    ionScrollStart: EventEmitter<CustomEvent>;
    ionScroll: EventEmitter<CustomEvent>;
    ionScrollEnd: EventEmitter<CustomEvent>;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonDatetime extends StencilComponents<'IonDatetime'> {
}
export declare class IonDatetime {
    ionCancel: EventEmitter<CustomEvent>;
    ionChange: EventEmitter<CustomEvent>;
    ionFocus: EventEmitter<CustomEvent>;
    ionBlur: EventEmitter<CustomEvent>;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonFab extends StencilComponents<'IonFab'> {
}
export declare class IonFab {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonFabButton extends StencilComponents<'IonFabButton'> {
}
export declare class IonFabButton {
    ionFocus: EventEmitter<CustomEvent>;
    ionBlur: EventEmitter<CustomEvent>;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonFabList extends StencilComponents<'IonFabList'> {
}
export declare class IonFabList {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonFooter extends StencilComponents<'IonFooter'> {
}
export declare class IonFooter {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonGrid extends StencilComponents<'IonGrid'> {
}
export declare class IonGrid {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonHeader extends StencilComponents<'IonHeader'> {
}
export declare class IonHeader {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonIcon extends StencilComponents<'IonIcon'> {
}
export declare class IonIcon {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonImg extends StencilComponents<'IonImg'> {
}
export declare class IonImg {
    ionImgDidLoad: EventEmitter<CustomEvent>;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonInfiniteScroll extends StencilComponents<'IonInfiniteScroll'> {
}
export declare class IonInfiniteScroll {
    ionInfinite: EventEmitter<CustomEvent>;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonInfiniteScrollContent extends StencilComponents<'IonInfiniteScrollContent'> {
}
export declare class IonInfiniteScrollContent {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonInput extends StencilComponents<'IonInput'> {
}
export declare class IonInput {
    ionInput: EventEmitter<CustomEvent>;
    ionChange: EventEmitter<CustomEvent>;
    ionBlur: EventEmitter<CustomEvent>;
    ionFocus: EventEmitter<CustomEvent>;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonItem extends StencilComponents<'IonItem'> {
}
export declare class IonItem {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonItemDivider extends StencilComponents<'IonItemDivider'> {
}
export declare class IonItemDivider {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonItemGroup extends StencilComponents<'IonItemGroup'> {
}
export declare class IonItemGroup {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonItemOption extends StencilComponents<'IonItemOption'> {
}
export declare class IonItemOption {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonItemOptions extends StencilComponents<'IonItemOptions'> {
}
export declare class IonItemOptions {
    ionSwipe: EventEmitter<CustomEvent>;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonItemSliding extends StencilComponents<'IonItemSliding'> {
}
export declare class IonItemSliding {
    ionDrag: EventEmitter<CustomEvent>;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonLabel extends StencilComponents<'IonLabel'> {
}
export declare class IonLabel {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonList extends StencilComponents<'IonList'> {
}
export declare class IonList {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonListHeader extends StencilComponents<'IonListHeader'> {
}
export declare class IonListHeader {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonMenu extends StencilComponents<'IonMenu'> {
}
export declare class IonMenu {
    ionWillOpen: EventEmitter<CustomEvent>;
    ionWillClose: EventEmitter<CustomEvent>;
    ionDidOpen: EventEmitter<CustomEvent>;
    ionDidClose: EventEmitter<CustomEvent>;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonMenuButton extends StencilComponents<'IonMenuButton'> {
}
export declare class IonMenuButton {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonMenuToggle extends StencilComponents<'IonMenuToggle'> {
}
export declare class IonMenuToggle {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonNav extends StencilComponents<'IonNav'> {
}
export declare class IonNav {
    ionNavWillChange: EventEmitter<CustomEvent>;
    ionNavDidChange: EventEmitter<CustomEvent>;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonNavPop extends StencilComponents<'IonNavPop'> {
}
export declare class IonNavPop {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonNavPush extends StencilComponents<'IonNavPush'> {
}
export declare class IonNavPush {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonNavSetRoot extends StencilComponents<'IonNavSetRoot'> {
}
export declare class IonNavSetRoot {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonNote extends StencilComponents<'IonNote'> {
}
export declare class IonNote {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonProgressBar extends StencilComponents<'IonProgressBar'> {
}
export declare class IonProgressBar {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonRadio extends StencilComponents<'IonRadio'> {
}
export declare class IonRadio {
    ionSelect: EventEmitter<CustomEvent>;
    ionFocus: EventEmitter<CustomEvent>;
    ionBlur: EventEmitter<CustomEvent>;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonRadioGroup extends StencilComponents<'IonRadioGroup'> {
}
export declare class IonRadioGroup {
    ionChange: EventEmitter<CustomEvent>;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonRange extends StencilComponents<'IonRange'> {
}
export declare class IonRange {
    ionChange: EventEmitter<CustomEvent>;
    ionFocus: EventEmitter<CustomEvent>;
    ionBlur: EventEmitter<CustomEvent>;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonRefresher extends StencilComponents<'IonRefresher'> {
}
export declare class IonRefresher {
    ionRefresh: EventEmitter<CustomEvent>;
    ionPull: EventEmitter<CustomEvent>;
    ionStart: EventEmitter<CustomEvent>;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonRefresherContent extends StencilComponents<'IonRefresherContent'> {
}
export declare class IonRefresherContent {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonReorder extends StencilComponents<'IonReorder'> {
}
export declare class IonReorder {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonReorderGroup extends StencilComponents<'IonReorderGroup'> {
}
export declare class IonReorderGroup {
    ionItemReorder: EventEmitter<CustomEvent>;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonRippleEffect extends StencilComponents<'IonRippleEffect'> {
}
export declare class IonRippleEffect {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonRow extends StencilComponents<'IonRow'> {
}
export declare class IonRow {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonSearchbar extends StencilComponents<'IonSearchbar'> {
}
export declare class IonSearchbar {
    ionInput: EventEmitter<CustomEvent>;
    ionChange: EventEmitter<CustomEvent>;
    ionCancel: EventEmitter<CustomEvent>;
    ionClear: EventEmitter<CustomEvent>;
    ionBlur: EventEmitter<CustomEvent>;
    ionFocus: EventEmitter<CustomEvent>;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonSegment extends StencilComponents<'IonSegment'> {
}
export declare class IonSegment {
    ionChange: EventEmitter<CustomEvent>;
    ionStyle: EventEmitter<CustomEvent>;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonSegmentButton extends StencilComponents<'IonSegmentButton'> {
}
export declare class IonSegmentButton {
    ionSelect: EventEmitter<CustomEvent>;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonSelect extends StencilComponents<'IonSelect'> {
}
export declare class IonSelect {
    ionChange: EventEmitter<CustomEvent>;
    ionCancel: EventEmitter<CustomEvent>;
    ionFocus: EventEmitter<CustomEvent>;
    ionBlur: EventEmitter<CustomEvent>;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonSelectOption extends StencilComponents<'IonSelectOption'> {
}
export declare class IonSelectOption {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonSkeletonText extends StencilComponents<'IonSkeletonText'> {
}
export declare class IonSkeletonText {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonSlide extends StencilComponents<'IonSlide'> {
}
export declare class IonSlide {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonSlides extends StencilComponents<'IonSlides'> {
}
export declare class IonSlides {
    ionSlidesDidLoad: EventEmitter<CustomEvent>;
    ionSlideTap: EventEmitter<CustomEvent>;
    ionSlideDoubleTap: EventEmitter<CustomEvent>;
    ionSlideWillChange: EventEmitter<CustomEvent>;
    ionSlideDidChange: EventEmitter<CustomEvent>;
    ionSlideNextStart: EventEmitter<CustomEvent>;
    ionSlidePrevStart: EventEmitter<CustomEvent>;
    ionSlideNextEnd: EventEmitter<CustomEvent>;
    ionSlidePrevEnd: EventEmitter<CustomEvent>;
    ionSlideTransitionStart: EventEmitter<CustomEvent>;
    ionSlideTransitionEnd: EventEmitter<CustomEvent>;
    ionSlideDrag: EventEmitter<CustomEvent>;
    ionSlideReachStart: EventEmitter<CustomEvent>;
    ionSlideReachEnd: EventEmitter<CustomEvent>;
    ionSlideTouchStart: EventEmitter<CustomEvent>;
    ionSlideTouchEnd: EventEmitter<CustomEvent>;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonSpinner extends StencilComponents<'IonSpinner'> {
}
export declare class IonSpinner {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonSplitPane extends StencilComponents<'IonSplitPane'> {
}
export declare class IonSplitPane {
    ionSplitPaneVisible: EventEmitter<CustomEvent>;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonTabBar extends StencilComponents<'IonTabBar'> {
}
export declare class IonTabBar {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonTabButton extends StencilComponents<'IonTabButton'> {
}
export declare class IonTabButton {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonText extends StencilComponents<'IonText'> {
}
export declare class IonText {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonTextarea extends StencilComponents<'IonTextarea'> {
}
export declare class IonTextarea {
    ionChange: EventEmitter<CustomEvent>;
    ionInput: EventEmitter<CustomEvent>;
    ionBlur: EventEmitter<CustomEvent>;
    ionFocus: EventEmitter<CustomEvent>;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonThumbnail extends StencilComponents<'IonThumbnail'> {
}
export declare class IonThumbnail {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonTitle extends StencilComponents<'IonTitle'> {
}
export declare class IonTitle {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonToggle extends StencilComponents<'IonToggle'> {
}
export declare class IonToggle {
    ionChange: EventEmitter<CustomEvent>;
    ionFocus: EventEmitter<CustomEvent>;
    ionBlur: EventEmitter<CustomEvent>;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export declare interface IonToolbar extends StencilComponents<'IonToolbar'> {
}
export declare class IonToolbar {
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef);
}
export {};
