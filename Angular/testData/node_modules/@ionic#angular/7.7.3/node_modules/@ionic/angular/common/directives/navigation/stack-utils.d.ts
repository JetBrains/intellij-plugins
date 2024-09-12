import { ComponentRef } from '@angular/core';
import { ActivatedRoute, NavigationExtras, Router } from '@angular/router';
import type { AnimationBuilder, NavDirection, RouterDirection } from '@ionic/core/components';
export declare const insertView: (views: RouteView[], view: RouteView, direction: RouterDirection) => RouteView[];
export declare const getUrl: (router: Router, activatedRoute: ActivatedRoute) => string;
export declare const isTabSwitch: (enteringView: RouteView, leavingView: RouteView | undefined) => boolean;
export declare const computeStackId: (prefixUrl: string[] | undefined, url: string) => string | undefined;
export declare const toSegments: (path: string) => string[];
export declare const destroyView: (view: RouteView | undefined) => void;
export interface StackWillChangeEvent {
    enteringView: RouteView;
    /**
     * `true` if the event is trigged as a result of a switch
     * between tab navigation stacks.
     */
    tabSwitch: boolean;
}
export interface StackDidChangeEvent {
    enteringView: RouteView;
    direction: RouterDirection;
    animation: NavDirection | undefined;
    /**
     * `true` if the event is trigged as a result of a switch
     * between tab navigation stacks.
     */
    tabSwitch: boolean;
}
export interface RouteView {
    id: number;
    url: string;
    stackId: string | undefined;
    element: HTMLElement;
    ref: ComponentRef<any>;
    savedData?: any;
    savedExtras?: NavigationExtras;
    unlistenEvents: () => void;
    animationBuilder?: AnimationBuilder;
}
