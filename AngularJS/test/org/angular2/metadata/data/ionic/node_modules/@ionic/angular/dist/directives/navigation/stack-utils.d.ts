import { ComponentRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NavDirection, RouterDirection } from '@ionic/core';
export declare function insertView(views: RouteView[], view: RouteView, direction: RouterDirection): RouteView[];
export declare function getUrl(router: Router, activatedRoute: ActivatedRoute): string;
export declare function isTabSwitch(enteringView: RouteView, leavingView: RouteView | undefined): boolean;
export declare function computeStackId(prefixUrl: string[] | undefined, url: string): string | undefined;
export declare function toSegments(path: string): string[];
export declare function destroyView(view: RouteView | undefined): void;
export interface StackEvent {
    enteringView: RouteView;
    direction: RouterDirection;
    animation: NavDirection | undefined;
    tabSwitch: boolean;
}
export interface RouteView {
    id: number;
    url: string;
    stackId: string | undefined;
    element: HTMLElement;
    ref: ComponentRef<any>;
    savedData?: any;
    unlistenEvents: () => void;
}
