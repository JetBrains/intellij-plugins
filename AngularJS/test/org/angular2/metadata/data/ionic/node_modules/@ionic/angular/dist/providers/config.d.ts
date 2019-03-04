import { InjectionToken } from '@angular/core';
import { IonicConfig } from '@ionic/core';
export declare class Config {
    get(key: keyof IonicConfig, fallback?: any): any;
    getBoolean(key: keyof IonicConfig, fallback?: boolean): boolean;
    getNumber(key: keyof IonicConfig, fallback?: number): number;
    set(key: keyof IonicConfig, value?: any): void;
}
export declare const ConfigToken: InjectionToken<any>;
