import { InjectionToken } from '@angular/core';
import { DocumentRef, WindowRef } from '../../utils/browser-globals';
import { MapsAPILoader } from './maps-api-loader';
export declare enum GoogleMapsScriptProtocol {
    HTTP = 1,
    HTTPS = 2,
    AUTO = 3,
}
/**
 * Token for the config of the LazyMapsAPILoader. Please provide an object of type {@link
 * LazyMapsAPILoaderConfig}.
 */
export declare const LAZY_MAPS_API_CONFIG: InjectionToken<LazyMapsAPILoaderConfigLiteral>;
/**
 * Configuration for the {@link LazyMapsAPILoader}.
 */
export interface LazyMapsAPILoaderConfigLiteral {
    /**
     * The Google Maps API Key (see:
     * https://developers.google.com/maps/documentation/javascript/get-api-key)
     */
    apiKey?: string;
    /**
     * The Google Maps client ID (for premium plans).
     * When you have a Google Maps APIs Premium Plan license, you must authenticate
     * your application with either an API key or a client ID.
     * The Google Maps API will fail to load if both a client ID and an API key are included.
     */
    clientId?: string;
    /**
     * The Google Maps channel name (for premium plans).
     * A channel parameter is an optional parameter that allows you to track usage under your client
     * ID by assigning a distinct channel to each of your applications.
     */
    channel?: string;
    /**
     * Google Maps API version.
     */
    apiVersion?: string;
    /**
     * Host and Path used for the `<script>` tag.
     */
    hostAndPath?: string;
    /**
     * Protocol used for the `<script>` tag.
     */
    protocol?: GoogleMapsScriptProtocol;
    /**
     * Defines which Google Maps libraries should get loaded.
     */
    libraries?: string[];
    /**
     * The default bias for the map behavior is US.
     * If you wish to alter your application to serve different map tiles or bias the
     * application, you can overwrite the default behavior (US) by defining a `region`.
     * See https://developers.google.com/maps/documentation/javascript/basics#Region
     */
    region?: string;
    /**
     * The Google Maps API uses the browser's preferred language when displaying
     * textual information. If you wish to overwrite this behavior and force the API
     * to use a given language, you can use this setting.
     * See https://developers.google.com/maps/documentation/javascript/basics#Language
     */
    language?: string;
}
export declare class LazyMapsAPILoader extends MapsAPILoader {
    protected _scriptLoadingPromise: Promise<void>;
    protected _config: LazyMapsAPILoaderConfigLiteral;
    protected _windowRef: WindowRef;
    protected _documentRef: DocumentRef;
    protected readonly _SCRIPT_ID: string;
    protected readonly callbackName: string;
    constructor(config: any, w: WindowRef, d: DocumentRef);
    load(): Promise<void>;
    private _assignScriptLoadingPromise(scriptElem);
    protected _getScriptSrc(callbackName: string): string;
}
