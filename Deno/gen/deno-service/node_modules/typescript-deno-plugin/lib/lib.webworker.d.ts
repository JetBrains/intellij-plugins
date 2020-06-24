// Copyright 2019-2020 justjavac. All rights reserved. MIT license.

/// <reference no-default-lib="true" />

declare function onerror(e: ErrorEvent): void;
declare function onmessage(e: MessageEvent): void;
declare function onmessageerror(e: MessageEvent): void;
declare function postMessage(message: any, transfer: ArrayBuffer[]): void;
declare function postMessage(message: any, options?: PostMessageOptions): void;
