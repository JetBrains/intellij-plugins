import { createRequire } from "node:module";
import { readFileSync } from "node:fs";
// \0rolldown/runtime.js
var __defProp = Object.defineProperty;
var __commonJSMin = (cb, mod) => () => (mod || (cb((mod = { exports: {} }).exports, mod, mod.exports), cb = null), mod.exports);
var __exportAll = (all, flags) => {
	let target = Object.setPrototypeOf({}, null);
	for (var name in all) __defProp(target, name, {
		get: all[name],
		enumerable: true
	});
	if (!(flags & 1)) __defProp(target, Symbol.toStringTag, { value: "Module" });
	return flags & 2 ? target : Object.preventExtensions(target);
};
var __require = /* #__PURE__ */ (() => createRequire(import.meta.url))();
// node_modules/vscode-jsonrpc/lib/common/is.js
var require_is = /* @__PURE__ */ __commonJSMin(((exports) => {
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.boolean = boolean;
	exports.string = string;
	exports.number = number;
	exports.error = error;
	exports.func = func;
	exports.array = array;
	exports.stringArray = stringArray;
	function boolean(value) {
		return value === true || value === false;
	}
	function string(value) {
		return typeof value === "string" || value instanceof String;
	}
	function number(value) {
		return typeof value === "number" || value instanceof Number;
	}
	function error(value) {
		return value instanceof Error;
	}
	function func(value) {
		return typeof value === "function";
	}
	function array(value) {
		return Array.isArray(value);
	}
	function stringArray(value) {
		return array(value) && value.every((elem) => string(elem));
	}
}));
// node_modules/vscode-jsonrpc/lib/common/messages.js
var require_messages = /* @__PURE__ */ __commonJSMin(((exports, module, this$9) => {
	var __createBinding = this$9 && this$9.__createBinding || (Object.create ? (function(o, m, k, k2) {
		if (k2 === void 0) k2 = k;
		var desc = Object.getOwnPropertyDescriptor(m, k);
		if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) desc = {
			enumerable: true,
			get: function() {
				return m[k];
			}
		};
		Object.defineProperty(o, k2, desc);
	}) : (function(o, m, k, k2) {
		if (k2 === void 0) k2 = k;
		o[k2] = m[k];
	}));
	var __setModuleDefault = this$9 && this$9.__setModuleDefault || (Object.create ? (function(o, v) {
		Object.defineProperty(o, "default", {
			enumerable: true,
			value: v
		});
	}) : function(o, v) {
		o["default"] = v;
	});
	var __importStar = this$9 && this$9.__importStar || (function() {
		var ownKeys = function(o) {
			ownKeys = Object.getOwnPropertyNames || function(o) {
				var ar = [];
				for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
				return ar;
			};
			return ownKeys(o);
		};
		return function(mod) {
			if (mod && mod.__esModule) return mod;
			var result = {};
			if (mod != null) {
				for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
			}
			__setModuleDefault(result, mod);
			return result;
		};
	})();
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.Message = exports.NotificationType9 = exports.NotificationType8 = exports.NotificationType7 = exports.NotificationType6 = exports.NotificationType5 = exports.NotificationType4 = exports.NotificationType3 = exports.NotificationType2 = exports.NotificationType1 = exports.NotificationType0 = exports.NotificationType = exports.RequestType9 = exports.RequestType8 = exports.RequestType7 = exports.RequestType6 = exports.RequestType5 = exports.RequestType4 = exports.RequestType3 = exports.RequestType2 = exports.RequestType1 = exports.RequestType = exports.RequestType0 = exports.AbstractMessageSignature = exports.ParameterStructures = exports.ResponseError = exports.ErrorCodes = void 0;
	const is = __importStar(require_is());
	/**
	* Predefined error codes.
	*/
	var ErrorCodes;
	(function(ErrorCodes) {
		ErrorCodes.ParseError = -32700;
		ErrorCodes.InvalidRequest = -32600;
		ErrorCodes.MethodNotFound = -32601;
		ErrorCodes.InvalidParams = -32602;
		ErrorCodes.InternalError = -32603;
		/**
		* This is the start range of JSON RPC reserved error codes.
		* It doesn't denote a real error code. No application error codes should
		* be defined between the start and end range. For backwards
		* compatibility the `ServerNotInitialized` and the `UnknownErrorCode`
		* are left in the range.
		*
		* @since 3.16.0
		*/
		ErrorCodes.jsonrpcReservedErrorRangeStart = -32099;
		/** @deprecated use  jsonrpcReservedErrorRangeStart */
		ErrorCodes.serverErrorStart = -32099;
		/**
		* An error occurred when write a message to the transport layer.
		*/
		ErrorCodes.MessageWriteError = -32099;
		/**
		* An error occurred when reading a message from the transport layer.
		*/
		ErrorCodes.MessageReadError = -32098;
		/**
		* The connection got disposed or lost and all pending responses got
		* rejected.
		*/
		ErrorCodes.PendingResponseRejected = -32097;
		/**
		* The connection is inactive and a use of it failed.
		*/
		ErrorCodes.ConnectionInactive = -32096;
		/**
		* Error code indicating that a server received a notification or
		* request before the server has received the `initialize` request.
		*/
		ErrorCodes.ServerNotInitialized = -32002;
		ErrorCodes.UnknownErrorCode = -32001;
		/**
		* This is the end range of JSON RPC reserved error codes.
		* It doesn't denote a real error code.
		*
		* @since 3.16.0
		*/
		ErrorCodes.jsonrpcReservedErrorRangeEnd = -32e3;
		/** @deprecated use  jsonrpcReservedErrorRangeEnd */
		ErrorCodes.serverErrorEnd = -32e3;
	})(ErrorCodes || (exports.ErrorCodes = ErrorCodes = {}));
	exports.ResponseError = class ResponseError extends Error {
		code;
		data;
		constructor(code, message, data) {
			super(message);
			this.code = is.number(code) ? code : ErrorCodes.UnknownErrorCode;
			this.data = data;
			Object.setPrototypeOf(this, ResponseError.prototype);
		}
		toJson() {
			const result = {
				code: this.code,
				message: this.message
			};
			if (this.data !== void 0) result.data = this.data;
			return result;
		}
	};
	var ParameterStructures = class ParameterStructures {
		kind;
		/**
		* The parameter structure is automatically inferred on the number of parameters
		* and the parameter type in case of a single param.
		*/
		static auto = new ParameterStructures("auto");
		/**
		* Forces `byPosition` parameter structure. This is useful if you have a single
		* parameter which has a literal type.
		*/
		static byPosition = new ParameterStructures("byPosition");
		/**
		* Forces `byName` parameter structure. This is only useful when having a single
		* parameter. The library will report errors if used with a different number of
		* parameters.
		*/
		static byName = new ParameterStructures("byName");
		constructor(kind) {
			this.kind = kind;
		}
		static is(value) {
			return value === ParameterStructures.auto || value === ParameterStructures.byName || value === ParameterStructures.byPosition;
		}
		toString() {
			return this.kind;
		}
	};
	exports.ParameterStructures = ParameterStructures;
	/**
	* An abstract implementation of a MessageType.
	*/
	var AbstractMessageSignature = class {
		method;
		numberOfParams;
		constructor(method, numberOfParams) {
			this.method = method;
			this.numberOfParams = numberOfParams;
		}
		get parameterStructures() {
			return ParameterStructures.auto;
		}
	};
	exports.AbstractMessageSignature = AbstractMessageSignature;
	/**
	* Classes to type request response pairs
	*/
	var RequestType0 = class extends AbstractMessageSignature {
		/**
		* Clients must not use this property. It is here to ensure correct typing.
		*/
		_;
		constructor(method) {
			super(method, 0);
		}
	};
	exports.RequestType0 = RequestType0;
	var RequestType = class extends AbstractMessageSignature {
		_parameterStructures;
		/**
		* Clients must not use this property. It is here to ensure correct typing.
		*/
		_;
		constructor(method, _parameterStructures = ParameterStructures.auto) {
			super(method, 1);
			this._parameterStructures = _parameterStructures;
		}
		get parameterStructures() {
			return this._parameterStructures;
		}
	};
	exports.RequestType = RequestType;
	var RequestType1 = class extends AbstractMessageSignature {
		_parameterStructures;
		/**
		* Clients must not use this property. It is here to ensure correct typing.
		*/
		_;
		constructor(method, _parameterStructures = ParameterStructures.auto) {
			super(method, 1);
			this._parameterStructures = _parameterStructures;
		}
		get parameterStructures() {
			return this._parameterStructures;
		}
	};
	exports.RequestType1 = RequestType1;
	var RequestType2 = class extends AbstractMessageSignature {
		/**
		* Clients must not use this property. It is here to ensure correct typing.
		*/
		_;
		constructor(method) {
			super(method, 2);
		}
	};
	exports.RequestType2 = RequestType2;
	var RequestType3 = class extends AbstractMessageSignature {
		/**
		* Clients must not use this property. It is here to ensure correct typing.
		*/
		_;
		constructor(method) {
			super(method, 3);
		}
	};
	exports.RequestType3 = RequestType3;
	var RequestType4 = class extends AbstractMessageSignature {
		/**
		* Clients must not use this property. It is here to ensure correct typing.
		*/
		_;
		constructor(method) {
			super(method, 4);
		}
	};
	exports.RequestType4 = RequestType4;
	var RequestType5 = class extends AbstractMessageSignature {
		/**
		* Clients must not use this property. It is here to ensure correct typing.
		*/
		_;
		constructor(method) {
			super(method, 5);
		}
	};
	exports.RequestType5 = RequestType5;
	var RequestType6 = class extends AbstractMessageSignature {
		/**
		* Clients must not use this property. It is here to ensure correct typing.
		*/
		_;
		constructor(method) {
			super(method, 6);
		}
	};
	exports.RequestType6 = RequestType6;
	var RequestType7 = class extends AbstractMessageSignature {
		/**
		* Clients must not use this property. It is here to ensure correct typing.
		*/
		_;
		constructor(method) {
			super(method, 7);
		}
	};
	exports.RequestType7 = RequestType7;
	var RequestType8 = class extends AbstractMessageSignature {
		/**
		* Clients must not use this property. It is here to ensure correct typing.
		*/
		_;
		constructor(method) {
			super(method, 8);
		}
	};
	exports.RequestType8 = RequestType8;
	var RequestType9 = class extends AbstractMessageSignature {
		/**
		* Clients must not use this property. It is here to ensure correct typing.
		*/
		_;
		constructor(method) {
			super(method, 9);
		}
	};
	exports.RequestType9 = RequestType9;
	var NotificationType = class extends AbstractMessageSignature {
		_parameterStructures;
		/**
		* Clients must not use this property. It is here to ensure correct typing.
		*/
		_;
		constructor(method, _parameterStructures = ParameterStructures.auto) {
			super(method, 1);
			this._parameterStructures = _parameterStructures;
		}
		get parameterStructures() {
			return this._parameterStructures;
		}
	};
	exports.NotificationType = NotificationType;
	var NotificationType0 = class extends AbstractMessageSignature {
		/**
		* Clients must not use this property. It is here to ensure correct typing.
		*/
		_;
		constructor(method) {
			super(method, 0);
		}
	};
	exports.NotificationType0 = NotificationType0;
	var NotificationType1 = class extends AbstractMessageSignature {
		_parameterStructures;
		/**
		* Clients must not use this property. It is here to ensure correct typing.
		*/
		_;
		constructor(method, _parameterStructures = ParameterStructures.auto) {
			super(method, 1);
			this._parameterStructures = _parameterStructures;
		}
		get parameterStructures() {
			return this._parameterStructures;
		}
	};
	exports.NotificationType1 = NotificationType1;
	var NotificationType2 = class extends AbstractMessageSignature {
		/**
		* Clients must not use this property. It is here to ensure correct typing.
		*/
		_;
		constructor(method) {
			super(method, 2);
		}
	};
	exports.NotificationType2 = NotificationType2;
	var NotificationType3 = class extends AbstractMessageSignature {
		/**
		* Clients must not use this property. It is here to ensure correct typing.
		*/
		_;
		constructor(method) {
			super(method, 3);
		}
	};
	exports.NotificationType3 = NotificationType3;
	var NotificationType4 = class extends AbstractMessageSignature {
		/**
		* Clients must not use this property. It is here to ensure correct typing.
		*/
		_;
		constructor(method) {
			super(method, 4);
		}
	};
	exports.NotificationType4 = NotificationType4;
	var NotificationType5 = class extends AbstractMessageSignature {
		/**
		* Clients must not use this property. It is here to ensure correct typing.
		*/
		_;
		constructor(method) {
			super(method, 5);
		}
	};
	exports.NotificationType5 = NotificationType5;
	var NotificationType6 = class extends AbstractMessageSignature {
		/**
		* Clients must not use this property. It is here to ensure correct typing.
		*/
		_;
		constructor(method) {
			super(method, 6);
		}
	};
	exports.NotificationType6 = NotificationType6;
	var NotificationType7 = class extends AbstractMessageSignature {
		/**
		* Clients must not use this property. It is here to ensure correct typing.
		*/
		_;
		constructor(method) {
			super(method, 7);
		}
	};
	exports.NotificationType7 = NotificationType7;
	var NotificationType8 = class extends AbstractMessageSignature {
		/**
		* Clients must not use this property. It is here to ensure correct typing.
		*/
		_;
		constructor(method) {
			super(method, 8);
		}
	};
	exports.NotificationType8 = NotificationType8;
	var NotificationType9 = class extends AbstractMessageSignature {
		/**
		* Clients must not use this property. It is here to ensure correct typing.
		*/
		_;
		constructor(method) {
			super(method, 9);
		}
	};
	exports.NotificationType9 = NotificationType9;
	var Message;
	(function(Message) {
		/**
		* Tests if the given message is a request message
		*/
		function isRequest(message) {
			const candidate = message;
			return candidate && is.string(candidate.method) && (is.string(candidate.id) || is.number(candidate.id));
		}
		Message.isRequest = isRequest;
		/**
		* Tests if the given message is a notification message
		*/
		function isNotification(message) {
			const candidate = message;
			return candidate && is.string(candidate.method) && message.id === void 0;
		}
		Message.isNotification = isNotification;
		/**
		* Tests if the given message is a response message
		*/
		function isResponse(message) {
			const candidate = message;
			return candidate && (candidate.result !== void 0 || !!candidate.error) && (is.string(candidate.id) || is.number(candidate.id) || candidate.id === null);
		}
		Message.isResponse = isResponse;
	})(Message || (exports.Message = Message = {}));
}));
// node_modules/vscode-jsonrpc/lib/common/linkedMap.js
var require_linkedMap = /* @__PURE__ */ __commonJSMin(((exports) => {
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.LRUCache = exports.LinkedMap = exports.Touch = void 0;
	var Touch;
	(function(Touch) {
		Touch.None = 0;
		Touch.First = 1;
		Touch.AsOld = Touch.First;
		Touch.Last = 2;
		Touch.AsNew = Touch.Last;
	})(Touch || (exports.Touch = Touch = {}));
	var LinkedMap = class {
		[Symbol.toStringTag] = "LinkedMap";
		_map;
		_head;
		_tail;
		_size;
		_state;
		constructor() {
			this._map = /* @__PURE__ */ new Map();
			this._head = void 0;
			this._tail = void 0;
			this._size = 0;
			this._state = 0;
		}
		clear() {
			this._map.clear();
			this._head = void 0;
			this._tail = void 0;
			this._size = 0;
			this._state++;
		}
		isEmpty() {
			return !this._head && !this._tail;
		}
		get size() {
			return this._size;
		}
		get first() {
			return this._head?.value;
		}
		get last() {
			return this._tail?.value;
		}
		before(key) {
			const item = this._map.get(key);
			return item ? item.previous?.value : void 0;
		}
		after(key) {
			const item = this._map.get(key);
			return item ? item.next?.value : void 0;
		}
		has(key) {
			return this._map.has(key);
		}
		get(key, touch = Touch.None) {
			const item = this._map.get(key);
			if (!item) return;
			if (touch !== Touch.None) this.touch(item, touch);
			return item.value;
		}
		set(key, value, touch = Touch.None) {
			let item = this._map.get(key);
			if (item) {
				item.value = value;
				if (touch !== Touch.None) this.touch(item, touch);
			} else {
				item = {
					key,
					value,
					next: void 0,
					previous: void 0
				};
				switch (touch) {
					case Touch.None:
						this.addItemLast(item);
						break;
					case Touch.First:
						this.addItemFirst(item);
						break;
					case Touch.Last:
						this.addItemLast(item);
						break;
					default: this.addItemLast(item);
				}
				this._map.set(key, item);
				this._size++;
			}
			return this;
		}
		delete(key) {
			return !!this.remove(key);
		}
		remove(key) {
			const item = this._map.get(key);
			if (!item) return;
			this._map.delete(key);
			this.removeItem(item);
			this._size--;
			return item.value;
		}
		shift() {
			if (!this._head && !this._tail) return;
			if (!this._head || !this._tail) throw new Error("Invalid list");
			const item = this._head;
			this._map.delete(item.key);
			this.removeItem(item);
			this._size--;
			return item.value;
		}
		forEach(callbackfn, thisArg) {
			const state = this._state;
			let current = this._head;
			while (current) {
				if (thisArg) callbackfn.bind(thisArg)(current.value, current.key, this);
				else callbackfn(current.value, current.key, this);
				if (this._state !== state) throw new Error(`LinkedMap got modified during iteration.`);
				current = current.next;
			}
		}
		keys() {
			const state = this._state;
			let current = this._head;
			const iterator = {
				[Symbol.iterator]: () => {
					return iterator;
				},
				next: () => {
					if (this._state !== state) throw new Error(`LinkedMap got modified during iteration.`);
					if (current) {
						const result = {
							value: current.key,
							done: false
						};
						current = current.next;
						return result;
					} else return {
						value: void 0,
						done: true
					};
				}
			};
			return iterator;
		}
		values() {
			const state = this._state;
			let current = this._head;
			const iterator = {
				[Symbol.iterator]: () => {
					return iterator;
				},
				next: () => {
					if (this._state !== state) throw new Error(`LinkedMap got modified during iteration.`);
					if (current) {
						const result = {
							value: current.value,
							done: false
						};
						current = current.next;
						return result;
					} else return {
						value: void 0,
						done: true
					};
				}
			};
			return iterator;
		}
		entries() {
			const state = this._state;
			let current = this._head;
			const iterator = {
				[Symbol.iterator]: () => {
					return iterator;
				},
				next: () => {
					if (this._state !== state) throw new Error(`LinkedMap got modified during iteration.`);
					if (current) {
						const result = {
							value: [current.key, current.value],
							done: false
						};
						current = current.next;
						return result;
					} else return {
						value: void 0,
						done: true
					};
				}
			};
			return iterator;
		}
		[Symbol.iterator]() {
			return this.entries();
		}
		trimOld(newSize) {
			if (newSize >= this.size) return;
			if (newSize === 0) {
				this.clear();
				return;
			}
			let current = this._head;
			let currentSize = this.size;
			while (current && currentSize > newSize) {
				this._map.delete(current.key);
				current = current.next;
				currentSize--;
			}
			this._head = current;
			this._size = currentSize;
			if (current) current.previous = void 0;
			this._state++;
		}
		addItemFirst(item) {
			if (!this._head && !this._tail) this._tail = item;
			else if (!this._head) throw new Error("Invalid list");
			else {
				item.next = this._head;
				this._head.previous = item;
			}
			this._head = item;
			this._state++;
		}
		addItemLast(item) {
			if (!this._head && !this._tail) this._head = item;
			else if (!this._tail) throw new Error("Invalid list");
			else {
				item.previous = this._tail;
				this._tail.next = item;
			}
			this._tail = item;
			this._state++;
		}
		removeItem(item) {
			if (item === this._head && item === this._tail) {
				this._head = void 0;
				this._tail = void 0;
			} else if (item === this._head) {
				if (!item.next) throw new Error("Invalid list");
				item.next.previous = void 0;
				this._head = item.next;
			} else if (item === this._tail) {
				if (!item.previous) throw new Error("Invalid list");
				item.previous.next = void 0;
				this._tail = item.previous;
			} else {
				const next = item.next;
				const previous = item.previous;
				if (!next || !previous) throw new Error("Invalid list");
				next.previous = previous;
				previous.next = next;
			}
			item.next = void 0;
			item.previous = void 0;
			this._state++;
		}
		touch(item, touch) {
			if (!this._head || !this._tail) throw new Error("Invalid list");
			if (touch !== Touch.First && touch !== Touch.Last) return;
			if (touch === Touch.First) {
				if (item === this._head) return;
				const next = item.next;
				const previous = item.previous;
				if (item === this._tail) {
					previous.next = void 0;
					this._tail = previous;
				} else {
					next.previous = previous;
					previous.next = next;
				}
				item.previous = void 0;
				item.next = this._head;
				this._head.previous = item;
				this._head = item;
				this._state++;
			} else if (touch === Touch.Last) {
				if (item === this._tail) return;
				const next = item.next;
				const previous = item.previous;
				if (item === this._head) {
					next.previous = void 0;
					this._head = next;
				} else {
					next.previous = previous;
					previous.next = next;
				}
				item.next = void 0;
				item.previous = this._tail;
				this._tail.next = item;
				this._tail = item;
				this._state++;
			}
		}
		toJSON() {
			const data = [];
			this.forEach((value, key) => {
				data.push([key, value]);
			});
			return data;
		}
		fromJSON(data) {
			this.clear();
			for (const [key, value] of data) this.set(key, value);
		}
	};
	exports.LinkedMap = LinkedMap;
	var LRUCache = class extends LinkedMap {
		_limit;
		_ratio;
		constructor(limit, ratio = 1) {
			super();
			this._limit = limit;
			this._ratio = Math.min(Math.max(0, ratio), 1);
		}
		get limit() {
			return this._limit;
		}
		set limit(limit) {
			this._limit = limit;
			this.checkTrim();
		}
		get ratio() {
			return this._ratio;
		}
		set ratio(ratio) {
			this._ratio = Math.min(Math.max(0, ratio), 1);
			this.checkTrim();
		}
		get(key, touch = Touch.AsNew) {
			return super.get(key, touch);
		}
		peek(key) {
			return super.get(key, Touch.None);
		}
		set(key, value) {
			super.set(key, value, Touch.Last);
			this.checkTrim();
			return this;
		}
		checkTrim() {
			if (this.size > this._limit) this.trimOld(Math.round(this._limit * this._ratio));
		}
	};
	exports.LRUCache = LRUCache;
}));
// node_modules/vscode-jsonrpc/lib/common/disposable.js
var require_disposable = /* @__PURE__ */ __commonJSMin(((exports) => {
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.Disposable = void 0;
	var Disposable;
	(function(Disposable) {
		function create(func) {
			return { dispose: func };
		}
		Disposable.create = create;
	})(Disposable || (exports.Disposable = Disposable = {}));
}));
// node_modules/vscode-jsonrpc/lib/common/ral.js
var require_ral = /* @__PURE__ */ __commonJSMin(((exports) => {
	Object.defineProperty(exports, "__esModule", { value: true });
	let _ral;
	function RAL() {
		if (_ral === void 0) throw new Error(`No runtime abstraction layer installed`);
		return _ral;
	}
	(function(RAL) {
		function install(ral) {
			if (ral === void 0) throw new Error(`No runtime abstraction layer provided`);
			_ral = ral;
		}
		RAL.install = install;
	})(RAL || (RAL = {}));
	exports.default = RAL;
}));
// node_modules/vscode-jsonrpc/lib/common/events.js
var require_events = /* @__PURE__ */ __commonJSMin(((exports, module, this$8) => {
	var __importDefault = this$8 && this$8.__importDefault || function(mod) {
		return mod && mod.__esModule ? mod : { "default": mod };
	};
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.Emitter = exports.Event = void 0;
	const ral_1 = __importDefault(require_ral());
	var Event;
	(function(Event) {
		const _disposable = { dispose() {} };
		Event.None = function() {
			return _disposable;
		};
	})(Event || (exports.Event = Event = {}));
	var CallbackList = class {
		_callbacks;
		_contexts;
		add(callback, context = null, bucket) {
			if (!this._callbacks) {
				this._callbacks = [];
				this._contexts = [];
			}
			this._callbacks.push(callback);
			this._contexts.push(context);
			if (Array.isArray(bucket)) bucket.push({ dispose: () => this.remove(callback, context) });
		}
		remove(callback, context = null) {
			if (!this._callbacks) return;
			let foundCallbackWithDifferentContext = false;
			for (let i = 0, len = this._callbacks.length; i < len; i++) if (this._callbacks[i] === callback) {
				if (this._contexts[i] === context) {
					this._callbacks.splice(i, 1);
					this._contexts.splice(i, 1);
					return;
				} else foundCallbackWithDifferentContext = true;
			}
			if (foundCallbackWithDifferentContext) throw new Error("When adding a listener with a context, you should remove it with the same context");
		}
		invoke(...args) {
			if (!this._callbacks) return [];
			const ret = [], callbacks = this._callbacks.slice(0), contexts = this._contexts.slice(0);
			for (let i = 0, len = callbacks.length; i < len; i++) try {
				ret.push(callbacks[i].apply(contexts[i], args));
			} catch (e) {
				(0, ral_1.default)().console.error(e);
			}
			return ret;
		}
		isEmpty() {
			return !this._callbacks || this._callbacks.length === 0;
		}
		dispose() {
			this._callbacks = void 0;
			this._contexts = void 0;
		}
	};
	exports.Emitter = class Emitter {
		_options;
		static _noop = function() {};
		_event;
		_callbacks;
		constructor(_options) {
			this._options = _options;
		}
		/**
		* For the public to allow to subscribe
		* to events from this Emitter
		*/
		get event() {
			if (!this._event) this._event = (listener, thisArgs, disposables) => {
				if (!this._callbacks) this._callbacks = new CallbackList();
				if (this._options && this._options.onFirstListenerAdd && this._callbacks.isEmpty()) this._options.onFirstListenerAdd(this);
				this._callbacks.add(listener, thisArgs);
				const result = { dispose: () => {
					if (!this._callbacks) return;
					this._callbacks.remove(listener, thisArgs);
					result.dispose = Emitter._noop;
					if (this._options && this._options.onLastListenerRemove && this._callbacks.isEmpty()) this._options.onLastListenerRemove(this);
				} };
				if (Array.isArray(disposables)) disposables.push(result);
				return result;
			};
			return this._event;
		}
		/**
		* To be kept private to fire an event to
		* subscribers
		*/
		fire(event) {
			if (this._callbacks) this._callbacks.invoke.call(this._callbacks, event);
		}
		dispose() {
			if (this._callbacks) {
				this._callbacks.dispose();
				this._callbacks = void 0;
			}
		}
	};
}));
// node_modules/vscode-jsonrpc/lib/common/cancellation.js
var require_cancellation = /* @__PURE__ */ __commonJSMin(((exports, module, this$7) => {
	var __createBinding = this$7 && this$7.__createBinding || (Object.create ? (function(o, m, k, k2) {
		if (k2 === void 0) k2 = k;
		var desc = Object.getOwnPropertyDescriptor(m, k);
		if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) desc = {
			enumerable: true,
			get: function() {
				return m[k];
			}
		};
		Object.defineProperty(o, k2, desc);
	}) : (function(o, m, k, k2) {
		if (k2 === void 0) k2 = k;
		o[k2] = m[k];
	}));
	var __setModuleDefault = this$7 && this$7.__setModuleDefault || (Object.create ? (function(o, v) {
		Object.defineProperty(o, "default", {
			enumerable: true,
			value: v
		});
	}) : function(o, v) {
		o["default"] = v;
	});
	var __importStar = this$7 && this$7.__importStar || (function() {
		var ownKeys = function(o) {
			ownKeys = Object.getOwnPropertyNames || function(o) {
				var ar = [];
				for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
				return ar;
			};
			return ownKeys(o);
		};
		return function(mod) {
			if (mod && mod.__esModule) return mod;
			var result = {};
			if (mod != null) {
				for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
			}
			__setModuleDefault(result, mod);
			return result;
		};
	})();
	var __importDefault = this$7 && this$7.__importDefault || function(mod) {
		return mod && mod.__esModule ? mod : { "default": mod };
	};
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.CancellationTokenSource = exports.CancellationToken = void 0;
	const ral_1 = __importDefault(require_ral());
	const Is = __importStar(require_is());
	const events_1 = require_events();
	var CancellationToken;
	(function(CancellationToken) {
		CancellationToken.None = Object.freeze({
			isCancellationRequested: false,
			onCancellationRequested: events_1.Event.None
		});
		CancellationToken.Cancelled = Object.freeze({
			isCancellationRequested: true,
			onCancellationRequested: events_1.Event.None
		});
		function is(value) {
			const candidate = value;
			return candidate && (candidate === CancellationToken.None || candidate === CancellationToken.Cancelled || Is.boolean(candidate.isCancellationRequested) && !!candidate.onCancellationRequested);
		}
		CancellationToken.is = is;
	})(CancellationToken || (exports.CancellationToken = CancellationToken = {}));
	const shortcutEvent = Object.freeze(function(callback, context) {
		const handle = (0, ral_1.default)().timer.setTimeout(callback.bind(context), 0);
		return { dispose() {
			handle.dispose();
		} };
	});
	var MutableToken = class {
		_isCancelled = false;
		_emitter;
		cancel() {
			if (!this._isCancelled) {
				this._isCancelled = true;
				if (this._emitter) {
					this._emitter.fire(void 0);
					this.dispose();
				}
			}
		}
		get isCancellationRequested() {
			return this._isCancelled;
		}
		get onCancellationRequested() {
			if (this._isCancelled) return shortcutEvent;
			if (!this._emitter) this._emitter = new events_1.Emitter();
			return this._emitter.event;
		}
		dispose() {
			if (this._emitter) {
				this._emitter.dispose();
				this._emitter = void 0;
			}
		}
	};
	var CancellationTokenSource = class {
		_token;
		get token() {
			if (!this._token) this._token = new MutableToken();
			return this._token;
		}
		cancel() {
			if (!this._token) this._token = CancellationToken.Cancelled;
			else this._token.cancel();
		}
		dispose() {
			if (!this._token) this._token = CancellationToken.None;
			else if (this._token instanceof MutableToken) this._token.dispose();
		}
	};
	exports.CancellationTokenSource = CancellationTokenSource;
}));
// node_modules/vscode-jsonrpc/lib/common/sharedArrayCancellation.js
var require_sharedArrayCancellation = /* @__PURE__ */ __commonJSMin(((exports) => {
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.SharedArrayReceiverStrategy = exports.SharedArraySenderStrategy = void 0;
	const cancellation_1 = require_cancellation();
	var CancellationState;
	(function(CancellationState) {
		CancellationState.Continue = 0;
		CancellationState.Cancelled = 1;
	})(CancellationState || (CancellationState = {}));
	var SharedArraySenderStrategy = class {
		buffers;
		constructor() {
			this.buffers = /* @__PURE__ */ new Map();
		}
		enableCancellation(request) {
			if (request.id === null) return;
			const buffer = new SharedArrayBuffer(4);
			const data = new Int32Array(buffer, 0, 1);
			data[0] = CancellationState.Continue;
			this.buffers.set(request.id, buffer);
			request.$cancellationData = buffer;
		}
		async sendCancellation(_conn, id) {
			const buffer = this.buffers.get(id);
			if (buffer === void 0) return;
			const data = new Int32Array(buffer, 0, 1);
			Atomics.store(data, 0, CancellationState.Cancelled);
		}
		cleanup(id) {
			this.buffers.delete(id);
		}
		dispose() {
			this.buffers.clear();
		}
	};
	exports.SharedArraySenderStrategy = SharedArraySenderStrategy;
	var SharedArrayBufferCancellationToken = class {
		data;
		constructor(buffer) {
			this.data = new Int32Array(buffer, 0, 1);
		}
		get isCancellationRequested() {
			return Atomics.load(this.data, 0) === CancellationState.Cancelled;
		}
		get onCancellationRequested() {
			throw new Error(`Cancellation over SharedArrayBuffer doesn't support cancellation events`);
		}
	};
	var SharedArrayBufferCancellationTokenSource = class {
		token;
		constructor(buffer) {
			this.token = new SharedArrayBufferCancellationToken(buffer);
		}
		cancel() {}
		dispose() {}
	};
	var SharedArrayReceiverStrategy = class {
		kind = "request";
		createCancellationTokenSource(request) {
			const buffer = request.$cancellationData;
			if (buffer === void 0) return new cancellation_1.CancellationTokenSource();
			return new SharedArrayBufferCancellationTokenSource(buffer);
		}
	};
	exports.SharedArrayReceiverStrategy = SharedArrayReceiverStrategy;
}));
// node_modules/vscode-jsonrpc/lib/common/semaphore.js
var require_semaphore = /* @__PURE__ */ __commonJSMin(((exports, module, this$6) => {
	var __importDefault = this$6 && this$6.__importDefault || function(mod) {
		return mod && mod.__esModule ? mod : { "default": mod };
	};
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.Semaphore = void 0;
	const ral_1 = __importDefault(require_ral());
	var Semaphore = class {
		_capacity;
		_active;
		_waiting;
		constructor(capacity = 1) {
			if (capacity <= 0) throw new Error("Capacity must be greater than 0");
			this._capacity = capacity;
			this._active = 0;
			this._waiting = [];
		}
		lock(thunk) {
			return new Promise((resolve, reject) => {
				this._waiting.push({
					thunk,
					resolve,
					reject
				});
				this.runNext();
			});
		}
		get active() {
			return this._active;
		}
		runNext() {
			if (this._waiting.length === 0 || this._active === this._capacity) return;
			(0, ral_1.default)().timer.setImmediate(() => this.doRunNext());
		}
		doRunNext() {
			if (this._waiting.length === 0 || this._active === this._capacity) return;
			const next = this._waiting.shift();
			this._active++;
			if (this._active > this._capacity) throw new Error(`Too many thunks active`);
			try {
				const result = next.thunk();
				if (result instanceof Promise) result.then((value) => {
					this._active--;
					next.resolve(value);
					this.runNext();
				}, (err) => {
					this._active--;
					next.reject(err);
					this.runNext();
				});
				else {
					this._active--;
					next.resolve(result);
					this.runNext();
				}
			} catch (err) {
				this._active--;
				next.reject(err);
				this.runNext();
			}
		}
	};
	exports.Semaphore = Semaphore;
}));
// node_modules/vscode-jsonrpc/lib/common/messageReader.js
var require_messageReader = /* @__PURE__ */ __commonJSMin(((exports, module, this$5) => {
	var __createBinding = this$5 && this$5.__createBinding || (Object.create ? (function(o, m, k, k2) {
		if (k2 === void 0) k2 = k;
		var desc = Object.getOwnPropertyDescriptor(m, k);
		if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) desc = {
			enumerable: true,
			get: function() {
				return m[k];
			}
		};
		Object.defineProperty(o, k2, desc);
	}) : (function(o, m, k, k2) {
		if (k2 === void 0) k2 = k;
		o[k2] = m[k];
	}));
	var __setModuleDefault = this$5 && this$5.__setModuleDefault || (Object.create ? (function(o, v) {
		Object.defineProperty(o, "default", {
			enumerable: true,
			value: v
		});
	}) : function(o, v) {
		o["default"] = v;
	});
	var __importStar = this$5 && this$5.__importStar || (function() {
		var ownKeys = function(o) {
			ownKeys = Object.getOwnPropertyNames || function(o) {
				var ar = [];
				for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
				return ar;
			};
			return ownKeys(o);
		};
		return function(mod) {
			if (mod && mod.__esModule) return mod;
			var result = {};
			if (mod != null) {
				for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
			}
			__setModuleDefault(result, mod);
			return result;
		};
	})();
	var __importDefault = this$5 && this$5.__importDefault || function(mod) {
		return mod && mod.__esModule ? mod : { "default": mod };
	};
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.ReadableStreamMessageReader = exports.AbstractMessageReader = exports.MessageReader = void 0;
	const ral_1 = __importDefault(require_ral());
	const Is = __importStar(require_is());
	const events_1 = require_events();
	const semaphore_1 = require_semaphore();
	var MessageReader;
	(function(MessageReader) {
		function is(value) {
			const candidate = value;
			return candidate && Is.func(candidate.listen) && Is.func(candidate.dispose) && Is.func(candidate.onError) && Is.func(candidate.onClose) && Is.func(candidate.onPartialMessage);
		}
		MessageReader.is = is;
	})(MessageReader || (exports.MessageReader = MessageReader = {}));
	var AbstractMessageReader = class {
		errorEmitter;
		closeEmitter;
		partialMessageEmitter;
		constructor() {
			this.errorEmitter = new events_1.Emitter();
			this.closeEmitter = new events_1.Emitter();
			this.partialMessageEmitter = new events_1.Emitter();
		}
		dispose() {
			this.errorEmitter.dispose();
			this.closeEmitter.dispose();
			this.partialMessageEmitter.dispose();
		}
		get onError() {
			return this.errorEmitter.event;
		}
		fireError(error) {
			this.errorEmitter.fire(this.asError(error));
		}
		get onClose() {
			return this.closeEmitter.event;
		}
		fireClose() {
			this.closeEmitter.fire(void 0);
		}
		get onPartialMessage() {
			return this.partialMessageEmitter.event;
		}
		firePartialMessage(info) {
			this.partialMessageEmitter.fire(info);
		}
		asError(error) {
			if (error instanceof Error) return error;
			else return /* @__PURE__ */ new Error(`Reader received error. Reason: ${Is.string(error.message) ? error.message : "unknown"}`);
		}
	};
	exports.AbstractMessageReader = AbstractMessageReader;
	var ResolvedMessageReaderOptions;
	(function(ResolvedMessageReaderOptions) {
		function fromOptions(options) {
			let charset;
			let contentDecoder;
			const contentDecoders = /* @__PURE__ */ new Map();
			let contentTypeDecoder;
			const contentTypeDecoders = /* @__PURE__ */ new Map();
			if (options === void 0 || typeof options === "string") charset = options ?? "utf-8";
			else {
				charset = options.charset ?? "utf-8";
				if (options.contentDecoder !== void 0) {
					contentDecoder = options.contentDecoder;
					contentDecoders.set(contentDecoder.name, contentDecoder);
				}
				if (options.contentDecoders !== void 0) for (const decoder of options.contentDecoders) contentDecoders.set(decoder.name, decoder);
				if (options.contentTypeDecoder !== void 0) {
					contentTypeDecoder = options.contentTypeDecoder;
					contentTypeDecoders.set(contentTypeDecoder.name, contentTypeDecoder);
				}
				if (options.contentTypeDecoders !== void 0) for (const decoder of options.contentTypeDecoders) contentTypeDecoders.set(decoder.name, decoder);
			}
			if (contentTypeDecoder === void 0) {
				contentTypeDecoder = (0, ral_1.default)().applicationJson.decoder;
				contentTypeDecoders.set(contentTypeDecoder.name, contentTypeDecoder);
			}
			return {
				charset,
				contentDecoder,
				contentDecoders,
				contentTypeDecoder,
				contentTypeDecoders
			};
		}
		ResolvedMessageReaderOptions.fromOptions = fromOptions;
	})(ResolvedMessageReaderOptions || (ResolvedMessageReaderOptions = {}));
	var ReadableStreamMessageReader = class extends AbstractMessageReader {
		readable;
		options;
		callback;
		nextMessageLength;
		messageToken;
		buffer;
		partialMessageTimer;
		_partialMessageTimeout;
		readSemaphore;
		constructor(readable, options) {
			super();
			this.readable = readable;
			this.options = ResolvedMessageReaderOptions.fromOptions(options);
			this.buffer = (0, ral_1.default)().messageBuffer.create(this.options.charset);
			this._partialMessageTimeout = 1e4;
			this.nextMessageLength = -1;
			this.messageToken = 0;
			this.readSemaphore = new semaphore_1.Semaphore(1);
		}
		set partialMessageTimeout(timeout) {
			this._partialMessageTimeout = timeout;
		}
		get partialMessageTimeout() {
			return this._partialMessageTimeout;
		}
		listen(callback) {
			this.nextMessageLength = -1;
			this.messageToken = 0;
			this.partialMessageTimer = void 0;
			this.callback = callback;
			const result = this.readable.onData((data) => {
				this.onData(data);
			});
			this.readable.onError((error) => this.fireError(error));
			this.readable.onClose(() => this.fireClose());
			return result;
		}
		onData(data) {
			try {
				this.buffer.append(data);
				while (true) {
					if (this.nextMessageLength === -1) {
						const headers = this.buffer.tryReadHeaders(true);
						if (!headers) return;
						const contentLength = headers.get("content-length");
						if (!contentLength) {
							this.fireError(/* @__PURE__ */ new Error(`Header must provide a Content-Length property.\n${JSON.stringify(Object.fromEntries(headers))}`));
							return;
						}
						const length = parseInt(contentLength);
						if (isNaN(length)) {
							this.fireError(/* @__PURE__ */ new Error(`Content-Length value must be a number. Got ${contentLength}`));
							return;
						}
						this.nextMessageLength = length;
					}
					const body = this.buffer.tryReadBody(this.nextMessageLength);
					if (body === void 0) {
						/** We haven't received the full message yet. */
						this.setPartialMessageTimer();
						return;
					}
					this.clearPartialMessageTimer();
					this.nextMessageLength = -1;
					this.readSemaphore.lock(async () => {
						const bytes = this.options.contentDecoder !== void 0 ? await this.options.contentDecoder.decode(body) : body;
						const message = await this.options.contentTypeDecoder.decode(bytes, this.options);
						this.callback(message);
					}).catch((error) => {
						this.fireError(error);
					});
				}
			} catch (error) {
				this.fireError(error);
			}
		}
		clearPartialMessageTimer() {
			if (this.partialMessageTimer) {
				this.partialMessageTimer.dispose();
				this.partialMessageTimer = void 0;
			}
		}
		setPartialMessageTimer() {
			this.clearPartialMessageTimer();
			if (this._partialMessageTimeout <= 0) return;
			this.partialMessageTimer = (0, ral_1.default)().timer.setTimeout((token, timeout) => {
				this.partialMessageTimer = void 0;
				if (token === this.messageToken) {
					this.firePartialMessage({
						messageToken: token,
						waitingTime: timeout
					});
					this.setPartialMessageTimer();
				}
			}, this._partialMessageTimeout, this.messageToken, this._partialMessageTimeout);
		}
	};
	exports.ReadableStreamMessageReader = ReadableStreamMessageReader;
}));
// node_modules/vscode-jsonrpc/lib/common/messageWriter.js
var require_messageWriter = /* @__PURE__ */ __commonJSMin(((exports, module, this$4) => {
	var __createBinding = this$4 && this$4.__createBinding || (Object.create ? (function(o, m, k, k2) {
		if (k2 === void 0) k2 = k;
		var desc = Object.getOwnPropertyDescriptor(m, k);
		if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) desc = {
			enumerable: true,
			get: function() {
				return m[k];
			}
		};
		Object.defineProperty(o, k2, desc);
	}) : (function(o, m, k, k2) {
		if (k2 === void 0) k2 = k;
		o[k2] = m[k];
	}));
	var __setModuleDefault = this$4 && this$4.__setModuleDefault || (Object.create ? (function(o, v) {
		Object.defineProperty(o, "default", {
			enumerable: true,
			value: v
		});
	}) : function(o, v) {
		o["default"] = v;
	});
	var __importStar = this$4 && this$4.__importStar || (function() {
		var ownKeys = function(o) {
			ownKeys = Object.getOwnPropertyNames || function(o) {
				var ar = [];
				for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
				return ar;
			};
			return ownKeys(o);
		};
		return function(mod) {
			if (mod && mod.__esModule) return mod;
			var result = {};
			if (mod != null) {
				for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
			}
			__setModuleDefault(result, mod);
			return result;
		};
	})();
	var __importDefault = this$4 && this$4.__importDefault || function(mod) {
		return mod && mod.__esModule ? mod : { "default": mod };
	};
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.WriteableStreamMessageWriter = exports.AbstractMessageWriter = exports.MessageWriter = void 0;
	const ral_1 = __importDefault(require_ral());
	const Is = __importStar(require_is());
	const semaphore_1 = require_semaphore();
	const events_1 = require_events();
	const ContentLength = "Content-Length: ";
	const CRLF = "\r\n";
	var MessageWriter;
	(function(MessageWriter) {
		function is(value) {
			const candidate = value;
			return candidate && Is.func(candidate.dispose) && Is.func(candidate.onClose) && Is.func(candidate.onError) && Is.func(candidate.write);
		}
		MessageWriter.is = is;
	})(MessageWriter || (exports.MessageWriter = MessageWriter = {}));
	var AbstractMessageWriter = class {
		errorEmitter;
		closeEmitter;
		constructor() {
			this.errorEmitter = new events_1.Emitter();
			this.closeEmitter = new events_1.Emitter();
		}
		dispose() {
			this.errorEmitter.dispose();
			this.closeEmitter.dispose();
		}
		get onError() {
			return this.errorEmitter.event;
		}
		fireError(error, message, count) {
			this.errorEmitter.fire([
				this.asError(error),
				message,
				count
			]);
		}
		get onClose() {
			return this.closeEmitter.event;
		}
		fireClose() {
			this.closeEmitter.fire(void 0);
		}
		asError(error) {
			if (error instanceof Error) return error;
			else return /* @__PURE__ */ new Error(`Writer received error. Reason: ${Is.string(error.message) ? error.message : "unknown"}`);
		}
	};
	exports.AbstractMessageWriter = AbstractMessageWriter;
	var ResolvedMessageWriterOptions;
	(function(ResolvedMessageWriterOptions) {
		function fromOptions(options) {
			if (options === void 0 || typeof options === "string") return {
				charset: options ?? "utf-8",
				contentTypeEncoder: (0, ral_1.default)().applicationJson.encoder
			};
			else return {
				charset: options.charset ?? "utf-8",
				contentEncoder: options.contentEncoder,
				contentTypeEncoder: options.contentTypeEncoder ?? (0, ral_1.default)().applicationJson.encoder
			};
		}
		ResolvedMessageWriterOptions.fromOptions = fromOptions;
	})(ResolvedMessageWriterOptions || (ResolvedMessageWriterOptions = {}));
	var WriteableStreamMessageWriter = class extends AbstractMessageWriter {
		writable;
		options;
		errorCount;
		writeSemaphore;
		constructor(writable, options) {
			super();
			this.writable = writable;
			this.options = ResolvedMessageWriterOptions.fromOptions(options);
			this.errorCount = 0;
			this.writeSemaphore = new semaphore_1.Semaphore(1);
			this.writable.onError((error) => this.fireError(error));
			this.writable.onClose(() => this.fireClose());
		}
		async write(msg) {
			return this.writeSemaphore.lock(async () => {
				return this.options.contentTypeEncoder.encode(msg, this.options).then((buffer) => {
					if (this.options.contentEncoder !== void 0) return this.options.contentEncoder.encode(buffer);
					else return buffer;
				}).then((buffer) => {
					const headers = [];
					headers.push(ContentLength, buffer.byteLength.toString(), CRLF);
					headers.push(CRLF);
					return this.doWrite(msg, headers, buffer);
				}, (error) => {
					this.fireError(error);
					throw error;
				});
			});
		}
		async doWrite(msg, headers, data) {
			try {
				await this.writable.write(headers.join(""), "ascii");
				return this.writable.write(data);
			} catch (error) {
				this.handleError(error, msg);
				return Promise.reject(error);
			}
		}
		handleError(error, msg) {
			this.errorCount++;
			this.fireError(error, msg, this.errorCount);
		}
		end() {
			this.writable.end();
		}
	};
	exports.WriteableStreamMessageWriter = WriteableStreamMessageWriter;
}));
// node_modules/vscode-jsonrpc/lib/common/messageBuffer.js
var require_messageBuffer = /* @__PURE__ */ __commonJSMin(((exports) => {
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.AbstractMessageBuffer = void 0;
	const CR = 13;
	const LF = 10;
	const CRLF = "\r\n";
	var AbstractMessageBuffer = class {
		_encoding;
		_chunks;
		_totalLength;
		constructor(encoding = "utf-8") {
			this._encoding = encoding;
			this._chunks = [];
			this._totalLength = 0;
		}
		get encoding() {
			return this._encoding;
		}
		append(chunk) {
			const toAppend = typeof chunk === "string" ? this.fromString(chunk, this._encoding) : chunk;
			this._chunks.push(toAppend);
			this._totalLength += toAppend.byteLength;
		}
		tryReadHeaders(lowerCaseKeys = false) {
			if (this._chunks.length === 0) return;
			let state = 0;
			let chunkIndex = 0;
			let offset = 0;
			let chunkBytesRead = 0;
			row: while (chunkIndex < this._chunks.length) {
				const chunk = this._chunks[chunkIndex];
				offset = 0;
				while (offset < chunk.length) {
					switch (chunk[offset]) {
						case CR:
							switch (state) {
								case 0:
									state = 1;
									break;
								case 2:
									state = 3;
									break;
								default: state = 0;
							}
							break;
						case LF:
							switch (state) {
								case 1:
									state = 2;
									break;
								case 3:
									state = 4;
									offset++;
									break row;
								default: state = 0;
							}
							break;
						default: state = 0;
					}
					offset++;
				}
				chunkBytesRead += chunk.byteLength;
				chunkIndex++;
			}
			if (state !== 4) return;
			const buffer = this._read(chunkBytesRead + offset);
			const result = /* @__PURE__ */ new Map();
			const headers = this.toString(buffer, "ascii").split(CRLF);
			if (headers.length < 2) return result;
			for (let i = 0; i < headers.length - 2; i++) {
				const header = headers[i];
				const index = header.indexOf(":");
				if (index === -1) throw new Error(`Message header must separate key and value using ':'\n${header}`);
				const key = header.substr(0, index);
				const value = header.substr(index + 1).trim();
				result.set(lowerCaseKeys ? key.toLowerCase() : key, value);
			}
			return result;
		}
		tryReadBody(length) {
			if (this._totalLength < length) return;
			return this._read(length);
		}
		get numberOfBytes() {
			return this._totalLength;
		}
		_read(byteCount) {
			if (byteCount === 0) return this.emptyBuffer();
			if (byteCount > this._totalLength) throw new Error(`Cannot read so many bytes!`);
			if (this._chunks[0].byteLength === byteCount) {
				const chunk = this._chunks[0];
				this._chunks.shift();
				this._totalLength -= byteCount;
				return this.asNative(chunk);
			}
			if (this._chunks[0].byteLength > byteCount) {
				const chunk = this._chunks[0];
				const result = this.asNative(chunk, byteCount);
				this._chunks[0] = chunk.slice(byteCount);
				this._totalLength -= byteCount;
				return result;
			}
			const result = this.allocNative(byteCount);
			let resultOffset = 0;
			const chunkIndex = 0;
			while (byteCount > 0) {
				const chunk = this._chunks[chunkIndex];
				if (chunk.byteLength > byteCount) {
					const chunkPart = chunk.slice(0, byteCount);
					result.set(chunkPart, resultOffset);
					resultOffset += byteCount;
					this._chunks[chunkIndex] = chunk.slice(byteCount);
					this._totalLength -= byteCount;
					byteCount -= byteCount;
				} else {
					result.set(chunk, resultOffset);
					resultOffset += chunk.byteLength;
					this._chunks.shift();
					this._totalLength -= chunk.byteLength;
					byteCount -= chunk.byteLength;
				}
			}
			return result;
		}
	};
	exports.AbstractMessageBuffer = AbstractMessageBuffer;
}));
// node_modules/vscode-jsonrpc/lib/common/connection.js
var require_connection = /* @__PURE__ */ __commonJSMin(((exports, module, this$3) => {
	var __createBinding = this$3 && this$3.__createBinding || (Object.create ? (function(o, m, k, k2) {
		if (k2 === void 0) k2 = k;
		var desc = Object.getOwnPropertyDescriptor(m, k);
		if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) desc = {
			enumerable: true,
			get: function() {
				return m[k];
			}
		};
		Object.defineProperty(o, k2, desc);
	}) : (function(o, m, k, k2) {
		if (k2 === void 0) k2 = k;
		o[k2] = m[k];
	}));
	var __setModuleDefault = this$3 && this$3.__setModuleDefault || (Object.create ? (function(o, v) {
		Object.defineProperty(o, "default", {
			enumerable: true,
			value: v
		});
	}) : function(o, v) {
		o["default"] = v;
	});
	var __importStar = this$3 && this$3.__importStar || (function() {
		var ownKeys = function(o) {
			ownKeys = Object.getOwnPropertyNames || function(o) {
				var ar = [];
				for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
				return ar;
			};
			return ownKeys(o);
		};
		return function(mod) {
			if (mod && mod.__esModule) return mod;
			var result = {};
			if (mod != null) {
				for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
			}
			__setModuleDefault(result, mod);
			return result;
		};
	})();
	var __importDefault = this$3 && this$3.__importDefault || function(mod) {
		return mod && mod.__esModule ? mod : { "default": mod };
	};
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.ConnectionOptions = exports.MessageStrategy = exports.CancellationStrategy = exports.CancellationSenderStrategy = exports.CancellationReceiverStrategy = exports.RequestCancellationReceiverStrategy = exports.IdCancellationReceiverStrategy = exports.ConnectionStrategy = exports.ConnectionError = exports.ConnectionErrors = exports.LogTraceNotification = exports.SetTraceNotification = exports.TraceFormat = exports.TraceValues = exports.TraceValue = exports.Trace = exports.NullLogger = exports.ProgressType = exports.ProgressToken = void 0;
	exports.createMessageConnection = createMessageConnection;
	const ral_1 = __importDefault(require_ral());
	const Is = __importStar(require_is());
	const messages_1 = require_messages();
	const linkedMap_1 = require_linkedMap();
	const events_1 = require_events();
	const cancellation_1 = require_cancellation();
	var CancelNotification;
	(function(CancelNotification) {
		CancelNotification.type = new messages_1.NotificationType("$/cancelRequest");
	})(CancelNotification || (CancelNotification = {}));
	var ProgressToken;
	(function(ProgressToken) {
		function is(value) {
			return typeof value === "string" || typeof value === "number";
		}
		ProgressToken.is = is;
	})(ProgressToken || (exports.ProgressToken = ProgressToken = {}));
	var ProgressNotification;
	(function(ProgressNotification) {
		ProgressNotification.type = new messages_1.NotificationType("$/progress");
	})(ProgressNotification || (ProgressNotification = {}));
	var ProgressType = class {
		/**
		* Clients must not use these properties. They are here to ensure correct typing.
		* in TypeScript
		*/
		__;
		_pr;
		constructor() {}
	};
	exports.ProgressType = ProgressType;
	var StarRequestHandler;
	(function(StarRequestHandler) {
		function is(value) {
			return Is.func(value);
		}
		StarRequestHandler.is = is;
	})(StarRequestHandler || (StarRequestHandler = {}));
	exports.NullLogger = Object.freeze({
		error: () => {},
		warn: () => {},
		info: () => {},
		log: () => {}
	});
	var Trace;
	(function(Trace) {
		Trace[Trace["Off"] = 0] = "Off";
		Trace[Trace["Messages"] = 1] = "Messages";
		Trace[Trace["Compact"] = 2] = "Compact";
		Trace[Trace["Verbose"] = 3] = "Verbose";
	})(Trace || (exports.Trace = Trace = {}));
	var TraceValue;
	(function(TraceValue) {
		/**
		* Turn tracing off.
		*/
		TraceValue.Off = "off";
		/**
		* Trace messages only.
		*/
		TraceValue.Messages = "messages";
		/**
		* Compact message tracing.
		*/
		TraceValue.Compact = "compact";
		/**
		* Verbose message tracing.
		*/
		TraceValue.Verbose = "verbose";
	})(TraceValue || (exports.TraceValue = TraceValue = {}));
	/**
	* @deprecated Use TraceValue instead
	*/
	exports.TraceValues = TraceValue;
	(function(Trace) {
		function fromString(value) {
			if (!Is.string(value)) return Trace.Off;
			value = value.toLowerCase();
			switch (value) {
				case "off": return Trace.Off;
				case "messages": return Trace.Messages;
				case "compact": return Trace.Compact;
				case "verbose": return Trace.Verbose;
				default: return Trace.Off;
			}
		}
		Trace.fromString = fromString;
		function toString(value) {
			switch (value) {
				case Trace.Off: return "off";
				case Trace.Messages: return "messages";
				case Trace.Compact: return "compact";
				case Trace.Verbose: return "verbose";
				default: return "off";
			}
		}
		Trace.toString = toString;
	})(Trace || (exports.Trace = Trace = {}));
	var TraceFormat;
	(function(TraceFormat) {
		TraceFormat["Text"] = "text";
		TraceFormat["JSON"] = "json";
	})(TraceFormat || (exports.TraceFormat = TraceFormat = {}));
	(function(TraceFormat) {
		function fromString(value) {
			if (!Is.string(value)) return TraceFormat.Text;
			value = value.toLowerCase();
			if (value === "json") return TraceFormat.JSON;
			else return TraceFormat.Text;
		}
		TraceFormat.fromString = fromString;
	})(TraceFormat || (exports.TraceFormat = TraceFormat = {}));
	var SetTraceNotification;
	(function(SetTraceNotification) {
		SetTraceNotification.type = new messages_1.NotificationType("$/setTrace");
	})(SetTraceNotification || (exports.SetTraceNotification = SetTraceNotification = {}));
	var LogTraceNotification;
	(function(LogTraceNotification) {
		LogTraceNotification.type = new messages_1.NotificationType("$/logTrace");
	})(LogTraceNotification || (exports.LogTraceNotification = LogTraceNotification = {}));
	var ConnectionErrors;
	(function(ConnectionErrors) {
		/**
		* The connection is closed.
		*/
		ConnectionErrors[ConnectionErrors["Closed"] = 1] = "Closed";
		/**
		* The connection got disposed.
		*/
		ConnectionErrors[ConnectionErrors["Disposed"] = 2] = "Disposed";
		/**
		* The connection is already in listening mode.
		*/
		ConnectionErrors[ConnectionErrors["AlreadyListening"] = 3] = "AlreadyListening";
	})(ConnectionErrors || (exports.ConnectionErrors = ConnectionErrors = {}));
	var ConnectionError = class ConnectionError extends Error {
		code;
		constructor(code, message) {
			super(message);
			this.code = code;
			Object.setPrototypeOf(this, ConnectionError.prototype);
		}
	};
	exports.ConnectionError = ConnectionError;
	var ConnectionStrategy;
	(function(ConnectionStrategy) {
		function is(value) {
			const candidate = value;
			return candidate && Is.func(candidate.cancelUndispatched);
		}
		ConnectionStrategy.is = is;
	})(ConnectionStrategy || (exports.ConnectionStrategy = ConnectionStrategy = {}));
	var IdCancellationReceiverStrategy;
	(function(IdCancellationReceiverStrategy) {
		function is(value) {
			const candidate = value;
			return candidate && (candidate.kind === void 0 || candidate.kind === "id") && Is.func(candidate.createCancellationTokenSource) && (candidate.dispose === void 0 || Is.func(candidate.dispose));
		}
		IdCancellationReceiverStrategy.is = is;
	})(IdCancellationReceiverStrategy || (exports.IdCancellationReceiverStrategy = IdCancellationReceiverStrategy = {}));
	var RequestCancellationReceiverStrategy;
	(function(RequestCancellationReceiverStrategy) {
		function is(value) {
			const candidate = value;
			return candidate && candidate.kind === "request" && Is.func(candidate.createCancellationTokenSource) && (candidate.dispose === void 0 || Is.func(candidate.dispose));
		}
		RequestCancellationReceiverStrategy.is = is;
	})(RequestCancellationReceiverStrategy || (exports.RequestCancellationReceiverStrategy = RequestCancellationReceiverStrategy = {}));
	var CancellationReceiverStrategy;
	(function(CancellationReceiverStrategy) {
		CancellationReceiverStrategy.Message = Object.freeze({ createCancellationTokenSource(_) {
			return new cancellation_1.CancellationTokenSource();
		} });
		function is(value) {
			return IdCancellationReceiverStrategy.is(value) || RequestCancellationReceiverStrategy.is(value);
		}
		CancellationReceiverStrategy.is = is;
	})(CancellationReceiverStrategy || (exports.CancellationReceiverStrategy = CancellationReceiverStrategy = {}));
	var CancellationSenderStrategy;
	(function(CancellationSenderStrategy) {
		CancellationSenderStrategy.Message = Object.freeze({
			sendCancellation(conn, id) {
				return conn.sendNotification(CancelNotification.type, { id });
			},
			cleanup(_) {}
		});
		function is(value) {
			const candidate = value;
			return candidate && Is.func(candidate.sendCancellation) && Is.func(candidate.cleanup);
		}
		CancellationSenderStrategy.is = is;
	})(CancellationSenderStrategy || (exports.CancellationSenderStrategy = CancellationSenderStrategy = {}));
	var CancellationStrategy;
	(function(CancellationStrategy) {
		CancellationStrategy.Message = Object.freeze({
			receiver: CancellationReceiverStrategy.Message,
			sender: CancellationSenderStrategy.Message
		});
		function is(value) {
			const candidate = value;
			return candidate && CancellationReceiverStrategy.is(candidate.receiver) && CancellationSenderStrategy.is(candidate.sender);
		}
		CancellationStrategy.is = is;
	})(CancellationStrategy || (exports.CancellationStrategy = CancellationStrategy = {}));
	var MessageStrategy;
	(function(MessageStrategy) {
		function is(value) {
			const candidate = value;
			return candidate && Is.func(candidate.handleMessage);
		}
		MessageStrategy.is = is;
	})(MessageStrategy || (exports.MessageStrategy = MessageStrategy = {}));
	var ConnectionOptions;
	(function(ConnectionOptions) {
		function is(value) {
			const candidate = value;
			return candidate && (CancellationStrategy.is(candidate.cancellationStrategy) || ConnectionStrategy.is(candidate.connectionStrategy) || MessageStrategy.is(candidate.messageStrategy) || Is.number(candidate.maxParallelism));
		}
		ConnectionOptions.is = is;
	})(ConnectionOptions || (exports.ConnectionOptions = ConnectionOptions = {}));
	var ConnectionState;
	(function(ConnectionState) {
		ConnectionState[ConnectionState["New"] = 1] = "New";
		ConnectionState[ConnectionState["Listening"] = 2] = "Listening";
		ConnectionState[ConnectionState["Closed"] = 3] = "Closed";
		ConnectionState[ConnectionState["Disposed"] = 4] = "Disposed";
	})(ConnectionState || (ConnectionState = {}));
	function createMessageConnection(messageReader, messageWriter, _logger, options) {
		const logger = _logger !== void 0 ? _logger : exports.NullLogger;
		let sequenceNumber = 0;
		let notificationSequenceNumber = 0;
		let unknownResponseSequenceNumber = 0;
		const version = "2.0";
		const maxParallelism = options?.maxParallelism ?? -1;
		let inFlight = 0;
		let starRequestHandler = void 0;
		const requestHandlers = /* @__PURE__ */ new Map();
		let starNotificationHandler = void 0;
		const notificationHandlers = /* @__PURE__ */ new Map();
		const progressHandlers = /* @__PURE__ */ new Map();
		let timer;
		let messageQueue = new linkedMap_1.LinkedMap();
		let responsePromises = /* @__PURE__ */ new Map();
		let knownCanceledRequests = /* @__PURE__ */ new Set();
		let requestTokens = /* @__PURE__ */ new Map();
		let trace = Trace.Off;
		let traceFormat = TraceFormat.Text;
		let tracer;
		let state = ConnectionState.New;
		const errorEmitter = new events_1.Emitter();
		const closeEmitter = new events_1.Emitter();
		const unhandledNotificationEmitter = new events_1.Emitter();
		const unhandledProgressEmitter = new events_1.Emitter();
		const disposeEmitter = new events_1.Emitter();
		const cancellationStrategy = options && options.cancellationStrategy ? options.cancellationStrategy : CancellationStrategy.Message;
		function cancelUndispatched(_message) {}
		function isListening() {
			return state === ConnectionState.Listening;
		}
		function isClosed() {
			return state === ConnectionState.Closed;
		}
		function isDisposed() {
			return state === ConnectionState.Disposed;
		}
		function closeHandler() {
			if (state === ConnectionState.New || state === ConnectionState.Listening) {
				state = ConnectionState.Closed;
				closeEmitter.fire(void 0);
			}
		}
		function readErrorHandler(error) {
			errorEmitter.fire([
				error,
				void 0,
				void 0
			]);
		}
		function writeErrorHandler(data) {
			errorEmitter.fire(data);
		}
		messageReader.onClose(closeHandler);
		messageReader.onError(readErrorHandler);
		messageWriter.onClose(closeHandler);
		messageWriter.onError(writeErrorHandler);
		function createRequestQueueKey(id) {
			if (id === null) throw new Error(`Can't send requests with id null since the response can't be correlated.`);
			return "req-" + id.toString();
		}
		function createResponseQueueKey(id) {
			if (id === null) return "res-unknown-" + (++unknownResponseSequenceNumber).toString();
			else return "res-" + id.toString();
		}
		function createNotificationQueueKey() {
			return "not-" + (++notificationSequenceNumber).toString();
		}
		function addMessageToQueue(queue, message) {
			if (messages_1.Message.isRequest(message)) queue.set(createRequestQueueKey(message.id), message);
			else if (messages_1.Message.isResponse(message)) {
				if (maxParallelism === -1) queue.set(createResponseQueueKey(message.id), message);
				else handleResponse(message);
			} else queue.set(createNotificationQueueKey(), message);
		}
		function triggerMessageQueue() {
			if (timer || messageQueue.size === 0) return;
			if (maxParallelism !== -1 && inFlight >= maxParallelism) return;
			timer = (0, ral_1.default)().timer.setImmediate(async () => {
				timer = void 0;
				if (messageQueue.size === 0) return;
				if (maxParallelism !== -1 && inFlight >= maxParallelism) return;
				const message = messageQueue.shift();
				let result;
				try {
					inFlight++;
					const messageStrategy = options?.messageStrategy;
					if (MessageStrategy.is(messageStrategy)) result = messageStrategy.handleMessage(message, handleMessage);
					else result = handleMessage(message);
				} catch (error) {
					logger.error(`Processing message queue failed: ${error.toString()}`);
				} finally {
					if (result instanceof Promise) result.then(() => {
						inFlight--;
						triggerMessageQueue();
					}).catch((error) => {
						logger.error(`Processing message queue failed: ${error.toString()}`);
					});
					else inFlight--;
					triggerMessageQueue();
				}
			});
		}
		async function handleMessage(message) {
			if (messages_1.Message.isRequest(message)) return handleRequest(message);
			else if (messages_1.Message.isNotification(message)) return handleNotification(message);
			else if (messages_1.Message.isResponse(message)) return handleResponse(message);
			else return handleInvalidMessage(message);
		}
		const callback = (message) => {
			try {
				if (messages_1.Message.isNotification(message) && message.method === CancelNotification.type.method) {
					const cancelId = message.params.id;
					const key = createRequestQueueKey(cancelId);
					const toCancel = messageQueue.get(key);
					if (messages_1.Message.isRequest(toCancel)) {
						const strategy = options?.connectionStrategy;
						const response = strategy && strategy.cancelUndispatched ? strategy.cancelUndispatched(toCancel, cancelUndispatched) : void 0;
						if (response && (response.error !== void 0 || response.result !== void 0)) {
							messageQueue.delete(key);
							requestTokens.delete(cancelId);
							response.id = toCancel.id;
							traceSendingResponse(response, message.method, Date.now());
							messageWriter.write(response).catch(() => logger.error(`Sending response for canceled message failed.`));
							return;
						}
					}
					const cancellationToken = requestTokens.get(cancelId);
					if (cancellationToken !== void 0) {
						cancellationToken.cancel();
						traceReceivedNotification(message);
						return;
					} else knownCanceledRequests.add(cancelId);
				}
				addMessageToQueue(messageQueue, message);
			} finally {
				triggerMessageQueue();
			}
		};
		async function handleRequest(requestMessage) {
			if (isDisposed()) return Promise.resolve();
			function reply(resultOrError, method, startTime) {
				const message = {
					jsonrpc: version,
					id: requestMessage.id
				};
				if (resultOrError instanceof messages_1.ResponseError) message.error = resultOrError.toJson();
				else message.result = resultOrError === void 0 ? null : resultOrError;
				traceSendingResponse(message, method, startTime);
				return messageWriter.write(message);
			}
			function replyError(error, method, startTime) {
				const message = {
					jsonrpc: version,
					id: requestMessage.id,
					error: error.toJson()
				};
				traceSendingResponse(message, method, startTime);
				return messageWriter.write(message);
			}
			traceReceivedRequest(requestMessage);
			const element = requestHandlers.get(requestMessage.method);
			let type;
			let requestHandler;
			if (element) {
				type = element.type;
				requestHandler = element.handler;
			}
			const startTime = Date.now();
			if (requestHandler || starRequestHandler) {
				const tokenKey = requestMessage.id ?? String(Date.now());
				const cancellationSource = IdCancellationReceiverStrategy.is(cancellationStrategy.receiver) ? cancellationStrategy.receiver.createCancellationTokenSource(tokenKey) : cancellationStrategy.receiver.createCancellationTokenSource(requestMessage);
				if (requestMessage.id !== null && knownCanceledRequests.has(requestMessage.id)) cancellationSource.cancel();
				if (requestMessage.id !== null) requestTokens.set(tokenKey, cancellationSource);
				try {
					let handlerResult;
					if (requestHandler) {
						if (requestMessage.params === void 0) {
							if (type !== void 0 && type.numberOfParams !== 0) return replyError(new messages_1.ResponseError(messages_1.ErrorCodes.InvalidParams, `Request ${requestMessage.method} defines ${type.numberOfParams} params but received none.`), requestMessage.method, startTime);
							handlerResult = requestHandler(cancellationSource.token);
						} else if (Array.isArray(requestMessage.params)) {
							if (type !== void 0 && type.parameterStructures === messages_1.ParameterStructures.byName) return replyError(new messages_1.ResponseError(messages_1.ErrorCodes.InvalidParams, `Request ${requestMessage.method} defines parameters by name but received parameters by position`), requestMessage.method, startTime);
							handlerResult = requestHandler(...requestMessage.params, cancellationSource.token);
						} else {
							if (type !== void 0 && type.parameterStructures === messages_1.ParameterStructures.byPosition) return replyError(new messages_1.ResponseError(messages_1.ErrorCodes.InvalidParams, `Request ${requestMessage.method} defines parameters by position but received parameters by name`), requestMessage.method, startTime);
							handlerResult = requestHandler(requestMessage.params, cancellationSource.token);
						}
					} else if (starRequestHandler) handlerResult = starRequestHandler(requestMessage.method, requestMessage.params, cancellationSource.token);
					await reply(await handlerResult, requestMessage.method, startTime);
				} catch (error) {
					if (error instanceof messages_1.ResponseError) await reply(error, requestMessage.method, startTime);
					else if (error && Is.string(error.message)) await replyError(new messages_1.ResponseError(messages_1.ErrorCodes.InternalError, `Request ${requestMessage.method} failed with message: ${error.message}`), requestMessage.method, startTime);
					else await replyError(new messages_1.ResponseError(messages_1.ErrorCodes.InternalError, `Request ${requestMessage.method} failed unexpectedly without providing any details.`), requestMessage.method, startTime);
				} finally {
					requestTokens.delete(tokenKey);
				}
			} else await replyError(new messages_1.ResponseError(messages_1.ErrorCodes.MethodNotFound, `Unhandled method ${requestMessage.method}`), requestMessage.method, startTime);
		}
		function handleResponse(responseMessage) {
			if (isDisposed()) return;
			if (responseMessage.id === null) {
				if (responseMessage.error) logger.error(`Received response message without id: Error is: \n${JSON.stringify(responseMessage.error, void 0, 4)}`);
				else logger.error(`Received response message without id. No further error information provided.`);
			} else {
				const key = responseMessage.id;
				const responsePromise = responsePromises.get(key);
				traceReceivedResponse(responseMessage, responsePromise);
				if (responsePromise !== void 0) {
					responsePromises.delete(key);
					try {
						if (responseMessage.error) {
							const error = responseMessage.error;
							responsePromise.reject(new messages_1.ResponseError(error.code, error.message, error.data));
						} else if (responseMessage.result !== void 0) responsePromise.resolve(responseMessage.result);
						else throw new Error("Should never happen.");
					} catch (error) {
						if (error.message) logger.error(`Response handler '${responsePromise.method}' failed with message: ${error.message}`);
						else logger.error(`Response handler '${responsePromise.method}' failed unexpectedly.`);
					}
				}
			}
		}
		async function handleNotification(message) {
			if (isDisposed()) return;
			let type = void 0;
			let notificationHandler;
			if (message.method === CancelNotification.type.method) {
				const cancelId = message.params.id;
				knownCanceledRequests.delete(cancelId);
				traceReceivedNotification(message);
				return;
			} else {
				const element = notificationHandlers.get(message.method);
				if (element) {
					notificationHandler = element.handler;
					type = element.type;
				}
			}
			if (notificationHandler || starNotificationHandler) try {
				traceReceivedNotification(message);
				if (notificationHandler) {
					if (message.params === void 0) {
						if (type !== void 0) {
							if (type.numberOfParams !== 0 && type.parameterStructures !== messages_1.ParameterStructures.byName) logger.error(`Notification ${message.method} defines ${type.numberOfParams} params but received none.`);
						}
						await notificationHandler();
					} else if (Array.isArray(message.params)) {
						const params = message.params;
						if (message.method === ProgressNotification.type.method && params.length === 2 && ProgressToken.is(params[0])) await notificationHandler({
							token: params[0],
							value: params[1]
						});
						else {
							if (type !== void 0) {
								if (type.parameterStructures === messages_1.ParameterStructures.byName) logger.error(`Notification ${message.method} defines parameters by name but received parameters by position`);
								if (type.numberOfParams !== message.params.length) logger.error(`Notification ${message.method} defines ${type.numberOfParams} params but received ${params.length} arguments`);
							}
							await notificationHandler(...params);
						}
					} else {
						if (type !== void 0 && type.parameterStructures === messages_1.ParameterStructures.byPosition) logger.error(`Notification ${message.method} defines parameters by position but received parameters by name`);
						await notificationHandler(message.params);
					}
				} else if (starNotificationHandler) await starNotificationHandler(message.method, message.params);
			} catch (error) {
				if (error.message) logger.error(`Notification handler '${message.method}' failed with message: ${error.message}`);
				else logger.error(`Notification handler '${message.method}' failed unexpectedly.`);
			}
			else unhandledNotificationEmitter.fire(message);
		}
		function handleInvalidMessage(message) {
			if (!message) {
				logger.error("Received empty message.");
				return;
			}
			logger.error(`Received message which is neither a response nor a notification message:\n${JSON.stringify(message, null, 4)}`);
			const responseMessage = message;
			if (Is.string(responseMessage.id) || Is.number(responseMessage.id)) {
				const key = responseMessage.id;
				const responseHandler = responsePromises.get(key);
				if (responseHandler) responseHandler.reject(/* @__PURE__ */ new Error("The received response has neither a result nor an error property."));
			}
		}
		function stringifyTrace(params) {
			if (params === void 0 || params === null) return;
			switch (trace) {
				case Trace.Verbose: return JSON.stringify(params, null, 4);
				case Trace.Compact: return JSON.stringify(params);
				default: return;
			}
		}
		function traceSendingRequest(message) {
			if (trace === Trace.Off || !tracer) return;
			if (traceFormat === TraceFormat.Text) {
				let data = void 0;
				if ((trace === Trace.Verbose || trace === Trace.Compact) && message.params) data = `Params: ${stringifyTrace(message.params)}`;
				tracer.log(`Sending request '${message.method} - (${message.id})'.`, data);
			} else logLSPMessage("send-request", message);
		}
		function traceSendingNotification(message) {
			if (trace === Trace.Off || !tracer) return;
			if (traceFormat === TraceFormat.Text) {
				let data = void 0;
				if (trace === Trace.Verbose || trace === Trace.Compact) {
					if (message.params) data = `Params: ${stringifyTrace(message.params)}`;
					else data = "No parameters provided.";
				}
				tracer.log(`Sending notification '${message.method}'.`, data);
			} else logLSPMessage("send-notification", message);
		}
		function traceSendingResponse(message, method, startTime) {
			if (trace === Trace.Off || !tracer) return;
			if (traceFormat === TraceFormat.Text) {
				let data = void 0;
				if (trace === Trace.Verbose || trace === Trace.Compact) {
					if (message.error && message.error.data) data = `Error data: ${stringifyTrace(message.error.data)}`;
					else if (message.result) data = `Result: ${stringifyTrace(message.result)}`;
					else if (message.error === void 0) data = "No result returned.";
				}
				const error = message.error ? ` Request failed: ${message.error.message} (${message.error.code}).` : "";
				tracer.log(`Sending response '${method} - (${message.id})'. Processing request took ${Date.now() - startTime}ms.${error}`, data);
			} else logLSPMessage("send-response", message);
		}
		function traceReceivedRequest(message) {
			if (trace === Trace.Off || !tracer) return;
			if (traceFormat === TraceFormat.Text) {
				let data = void 0;
				if ((trace === Trace.Verbose || trace === Trace.Compact) && message.params) data = `Params: ${stringifyTrace(message.params)}`;
				tracer.log(`Received request '${message.method} - (${message.id})'.`, data);
			} else logLSPMessage("receive-request", message);
		}
		function traceReceivedNotification(message) {
			if (trace === Trace.Off || !tracer || message.method === LogTraceNotification.type.method) return;
			if (traceFormat === TraceFormat.Text) {
				let data = void 0;
				if (trace === Trace.Verbose || trace === Trace.Compact) {
					if (message.params) data = `Params: ${stringifyTrace(message.params)}`;
					else data = "No parameters provided.";
				}
				tracer.log(`Received notification '${message.method}'.`, data);
			} else logLSPMessage("receive-notification", message);
		}
		function traceReceivedResponse(message, responsePromise) {
			if (trace === Trace.Off || !tracer) return;
			if (traceFormat === TraceFormat.Text) {
				let data = void 0;
				if (trace === Trace.Verbose || trace === Trace.Compact) {
					if (message.error && message.error.data) data = `Error data: ${stringifyTrace(message.error.data)}`;
					else if (message.result) data = `Result: ${stringifyTrace(message.result)}`;
					else if (message.error === void 0) data = "No result returned.";
				}
				if (responsePromise) {
					const error = message.error ? ` Request failed: ${message.error.message} (${message.error.code}).` : "";
					tracer.log(`Received response '${responsePromise.method} - (${message.id})' in ${Date.now() - responsePromise.timerStart}ms.${error}`, data);
				} else tracer.log(`Received response ${message.id} without active response promise.`, data);
			} else logLSPMessage("receive-response", message);
		}
		function logLSPMessage(type, message) {
			if (!tracer || trace === Trace.Off) return;
			const lspMessage = {
				isLSPMessage: true,
				type,
				message,
				timestamp: Date.now()
			};
			tracer.log(lspMessage);
		}
		function throwIfClosedOrDisposed() {
			if (isClosed()) throw new ConnectionError(ConnectionErrors.Closed, "Connection is closed.");
			if (isDisposed()) throw new ConnectionError(ConnectionErrors.Disposed, "Connection is disposed.");
		}
		function throwIfListening() {
			if (isListening()) throw new ConnectionError(ConnectionErrors.AlreadyListening, "Connection is already listening");
		}
		function throwIfNotListening() {
			if (!isListening()) throw new Error("Call listen() first.");
		}
		function undefinedToNull(param) {
			if (param === void 0) return null;
			else return param;
		}
		function nullToUndefined(param) {
			if (param === null) return;
			else return param;
		}
		function isNamedParam(param) {
			return param !== void 0 && param !== null && !Array.isArray(param) && typeof param === "object";
		}
		function computeSingleParam(parameterStructures, param) {
			switch (parameterStructures) {
				case messages_1.ParameterStructures.auto: if (isNamedParam(param)) return nullToUndefined(param);
				else return [undefinedToNull(param)];
				case messages_1.ParameterStructures.byName:
					if (!isNamedParam(param)) throw new Error(`Received parameters by name but param is not an object literal.`);
					return nullToUndefined(param);
				case messages_1.ParameterStructures.byPosition: return [undefinedToNull(param)];
				default: throw new Error(`Unknown parameter structure ${parameterStructures.toString()}`);
			}
		}
		function computeMessageParams(type, params) {
			let result;
			const numberOfParams = type.numberOfParams;
			switch (numberOfParams) {
				case 0:
					result = void 0;
					break;
				case 1:
					result = computeSingleParam(type.parameterStructures, params[0]);
					break;
				default:
					result = [];
					for (let i = 0; i < params.length && i < numberOfParams; i++) result.push(undefinedToNull(params[i]));
					if (params.length < numberOfParams) for (let i = params.length; i < numberOfParams; i++) result.push(null);
			}
			return result;
		}
		const connection = {
			sendNotification: (type, ...args) => {
				throwIfClosedOrDisposed();
				let method;
				let messageParams;
				if (Is.string(type)) {
					method = type;
					const first = args[0];
					let paramStart = 0;
					let parameterStructures = messages_1.ParameterStructures.auto;
					if (messages_1.ParameterStructures.is(first)) {
						paramStart = 1;
						parameterStructures = first;
					}
					const paramEnd = args.length;
					const numberOfParams = paramEnd - paramStart;
					switch (numberOfParams) {
						case 0:
							messageParams = void 0;
							break;
						case 1:
							messageParams = computeSingleParam(parameterStructures, args[paramStart]);
							break;
						default:
							if (parameterStructures === messages_1.ParameterStructures.byName) throw new Error(`Received ${numberOfParams} parameters for 'by Name' notification parameter structure.`);
							messageParams = args.slice(paramStart, paramEnd).map((value) => undefinedToNull(value));
					}
				} else {
					const params = args;
					method = type.method;
					messageParams = computeMessageParams(type, params);
				}
				const notificationMessage = {
					jsonrpc: version,
					method,
					params: messageParams
				};
				traceSendingNotification(notificationMessage);
				return messageWriter.write(notificationMessage).catch((error) => {
					logger.error(`Sending notification failed.`);
					throw error;
				});
			},
			onNotification: (type, handler) => {
				throwIfClosedOrDisposed();
				let method;
				if (Is.func(type)) starNotificationHandler = type;
				else if (handler) {
					if (Is.string(type)) {
						method = type;
						notificationHandlers.set(type, {
							type: void 0,
							handler
						});
					} else {
						method = type.method;
						notificationHandlers.set(type.method, {
							type,
							handler
						});
					}
				}
				return { dispose: () => {
					if (method !== void 0) {
						if (notificationHandlers.get(method)?.handler === handler) notificationHandlers.delete(method);
					} else if (starNotificationHandler === type) starNotificationHandler = void 0;
				} };
			},
			onProgress: (_type, token, handler) => {
				if (progressHandlers.has(token)) throw new Error(`Progress handler for token ${token} already registered`);
				progressHandlers.set(token, handler);
				return { dispose: () => {
					if (progressHandlers.get(token) === handler) progressHandlers.delete(token);
				} };
			},
			sendProgress: (_type, token, value) => {
				return connection.sendNotification(ProgressNotification.type, {
					token,
					value
				});
			},
			onUnhandledProgress: unhandledProgressEmitter.event,
			sendRequest: (type, ...args) => {
				throwIfClosedOrDisposed();
				throwIfNotListening();
				function sendCancellation(connection, id) {
					const p = cancellationStrategy.sender.sendCancellation(connection, id);
					if (p === void 0) logger.log(`Received no promise from cancellation strategy when cancelling id ${id}`);
					else p.catch(() => {
						logger.log(`Sending cancellation messages for id ${id} failed.`);
					});
				}
				let method;
				let messageParams;
				let token = void 0;
				if (Is.string(type)) {
					method = type;
					const first = args[0];
					const last = args[args.length - 1];
					let paramStart = 0;
					let parameterStructures = messages_1.ParameterStructures.auto;
					if (messages_1.ParameterStructures.is(first)) {
						paramStart = 1;
						parameterStructures = first;
					}
					let paramEnd = args.length;
					if (cancellation_1.CancellationToken.is(last)) {
						paramEnd = paramEnd - 1;
						token = last;
					}
					const numberOfParams = paramEnd - paramStart;
					switch (numberOfParams) {
						case 0:
							messageParams = void 0;
							break;
						case 1:
							messageParams = computeSingleParam(parameterStructures, args[paramStart]);
							break;
						default:
							if (parameterStructures === messages_1.ParameterStructures.byName) throw new Error(`Received ${numberOfParams} parameters for 'by Name' request parameter structure.`);
							messageParams = args.slice(paramStart, paramEnd).map((value) => undefinedToNull(value));
					}
				} else {
					const params = args;
					method = type.method;
					messageParams = computeMessageParams(type, params);
					const numberOfParams = type.numberOfParams;
					token = cancellation_1.CancellationToken.is(params[numberOfParams]) ? params[numberOfParams] : void 0;
				}
				const id = sequenceNumber++;
				let disposable;
				let tokenWasCancelled = false;
				if (token !== void 0) {
					if (token.isCancellationRequested) tokenWasCancelled = true;
					else disposable = token.onCancellationRequested(() => {
						sendCancellation(connection, id);
					});
				}
				const requestMessage = {
					jsonrpc: version,
					id,
					method,
					params: messageParams
				};
				traceSendingRequest(requestMessage);
				if (typeof cancellationStrategy.sender.enableCancellation === "function") cancellationStrategy.sender.enableCancellation(requestMessage);
				return new Promise(async (resolve, reject) => {
					const resolveWithCleanup = (r) => {
						resolve(r);
						cancellationStrategy.sender.cleanup(id);
						disposable?.dispose();
					};
					const rejectWithCleanup = (r) => {
						reject(r);
						cancellationStrategy.sender.cleanup(id);
						disposable?.dispose();
					};
					const responsePromise = {
						method,
						timerStart: Date.now(),
						resolve: resolveWithCleanup,
						reject: rejectWithCleanup
					};
					try {
						responsePromises.set(id, responsePromise);
						await messageWriter.write(requestMessage);
						if (tokenWasCancelled) sendCancellation(connection, id);
					} catch (error) {
						responsePromises.delete(id);
						responsePromise.reject(new messages_1.ResponseError(messages_1.ErrorCodes.MessageWriteError, error.message ? error.message : "Unknown reason"));
						logger.error(`Sending request failed.`);
					}
				});
			},
			onRequest: (type, handler) => {
				throwIfClosedOrDisposed();
				let method = null;
				if (StarRequestHandler.is(type)) {
					method = void 0;
					starRequestHandler = type;
				} else if (Is.string(type)) {
					method = null;
					if (handler !== void 0) {
						method = type;
						requestHandlers.set(type, {
							handler,
							type: void 0
						});
					}
				} else if (handler !== void 0) {
					method = type.method;
					requestHandlers.set(type.method, {
						type,
						handler
					});
				}
				return { dispose: () => {
					if (method === null) return;
					if (method !== void 0) {
						if (requestHandlers.get(method)?.handler === handler) requestHandlers.delete(method);
					} else if (starRequestHandler === type) starRequestHandler = void 0;
				} };
			},
			hasPendingResponse: () => {
				return responsePromises.size > 0;
			},
			trace: async (_value, _tracer, sendNotificationOrTraceOptions) => {
				let _sendNotification = false;
				let _traceFormat = TraceFormat.Text;
				if (sendNotificationOrTraceOptions !== void 0) {
					if (Is.boolean(sendNotificationOrTraceOptions)) _sendNotification = sendNotificationOrTraceOptions;
					else {
						_sendNotification = sendNotificationOrTraceOptions.sendNotification || false;
						_traceFormat = sendNotificationOrTraceOptions.traceFormat || TraceFormat.Text;
					}
				}
				trace = _value;
				traceFormat = _traceFormat;
				if (trace === Trace.Off) tracer = void 0;
				else tracer = _tracer;
				if (_sendNotification && !isClosed() && !isDisposed()) await connection.sendNotification(SetTraceNotification.type, { value: Trace.toString(_value) });
			},
			onError: errorEmitter.event,
			onClose: closeEmitter.event,
			onUnhandledNotification: unhandledNotificationEmitter.event,
			onDispose: disposeEmitter.event,
			end: () => {
				messageWriter.end();
			},
			dispose: () => {
				if (isDisposed()) return;
				state = ConnectionState.Disposed;
				disposeEmitter.fire(void 0);
				const error = new messages_1.ResponseError(messages_1.ErrorCodes.PendingResponseRejected, "Pending response rejected since connection got disposed");
				for (const promise of responsePromises.values()) promise.reject(error);
				responsePromises = /* @__PURE__ */ new Map();
				requestTokens = /* @__PURE__ */ new Map();
				knownCanceledRequests = /* @__PURE__ */ new Set();
				messageQueue = new linkedMap_1.LinkedMap();
				if (Is.func(messageWriter.dispose)) messageWriter.dispose();
				if (Is.func(messageReader.dispose)) messageReader.dispose();
			},
			listen: () => {
				throwIfClosedOrDisposed();
				throwIfListening();
				state = ConnectionState.Listening;
				messageReader.listen(callback);
			},
			inspect: () => {
				(0, ral_1.default)().console.log("inspect");
			}
		};
		connection.onNotification(LogTraceNotification.type, (params) => {
			if (trace === Trace.Off || !tracer) return;
			const verbose = trace === Trace.Verbose || trace === Trace.Compact;
			tracer.log(params.message, verbose ? params.verbose : void 0);
		});
		connection.onNotification(ProgressNotification.type, async (params) => {
			const handler = progressHandlers.get(params.token);
			if (handler) await handler(params.value);
			else unhandledProgressEmitter.fire(params);
		});
		return connection;
	}
}));
// node_modules/vscode-jsonrpc/lib/common/api.js
var require_api = /* @__PURE__ */ __commonJSMin(((exports, module, this$2) => {
	var __importDefault = this$2 && this$2.__importDefault || function(mod) {
		return mod && mod.__esModule ? mod : { "default": mod };
	};
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.ProgressType = exports.ProgressToken = exports.createMessageConnection = exports.NullLogger = exports.ConnectionOptions = exports.ConnectionStrategy = exports.AbstractMessageBuffer = exports.WriteableStreamMessageWriter = exports.AbstractMessageWriter = exports.MessageWriter = exports.ReadableStreamMessageReader = exports.AbstractMessageReader = exports.MessageReader = exports.SharedArrayReceiverStrategy = exports.SharedArraySenderStrategy = exports.CancellationToken = exports.CancellationTokenSource = exports.Emitter = exports.Event = exports.Disposable = exports.LRUCache = exports.Touch = exports.LinkedMap = exports.ParameterStructures = exports.NotificationType9 = exports.NotificationType8 = exports.NotificationType7 = exports.NotificationType6 = exports.NotificationType5 = exports.NotificationType4 = exports.NotificationType3 = exports.NotificationType2 = exports.NotificationType1 = exports.NotificationType0 = exports.NotificationType = exports.ErrorCodes = exports.ResponseError = exports.RequestType9 = exports.RequestType8 = exports.RequestType7 = exports.RequestType6 = exports.RequestType5 = exports.RequestType4 = exports.RequestType3 = exports.RequestType2 = exports.RequestType1 = exports.RequestType0 = exports.RequestType = exports.Message = exports.RAL = void 0;
	exports.MessageStrategy = exports.CancellationStrategy = exports.CancellationSenderStrategy = exports.RequestCancellationReceiverStrategy = exports.IdCancellationReceiverStrategy = exports.CancellationReceiverStrategy = exports.ConnectionError = exports.ConnectionErrors = exports.LogTraceNotification = exports.SetTraceNotification = exports.TraceFormat = exports.TraceValues = exports.TraceValue = exports.Trace = void 0;
	const messages_1 = require_messages();
	Object.defineProperty(exports, "Message", {
		enumerable: true,
		get: function() {
			return messages_1.Message;
		}
	});
	Object.defineProperty(exports, "RequestType", {
		enumerable: true,
		get: function() {
			return messages_1.RequestType;
		}
	});
	Object.defineProperty(exports, "RequestType0", {
		enumerable: true,
		get: function() {
			return messages_1.RequestType0;
		}
	});
	Object.defineProperty(exports, "RequestType1", {
		enumerable: true,
		get: function() {
			return messages_1.RequestType1;
		}
	});
	Object.defineProperty(exports, "RequestType2", {
		enumerable: true,
		get: function() {
			return messages_1.RequestType2;
		}
	});
	Object.defineProperty(exports, "RequestType3", {
		enumerable: true,
		get: function() {
			return messages_1.RequestType3;
		}
	});
	Object.defineProperty(exports, "RequestType4", {
		enumerable: true,
		get: function() {
			return messages_1.RequestType4;
		}
	});
	Object.defineProperty(exports, "RequestType5", {
		enumerable: true,
		get: function() {
			return messages_1.RequestType5;
		}
	});
	Object.defineProperty(exports, "RequestType6", {
		enumerable: true,
		get: function() {
			return messages_1.RequestType6;
		}
	});
	Object.defineProperty(exports, "RequestType7", {
		enumerable: true,
		get: function() {
			return messages_1.RequestType7;
		}
	});
	Object.defineProperty(exports, "RequestType8", {
		enumerable: true,
		get: function() {
			return messages_1.RequestType8;
		}
	});
	Object.defineProperty(exports, "RequestType9", {
		enumerable: true,
		get: function() {
			return messages_1.RequestType9;
		}
	});
	Object.defineProperty(exports, "ResponseError", {
		enumerable: true,
		get: function() {
			return messages_1.ResponseError;
		}
	});
	Object.defineProperty(exports, "ErrorCodes", {
		enumerable: true,
		get: function() {
			return messages_1.ErrorCodes;
		}
	});
	Object.defineProperty(exports, "NotificationType", {
		enumerable: true,
		get: function() {
			return messages_1.NotificationType;
		}
	});
	Object.defineProperty(exports, "NotificationType0", {
		enumerable: true,
		get: function() {
			return messages_1.NotificationType0;
		}
	});
	Object.defineProperty(exports, "NotificationType1", {
		enumerable: true,
		get: function() {
			return messages_1.NotificationType1;
		}
	});
	Object.defineProperty(exports, "NotificationType2", {
		enumerable: true,
		get: function() {
			return messages_1.NotificationType2;
		}
	});
	Object.defineProperty(exports, "NotificationType3", {
		enumerable: true,
		get: function() {
			return messages_1.NotificationType3;
		}
	});
	Object.defineProperty(exports, "NotificationType4", {
		enumerable: true,
		get: function() {
			return messages_1.NotificationType4;
		}
	});
	Object.defineProperty(exports, "NotificationType5", {
		enumerable: true,
		get: function() {
			return messages_1.NotificationType5;
		}
	});
	Object.defineProperty(exports, "NotificationType6", {
		enumerable: true,
		get: function() {
			return messages_1.NotificationType6;
		}
	});
	Object.defineProperty(exports, "NotificationType7", {
		enumerable: true,
		get: function() {
			return messages_1.NotificationType7;
		}
	});
	Object.defineProperty(exports, "NotificationType8", {
		enumerable: true,
		get: function() {
			return messages_1.NotificationType8;
		}
	});
	Object.defineProperty(exports, "NotificationType9", {
		enumerable: true,
		get: function() {
			return messages_1.NotificationType9;
		}
	});
	Object.defineProperty(exports, "ParameterStructures", {
		enumerable: true,
		get: function() {
			return messages_1.ParameterStructures;
		}
	});
	const linkedMap_1 = require_linkedMap();
	Object.defineProperty(exports, "LinkedMap", {
		enumerable: true,
		get: function() {
			return linkedMap_1.LinkedMap;
		}
	});
	Object.defineProperty(exports, "LRUCache", {
		enumerable: true,
		get: function() {
			return linkedMap_1.LRUCache;
		}
	});
	Object.defineProperty(exports, "Touch", {
		enumerable: true,
		get: function() {
			return linkedMap_1.Touch;
		}
	});
	const disposable_1 = require_disposable();
	Object.defineProperty(exports, "Disposable", {
		enumerable: true,
		get: function() {
			return disposable_1.Disposable;
		}
	});
	const events_1 = require_events();
	Object.defineProperty(exports, "Event", {
		enumerable: true,
		get: function() {
			return events_1.Event;
		}
	});
	Object.defineProperty(exports, "Emitter", {
		enumerable: true,
		get: function() {
			return events_1.Emitter;
		}
	});
	const cancellation_1 = require_cancellation();
	Object.defineProperty(exports, "CancellationTokenSource", {
		enumerable: true,
		get: function() {
			return cancellation_1.CancellationTokenSource;
		}
	});
	Object.defineProperty(exports, "CancellationToken", {
		enumerable: true,
		get: function() {
			return cancellation_1.CancellationToken;
		}
	});
	const sharedArrayCancellation_1 = require_sharedArrayCancellation();
	Object.defineProperty(exports, "SharedArraySenderStrategy", {
		enumerable: true,
		get: function() {
			return sharedArrayCancellation_1.SharedArraySenderStrategy;
		}
	});
	Object.defineProperty(exports, "SharedArrayReceiverStrategy", {
		enumerable: true,
		get: function() {
			return sharedArrayCancellation_1.SharedArrayReceiverStrategy;
		}
	});
	const messageReader_1 = require_messageReader();
	Object.defineProperty(exports, "MessageReader", {
		enumerable: true,
		get: function() {
			return messageReader_1.MessageReader;
		}
	});
	Object.defineProperty(exports, "AbstractMessageReader", {
		enumerable: true,
		get: function() {
			return messageReader_1.AbstractMessageReader;
		}
	});
	Object.defineProperty(exports, "ReadableStreamMessageReader", {
		enumerable: true,
		get: function() {
			return messageReader_1.ReadableStreamMessageReader;
		}
	});
	const messageWriter_1 = require_messageWriter();
	Object.defineProperty(exports, "MessageWriter", {
		enumerable: true,
		get: function() {
			return messageWriter_1.MessageWriter;
		}
	});
	Object.defineProperty(exports, "AbstractMessageWriter", {
		enumerable: true,
		get: function() {
			return messageWriter_1.AbstractMessageWriter;
		}
	});
	Object.defineProperty(exports, "WriteableStreamMessageWriter", {
		enumerable: true,
		get: function() {
			return messageWriter_1.WriteableStreamMessageWriter;
		}
	});
	const messageBuffer_1 = require_messageBuffer();
	Object.defineProperty(exports, "AbstractMessageBuffer", {
		enumerable: true,
		get: function() {
			return messageBuffer_1.AbstractMessageBuffer;
		}
	});
	const connection_1 = require_connection();
	Object.defineProperty(exports, "ConnectionStrategy", {
		enumerable: true,
		get: function() {
			return connection_1.ConnectionStrategy;
		}
	});
	Object.defineProperty(exports, "ConnectionOptions", {
		enumerable: true,
		get: function() {
			return connection_1.ConnectionOptions;
		}
	});
	Object.defineProperty(exports, "NullLogger", {
		enumerable: true,
		get: function() {
			return connection_1.NullLogger;
		}
	});
	Object.defineProperty(exports, "createMessageConnection", {
		enumerable: true,
		get: function() {
			return connection_1.createMessageConnection;
		}
	});
	Object.defineProperty(exports, "ProgressToken", {
		enumerable: true,
		get: function() {
			return connection_1.ProgressToken;
		}
	});
	Object.defineProperty(exports, "ProgressType", {
		enumerable: true,
		get: function() {
			return connection_1.ProgressType;
		}
	});
	Object.defineProperty(exports, "Trace", {
		enumerable: true,
		get: function() {
			return connection_1.Trace;
		}
	});
	Object.defineProperty(exports, "TraceValue", {
		enumerable: true,
		get: function() {
			return connection_1.TraceValue;
		}
	});
	Object.defineProperty(exports, "TraceFormat", {
		enumerable: true,
		get: function() {
			return connection_1.TraceFormat;
		}
	});
	Object.defineProperty(exports, "SetTraceNotification", {
		enumerable: true,
		get: function() {
			return connection_1.SetTraceNotification;
		}
	});
	Object.defineProperty(exports, "LogTraceNotification", {
		enumerable: true,
		get: function() {
			return connection_1.LogTraceNotification;
		}
	});
	Object.defineProperty(exports, "ConnectionErrors", {
		enumerable: true,
		get: function() {
			return connection_1.ConnectionErrors;
		}
	});
	Object.defineProperty(exports, "ConnectionError", {
		enumerable: true,
		get: function() {
			return connection_1.ConnectionError;
		}
	});
	Object.defineProperty(exports, "CancellationReceiverStrategy", {
		enumerable: true,
		get: function() {
			return connection_1.CancellationReceiverStrategy;
		}
	});
	Object.defineProperty(exports, "IdCancellationReceiverStrategy", {
		enumerable: true,
		get: function() {
			return connection_1.IdCancellationReceiverStrategy;
		}
	});
	Object.defineProperty(exports, "RequestCancellationReceiverStrategy", {
		enumerable: true,
		get: function() {
			return connection_1.RequestCancellationReceiverStrategy;
		}
	});
	Object.defineProperty(exports, "CancellationSenderStrategy", {
		enumerable: true,
		get: function() {
			return connection_1.CancellationSenderStrategy;
		}
	});
	Object.defineProperty(exports, "CancellationStrategy", {
		enumerable: true,
		get: function() {
			return connection_1.CancellationStrategy;
		}
	});
	Object.defineProperty(exports, "MessageStrategy", {
		enumerable: true,
		get: function() {
			return connection_1.MessageStrategy;
		}
	});
	Object.defineProperty(exports, "TraceValues", {
		enumerable: true,
		get: function() {
			return connection_1.TraceValues;
		}
	});
	exports.RAL = __importDefault(require_ral()).default;
}));
// node_modules/vscode-jsonrpc/lib/node/ril.js
var require_ril = /* @__PURE__ */ __commonJSMin(((exports) => {
	Object.defineProperty(exports, "__esModule", { value: true });
	const util_1 = __require("util");
	const api_1 = require_api();
	var MessageBuffer = class MessageBuffer extends api_1.AbstractMessageBuffer {
		static emptyBuffer = Buffer.allocUnsafe(0);
		constructor(encoding = "utf-8") {
			super(encoding);
		}
		emptyBuffer() {
			return MessageBuffer.emptyBuffer;
		}
		fromString(value, encoding) {
			return Buffer.from(value, encoding);
		}
		toString(value, encoding) {
			if (value instanceof Buffer) return value.toString(encoding);
			else return new util_1.TextDecoder(encoding).decode(value);
		}
		asNative(buffer, length) {
			if (length === void 0) return buffer instanceof Buffer ? buffer : Buffer.from(buffer);
			else return buffer instanceof Buffer ? buffer.slice(0, length) : Buffer.from(buffer, 0, length);
		}
		allocNative(length) {
			return Buffer.allocUnsafe(length);
		}
	};
	var ReadableStreamWrapper = class {
		stream;
		constructor(stream) {
			this.stream = stream;
		}
		onClose(listener) {
			this.stream.on("close", listener);
			return api_1.Disposable.create(() => this.stream.off("close", listener));
		}
		onError(listener) {
			this.stream.on("error", listener);
			return api_1.Disposable.create(() => this.stream.off("error", listener));
		}
		onEnd(listener) {
			this.stream.on("end", listener);
			return api_1.Disposable.create(() => this.stream.off("end", listener));
		}
		onData(listener) {
			this.stream.on("data", listener);
			return api_1.Disposable.create(() => this.stream.off("data", listener));
		}
	};
	var WritableStreamWrapper = class {
		stream;
		constructor(stream) {
			this.stream = stream;
		}
		onClose(listener) {
			this.stream.on("close", listener);
			return api_1.Disposable.create(() => this.stream.off("close", listener));
		}
		onError(listener) {
			this.stream.on("error", listener);
			return api_1.Disposable.create(() => this.stream.off("error", listener));
		}
		onEnd(listener) {
			this.stream.on("end", listener);
			return api_1.Disposable.create(() => this.stream.off("end", listener));
		}
		write(data, encoding) {
			return new Promise((resolve, reject) => {
				const callback = (error) => {
					if (error === void 0 || error === null) resolve();
					else reject(error);
				};
				if (typeof data === "string") this.stream.write(data, encoding, callback);
				else this.stream.write(data, callback);
			});
		}
		end() {
			this.stream.end();
		}
	};
	const _ril = Object.freeze({
		messageBuffer: Object.freeze({ create: (encoding) => new MessageBuffer(encoding) }),
		applicationJson: Object.freeze({
			encoder: Object.freeze({
				name: "application/json",
				encode: (msg, options) => {
					try {
						return Promise.resolve(Buffer.from(JSON.stringify(msg, void 0, 0), options.charset));
					} catch (err) {
						return Promise.reject(err);
					}
				}
			}),
			decoder: Object.freeze({
				name: "application/json",
				decode: (buffer, options) => {
					try {
						if (buffer instanceof Buffer) return Promise.resolve(JSON.parse(buffer.toString(options.charset)));
						else return Promise.resolve(JSON.parse(new util_1.TextDecoder(options.charset).decode(buffer)));
					} catch (err) {
						return Promise.reject(err);
					}
				}
			})
		}),
		stream: Object.freeze({
			asReadableStream: (stream) => new ReadableStreamWrapper(stream),
			asWritableStream: (stream) => new WritableStreamWrapper(stream)
		}),
		console,
		timer: Object.freeze({
			setTimeout(callback, ms, ...args) {
				const handle = setTimeout(callback, ms, ...args);
				return { dispose: () => clearTimeout(handle) };
			},
			setImmediate(callback, ...args) {
				const handle = setImmediate(callback, ...args);
				return { dispose: () => clearImmediate(handle) };
			},
			setInterval(callback, ms, ...args) {
				const handle = setInterval(callback, ms, ...args);
				return { dispose: () => clearInterval(handle) };
			}
		})
	});
	function RIL() {
		return _ril;
	}
	(function(RIL) {
		function install() {
			api_1.RAL.install(_ril);
		}
		RIL.install = install;
	})(RIL || (RIL = {}));
	exports.default = RIL;
}));
// node_modules/vscode-jsonrpc/lib/node/main.js
var require_main = /* @__PURE__ */ __commonJSMin(((exports, module, this$1) => {
	var __createBinding = this$1 && this$1.__createBinding || (Object.create ? (function(o, m, k, k2) {
		if (k2 === void 0) k2 = k;
		var desc = Object.getOwnPropertyDescriptor(m, k);
		if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) desc = {
			enumerable: true,
			get: function() {
				return m[k];
			}
		};
		Object.defineProperty(o, k2, desc);
	}) : (function(o, m, k, k2) {
		if (k2 === void 0) k2 = k;
		o[k2] = m[k];
	}));
	var __setModuleDefault = this$1 && this$1.__setModuleDefault || (Object.create ? (function(o, v) {
		Object.defineProperty(o, "default", {
			enumerable: true,
			value: v
		});
	}) : function(o, v) {
		o["default"] = v;
	});
	var __importStar = this$1 && this$1.__importStar || (function() {
		var ownKeys = function(o) {
			ownKeys = Object.getOwnPropertyNames || function(o) {
				var ar = [];
				for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
				return ar;
			};
			return ownKeys(o);
		};
		return function(mod) {
			if (mod && mod.__esModule) return mod;
			var result = {};
			if (mod != null) {
				for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
			}
			__setModuleDefault(result, mod);
			return result;
		};
	})();
	var __exportStar = this$1 && this$1.__exportStar || function(m, exports) {
		for (var p in m) if (p !== "default" && !Object.prototype.hasOwnProperty.call(exports, p)) __createBinding(exports, m, p);
	};
	var __importDefault = this$1 && this$1.__importDefault || function(mod) {
		return mod && mod.__esModule ? mod : { "default": mod };
	};
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.StreamMessageWriter = exports.StreamMessageReader = exports.SocketMessageWriter = exports.SocketMessageReader = exports.PortMessageWriter = exports.PortMessageReader = exports.IPCMessageWriter = exports.IPCMessageReader = void 0;
	exports.generateRandomPipeName = generateRandomPipeName;
	exports.createClientPipeTransport = createClientPipeTransport;
	exports.createServerPipeTransport = createServerPipeTransport;
	exports.createClientSocketTransport = createClientSocketTransport;
	exports.createServerSocketTransport = createServerSocketTransport;
	exports.createMessageConnection = createMessageConnection;
	const ril_1 = __importDefault(require_ril());
	ril_1.default.install();
	const path = __importStar(__require("path"));
	const os = __importStar(__require("os"));
	const fs = __importStar(__require("fs"));
	const crypto_1 = __require("crypto");
	const net_1 = __require("net");
	const api_1 = require_api();
	__exportStar(require_api(), exports);
	var IPCMessageReader = class extends api_1.AbstractMessageReader {
		process;
		constructor(process) {
			super();
			this.process = process;
			const eventEmitter = this.process;
			eventEmitter.on("error", (error) => this.fireError(error));
			eventEmitter.on("close", () => this.fireClose());
		}
		listen(callback) {
			this.process.on("message", callback);
			return api_1.Disposable.create(() => this.process.off("message", callback));
		}
	};
	exports.IPCMessageReader = IPCMessageReader;
	var IPCMessageWriter = class extends api_1.AbstractMessageWriter {
		process;
		errorCount;
		constructor(process) {
			super();
			this.process = process;
			this.errorCount = 0;
			const eventEmitter = this.process;
			eventEmitter.on("error", (error) => this.fireError(error));
			eventEmitter.on("close", () => this.fireClose);
		}
		write(msg) {
			try {
				if (typeof this.process.send === "function") this.process.send(msg, void 0, void 0, (error) => {
					if (error) {
						this.errorCount++;
						this.handleError(error, msg);
					} else this.errorCount = 0;
				});
				return Promise.resolve();
			} catch (error) {
				this.handleError(error, msg);
				return Promise.reject(error);
			}
		}
		handleError(error, msg) {
			this.errorCount++;
			this.fireError(error, msg, this.errorCount);
		}
		end() {}
	};
	exports.IPCMessageWriter = IPCMessageWriter;
	var PortMessageReader = class extends api_1.AbstractMessageReader {
		onData;
		constructor(port) {
			super();
			this.onData = new api_1.Emitter();
			port.on("close", () => this.fireClose);
			port.on("error", (error) => this.fireError(error));
			port.on("message", (message) => {
				this.onData.fire(message);
			});
		}
		listen(callback) {
			return this.onData.event(callback);
		}
	};
	exports.PortMessageReader = PortMessageReader;
	var PortMessageWriter = class extends api_1.AbstractMessageWriter {
		port;
		errorCount;
		constructor(port) {
			super();
			this.port = port;
			this.errorCount = 0;
			port.on("close", () => this.fireClose());
			port.on("error", (error) => this.fireError(error));
		}
		write(msg) {
			try {
				this.port.postMessage(msg);
				return Promise.resolve();
			} catch (error) {
				this.handleError(error, msg);
				return Promise.reject(error);
			}
		}
		handleError(error, msg) {
			this.errorCount++;
			this.fireError(error, msg, this.errorCount);
		}
		end() {}
	};
	exports.PortMessageWriter = PortMessageWriter;
	var SocketMessageReader = class extends api_1.ReadableStreamMessageReader {
		constructor(socket, encoding = "utf-8") {
			super((0, ril_1.default)().stream.asReadableStream(socket), encoding);
		}
	};
	exports.SocketMessageReader = SocketMessageReader;
	var SocketMessageWriter = class extends api_1.WriteableStreamMessageWriter {
		socket;
		constructor(socket, options) {
			super((0, ril_1.default)().stream.asWritableStream(socket), options);
			this.socket = socket;
		}
		dispose() {
			super.dispose();
			this.socket.destroy();
		}
	};
	exports.SocketMessageWriter = SocketMessageWriter;
	var StreamMessageReader = class extends api_1.ReadableStreamMessageReader {
		constructor(readable, encoding) {
			super((0, ril_1.default)().stream.asReadableStream(readable), encoding);
		}
	};
	exports.StreamMessageReader = StreamMessageReader;
	var StreamMessageWriter = class extends api_1.WriteableStreamMessageWriter {
		constructor(writable, options) {
			super((0, ril_1.default)().stream.asWritableStream(writable), options);
		}
	};
	exports.StreamMessageWriter = StreamMessageWriter;
	const XDG_RUNTIME_DIR = process.env["XDG_RUNTIME_DIR"];
	const safeIpcPathLengths = /* @__PURE__ */ new Map([["linux", 107], ["darwin", 102]]);
	function generateRandomPipeName() {
		if (process.platform === "win32") return `\\\\.\\pipe\\lsp-${(0, crypto_1.randomBytes)(16).toString("hex")}-sock`;
		let randomLength = 32;
		const fixedLength = 10;
		const tmpDir = fs.realpathSync(XDG_RUNTIME_DIR ?? os.tmpdir());
		const limit = safeIpcPathLengths.get(process.platform);
		if (limit !== void 0) randomLength = Math.min(limit - tmpDir.length - fixedLength, randomLength);
		if (randomLength < 16) throw new Error(`Unable to generate a random pipe name with ${randomLength} characters.`);
		const randomSuffix = (0, crypto_1.randomBytes)(Math.floor(randomLength / 2)).toString("hex");
		return path.join(tmpDir, `lsp-${randomSuffix}.sock`);
	}
	function createClientPipeTransport(pipeName, encoding = "utf-8") {
		let connectResolve;
		const connected = new Promise((resolve, _reject) => {
			connectResolve = resolve;
		});
		return new Promise((resolve, reject) => {
			const server = (0, net_1.createServer)((socket) => {
				server.close();
				connectResolve([new SocketMessageReader(socket, encoding), new SocketMessageWriter(socket, encoding)]);
			});
			server.on("error", reject);
			server.listen(pipeName, () => {
				server.removeListener("error", reject);
				resolve({ onConnected: () => {
					return connected;
				} });
			});
		});
	}
	function createServerPipeTransport(pipeName, encoding = "utf-8") {
		const socket = (0, net_1.createConnection)(pipeName);
		return [new SocketMessageReader(socket, encoding), new SocketMessageWriter(socket, encoding)];
	}
	function createClientSocketTransport(port, encoding = "utf-8") {
		let connectResolve;
		const connected = new Promise((resolve, _reject) => {
			connectResolve = resolve;
		});
		return new Promise((resolve, reject) => {
			const server = (0, net_1.createServer)((socket) => {
				server.close();
				connectResolve([new SocketMessageReader(socket, encoding), new SocketMessageWriter(socket, encoding)]);
			});
			server.on("error", reject);
			server.listen(port, "127.0.0.1", () => {
				server.removeListener("error", reject);
				const address = server.address();
				if (address === null || typeof address === "string") {
					reject(/* @__PURE__ */ new Error(`Unexpected server address: ${address}`));
					return;
				}
				const boundPort = address.port;
				resolve({
					port: () => boundPort,
					onConnected: () => {
						return connected;
					}
				});
			});
		});
	}
	function createServerSocketTransport(port, encoding = "utf-8") {
		const socket = (0, net_1.createConnection)(port, "127.0.0.1");
		return [new SocketMessageReader(socket, encoding), new SocketMessageWriter(socket, encoding)];
	}
	function isReadableStream(value) {
		const candidate = value;
		return candidate.read !== void 0 && candidate.addListener !== void 0;
	}
	function isWritableStream(value) {
		const candidate = value;
		return candidate.write !== void 0 && candidate.addListener !== void 0;
	}
	function createMessageConnection(input, output, logger, options) {
		if (!logger) logger = api_1.NullLogger;
		const reader = isReadableStream(input) ? new StreamMessageReader(input) : input;
		const writer = isWritableStream(output) ? new StreamMessageWriter(output) : output;
		if (api_1.ConnectionStrategy.is(options)) options = { connectionStrategy: options };
		return (0, api_1.createMessageConnection)(reader, writer, logger, options);
	}
}));
// node_modules/@emnapi/wasi-threads/dist/wasi-threads.esm-bundler.js
var import_api = require_api();
var import_main = require_main();
var _WebAssembly$2 = typeof WebAssembly !== "undefined" ? WebAssembly : typeof WXWebAssembly !== "undefined" ? WXWebAssembly : void 0;
var ENVIRONMENT_IS_NODE = typeof process === "object" && process !== null && typeof process.versions === "object" && process.versions !== null && typeof process.versions.node === "string";
function getPostMessage(options) {
	return typeof (options === null || options === void 0 ? void 0 : options.postMessage) === "function" ? options.postMessage : typeof postMessage === "function" ? postMessage : void 0;
}
function serizeErrorToBuffer(sab, code, error) {
	var i32array = new Int32Array(sab);
	Atomics.store(i32array, 0, code);
	if (code > 1 && error) {
		var name_1 = error.name;
		var message = error.message;
		var stack = error.stack;
		var nameBuffer = new TextEncoder().encode(name_1);
		var messageBuffer = new TextEncoder().encode(message);
		var stackBuffer = new TextEncoder().encode(stack);
		Atomics.store(i32array, 1, nameBuffer.length);
		Atomics.store(i32array, 2, messageBuffer.length);
		Atomics.store(i32array, 3, stackBuffer.length);
		var buffer = new Uint8Array(sab);
		buffer.set(nameBuffer, 16);
		buffer.set(messageBuffer, 16 + nameBuffer.length);
		buffer.set(stackBuffer, 16 + nameBuffer.length + messageBuffer.length);
	}
}
function deserizeErrorFromBuffer(sab) {
	var _a, _b;
	var i32array = new Int32Array(sab);
	if (Atomics.load(i32array, 0) <= 1) return null;
	var nameLength = Atomics.load(i32array, 1);
	var messageLength = Atomics.load(i32array, 2);
	var stackLength = Atomics.load(i32array, 3);
	var buffer = new Uint8Array(sab);
	var nameBuffer = buffer.slice(16, 16 + nameLength);
	var messageBuffer = buffer.slice(16 + nameLength, 16 + nameLength + messageLength);
	var stackBuffer = buffer.slice(16 + nameLength + messageLength, 16 + nameLength + messageLength + stackLength);
	var name = new TextDecoder().decode(nameBuffer);
	var message = new TextDecoder().decode(messageBuffer);
	var stack = new TextDecoder().decode(stackBuffer);
	var error = new ((_a = globalThis[name]) !== null && _a !== void 0 ? _a : name === "RuntimeError" ? (_b = _WebAssembly$2.RuntimeError) !== null && _b !== void 0 ? _b : Error : Error)(message);
	Object.defineProperty(error, "stack", {
		value: stack,
		writable: true,
		enumerable: false,
		configurable: true
	});
	return error;
}
/** @public */
function isSharedArrayBuffer(value) {
	return typeof SharedArrayBuffer === "function" && value instanceof SharedArrayBuffer || Object.prototype.toString.call(value) === "[object SharedArrayBuffer]";
}
/** @public */
function isTrapError(e) {
	try {
		return e instanceof _WebAssembly$2.RuntimeError;
	} catch (_) {
		return false;
	}
}
function createMessage(type, payload) {
	return { __emnapi__: {
		type,
		payload
	} };
}
var WASI_THREADS_MAX_TID = 536870911;
function checkSharedWasmMemory(wasmMemory) {
	if (wasmMemory) {
		if (!isSharedArrayBuffer(wasmMemory.buffer)) throw new Error("Multithread features require shared wasm memory. Try to compile with `-matomics -mbulk-memory` and use `--import-memory --shared-memory` during linking, then create WebAssembly.Memory with `shared: true` option");
	} else if (typeof SharedArrayBuffer === "undefined") throw new Error("Current environment does not support SharedArrayBuffer, threads are not available!");
}
function getReuseWorker(value) {
	var _a;
	if (typeof value === "boolean") return value ? {
		size: 0,
		strict: false
	} : false;
	if (typeof value === "number") {
		if (!(value >= 0)) throw new RangeError("reuseWorker: size must be a non-negative integer");
		return {
			size: value,
			strict: false
		};
	}
	if (!value) return false;
	var size = (_a = Number(value.size)) !== null && _a !== void 0 ? _a : 0;
	var strict = Boolean(value.strict);
	if (!(size > 0) && strict) throw new RangeError("reuseWorker: size must be set to positive integer if strict is set to true");
	return {
		size,
		strict
	};
}
var nextWorkerID = 0;
/** @public */
var ThreadManager = /* @__PURE__ */ function() {
	function ThreadManager(options) {
		var _a;
		this.unusedWorkers = [];
		this.pthreads = Object.create(null);
		this.wasmModule = null;
		this.wasmMemory = null;
		this.messageEvents = /* @__PURE__ */ new WeakMap();
		if (!options) throw new TypeError("ThreadManager(): options is not provided");
		if ("childThread" in options) this._childThread = Boolean(options.childThread);
		else this._childThread = false;
		if (this._childThread) {
			this._onCreateWorker = void 0;
			this._reuseWorker = false;
			this._beforeLoad = void 0;
		} else {
			this._onCreateWorker = options.onCreateWorker;
			this._reuseWorker = getReuseWorker(options.reuseWorker);
			this._beforeLoad = options.beforeLoad;
		}
		this.printErr = (_a = options.printErr) !== null && _a !== void 0 ? _a : console.error.bind(console);
		this.threadSpawn = options.threadSpawn;
	}
	Object.defineProperty(ThreadManager.prototype, "nextWorkerID", {
		get: function() {
			return nextWorkerID;
		},
		enumerable: false,
		configurable: true
	});
	ThreadManager.prototype.init = function() {
		if (!this._childThread) this.initMainThread();
	};
	ThreadManager.prototype.initMainThread = function() {
		this.preparePool();
	};
	ThreadManager.prototype.preparePool = function() {
		if (this._reuseWorker) {
			if (this._reuseWorker.size) {
				var pthreadPoolSize = this._reuseWorker.size;
				while (pthreadPoolSize--) {
					var worker = this.allocateUnusedWorker();
					if (ENVIRONMENT_IS_NODE) {
						worker.once("message", function() {});
						worker.unref();
					}
				}
			}
		}
	};
	ThreadManager.prototype.shouldPreloadWorkers = function() {
		return !this._childThread && this._reuseWorker && this._reuseWorker.size > 0;
	};
	ThreadManager.prototype.loadWasmModuleToAllWorkers = function() {
		var _this_1 = this;
		var promises = Array(this.unusedWorkers.length);
		var _loop_1 = function(i) {
			var worker = this_1.unusedWorkers[i];
			if (ENVIRONMENT_IS_NODE) worker.ref();
			promises[i] = this_1.loadWasmModuleToWorker(worker).then(function(w) {
				if (ENVIRONMENT_IS_NODE) worker.unref();
				return w;
			}, function(e) {
				if (ENVIRONMENT_IS_NODE) worker.unref();
				throw e;
			});
		};
		var this_1 = this;
		for (var i = 0; i < this.unusedWorkers.length; ++i) _loop_1(i);
		return Promise.all(promises).catch(function(err) {
			_this_1.terminateAllThreads();
			throw err;
		});
	};
	ThreadManager.prototype.preloadWorkers = function() {
		if (this.shouldPreloadWorkers()) return this.loadWasmModuleToAllWorkers();
		return Promise.resolve([]);
	};
	ThreadManager.prototype.setup = function(wasmModule, wasmMemory) {
		this.wasmModule = wasmModule;
		this.wasmMemory = wasmMemory;
	};
	ThreadManager.prototype.markId = function(worker) {
		if (worker.__emnapi_tid) return worker.__emnapi_tid;
		var tid = nextWorkerID + 43;
		nextWorkerID = (nextWorkerID + 1) % (WASI_THREADS_MAX_TID - 42);
		this.pthreads[tid] = worker;
		worker.__emnapi_tid = tid;
		return tid;
	};
	ThreadManager.prototype.returnWorkerToPool = function(worker) {
		var tid = worker.__emnapi_tid;
		if (tid !== void 0) delete this.pthreads[tid];
		this.unusedWorkers.push(worker);
		delete worker.__emnapi_tid;
		if (ENVIRONMENT_IS_NODE) worker.unref();
	};
	ThreadManager.prototype.loadWasmModuleToWorker = function(worker, sab) {
		var _this_1 = this;
		if (worker.whenLoaded) return worker.whenLoaded;
		var err = this.printErr;
		var beforeLoad = this._beforeLoad;
		var _this = this;
		worker.whenLoaded = new Promise(function(resolve, reject) {
			var handleError = function(e) {
				var message = "worker sent an error!";
				if (worker.__emnapi_tid !== void 0) message = "worker (tid = " + worker.__emnapi_tid + ") sent an error!";
				if ("message" in e) {
					err(message + " " + e.message);
					if (e.message.indexOf("RuntimeError") !== -1 || e.message.indexOf("unreachable") !== -1) try {
						_this.terminateAllThreads();
					} catch (_) {}
				} else err(message);
				reject(e);
				throw e;
			};
			var handleMessage = function(data) {
				if (data.__emnapi__) {
					var type = data.__emnapi__.type;
					var payload = data.__emnapi__.payload;
					if (type === "loaded") {
						worker.loaded = true;
						if (ENVIRONMENT_IS_NODE && !worker.__emnapi_tid) worker.unref();
						resolve(worker);
					} else if (type === "cleanup-thread") {
						if (payload.tid in _this_1.pthreads) _this_1.cleanThread(worker, payload.tid);
					} else if (type === "spawn-thread") _this_1.threadSpawn(payload.startArg, payload.errorOrTid);
					else if (type === "terminate-all-threads") _this_1.terminateAllThreads();
				}
			};
			worker.onmessage = function(e) {
				handleMessage(e.data);
				_this_1.fireMessageEvent(worker, e);
			};
			worker.onerror = handleError;
			if (ENVIRONMENT_IS_NODE) {
				worker.on("message", function(data) {
					var _a, _b;
					(_b = (_a = worker).onmessage) === null || _b === void 0 || _b.call(_a, { data });
				});
				worker.on("error", function(e) {
					var _a, _b;
					(_b = (_a = worker).onerror) === null || _b === void 0 || _b.call(_a, e);
				});
				worker.on("detachedExit", function() {});
			}
			if (typeof beforeLoad === "function") beforeLoad(worker);
			try {
				worker.postMessage(createMessage("load", {
					wasmModule: _this_1.wasmModule,
					wasmMemory: _this_1.wasmMemory,
					sab
				}));
			} catch (err) {
				checkSharedWasmMemory(_this_1.wasmMemory);
				throw err;
			}
		});
		return worker.whenLoaded;
	};
	ThreadManager.prototype.allocateUnusedWorker = function() {
		var _onCreateWorker = this._onCreateWorker;
		if (typeof _onCreateWorker !== "function") throw new TypeError("`options.onCreateWorker` is not provided");
		var worker = _onCreateWorker({
			type: "thread",
			name: "emnapi-pthread"
		});
		this.unusedWorkers.push(worker);
		return worker;
	};
	ThreadManager.prototype.getNewWorker = function(sab) {
		if (this._reuseWorker) {
			if (this.unusedWorkers.length === 0) {
				if (this._reuseWorker.strict) {
					if (!ENVIRONMENT_IS_NODE) {
						(0, this.printErr)("Tried to spawn a new thread, but the thread pool is exhausted.\nThis might result in a deadlock unless some threads eventually exit or the code explicitly breaks out to the event loop.");
						return;
					}
				}
				var worker_1 = this.allocateUnusedWorker();
				this.loadWasmModuleToWorker(worker_1, sab);
			}
			return this.unusedWorkers.pop();
		}
		var worker = this.allocateUnusedWorker();
		this.loadWasmModuleToWorker(worker, sab);
		return this.unusedWorkers.pop();
	};
	ThreadManager.prototype.cleanThread = function(worker, tid, force) {
		if (!force && this._reuseWorker) this.returnWorkerToPool(worker);
		else {
			delete this.pthreads[tid];
			this.terminateWorker(worker);
			delete worker.__emnapi_tid;
		}
	};
	ThreadManager.prototype.terminateWorker = function(worker) {
		var _this_1 = this;
		var _a;
		var tid = worker.__emnapi_tid;
		worker.terminate();
		(_a = this.messageEvents.get(worker)) === null || _a === void 0 || _a.clear();
		this.messageEvents.delete(worker);
		worker.onmessage = function(e) {
			if (e.data.__emnapi__) (0, _this_1.printErr)("received \"" + e.data.__emnapi__.type + "\" command from terminated worker: " + tid);
		};
	};
	ThreadManager.prototype.terminateAllThreads = function() {
		var runningWorkers = Object.values(this.pthreads);
		for (var i = 0; i < runningWorkers.length; ++i) this.terminateWorker(runningWorkers[i]);
		for (var i = 0; i < this.unusedWorkers.length; ++i) this.terminateWorker(this.unusedWorkers[i]);
		this.unusedWorkers = [];
		this.pthreads = Object.create(null);
		this.preparePool();
	};
	ThreadManager.prototype.addMessageEventListener = function(worker, onMessage) {
		var listeners = this.messageEvents.get(worker);
		if (!listeners) {
			listeners = /* @__PURE__ */ new Set();
			this.messageEvents.set(worker, listeners);
		}
		listeners.add(onMessage);
		return function() {
			listeners === null || listeners === void 0 || listeners.delete(onMessage);
		};
	};
	ThreadManager.prototype.fireMessageEvent = function(worker, e) {
		var listeners = this.messageEvents.get(worker);
		if (!listeners) return;
		var err = this.printErr;
		listeners.forEach(function(listener) {
			try {
				listener(e);
			} catch (e) {
				err(e.stack);
			}
		});
	};
	return ThreadManager;
}();
var kIsProxy = Symbol("kIsProxy");
/** @public */
function createInstanceProxy(instance, memory) {
	if (instance[kIsProxy]) return instance;
	var originalExports = instance.exports;
	var createHandler = function(target) {
		var handlers = [
			"apply",
			"construct",
			"defineProperty",
			"deleteProperty",
			"get",
			"getOwnPropertyDescriptor",
			"getPrototypeOf",
			"has",
			"isExtensible",
			"ownKeys",
			"preventExtensions",
			"set",
			"setPrototypeOf"
		];
		var handler = {};
		var _loop_1 = function(i) {
			var name_1 = handlers[i];
			handler[name_1] = function() {
				var args = Array.prototype.slice.call(arguments, 1);
				args.unshift(target);
				return Reflect[name_1].apply(Reflect, args);
			};
		};
		for (var i = 0; i < handlers.length; i++) _loop_1(i);
		return handler;
	};
	var handler = createHandler(originalExports);
	var _initialize = function() {};
	var _start = function() {
		return 0;
	};
	handler.get = function(_target, p, receiver) {
		var _a;
		if (p === "memory") return (_a = typeof memory === "function" ? memory() : memory) !== null && _a !== void 0 ? _a : Reflect.get(originalExports, p, receiver);
		if (p === "_initialize") return p in originalExports ? _initialize : void 0;
		if (p === "_start") return p in originalExports ? _start : void 0;
		return Reflect.get(originalExports, p, receiver);
	};
	handler.has = function(_target, p) {
		if (p === "memory") return true;
		return Reflect.has(originalExports, p);
	};
	var exportsProxy = new Proxy(Object.create(null), handler);
	return new Proxy(instance, { get: function(target, p, receiver) {
		if (p === "exports") return exportsProxy;
		if (p === kIsProxy) return true;
		return Reflect.get(target, p, receiver);
	} });
}
var patchedWasiInstances = /* @__PURE__ */ new WeakMap();
var THREAD_SPAWN_RESULT_SIZE = Int32Array.BYTES_PER_ELEMENT * 2;
var sharedArrayBufferByteLength = typeof SharedArrayBuffer === "function" ? Object.getOwnPropertyDescriptor(SharedArrayBuffer.prototype, "byteLength").get : void 0;
function isConclusivelySharedArrayBuffer(buffer) {
	if (sharedArrayBufferByteLength === void 0) return false;
	try {
		sharedArrayBufferByteLength.call(buffer);
		return true;
	} catch (_) {
		return false;
	}
}
function getThreadSpawnResultView(memory, address, wasm64) {
	var offset = typeof address === "bigint" ? Number(address) : address >>> 0;
	var buffer = memory.buffer;
	if (offset + THREAD_SPAWN_RESULT_SIZE > buffer.byteLength && isConclusivelySharedArrayBuffer(buffer)) {
		if (wasm64) memory.grow(BigInt(0));
		else memory.grow(0);
		buffer = memory.buffer;
	}
	return new Int32Array(buffer, offset, 2);
}
/** @public */
var WASIThreads = /* @__PURE__ */ function() {
	function WASIThreads(options) {
		var _this_1 = this;
		if (!options) throw new TypeError("WASIThreads(): options is not provided");
		if (!options.wasi) throw new TypeError("WASIThreads(): options.wasi is not provided");
		patchedWasiInstances.set(this, /* @__PURE__ */ new WeakSet());
		var wasi = options.wasi;
		patchWasiInstance(this, wasi);
		this.wasi = wasi;
		if ("childThread" in options) this.childThread = Boolean(options.childThread);
		else this.childThread = false;
		this.PThread = void 0;
		if ("threadManager" in options) {
			if (typeof options.threadManager === "function") this.PThread = options.threadManager();
			else this.PThread = options.threadManager;
		} else if (!this.childThread) {
			this.PThread = new ThreadManager(options);
			this.PThread.init();
		}
		var waitThreadStart = false;
		if ("waitThreadStart" in options) waitThreadStart = typeof options.waitThreadStart === "number" ? options.waitThreadStart : Boolean(options.waitThreadStart);
		var postMessage = getPostMessage(options);
		if (this.childThread && typeof postMessage !== "function") throw new TypeError("options.postMessage is not a function");
		this.postMessage = postMessage;
		var wasm64 = Boolean(options.wasm64);
		var threadSpawn = function(startArg, errorOrTid) {
			var _a;
			var EAGAIN = 6;
			var isNewABI = errorOrTid !== void 0;
			try {
				checkSharedWasmMemory(_this_1.wasmMemory);
			} catch (err) {
				(_a = _this_1.PThread) === null || _a === void 0 || _a.printErr(err.stack);
				if (isNewABI) {
					var struct_1 = getThreadSpawnResultView(_this_1.wasmMemory, errorOrTid, wasm64);
					Atomics.store(struct_1, 0, 1);
					Atomics.store(struct_1, 1, EAGAIN);
					Atomics.notify(struct_1, 1);
					return 1;
				} else return -EAGAIN;
			}
			if (!isNewABI) {
				var malloc = _this_1.wasmInstance.exports.malloc;
				errorOrTid = wasm64 ? Number(malloc(BigInt(8))) : malloc(8) >>> 0;
				if (!errorOrTid) return -48;
			}
			var _free = _this_1.wasmInstance.exports.free;
			var free = wasm64 ? function(ptr) {
				_free(BigInt(ptr));
			} : _free;
			var struct = getThreadSpawnResultView(_this_1.wasmMemory, errorOrTid, wasm64);
			Atomics.store(struct, 0, 0);
			Atomics.store(struct, 1, 0);
			if (_this_1.childThread) {
				postMessage(createMessage("spawn-thread", {
					startArg,
					errorOrTid
				}));
				Atomics.wait(struct, 1, 0);
				var isError = Atomics.load(struct, 0);
				var result = Atomics.load(struct, 1);
				if (isNewABI) return isError;
				free(errorOrTid);
				return isError ? -result : result;
			}
			var shouldWait = waitThreadStart || waitThreadStart === 0;
			var sab;
			if (shouldWait) {
				sab = new Int32Array(new SharedArrayBuffer(8208));
				Atomics.store(sab, 0, 0);
			}
			var worker;
			var tid;
			var PThread = _this_1.PThread;
			try {
				worker = PThread.getNewWorker(sab);
				if (!worker) throw new Error("failed to get new worker");
				tid = PThread.markId(worker);
				if (ENVIRONMENT_IS_NODE) worker.unref();
				worker.postMessage(createMessage("start", {
					tid,
					arg: startArg,
					sab
				}));
				if (shouldWait) {
					if (typeof waitThreadStart === "number") {
						if (Atomics.wait(sab, 0, 0, waitThreadStart) === "timed-out") throw new Error("Spawning thread timed out. Please check if the worker is created successfully and if message is handled properly in the worker.");
					} else Atomics.wait(sab, 0, 0);
					if (Atomics.load(sab, 0) > 1) throw deserizeErrorFromBuffer(sab.buffer);
				}
			} catch (e) {
				if (worker !== void 0 && tid !== void 0) try {
					PThread.cleanThread(worker, tid, true);
				} catch (_) {}
				Atomics.store(struct, 0, 1);
				Atomics.store(struct, 1, EAGAIN);
				Atomics.notify(struct, 1);
				PThread === null || PThread === void 0 || PThread.printErr(e.stack);
				if (isNewABI) return 1;
				free(errorOrTid);
				return -EAGAIN;
			}
			Atomics.store(struct, 0, 0);
			Atomics.store(struct, 1, tid);
			Atomics.notify(struct, 1);
			if (!shouldWait) worker.whenLoaded.catch(function(err) {
				delete worker.whenLoaded;
				PThread.cleanThread(worker, tid, true);
				throw err;
			});
			if (isNewABI) return 0;
			free(errorOrTid);
			return tid;
		};
		this.threadSpawn = threadSpawn;
		if (this.PThread) this.PThread.threadSpawn = threadSpawn;
	}
	WASIThreads.prototype.getImportObject = function() {
		return { wasi: { "thread-spawn": this.threadSpawn } };
	};
	WASIThreads.prototype.setup = function(wasmInstance, wasmModule, wasmMemory) {
		wasmMemory !== null && wasmMemory !== void 0 || (wasmMemory = wasmInstance.exports.memory);
		this.wasmInstance = wasmInstance;
		this.wasmMemory = wasmMemory;
		if (this.PThread) this.PThread.setup(wasmModule, wasmMemory);
	};
	WASIThreads.prototype.preloadWorkers = function() {
		if (this.PThread) return this.PThread.preloadWorkers();
		return Promise.resolve([]);
	};
	/**
	* It's ok to call this method to a WASI command module.
	*
	* in child thread, must call this method instead of {@link WASIThreads.start} even if it's a WASI command module
	*
	* @returns A proxied WebAssembly instance if in child thread, other wise the original instance
	*/
	WASIThreads.prototype.initialize = function(instance, module, memory) {
		var exports = instance.exports;
		memory !== null && memory !== void 0 || (memory = exports.memory);
		if (this.childThread) instance = createInstanceProxy(instance, memory);
		this.setup(instance, module, memory);
		var wasi = this.wasi;
		if ("_start" in exports && typeof exports._start === "function") {
			if (this.childThread) {
				wasi.start(instance);
				try {
					var kStarted = getWasiSymbol(wasi, "kStarted");
					wasi[kStarted] = false;
				} catch (_) {}
			} else setupInstance(wasi, instance);
		} else wasi.initialize(instance);
		return instance;
	};
	/**
	* Equivalent to calling {@link WASIThreads.initialize} and then calling {@link WASIInstance.start}
	* ```js
	* this.initialize(instance, module, memory)
	* this.wasi.start(instance)
	* ```
	*/
	WASIThreads.prototype.start = function(instance, module, memory) {
		var exports = instance.exports;
		memory !== null && memory !== void 0 || (memory = exports.memory);
		if (this.childThread) instance = createInstanceProxy(instance, memory);
		this.setup(instance, module, memory);
		return {
			exitCode: this.wasi.start(instance),
			instance
		};
	};
	WASIThreads.prototype.terminateAllThreads = function() {
		var _a;
		if (!this.childThread) (_a = this.PThread) === null || _a === void 0 || _a.terminateAllThreads();
		else this.postMessage(createMessage("terminate-all-threads", {}));
	};
	return WASIThreads;
}();
function patchWasiInstance(wasiThreads, wasi) {
	var patched = patchedWasiInstances.get(wasiThreads);
	if (patched.has(wasi)) return;
	var _this = wasiThreads;
	var wasiImport = wasi.wasiImport;
	if (wasiImport) {
		var proc_exit_1 = wasiImport.proc_exit;
		wasiImport.proc_exit = function(code) {
			_this.terminateAllThreads();
			return proc_exit_1.call(this, code);
		};
	}
	if (!_this.childThread) {
		var start_1 = wasi.start;
		if (typeof start_1 === "function") wasi.start = function(instance) {
			try {
				return start_1.call(this, instance);
			} catch (err) {
				if (isTrapError(err)) _this.terminateAllThreads();
				throw err;
			}
		};
	}
	patched.add(wasi);
}
function getWasiSymbol(wasi, description) {
	var symbols = Object.getOwnPropertySymbols(wasi);
	var selectDescription = function(description) {
		return function(s) {
			if (s.description) return s.description === description;
			return s.toString() === "Symbol(".concat(description, ")");
		};
	};
	if (Array.isArray(description)) return description.map(function(d) {
		return symbols.filter(selectDescription(d))[0];
	});
	return symbols.filter(selectDescription(description))[0];
}
function setupInstance(wasi, instance) {
	var _a = getWasiSymbol(wasi, ["kInstance", "kSetMemory"]), kInstance = _a[0], kSetMemory = _a[1];
	wasi[kInstance] = instance;
	wasi[kSetMemory](instance.exports.memory);
}
/** @public */
var ThreadMessageHandler = /* @__PURE__ */ function() {
	function ThreadMessageHandler(options) {
		var postMsg = getPostMessage(options);
		if (typeof postMsg !== "function") throw new TypeError("options.postMessage is not a function");
		this.postMessage = postMsg;
		this.onLoad = options === null || options === void 0 ? void 0 : options.onLoad;
		this.onError = typeof (options === null || options === void 0 ? void 0 : options.onError) === "function" ? options.onError : function(_type, err) {
			throw err;
		};
		this.instance = void 0;
		this.messagesBeforeLoad = [];
	}
	/** @virtual */
	ThreadMessageHandler.prototype.instantiate = function(data) {
		if (typeof this.onLoad === "function") return this.onLoad(data);
		throw new Error("ThreadMessageHandler.prototype.instantiate is not implemented");
	};
	/** @virtual */
	ThreadMessageHandler.prototype.handle = function(e) {
		var _this = this;
		var _a;
		if ((_a = e === null || e === void 0 ? void 0 : e.data) === null || _a === void 0 ? void 0 : _a.__emnapi__) {
			var type = e.data.__emnapi__.type;
			var payload_1 = e.data.__emnapi__.payload;
			try {
				if (type === "load") this._load(payload_1);
				else if (type === "start") this.handleAfterLoad(e, function() {
					_this._start(payload_1);
				});
			} catch (err) {
				this.onError(err, type);
			}
		}
	};
	ThreadMessageHandler.prototype._load = function(payload) {
		var _this = this;
		if (this.instance !== void 0) return;
		var source;
		try {
			source = this.instantiate(payload);
		} catch (err) {
			this._loaded(err, null, payload);
			return;
		}
		var then = source && "then" in source ? source.then : void 0;
		if (typeof then === "function") then.call(source, function(source) {
			_this._loaded(null, source, payload);
		}, function(err) {
			_this._loaded(err, null, payload);
		});
		else this._loaded(null, source, payload);
	};
	ThreadMessageHandler.prototype._start = function(payload) {
		var wasi_thread_start = this.instance.exports.wasi_thread_start;
		if (typeof wasi_thread_start !== "function") {
			var err = /* @__PURE__ */ new TypeError("wasi_thread_start is not exported");
			notifyPthreadCreateResult(payload.sab, 2, err);
			throw err;
		}
		var postMessage = this.postMessage;
		var tid = payload.tid;
		var startArg = payload.arg;
		notifyPthreadCreateResult(payload.sab, 1);
		try {
			wasi_thread_start(tid, startArg);
		} catch (err) {
			if (err !== "unwind") throw err;
			else return;
		}
		postMessage(createMessage("cleanup-thread", { tid }));
	};
	ThreadMessageHandler.prototype._loaded = function(err, source, payload) {
		if (err) {
			notifyPthreadCreateResult(payload.sab, 2, err);
			throw err;
		}
		if (source == null) {
			var err_1 = /* @__PURE__ */ new TypeError("onLoad should return an object");
			notifyPthreadCreateResult(payload.sab, 2, err_1);
			throw err_1;
		}
		var instance = source.instance;
		if (!instance) {
			var err_2 = /* @__PURE__ */ new TypeError("onLoad should return an object which includes \"instance\"");
			notifyPthreadCreateResult(payload.sab, 2, err_2);
			throw err_2;
		}
		this.instance = instance;
		(0, this.postMessage)(createMessage("loaded", {}));
		var messages = this.messagesBeforeLoad;
		this.messagesBeforeLoad = [];
		for (var i = 0; i < messages.length; i++) {
			var data = messages[i];
			this.handle({ data });
		}
	};
	ThreadMessageHandler.prototype.handleAfterLoad = function(e, f) {
		if (this.instance !== void 0) f.call(this, e);
		else this.messagesBeforeLoad.push(e.data);
	};
	return ThreadMessageHandler;
}();
function notifyPthreadCreateResult(sab, result, error) {
	if (sab) {
		serizeErrorToBuffer(sab.buffer, result, error);
		Atomics.notify(sab, 0);
	}
}
// node_modules/tslib/tslib.es6.mjs
/******************************************************************************
Copyright (c) Microsoft Corporation.

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH
REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY
AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR
OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
PERFORMANCE OF THIS SOFTWARE.
***************************************************************************** */
var extendStatics = function(d, b) {
	extendStatics = Object.setPrototypeOf || { __proto__: [] } instanceof Array && function(d, b) {
		d.__proto__ = b;
	} || function(d, b) {
		for (var p in b) if (Object.prototype.hasOwnProperty.call(b, p)) d[p] = b[p];
	};
	return extendStatics(d, b);
};
function __extends(d, b) {
	if (typeof b !== "function" && b !== null) throw new TypeError("Class extends value " + String(b) + " is not a constructor or null");
	extendStatics(d, b);
	function __() {
		this.constructor = d;
	}
	d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
}
var __assign = function() {
	__assign = Object.assign || function __assign(t) {
		for (var s, i = 1, n = arguments.length; i < n; i++) {
			s = arguments[i];
			for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p)) t[p] = s[p];
		}
		return t;
	};
	return __assign.apply(this, arguments);
};
// node_modules/@emnapi/core/dist/emnapi-core.esm-bundler.js
var emnapi_core_esm_bundler_exports = /* @__PURE__ */ __exportAll({
	MessageHandler: () => MessageHandler$1,
	ThreadManager: () => ThreadManager,
	ThreadMessageHandler: () => ThreadMessageHandler,
	WASIThreads: () => WASIThreads,
	createInstanceProxy: () => createInstanceProxy,
	createNapiModule: () => createNapiModule,
	instantiateNapiModule: () => instantiateNapiModule$1,
	instantiateNapiModuleSync: () => instantiateNapiModuleSync$1,
	isSharedArrayBuffer: () => isSharedArrayBuffer,
	isTrapError: () => isTrapError,
	loadNapiModule: () => loadNapiModule,
	loadNapiModuleSync: () => loadNapiModuleSync,
	version: () => version$1
});
var _WebAssembly$1 = typeof WebAssembly !== "undefined" ? WebAssembly : typeof WXWebAssembly !== "undefined" ? WXWebAssembly : void 0;
function validateImports(imports) {
	if (imports && typeof imports !== "object") throw new TypeError("imports must be an object or undefined");
	return true;
}
function load(wasmInput, imports) {
	if (!wasmInput) throw new TypeError("Invalid wasm source");
	validateImports(imports);
	imports = imports !== null && imports !== void 0 ? imports : {};
	try {
		var then = typeof wasmInput === "object" && wasmInput !== null && "then" in wasmInput ? wasmInput.then : void 0;
		if (typeof then === "function") return then.call(wasmInput, function(input) {
			return load(input, imports);
		});
	} catch (_) {}
	if (wasmInput instanceof ArrayBuffer || ArrayBuffer.isView(wasmInput)) return _WebAssembly$1.instantiate(wasmInput, imports);
	if (wasmInput instanceof _WebAssembly$1.Module) return _WebAssembly$1.instantiate(wasmInput, imports).then(function(instance) {
		return {
			instance,
			module: wasmInput
		};
	});
	if (typeof Response !== "undefined" && wasmInput instanceof Response) return wasmInput.arrayBuffer().then(function(buffer) {
		return _WebAssembly$1.instantiate(buffer, imports);
	});
	var inputIsString = typeof wasmInput === "string";
	if (inputIsString || typeof URL !== "undefined" && wasmInput instanceof URL) {
		if (inputIsString && typeof wx !== "undefined" && typeof __wxConfig !== "undefined") return _WebAssembly$1.instantiate(wasmInput, imports);
		if (typeof fetch !== "function") throw new TypeError("wasm source can not be a string or URL in this environment");
		if (typeof _WebAssembly$1.instantiateStreaming === "function") try {
			return _WebAssembly$1.instantiateStreaming(fetch(wasmInput), imports).catch(function() {
				return load(fetch(wasmInput), imports);
			});
		} catch (_) {
			return load(fetch(wasmInput), imports);
		}
		else return load(fetch(wasmInput), imports);
	}
	throw new TypeError("Invalid wasm source");
}
function loadSync(wasmInput, imports) {
	if (!wasmInput) throw new TypeError("Invalid wasm source");
	validateImports(imports);
	imports = imports !== null && imports !== void 0 ? imports : {};
	var module;
	if (wasmInput instanceof ArrayBuffer || ArrayBuffer.isView(wasmInput)) module = new _WebAssembly$1.Module(wasmInput);
	else if (wasmInput instanceof WebAssembly.Module) module = wasmInput;
	else throw new TypeError("Invalid wasm source");
	return {
		instance: new _WebAssembly$1.Instance(module, imports),
		module
	};
}
function createNapiModule(options) {
	return (function() {
		var ENVIRONMENT_IS_NODE = typeof process === "object" && process !== null && typeof process.versions === "object" && process.versions !== null && typeof process.versions.node === "string";
		var ENVIRONMENT_IS_PTHREAD = Boolean(options.childThread);
		var waitThreadStart = typeof options.waitThreadStart === "number" ? options.waitThreadStart : Boolean(options.waitThreadStart);
		var wasmInstance;
		var wasmMemory;
		var wasmTable;
		var _malloc;
		var _free;
		function abort(msg) {
			if (typeof _WebAssembly$1.RuntimeError === "function") throw new _WebAssembly$1.RuntimeError(msg);
			throw Error(msg);
		}
		var napiModule = {
			imports: {
				env: {},
				napi: {},
				emnapi: {}
			},
			exports: {},
			emnapi: {},
			loaded: false,
			filename: "",
			childThread: ENVIRONMENT_IS_PTHREAD,
			initWorker: void 0,
			waitThreadStart,
			PThread: void 0,
			init: function(options) {
				if (napiModule.loaded) return napiModule.exports;
				if (!options) throw new TypeError("Invalid napi init options");
				var instance = options.instance;
				if (!(instance === null || instance === void 0 ? void 0 : instance.exports)) throw new TypeError("Invalid wasm instance");
				wasmInstance = instance;
				var exports = instance.exports;
				var module = options.module;
				var memory = options.memory || exports.memory;
				var table = options.table || exports.__indirect_function_table;
				if (!(module instanceof _WebAssembly$1.Module)) throw new TypeError("Invalid wasm module");
				if (!(memory instanceof _WebAssembly$1.Memory)) throw new TypeError("Invalid wasm memory");
				if (!(table instanceof _WebAssembly$1.Table)) throw new TypeError("Invalid wasm table");
				wasmMemory = memory;
				wasmTable = table;
				if (typeof exports.malloc !== "function") throw new TypeError("malloc is not exported");
				if (typeof exports.free !== "function") throw new TypeError("free is not exported");
				_malloc = exports.malloc;
				_free = exports.free;
				if (!napiModule.childThread) {
					var moduleApiVersion = 8;
					var node_api_module_get_api_version_v1 = instance.exports.node_api_module_get_api_version_v1;
					if (typeof node_api_module_get_api_version_v1 === "function") moduleApiVersion = node_api_module_get_api_version_v1();
					var envObject = napiModule.envObject || (napiModule.envObject = emnapiCtx.createEnv(napiModule.filename, moduleApiVersion, function(cb) {
						return wasmTable.get(cb);
					}, function(cb) {
						return wasmTable.get(cb);
					}, abort, emnapiNodeBinding));
					var scope_1 = emnapiCtx.openScope(envObject);
					try {
						envObject.callIntoModule(function(_envObject) {
							var exports = napiModule.exports;
							var exportsHandle = scope_1.add(exports);
							var napiValue = (0, instance.exports.napi_register_wasm_v1)(_envObject.id, exportsHandle.id);
							napiModule.exports = !napiValue ? exports : emnapiCtx.handleStore.get(napiValue).value;
						});
					} catch (e) {
						if (e !== "unwind") throw e;
					} finally {
						emnapiCtx.closeScope(envObject, scope_1);
					}
					napiModule.loaded = true;
					delete napiModule.envObject;
					return napiModule.exports;
				}
			}
		};
		var emnapiCtx;
		var emnapiNodeBinding;
		var onCreateWorker = void 0;
		var err;
		if (!ENVIRONMENT_IS_PTHREAD) {
			var context = options.context;
			if (typeof context !== "object" || context === null) throw new TypeError("Invalid `options.context`. Use `import { getDefaultContext } from '@emnapi/runtime'`");
			emnapiCtx = context;
		} else {
			emnapiCtx = options === null || options === void 0 ? void 0 : options.context;
			var postMsg = typeof options.postMessage === "function" ? options.postMessage : typeof postMessage === "function" ? postMessage : void 0;
			if (typeof postMsg !== "function") throw new TypeError("No postMessage found");
			napiModule.postMessage = postMsg;
		}
		if (typeof options.filename === "string") napiModule.filename = options.filename;
		if (typeof options.onCreateWorker === "function") onCreateWorker = options.onCreateWorker;
		if (typeof options.print === "function") options.print;
		else console.log.bind(console);
		if (typeof options.printErr === "function") err = options.printErr;
		else err = console.warn.bind(console);
		if ("nodeBinding" in options) {
			var nodeBinding = options.nodeBinding;
			if (typeof nodeBinding !== "object" || nodeBinding === null) throw new TypeError("Invalid `options.nodeBinding`. Use @emnapi/node-binding package");
			emnapiNodeBinding = nodeBinding;
		}
		var emnapiAsyncWorkPoolSize = 0;
		if ("asyncWorkPoolSize" in options) {
			if (typeof options.asyncWorkPoolSize !== "number") throw new TypeError("options.asyncWorkPoolSize must be a integer");
			emnapiAsyncWorkPoolSize = options.asyncWorkPoolSize >> 0;
			if (emnapiAsyncWorkPoolSize > 1024) emnapiAsyncWorkPoolSize = 1024;
			else if (emnapiAsyncWorkPoolSize < -1024) emnapiAsyncWorkPoolSize = -1024;
		}
		var singleThreadAsyncWork = ENVIRONMENT_IS_PTHREAD ? false : emnapiAsyncWorkPoolSize <= 0;
		function _emnapi_async_work_pool_size() {
			return Math.abs(emnapiAsyncWorkPoolSize);
		}
		napiModule.imports.env._emnapi_async_work_pool_size = _emnapi_async_work_pool_size;
		function emnapiAddSendListener(worker) {
			if (!worker) return false;
			if (worker._emnapiSendListener) return true;
			var handler = function(e) {
				var __emnapi__ = (ENVIRONMENT_IS_NODE ? e : e.data).__emnapi__;
				if (__emnapi__ && __emnapi__.type === "async-send") {
					if (ENVIRONMENT_IS_PTHREAD) (0, napiModule.postMessage)({ __emnapi__ });
					else {
						var callback = __emnapi__.payload.callback;
						wasmTable.get(callback)(__emnapi__.payload.data);
					}
				}
			};
			var dispose = function() {
				if (ENVIRONMENT_IS_NODE) worker.off("message", handler);
				else worker.removeEventListener("message", handler, false);
				delete worker._emnapiSendListener;
			};
			worker._emnapiSendListener = {
				handler,
				dispose
			};
			if (ENVIRONMENT_IS_NODE) worker.on("message", handler);
			else worker.addEventListener("message", handler, false);
			return true;
		}
		napiModule.emnapi.addSendListener = emnapiAddSendListener;
		var PThread = new ThreadManager(ENVIRONMENT_IS_PTHREAD ? {
			printErr: err,
			childThread: true
		} : {
			printErr: err,
			beforeLoad: function(worker) {
				emnapiAddSendListener(worker);
			},
			reuseWorker: options.reuseWorker,
			onCreateWorker
		});
		napiModule.PThread = PThread;
		/**
		* @__sig ipiip
		*/
		function napi_set_last_error(env, error_code, engine_error_code, engine_reserved) {
			return emnapiCtx.envStore.get(env).setLastError(error_code, engine_error_code, engine_reserved);
		}
		/**
		* @__sig ip
		*/
		function napi_clear_last_error(env) {
			return emnapiCtx.envStore.get(env).clearLastError();
		}
		/**
		* @__sig vppp
		*/
		function _emnapi_get_node_version(major, minor, patch) {
			major >>>= 0;
			minor >>>= 0;
			patch >>>= 0;
			var versions = typeof process === "object" && process !== null && typeof process.versions === "object" && process.versions !== null && typeof process.versions.node === "string" ? process.versions.node.split(".").map(function(n) {
				return Number(n);
			}) : [
				0,
				0,
				0
			];
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(major, versions[0], true);
			GET_HEAP_DATA_VIEW().setUint32(minor, versions[1], true);
			GET_HEAP_DATA_VIEW().setUint32(patch, versions[2], true);
		}
		/**
		* @__sig v
		* @__deps $runtimeKeepalivePush
		*/
		function _emnapi_runtime_keepalive_push() {}
		/**
		* @__sig v
		* @__deps $runtimeKeepalivePop
		*/
		function _emnapi_runtime_keepalive_pop() {}
		/**
		* @__sig vpp
		*/
		function _emnapi_set_immediate(callback, data) {
			emnapiCtx.feature.setImmediate(function() {
				wasmTable.get(callback)(data);
			});
		}
		/**
		* @__sig vpp
		*/
		function _emnapi_next_tick(callback, data) {
			Promise.resolve().then(function() {
				wasmTable.get(callback)(data);
			});
		}
		/**
		* @__sig vipppi
		*/
		function _emnapi_callback_into_module(forceUncaught, env, callback, data, close_scope_if_throw) {
			var envObject = emnapiCtx.envStore.get(env);
			var scope = emnapiCtx.openScope(envObject);
			try {
				envObject.callbackIntoModule(Boolean(forceUncaught), function() {
					wasmTable.get(callback)(env, data);
				});
			} catch (err) {
				emnapiCtx.closeScope(envObject, scope);
				if (close_scope_if_throw) emnapiCtx.closeScope(envObject);
				throw err;
			}
			emnapiCtx.closeScope(envObject, scope);
		}
		/**
		* @__sig vipppp
		*/
		function _emnapi_call_finalizer(forceUncaught, env, callback, data, hint) {
			var envObject = emnapiCtx.envStore.get(env);
			callback >>>= 0;
			envObject.callFinalizerInternal(forceUncaught, callback, data, hint);
		}
		/**
		* @__sig v
		*/
		function _emnapi_ctx_increase_waiting_request_counter() {
			emnapiCtx.increaseWaitingRequestCounter();
		}
		/**
		* @__sig v
		*/
		function _emnapi_ctx_decrease_waiting_request_counter() {
			emnapiCtx.decreaseWaitingRequestCounter();
		}
		/**
		* @__sig i
		*/
		function _emnapi_is_main_runtime_thread() {
			return ENVIRONMENT_IS_PTHREAD ? 0 : 1;
		}
		/**
		* @__sig i
		*/
		function _emnapi_is_main_browser_thread() {
			return typeof window !== "undefined" && typeof document !== "undefined" && !ENVIRONMENT_IS_NODE ? 1 : 0;
		}
		/**
		* @__sig v
		*/
		function _emnapi_unwind() {
			throw "unwind";
		}
		/**
		* @__sig d
		*/
		function _emnapi_get_now() {
			return performance.timeOrigin + performance.now();
		}
		function $emnapiSetValueI64(result, numberValue) {
			var tempDouble;
			var tempI64 = [numberValue >>> 0, (tempDouble = numberValue, +Math.abs(tempDouble) >= 1 ? tempDouble > 0 ? (Math.min(+Math.floor(tempDouble / 4294967296), 0xffffffff) | 0) >>> 0 : ~~+Math.ceil((tempDouble - +(~~tempDouble >>> 0)) / 4294967296) >>> 0 : 0)];
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setInt32(result, tempI64[0], true);
			GET_HEAP_DATA_VIEW().setInt32(result + 4, tempI64[1], true);
		}
		/**
		* @__deps $emnapiCtx
		* @__sig p
		*/
		function _emnapi_open_handle_scope() {
			return emnapiCtx.openScope().id;
		}
		/**
		* @__deps $emnapiCtx
		* @__sig vp
		*/
		function _emnapi_close_handle_scope(_scope) {
			return emnapiCtx.closeScope();
		}
		var utilMod = /*#__PURE__*/ Object.freeze({
			__proto__: null,
			$emnapiSetValueI64,
			_emnapi_call_finalizer,
			_emnapi_callback_into_module,
			_emnapi_close_handle_scope,
			_emnapi_ctx_decrease_waiting_request_counter,
			_emnapi_ctx_increase_waiting_request_counter,
			_emnapi_get_node_version,
			_emnapi_get_now,
			_emnapi_is_main_browser_thread,
			_emnapi_is_main_runtime_thread,
			_emnapi_next_tick,
			_emnapi_open_handle_scope,
			_emnapi_runtime_keepalive_pop,
			_emnapi_runtime_keepalive_push,
			_emnapi_set_immediate,
			_emnapi_unwind,
			napi_clear_last_error,
			napi_set_last_error
		});
		function emnapiGetWorkerByPthreadPtr(pthreadPtr) {
			var tid = new DataView(wasmMemory.buffer).getInt32(pthreadPtr + 20, true);
			return PThread.pthreads[tid];
		}
		/** @__sig vp */
		function _emnapi_worker_ref(pthreadPtr) {
			if (ENVIRONMENT_IS_PTHREAD) return;
			pthreadPtr >>>= 0;
			var worker = emnapiGetWorkerByPthreadPtr(pthreadPtr);
			if (worker && typeof worker.ref === "function") worker.ref();
		}
		/** @__sig vp */
		function _emnapi_worker_unref(pthreadPtr) {
			if (ENVIRONMENT_IS_PTHREAD) return;
			pthreadPtr >>>= 0;
			var worker = emnapiGetWorkerByPthreadPtr(pthreadPtr);
			if (worker && typeof worker.unref === "function") worker.unref();
		}
		/** @__sig vipp */
		function _emnapi_async_send_js(type, callback, data) {
			if (ENVIRONMENT_IS_PTHREAD) (0, napiModule.postMessage)({ __emnapi__: {
				type: "async-send",
				payload: {
					callback,
					data
				}
			} });
			else switch (type) {
				case 0:
					_emnapi_set_immediate(callback, data);
					break;
				case 1: _emnapi_next_tick(callback, data);
			}
		}
		var uvThreadpoolReadyResolve;
		var uvThreadpoolReady = new Promise(function(resolve) {
			uvThreadpoolReadyResolve = function() {
				uvThreadpoolReady.ready = true;
				resolve();
			};
		});
		uvThreadpoolReady.ready = false;
		/** @__sig vppi */
		function _emnapi_after_uvthreadpool_ready(callback, q, type) {
			if (uvThreadpoolReady.ready) wasmTable.get(callback)(q, type);
			else uvThreadpoolReady.then(function() {
				wasmTable.get(callback)(q, type);
			});
		}
		/** @__sig vpi */
		function _emnapi_tell_js_uvthreadpool(threads, size) {
			var p = [];
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			var _loop_1 = function(i) {
				var worker = emnapiGetWorkerByPthreadPtr(GET_HEAP_DATA_VIEW().getUint32(threads + i * 4, true));
				p.push(new Promise(function(resolve) {
					var handler = function(e) {
						var __emnapi__ = (ENVIRONMENT_IS_NODE ? e : e.data).__emnapi__;
						if (__emnapi__ && __emnapi__.type === "async-thread-ready") {
							resolve();
							if (worker && typeof worker.unref === "function") worker.unref();
							if (ENVIRONMENT_IS_NODE) worker.off("message", handler);
							else worker.removeEventListener("message", handler);
						}
					};
					if (ENVIRONMENT_IS_NODE) worker.on("message", handler);
					else worker.addEventListener("message", handler);
				}));
			};
			for (var i = 0; i < size; i++) _loop_1(i);
			Promise.all(p).then(uvThreadpoolReadyResolve);
		}
		/** @__sig v */
		function _emnapi_emit_async_thread_ready() {
			if (!ENVIRONMENT_IS_PTHREAD) return;
			(0, napiModule.postMessage)({ __emnapi__: {
				type: "async-thread-ready",
				payload: {}
			} });
		}
		var asyncMod = /*#__PURE__*/ Object.freeze({
			__proto__: null,
			_emnapi_after_uvthreadpool_ready,
			_emnapi_async_send_js,
			_emnapi_emit_async_thread_ready,
			_emnapi_tell_js_uvthreadpool,
			_emnapi_worker_ref,
			_emnapi_worker_unref
		});
		/** @__sig ipjp */
		function napi_adjust_external_memory(env, change_in_bytes, adjusted_value) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			if (!adjusted_value) return envObject.setLastError(1);
			var adjusted_memory = emnapiCtx.adjustAmountOfExternalAllocatedMemory(change_in_bytes);
			adjusted_value >>>= 0;
			if (emnapiCtx.feature.supportBigInt) {
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setBigInt64(adjusted_value, BigInt(adjusted_memory), true);
			} else $emnapiSetValueI64(adjusted_value, Number(adjusted_memory));
			return envObject.clearLastError();
		}
		var memoryMod = /*#__PURE__*/ Object.freeze({
			__proto__: null,
			napi_adjust_external_memory
		});
		/**
		* @__postset
		* ```
		* emnapiAWST.init();
		* ```
		*/
		var emnapiAWST = {
			idGen: {},
			values: [void 0],
			queued: /* @__PURE__ */ new Set(),
			pending: [],
			init: function() {
				var idGen = {
					nextId: 1,
					list: [],
					generate: function() {
						var id;
						if (idGen.list.length) id = idGen.list.shift();
						else {
							id = idGen.nextId;
							idGen.nextId++;
						}
						return id;
					},
					reuse: function(id) {
						idGen.list.push(id);
					}
				};
				emnapiAWST.idGen = idGen;
				emnapiAWST.values = [void 0];
				emnapiAWST.queued = /* @__PURE__ */ new Set();
				emnapiAWST.pending = [];
			},
			create: function(env, resource, resourceName, execute, complete, data) {
				var asyncId = 0;
				var triggerAsyncId = 0;
				if (emnapiNodeBinding) {
					var asyncContext = emnapiNodeBinding.node.emitAsyncInit(resource, resourceName, -1);
					asyncId = asyncContext.asyncId;
					triggerAsyncId = asyncContext.triggerAsyncId;
				}
				var id = emnapiAWST.idGen.generate();
				emnapiAWST.values[id] = {
					env,
					id,
					resource,
					asyncId,
					triggerAsyncId,
					status: 0,
					execute,
					complete,
					data
				};
				return id;
			},
			callComplete: function(work, status) {
				var complete = work.complete;
				var env = work.env;
				var data = work.data;
				var callback = function() {
					if (!complete) return;
					var envObject = emnapiCtx.envStore.get(env);
					var scope = emnapiCtx.openScope(envObject);
					try {
						envObject.callbackIntoModule(true, function() {
							wasmTable.get(complete)(env, status, data);
						});
					} finally {
						emnapiCtx.closeScope(envObject, scope);
					}
				};
				if (emnapiNodeBinding) emnapiNodeBinding.node.makeCallback(work.resource, callback, [], {
					asyncId: work.asyncId,
					triggerAsyncId: work.triggerAsyncId
				});
				else callback();
			},
			queue: function(id) {
				var work = emnapiAWST.values[id];
				if (!work) return;
				if (work.status === 0) {
					work.status = 1;
					if (emnapiAWST.queued.size >= (Math.abs(emnapiAsyncWorkPoolSize) || 4)) {
						emnapiAWST.pending.push(id);
						return;
					}
					emnapiAWST.queued.add(id);
					var env_1 = work.env;
					var data_1 = work.data;
					var execute = work.execute;
					work.status = 2;
					emnapiCtx.feature.setImmediate(function() {
						wasmTable.get(execute)(env_1, data_1);
						emnapiAWST.queued.delete(id);
						work.status = 3;
						emnapiCtx.feature.setImmediate(function() {
							emnapiAWST.callComplete(work, 0);
						});
						if (emnapiAWST.pending.length > 0) {
							var nextWorkId = emnapiAWST.pending.shift();
							emnapiAWST.values[nextWorkId].status = 0;
							emnapiAWST.queue(nextWorkId);
						}
					});
				}
			},
			cancel: function(id) {
				var index = emnapiAWST.pending.indexOf(id);
				if (index !== -1) {
					var work_1 = emnapiAWST.values[id];
					if (work_1 && work_1.status === 1) {
						work_1.status = 4;
						emnapiAWST.pending.splice(index, 1);
						emnapiCtx.feature.setImmediate(function() {
							emnapiAWST.callComplete(work_1, 11);
						});
						return 0;
					} else return 9;
				}
				return 9;
			},
			remove: function(id) {
				var work = emnapiAWST.values[id];
				if (!work) return;
				if (emnapiNodeBinding) emnapiNodeBinding.node.emitAsyncDestroy({
					asyncId: work.asyncId,
					triggerAsyncId: work.triggerAsyncId
				});
				emnapiAWST.values[id] = void 0;
				emnapiAWST.idGen.reuse(id);
			}
		};
		/** @__sig vppdp */
		function _emnapi_node_emit_async_init(async_resource, async_resource_name, trigger_async_id, result) {
			if (!emnapiNodeBinding) return;
			var resource = emnapiCtx.handleStore.get(async_resource).value;
			var resource_name = emnapiCtx.handleStore.get(async_resource_name).value;
			var asyncContext = emnapiNodeBinding.node.emitAsyncInit(resource, resource_name, trigger_async_id);
			var asyncId = asyncContext.asyncId;
			var triggerAsyncId = asyncContext.triggerAsyncId;
			if (result) {
				result >>>= 0;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setFloat64(result, asyncId, true);
				GET_HEAP_DATA_VIEW().setFloat64(result + 8, triggerAsyncId, true);
			}
		}
		/** @__sig vdd */
		function _emnapi_node_emit_async_destroy(async_id, trigger_async_id) {
			if (!emnapiNodeBinding) return;
			emnapiNodeBinding.node.emitAsyncDestroy({
				asyncId: async_id,
				triggerAsyncId: trigger_async_id
			});
		}
		/** @__sig ipppppddp */
		function _emnapi_node_make_callback(env, async_resource, cb, argv, size, async_id, trigger_async_id, result) {
			var i = 0;
			var v;
			if (!emnapiNodeBinding) return;
			var resource = emnapiCtx.handleStore.get(async_resource).value;
			var callback = emnapiCtx.handleStore.get(cb).value;
			argv >>>= 0;
			size >>>= 0;
			size = size >>> 0;
			var arr = Array(size);
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			for (; i < size; i++) {
				var argVal = GET_HEAP_DATA_VIEW().getUint32(argv + i * 4, true);
				arr[i] = emnapiCtx.handleStore.get(argVal).value;
			}
			var ret = emnapiNodeBinding.node.makeCallback(resource, callback, arr, {
				asyncId: async_id,
				triggerAsyncId: trigger_async_id
			});
			if (result) {
				result >>>= 0;
				v = emnapiCtx.envStore.get(env).ensureHandleId(ret);
				GET_HEAP_DATA_VIEW().setUint32(result, v, true);
			}
		}
		/** @__sig ippp */
		function _emnapi_async_init_js(async_resource, async_resource_name, result) {
			if (!emnapiNodeBinding) return 9;
			var resource;
			if (async_resource) resource = Object(emnapiCtx.handleStore.get(async_resource).value);
			var name = emnapiCtx.handleStore.get(async_resource_name).value;
			var ret = emnapiNodeBinding.napi.asyncInit(resource, name);
			if (ret.status !== 0) return ret.status;
			var numberValue = ret.value;
			if (!(numberValue >= BigInt(-1) * (BigInt(1) << BigInt(63)) && numberValue < BigInt(1) << BigInt(63))) {
				numberValue = numberValue & (BigInt(1) << BigInt(64)) - BigInt(1);
				if (numberValue >= BigInt(1) << BigInt(63)) numberValue = numberValue - (BigInt(1) << BigInt(64));
			}
			var low = Number(numberValue & BigInt(0xffffffff));
			var high = Number(numberValue >> BigInt(32));
			result >>>= 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setInt32(result, low, true);
			GET_HEAP_DATA_VIEW().setInt32(result + 4, high, true);
			return 0;
		}
		/** @__sig ip */
		function _emnapi_async_destroy_js(async_context) {
			if (!emnapiNodeBinding) return 9;
			async_context >>>= 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			var low = GET_HEAP_DATA_VIEW().getInt32(async_context, true);
			var high = GET_HEAP_DATA_VIEW().getInt32(async_context + 4, true);
			var pointer = BigInt(low >>> 0) | BigInt(high) << BigInt(32);
			var ret = emnapiNodeBinding.napi.asyncDestroy(pointer);
			if (ret.status !== 0) return ret.status;
			return 0;
		}
		/** @__sig ipppp */
		function napi_open_callback_scope(env, ignored, async_context_handle, result) {
			throw new Error("napi_open_callback_scope has not been implemented yet");
		}
		/** @__sig ipp */
		function napi_close_callback_scope(env, scope) {
			throw new Error("napi_close_callback_scope has not been implemented yet");
		}
		/** @__sig ippppppp */
		function napi_make_callback(env, async_context, recv, func, argc, argv, result) {
			var i = 0;
			var v;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!emnapiNodeBinding) return envObject.setLastError(9);
				if (!recv) return envObject.setLastError(1);
				if (argc > 0) {
					if (!argv) return envObject.setLastError(1);
				}
				var v8recv = Object(emnapiCtx.handleStore.get(recv).value);
				var v8func = emnapiCtx.handleStore.get(func).value;
				if (typeof v8func !== "function") return envObject.setLastError(1);
				async_context >>>= 0;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				var low = GET_HEAP_DATA_VIEW().getInt32(async_context, true);
				var high = GET_HEAP_DATA_VIEW().getInt32(async_context + 4, true);
				var ctx = BigInt(low >>> 0) | BigInt(high) << BigInt(32);
				argv >>>= 0;
				argc >>>= 0;
				argc = argc >>> 0;
				var arr = Array(argc);
				for (; i < argc; i++) {
					var argVal = GET_HEAP_DATA_VIEW().getUint32(argv + i * 4, true);
					arr[i] = emnapiCtx.handleStore.get(argVal).value;
				}
				var ret = emnapiNodeBinding.napi.makeCallback(ctx, v8recv, v8func, arr);
				if (ret.error) throw ret.error;
				if (ret.status !== 0) return envObject.setLastError(ret.status);
				if (result) {
					result >>>= 0;
					v = envObject.ensureHandleId(ret.value);
					GET_HEAP_DATA_VIEW().setUint32(result, v, true);
				}
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig vp */
		function _emnapi_env_check_gc_access(env) {
			emnapiCtx.envStore.get(env).checkGCAccess();
		}
		var nodeMod = /*#__PURE__*/ Object.freeze({
			__proto__: null,
			_emnapi_async_destroy_js,
			_emnapi_async_init_js,
			_emnapi_env_check_gc_access,
			_emnapi_node_emit_async_destroy,
			_emnapi_node_emit_async_init,
			_emnapi_node_make_callback,
			napi_close_callback_scope,
			napi_make_callback,
			napi_open_callback_scope
		});
		/**
		* @__deps malloc
		* @__deps free
		* @__postset
		* ```
		* emnapiTSFN.init();
		* ```
		*/
		var emnapiTSFN = {
			_liveSet: {},
			offset: {
				__size__: 0,
				resource: 0,
				async_id: 0,
				trigger_async_id: 0,
				queue_size: 0,
				is_some: 0,
				queue: 0,
				async_pending: 0,
				async_u_fd: 0,
				thread_count: 0,
				state: 0,
				dispatch_state: 0,
				context: 0,
				max_queue_size: 0,
				ref: 0,
				env: 0,
				finalize_data: 0,
				finalize_cb: 0,
				call_js_cb: 0,
				handles_closing: 0,
				async_ref: 0,
				mutex: 0,
				cond: 0
			},
			init: function() {
				emnapiTSFN._liveSet = /* @__PURE__ */ new Set();
				emnapiTSFN.offset.__size__ = 184;
				emnapiTSFN.offset.resource = 0;
				emnapiTSFN.offset.async_id = 8;
				emnapiTSFN.offset.trigger_async_id = 16;
				emnapiTSFN.offset.queue_size = 60;
				emnapiTSFN.offset.is_some = 24;
				emnapiTSFN.offset.queue = 64;
				emnapiTSFN.offset.async_pending = 132;
				emnapiTSFN.offset.async_u_fd = 96;
				emnapiTSFN.offset.thread_count = 136;
				emnapiTSFN.offset.state = 140;
				emnapiTSFN.offset.dispatch_state = 144;
				emnapiTSFN.offset.context = 148;
				emnapiTSFN.offset.max_queue_size = 152;
				emnapiTSFN.offset.ref = 156;
				emnapiTSFN.offset.env = 160;
				emnapiTSFN.offset.finalize_data = 164;
				emnapiTSFN.offset.finalize_cb = 168;
				emnapiTSFN.offset.call_js_cb = 172;
				emnapiTSFN.offset.handles_closing = 176;
				emnapiTSFN.offset.async_ref = 180;
				emnapiTSFN.offset.mutex = 32;
				emnapiTSFN.offset.cond = 56;
				emnapiTSFN.offset.mutex = emnapiTSFN.offset.mutex + 4;
				if (typeof PThread !== "undefined") {
					PThread.unusedWorkers.forEach(emnapiTSFN.addListener);
					Object.values(PThread.pthreads).forEach(emnapiTSFN.addListener);
					var __original_getNewWorker_1 = PThread.getNewWorker;
					PThread.getNewWorker = function() {
						var r = __original_getNewWorker_1.apply(this, arguments);
						emnapiTSFN.addListener(r);
						return r;
					};
				}
			},
			addListener: function(worker) {
				if (!worker) return false;
				if (worker._emnapiTSFNListener) return true;
				var handler = function(e) {
					var __emnapi__ = (ENVIRONMENT_IS_NODE ? e : e.data).__emnapi__;
					if (__emnapi__) {
						var type = __emnapi__.type;
						var payload = __emnapi__.payload;
						if (type === "tsfn-send") {
							var pendng = payload.tsfn + emnapiTSFN.offset.async_pending;
							if (Atomics.load(new Int32Array(emnapiTSFN.ensureBufferFor(pendng + 4)), pendng >>> 2) !== 0) emnapiTSFN.enqueue(payload.tsfn);
						}
					}
				};
				var dispose = function() {
					if (ENVIRONMENT_IS_NODE) worker.off("message", handler);
					else worker.removeEventListener("message", handler, false);
					delete worker._emnapiTSFNListener;
				};
				worker._emnapiTSFNListener = {
					handler,
					dispose
				};
				if (ENVIRONMENT_IS_NODE) worker.on("message", handler);
				else worker.addEventListener("message", handler, false);
				return true;
			},
			/**
			* When another thread grows the shared WebAssembly.Memory, this agent's
			* cached `wasmMemory.buffer` may still have the old shorter length
			* (V8 refreshes it lazily). If a pointer derived from shared memory lies
			* beyond the cached length, `wasmMemory.grow(0)` forces the agent to
			* observe the current memory size and refreshes the buffer.
			*/
			ensureBufferFor: function(end) {
				var buffer = wasmMemory.buffer;
				if (end > buffer.byteLength) {
					wasmMemory.grow(0);
					buffer = wasmMemory.buffer;
				}
				return buffer;
			},
			initQueue: function(func) {
				var size = 8;
				var queue = _malloc(size);
				if (!queue) return false;
				queue >>>= 0;
				new Uint8Array(emnapiTSFN.ensureBufferFor(queue + size), queue, size).fill(0);
				emnapiTSFN.storeSizeTypeValue(func + emnapiTSFN.offset.queue, queue, false);
				return true;
			},
			destroyQueue: function(func) {
				var queue = emnapiTSFN.loadSizeTypeValue(func + emnapiTSFN.offset.queue, false);
				if (queue) {
					var node = emnapiTSFN.loadSizeTypeValue(queue, false);
					while (node !== 0) {
						var next = emnapiTSFN.loadSizeTypeValue(node + 4, false);
						_free(node);
						node = next;
					}
					_free(queue);
				}
			},
			pushQueue: function(func, data) {
				var queue = emnapiTSFN.loadSizeTypeValue(func + emnapiTSFN.offset.queue, false);
				var head = emnapiTSFN.loadSizeTypeValue(queue, false);
				var tail = emnapiTSFN.loadSizeTypeValue(queue + 4, false);
				var node = _malloc(8);
				if (!node) throw new Error("OOM");
				node >>>= 0;
				emnapiTSFN.storeSizeTypeValue(node, data, false);
				emnapiTSFN.storeSizeTypeValue(node + 4, 0, false);
				if (head === 0 && tail === 0) {
					emnapiTSFN.storeSizeTypeValue(queue, node, false);
					emnapiTSFN.storeSizeTypeValue(queue + 4, node, false);
				} else {
					emnapiTSFN.storeSizeTypeValue(tail + 4, node, false);
					emnapiTSFN.storeSizeTypeValue(queue + 4, node, false);
				}
				emnapiTSFN.addQueueSize(func);
			},
			shiftQueue: function(func) {
				var queue = emnapiTSFN.loadSizeTypeValue(func + emnapiTSFN.offset.queue, false);
				var head = emnapiTSFN.loadSizeTypeValue(queue, false);
				if (head === 0) return 0;
				var node = head;
				var next = emnapiTSFN.loadSizeTypeValue(head + 4, false);
				emnapiTSFN.storeSizeTypeValue(queue, next, false);
				if (next === 0) emnapiTSFN.storeSizeTypeValue(queue + 4, 0, false);
				emnapiTSFN.storeSizeTypeValue(node + 4, 0, false);
				var value = emnapiTSFN.loadSizeTypeValue(node, false);
				_free(node);
				emnapiTSFN.subQueueSize(func);
				return value;
			},
			push: function(func, data, mode) {
				var mutex = emnapiTSFN.getMutex(func);
				var cond = emnapiTSFN.getCond(func);
				var waitCondition = function() {
					var queueSize = emnapiTSFN.getQueueSize(func);
					var maxSize = emnapiTSFN.getMaxQueueSize(func);
					return queueSize >= maxSize && maxSize > 0 && emnapiTSFN.getState(func) === 0;
				};
				var isBrowserMain = typeof window !== "undefined" && typeof document !== "undefined" && !ENVIRONMENT_IS_NODE;
				var shouldDelete = false;
				var ret = mutex.execute(function() {
					while (waitCondition()) {
						if (mode === 0) return 15;
						/**
						* Browser JS main thread can not use `Atomics.wait`
						*
						* Related:
						* https://github.com/nodejs/node/pull/32689
						* https://github.com/nodejs/node/pull/33453
						*/
						if (isBrowserMain) return 21;
						cond.wait();
					}
					if (emnapiTSFN.getState(func) === 0) {
						emnapiTSFN.pushQueue(func, data);
						emnapiTSFN.send(func);
						return 0;
					}
					if (emnapiTSFN.getThreadCount(func) === 0) return 1;
					emnapiTSFN.subThreadCount(func);
					if (!(emnapiTSFN.getState(func) === 2 && emnapiTSFN.getThreadCount(func) === 0)) return 16;
					shouldDelete = true;
					return 16;
				});
				if (shouldDelete) emnapiTSFN.destroy(func);
				return ret;
			},
			getMutex: function(func) {
				var index = func + emnapiTSFN.offset.mutex;
				var mutex = {
					lock: function() {
						var isBrowserMain = typeof window !== "undefined" && typeof document !== "undefined" && !ENVIRONMENT_IS_NODE;
						var i32a = new Int32Array(emnapiTSFN.ensureBufferFor(index + 4), index, 1);
						if (isBrowserMain) while (true) {
							var oldValue = Atomics.compareExchange(i32a, 0, 0, 10);
							if (oldValue === 0) return;
						}
						else while (true) {
							var oldValue = Atomics.compareExchange(i32a, 0, 0, 10);
							if (oldValue === 0) return;
							Atomics.wait(i32a, 0, 10);
						}
					},
					unlock: function() {
						var i32a = new Int32Array(emnapiTSFN.ensureBufferFor(index + 4), index, 1);
						if (Atomics.compareExchange(i32a, 0, 10, 0) !== 10) throw new Error("Tried to unlock while not holding the mutex");
						Atomics.notify(i32a, 0, 1);
					},
					execute: function(fn) {
						mutex.lock();
						try {
							return fn();
						} finally {
							mutex.unlock();
						}
					}
				};
				return mutex;
			},
			getCond: function(func) {
				var index = func + emnapiTSFN.offset.cond;
				var mutex = emnapiTSFN.getMutex(func);
				return {
					wait: function() {
						var i32a = new Int32Array(emnapiTSFN.ensureBufferFor(index + 4), index, 1);
						var value = Atomics.load(i32a, 0);
						mutex.unlock();
						Atomics.wait(i32a, 0, value);
						mutex.lock();
					},
					signal: function() {
						var i32a = new Int32Array(emnapiTSFN.ensureBufferFor(index + 4), index, 1);
						Atomics.add(i32a, 0, 1);
						Atomics.notify(i32a, 0, 1);
					}
				};
			},
			getQueueSize: function(func) {
				return emnapiTSFN.loadSizeTypeValue(func + emnapiTSFN.offset.queue_size, true);
			},
			addQueueSize: function(func) {
				var offset = emnapiTSFN.offset.queue_size;
				var arr = new Uint32Array(emnapiTSFN.ensureBufferFor(func + offset + 4)), index = func + offset >>> 2;
				Atomics.add(arr, index, 1);
			},
			subQueueSize: function(func) {
				var offset = emnapiTSFN.offset.queue_size;
				var arr = new Uint32Array(emnapiTSFN.ensureBufferFor(func + offset + 4)), index = func + offset >>> 2;
				Atomics.sub(arr, index, 1);
			},
			getThreadCount: function(func) {
				return emnapiTSFN.loadSizeTypeValue(func + emnapiTSFN.offset.thread_count, true);
			},
			addThreadCount: function(func) {
				var offset = emnapiTSFN.offset.thread_count;
				var arr = new Uint32Array(emnapiTSFN.ensureBufferFor(func + offset + 4)), index = func + offset >>> 2;
				Atomics.add(arr, index, 1);
			},
			subThreadCount: function(func) {
				var offset = emnapiTSFN.offset.thread_count;
				var arr = new Uint32Array(emnapiTSFN.ensureBufferFor(func + offset + 4)), index = func + offset >>> 2;
				Atomics.sub(arr, index, 1);
			},
			getState: function(func) {
				return Atomics.load(new Int32Array(emnapiTSFN.ensureBufferFor(func + emnapiTSFN.offset.state + 4)), func + emnapiTSFN.offset.state >>> 2);
			},
			setState: function(func, value) {
				Atomics.store(new Int32Array(emnapiTSFN.ensureBufferFor(func + emnapiTSFN.offset.state + 4)), func + emnapiTSFN.offset.state >>> 2, value);
			},
			getHandlesClosing: function(func) {
				return Atomics.load(new Int8Array(emnapiTSFN.ensureBufferFor(func + emnapiTSFN.offset.handles_closing + 1)), func + emnapiTSFN.offset.handles_closing);
			},
			setHandlesClosing: function(func, value) {
				Atomics.store(new Int8Array(emnapiTSFN.ensureBufferFor(func + emnapiTSFN.offset.handles_closing + 1)), func + emnapiTSFN.offset.handles_closing, value);
			},
			getDispatchState: function(func) {
				return Atomics.load(new Uint32Array(emnapiTSFN.ensureBufferFor(func + emnapiTSFN.offset.dispatch_state + 4)), func + emnapiTSFN.offset.dispatch_state >>> 2);
			},
			getContext: function(func) {
				return emnapiTSFN.loadSizeTypeValue(func + emnapiTSFN.offset.context, false);
			},
			getMaxQueueSize: function(func) {
				return emnapiTSFN.loadSizeTypeValue(func + emnapiTSFN.offset.max_queue_size, true);
			},
			getEnv: function(func) {
				return emnapiTSFN.loadSizeTypeValue(func + emnapiTSFN.offset.env, false);
			},
			getCallJSCb: function(func) {
				return emnapiTSFN.loadSizeTypeValue(func + emnapiTSFN.offset.call_js_cb, false);
			},
			getRef: function(func) {
				return emnapiTSFN.loadSizeTypeValue(func + emnapiTSFN.offset.ref, false);
			},
			getResource: function(func) {
				return emnapiTSFN.loadSizeTypeValue(func + emnapiTSFN.offset.resource, false);
			},
			getFinalizeCb: function(func) {
				return emnapiTSFN.loadSizeTypeValue(func + emnapiTSFN.offset.finalize_cb, false);
			},
			getFinalizeData: function(func) {
				return emnapiTSFN.loadSizeTypeValue(func + emnapiTSFN.offset.finalize_data, false);
			},
			loadSizeTypeValue: function(offset, unsigned) {
				var ret;
				var arr;
				if (unsigned) {
					arr = new Uint32Array(emnapiTSFN.ensureBufferFor(offset + 4));
					ret = Atomics.load(arr, offset >>> 2);
					return ret;
				} else {
					arr = new Int32Array(emnapiTSFN.ensureBufferFor(offset + 4));
					ret = Atomics.load(arr, offset >>> 2);
					return ret;
				}
			},
			storeSizeTypeValue: function(offset, value, unsigned) {
				var arr;
				if (unsigned) {
					arr = new Uint32Array(emnapiTSFN.ensureBufferFor(offset + 4));
					Atomics.store(arr, offset >>> 2, value);
					return;
				} else {
					arr = new Int32Array(emnapiTSFN.ensureBufferFor(offset + 4));
					Atomics.store(arr, offset >>> 2, value >>> 0);
					return;
				}
			},
			releaseResources: function(func) {
				if (emnapiTSFN.getState(func) !== 2) {
					emnapiTSFN.setState(func, 2);
					var env = emnapiTSFN.getEnv(func);
					var envObject = emnapiCtx.envStore.get(env);
					var ref = emnapiTSFN.getRef(func);
					if (ref) emnapiCtx.refStore.get(ref).dispose();
					var resource = emnapiTSFN.getResource(func);
					emnapiCtx.refStore.get(resource).dispose();
					emnapiTSFN.ensureBufferFor(func + emnapiTSFN.offset.is_some + 1);
					var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
						return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
					};
					GET_HEAP_DATA_VIEW().setInt8(func + emnapiTSFN.offset.is_some, 0, true);
					emnapiCtx.removeCleanupHook(envObject, emnapiTSFN.cleanup, func);
					envObject.unref();
					var asyncRefAddress = func + emnapiTSFN.offset.async_ref;
					var asyncRefOffset = asyncRefAddress >>> 2;
					var arr = new Uint32Array(emnapiTSFN.ensureBufferFor(asyncRefAddress + 4));
					if (Atomics.load(arr, asyncRefOffset) > 0) {
						Atomics.store(arr, asyncRefOffset, 0);
						emnapiCtx.decreaseWaitingRequestCounter();
					}
					if (emnapiNodeBinding) {
						var view = new DataView(emnapiTSFN.ensureBufferFor(func + emnapiTSFN.offset.trigger_async_id + 8));
						_emnapi_node_emit_async_destroy(view.getFloat64(func + emnapiTSFN.offset.async_id, true), view.getFloat64(func + emnapiTSFN.offset.trigger_async_id, true));
					}
				}
			},
			destroy: function(func) {
				emnapiTSFN._liveSet.delete(func);
				emnapiTSFN.destroyQueue(func);
				emnapiTSFN.releaseResources(func);
				_free(func);
			},
			emptyQueue: function(func) {
				var drainQueue = [];
				emnapiTSFN.getMutex(func).execute(function() {
					while (emnapiTSFN.getQueueSize(func) > 0) drainQueue.push(emnapiTSFN.shiftQueue(func));
				});
				var callJsCb = emnapiTSFN.getCallJSCb(func);
				var context = emnapiTSFN.getContext(func);
				var data;
				for (var i = 0; i < drainQueue.length; i++) {
					data = drainQueue[i];
					if (callJsCb) wasmTable.get(callJsCb)(0, 0, context, data);
				}
			},
			maybeDelete: function(func) {
				var shouldDelete = false;
				emnapiTSFN.getMutex(func).execute(function() {
					if (emnapiTSFN.getThreadCount(func) > 0) emnapiTSFN.releaseResources(func);
					else shouldDelete = true;
				});
				if (shouldDelete) emnapiTSFN.destroy(func);
			},
			finalize: function(func) {
				var env = emnapiTSFN.getEnv(func);
				var envObject = emnapiCtx.envStore.get(env);
				emnapiCtx.openScope(envObject);
				var finalize = emnapiTSFN.getFinalizeCb(func);
				var data = emnapiTSFN.getFinalizeData(func);
				var context = emnapiTSFN.getContext(func);
				var f = function() {
					envObject.callFinalizerInternal(0, finalize, data, context);
				};
				try {
					emnapiTSFN.emptyQueue(func);
					if (finalize) {
						if (emnapiNodeBinding) {
							var resource = emnapiTSFN.getResource(func);
							var resource_value = emnapiCtx.refStore.get(resource).get();
							var resourceObject = emnapiCtx.handleStore.get(resource_value).value;
							var view = new DataView(emnapiTSFN.ensureBufferFor(func + emnapiTSFN.offset.trigger_async_id + 8));
							var asyncId = view.getFloat64(func + emnapiTSFN.offset.async_id, true);
							var triggerAsyncId = view.getFloat64(func + emnapiTSFN.offset.trigger_async_id, true);
							emnapiNodeBinding.node.makeCallback(resourceObject, f, [], {
								asyncId,
								triggerAsyncId
							});
						} else f();
					}
					emnapiTSFN.maybeDelete(func);
				} finally {
					emnapiCtx.closeScope(envObject);
				}
			},
			cleanup: function(func) {
				emnapiTSFN.closeHandlesAndMaybeDelete(func, 1);
			},
			closeHandlesAndMaybeDelete: function(func, set_closing) {
				var env = emnapiTSFN.getEnv(func);
				var envObject = emnapiCtx.envStore.get(env);
				emnapiCtx.openScope(envObject);
				try {
					if (set_closing) emnapiTSFN.getMutex(func).execute(function() {
						emnapiTSFN.setState(func, 1);
						if (emnapiTSFN.getMaxQueueSize(func) > 0) emnapiTSFN.getCond(func).signal();
					});
					if (emnapiTSFN.getHandlesClosing(func)) return;
					emnapiTSFN.setHandlesClosing(func, 1);
					Atomics.store(new Int32Array(emnapiTSFN.ensureBufferFor(func + emnapiTSFN.offset.async_pending + 4)), func + emnapiTSFN.offset.async_pending >>> 2, 1);
					emnapiCtx.feature.setImmediate(function() {
						emnapiTSFN.finalize(func);
					});
				} finally {
					emnapiCtx.closeScope(envObject);
				}
			},
			dispatchOne: function(func) {
				var data = 0;
				var popped_value = false;
				var has_more = false;
				var mutex = emnapiTSFN.getMutex(func);
				var cond = emnapiTSFN.getCond(func);
				mutex.execute(function() {
					if (emnapiTSFN.getState(func) === 0) {
						var size = emnapiTSFN.getQueueSize(func);
						if (size > 0) {
							data = emnapiTSFN.shiftQueue(func);
							popped_value = true;
							var maxQueueSize = emnapiTSFN.getMaxQueueSize(func);
							if (size === maxQueueSize && maxQueueSize > 0) cond.signal();
							size--;
						}
						if (size === 0) {
							if (emnapiTSFN.getThreadCount(func) === 0) {
								emnapiTSFN.setState(func, 1);
								if (emnapiTSFN.getMaxQueueSize(func) > 0) cond.signal();
								emnapiTSFN.closeHandlesAndMaybeDelete(func, 0);
							}
						} else has_more = true;
					} else emnapiTSFN.closeHandlesAndMaybeDelete(func, 0);
				});
				if (popped_value) {
					var env = emnapiTSFN.getEnv(func);
					var envObject_1 = emnapiCtx.envStore.get(env);
					emnapiCtx.openScope(envObject_1);
					var f = function() {
						envObject_1.callbackIntoModule(false, function() {
							var callJsCb = emnapiTSFN.getCallJSCb(func);
							var ref = emnapiTSFN.getRef(func);
							var js_callback = ref ? emnapiCtx.refStore.get(ref).get() : 0;
							if (callJsCb) {
								var context = emnapiTSFN.getContext(func);
								wasmTable.get(callJsCb)(env, js_callback, context, data);
							} else {
								var jsCallback = js_callback ? emnapiCtx.handleStore.get(js_callback).value : null;
								if (typeof jsCallback === "function") jsCallback();
							}
						});
					};
					try {
						if (emnapiNodeBinding) {
							var resource = emnapiTSFN.getResource(func);
							var resource_value = emnapiCtx.refStore.get(resource).get();
							var resourceObject = emnapiCtx.handleStore.get(resource_value).value;
							var view = new DataView(emnapiTSFN.ensureBufferFor(func + emnapiTSFN.offset.trigger_async_id + 8));
							emnapiNodeBinding.node.makeCallback(resourceObject, f, [], {
								asyncId: view.getFloat64(func + emnapiTSFN.offset.async_id, true),
								triggerAsyncId: view.getFloat64(func + emnapiTSFN.offset.trigger_async_id, true)
							});
						} else f();
					} finally {
						emnapiCtx.closeScope(envObject_1);
					}
				}
				return has_more;
			},
			dispatch: function(func) {
				var has_more = true;
				var iterations_left = 1e3;
				var dispatchStateAddress = func + emnapiTSFN.offset.dispatch_state;
				var index = dispatchStateAddress >>> 2;
				while (has_more && --iterations_left !== 0) {
					Atomics.store(new Uint32Array(emnapiTSFN.ensureBufferFor(dispatchStateAddress + 4)), index, 1);
					try {
						has_more = emnapiTSFN.dispatchOne(func);
					} catch (err) {
						if (emnapiTSFN._liveSet.has(func)) {
							Atomics.exchange(new Uint32Array(emnapiTSFN.ensureBufferFor(dispatchStateAddress + 4)), index, 0);
							emnapiTSFN.send(func);
						}
						throw err;
					}
					if (Atomics.exchange(new Uint32Array(emnapiTSFN.ensureBufferFor(dispatchStateAddress + 4)), index, 0) !== 1) has_more = true;
				}
				if (has_more) emnapiTSFN.send(func);
			},
			enqueue: function(func) {
				var pending = func + emnapiTSFN.offset.async_pending;
				var scheduled = func + emnapiTSFN.offset.async_u_fd;
				var end = Math.max(pending, scheduled) + 4;
				var state = function() {
					return new Int32Array(emnapiTSFN.ensureBufferFor(end));
				};
				if (Atomics.exchange(state(), scheduled >>> 2, 1) !== 0) return;
				emnapiCtx.feature.setImmediate(function() {
					if (!emnapiTSFN._liveSet.has(func)) return;
					if (Atomics.load(state(), pending >>> 2) === 0) {
						Atomics.store(state(), scheduled >>> 2, 0);
						return;
					}
					emnapiCtx.feature.setImmediate(function() {
						if (!emnapiTSFN._liveSet.has(func)) return;
						try {
							if (Atomics.exchange(state(), pending >>> 2, 0) === 0) return;
							emnapiTSFN.dispatch(func);
						} finally {
							if (emnapiTSFN._liveSet.has(func)) {
								Atomics.store(state(), scheduled >>> 2, 0);
								if (Atomics.load(state(), pending >>> 2) !== 0) emnapiTSFN.enqueue(func);
							}
						}
					});
				});
			},
			send: function(func) {
				var dispatchStateAddress = func + emnapiTSFN.offset.dispatch_state;
				if ((Atomics.or(new Uint32Array(emnapiTSFN.ensureBufferFor(dispatchStateAddress + 4)), dispatchStateAddress >>> 2, 2) & 1) === 1) return;
				var pendng = func + emnapiTSFN.offset.async_pending;
				if (Atomics.load(new Int32Array(emnapiTSFN.ensureBufferFor(pendng + 4)), pendng >>> 2) !== 0) return;
				if (Atomics.exchange(new Int32Array(emnapiTSFN.ensureBufferFor(pendng + 4)), pendng >>> 2, 1) === 0) {
					if (typeof ENVIRONMENT_IS_PTHREAD !== "undefined" && ENVIRONMENT_IS_PTHREAD) postMessage({ __emnapi__: {
						type: "tsfn-send",
						payload: { tsfn: func }
					} });
					else emnapiTSFN.enqueue(func);
				}
			}
		};
		/** @__sig ippppppppppp */
		function napi_create_threadsafe_function(env, func, async_resource, async_resource_name, max_queue_size, initial_thread_count, thread_finalize_data, thread_finalize_cb, context, call_js_cb, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!async_resource_name) return envObject.setLastError(1);
			max_queue_size >>>= 0;
			initial_thread_count >>>= 0;
			env >>>= 0;
			thread_finalize_data >>>= 0;
			thread_finalize_cb >>>= 0;
			context >>>= 0;
			call_js_cb >>>= 0;
			max_queue_size = max_queue_size >>> 0;
			initial_thread_count = initial_thread_count >>> 0;
			if (initial_thread_count === 0) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			var ref = 0;
			func >>>= 0;
			if (!func) {
				if (!call_js_cb) return envObject.setLastError(1);
			} else {
				if (typeof emnapiCtx.handleStore.get(func).value !== "function") return envObject.setLastError(1);
				ref = emnapiCtx.createReference(envObject, func, 1, 1).id;
			}
			var asyncResourceObject;
			if (async_resource) {
				asyncResourceObject = emnapiCtx.handleStore.get(async_resource).value;
				if (asyncResourceObject == null) return envObject.setLastError(2);
				asyncResourceObject = Object(asyncResourceObject);
			} else asyncResourceObject = {};
			var resource = envObject.ensureHandleId(asyncResourceObject);
			var asyncResourceName = emnapiCtx.handleStore.get(async_resource_name).value;
			if (typeof asyncResourceName === "symbol") return envObject.setLastError(3);
			asyncResourceName = String(asyncResourceName);
			var resource_name = envObject.ensureHandleId(asyncResourceName);
			var sizeofTSFN = emnapiTSFN.offset.__size__;
			var tsfn = _malloc(sizeofTSFN);
			if (!tsfn) return envObject.setLastError(9);
			tsfn >>>= 0;
			new Uint8Array(emnapiTSFN.ensureBufferFor(tsfn + sizeofTSFN)).subarray(tsfn, tsfn + sizeofTSFN).fill(0);
			var resourceRef = emnapiCtx.createReference(envObject, resource, 1, 1);
			var resource_ = resourceRef.id;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(tsfn + emnapiTSFN.offset.resource, resource_, true);
			if (!emnapiTSFN.initQueue(tsfn)) {
				_free(tsfn);
				resourceRef.dispose();
				return envObject.setLastError(9);
			}
			_emnapi_node_emit_async_init(resource, resource_name, -1, tsfn + emnapiTSFN.offset.async_id);
			GET_HEAP_DATA_VIEW().setInt8(tsfn + emnapiTSFN.offset.is_some, 1, true);
			GET_HEAP_DATA_VIEW().setUint32(tsfn + emnapiTSFN.offset.thread_count, initial_thread_count, true);
			GET_HEAP_DATA_VIEW().setUint32(tsfn + emnapiTSFN.offset.context, context, true);
			GET_HEAP_DATA_VIEW().setUint32(tsfn + emnapiTSFN.offset.max_queue_size, max_queue_size, true);
			GET_HEAP_DATA_VIEW().setUint32(tsfn + emnapiTSFN.offset.ref, ref, true);
			GET_HEAP_DATA_VIEW().setUint32(tsfn + emnapiTSFN.offset.env, env, true);
			GET_HEAP_DATA_VIEW().setUint32(tsfn + emnapiTSFN.offset.finalize_data, thread_finalize_data, true);
			GET_HEAP_DATA_VIEW().setUint32(tsfn + emnapiTSFN.offset.finalize_cb, thread_finalize_cb, true);
			GET_HEAP_DATA_VIEW().setUint32(tsfn + emnapiTSFN.offset.call_js_cb, call_js_cb, true);
			emnapiCtx.addCleanupHook(envObject, emnapiTSFN.cleanup, tsfn);
			emnapiTSFN._liveSet.add(tsfn);
			envObject.ref();
			emnapiCtx.increaseWaitingRequestCounter();
			GET_HEAP_DATA_VIEW().setUint32(tsfn + emnapiTSFN.offset.async_ref, 1, true);
			result >>>= 0;
			GET_HEAP_DATA_VIEW().setUint32(result, tsfn, true);
			return envObject.clearLastError();
		}
		/** @__sig ipp */
		function napi_get_threadsafe_function_context(func, result) {
			if (!func || !result) {
				abort();
				return 1;
			}
			func >>>= 0;
			result >>>= 0;
			var context = emnapiTSFN.getContext(func);
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(result, context, true);
			return 0;
		}
		/** @__sig ippi */
		function napi_call_threadsafe_function(func, data, mode) {
			if (!func) {
				abort();
				return 1;
			}
			func >>>= 0;
			data >>>= 0;
			return emnapiTSFN.push(func, data, mode);
		}
		/** @__sig ip */
		function napi_acquire_threadsafe_function(func) {
			if (!func) {
				abort();
				return 1;
			}
			func >>>= 0;
			return emnapiTSFN.getMutex(func).execute(function() {
				if (emnapiTSFN.getState(func) === 0) {
					emnapiTSFN.addThreadCount(func);
					return 0;
				}
				return 16;
			});
		}
		/** @__sig ipi */
		function napi_release_threadsafe_function(func, mode) {
			if (!func) {
				abort();
				return 1;
			}
			func >>>= 0;
			var mutex = emnapiTSFN.getMutex(func);
			var cond = emnapiTSFN.getCond(func);
			var shouldDelete = false;
			var ret = mutex.execute(function() {
				if (emnapiTSFN.getThreadCount(func) === 0) return 1;
				emnapiTSFN.subThreadCount(func);
				if (emnapiTSFN.getThreadCount(func) === 0 || mode === 1) {
					if (emnapiTSFN.getState(func) === 0) {
						if (mode === 1) emnapiTSFN.setState(func, 1);
						if (emnapiTSFN.getState(func) === 1 && emnapiTSFN.getMaxQueueSize(func) > 0) cond.signal();
						emnapiTSFN.send(func);
					}
				}
				if (!(emnapiTSFN.getState(func) === 2 && emnapiTSFN.getThreadCount(func) === 0)) return 0;
				shouldDelete = true;
				return 0;
			});
			if (shouldDelete) emnapiTSFN.destroy(func);
			return ret;
		}
		/** @__sig ipp */
		function napi_unref_threadsafe_function(env, func) {
			if (!func) {
				abort();
				return 1;
			}
			func >>>= 0;
			var asyncRefAddress = func + emnapiTSFN.offset.async_ref;
			var asyncRefOffset = asyncRefAddress >>> 2;
			var arr = new Uint32Array(emnapiTSFN.ensureBufferFor(asyncRefAddress + 4));
			var currentValue = Atomics.load(arr, asyncRefOffset);
			if (currentValue > 0) {
				Atomics.store(arr, asyncRefOffset, currentValue - 1);
				if (currentValue === 1) emnapiCtx.decreaseWaitingRequestCounter();
			}
			return 0;
		}
		/** @__sig ipp */
		function napi_ref_threadsafe_function(env, func) {
			if (!func) {
				abort();
				return 1;
			}
			func >>>= 0;
			var asyncRefAddress = func + emnapiTSFN.offset.async_ref;
			var asyncRefOffset = asyncRefAddress >>> 2;
			var arr = new Uint32Array(emnapiTSFN.ensureBufferFor(asyncRefAddress + 4));
			var currentValue = Atomics.load(arr, asyncRefOffset);
			if (!currentValue) emnapiCtx.increaseWaitingRequestCounter();
			Atomics.store(arr, asyncRefOffset, currentValue + 1);
			return 0;
		}
		var emnapiAWMT = {
			pool: [],
			workerReady: null,
			globalAddress: 0,
			globalOffset: {
				idle_threads: 0,
				q: 4,
				next: 4,
				prev: 8,
				mutex: 12,
				cond: 16,
				exit_message: 20,
				end: 28
			},
			offset: {
				resource: 0,
				async_id: 8,
				trigger_async_id: 16,
				env: 24,
				status: 28,
				queue: 32,
				queue_next: 32,
				queue_prev: 36,
				data: 40,
				execute: 44,
				complete: 48,
				end: 52
			},
			/**
			* When another thread grows the shared WebAssembly.Memory, this agent's
			* cached `wasmMemory.buffer` may still have the old shorter length
			* (V8 refreshes it lazily). If a pointer derived from shared memory lies
			* beyond the cached length, `wasmMemory.grow(0)` forces the agent to
			* observe the current memory size and refreshes the buffer.
			*/
			ensureBufferFor: function(end) {
				var buffer = wasmMemory.buffer;
				if (end > buffer.byteLength) {
					wasmMemory.grow(0);
					buffer = wasmMemory.buffer;
				}
				return buffer;
			},
			init: function() {
				emnapiAWMT.pool = [];
				emnapiAWMT.workerReady = null;
				if (typeof PThread !== "undefined") {
					PThread.unusedWorkers.forEach(emnapiAWMT.addListener);
					Object.values(PThread.pthreads).forEach(emnapiAWMT.addListener);
					var __original_getNewWorker_1 = PThread.getNewWorker;
					PThread.getNewWorker = function() {
						var r = __original_getNewWorker_1.apply(this, arguments);
						emnapiAWMT.addListener(r);
						return r;
					};
				}
			},
			addListener: function(worker) {
				if (!worker) return false;
				if (worker._emnapiAWMTListener) return true;
				var handler = function(e) {
					var __emnapi__ = (ENVIRONMENT_IS_NODE ? e : e.data).__emnapi__;
					if (__emnapi__) {
						var type = __emnapi__.type;
						var payload = __emnapi__.payload;
						if (type === "async-work-complete") emnapiAWMT.callComplete(payload.work, 0);
					}
				};
				var dispose = function() {
					if (ENVIRONMENT_IS_NODE) worker.off("message", handler);
					else worker.removeEventListener("message", handler, false);
					delete worker._emnapiAWMTListener;
				};
				worker._emnapiAWMTListener = {
					handler,
					dispose
				};
				if (ENVIRONMENT_IS_NODE) worker.on("message", handler);
				else worker.addEventListener("message", handler, false);
				return true;
			},
			initGlobal: function() {
				if (!emnapiAWMT.globalAddress) {
					emnapiAWMT.globalAddress = _malloc(emnapiAWMT.globalOffset.end);
					emnapiAWMT.globalAddress >>>= 0;
					var size = emnapiAWMT.globalOffset.end;
					var addr = emnapiAWMT.globalAddress;
					new Uint8Array(emnapiAWMT.ensureBufferFor(addr + size), addr, size).fill(0);
					emnapiAWMT.queueInit(emnapiAWMT.globalAddress + emnapiAWMT.globalOffset.q);
					emnapiAWMT.queueInit(emnapiAWMT.globalAddress + emnapiAWMT.globalOffset.exit_message);
				}
			},
			terminateWorkers: function() {
				emnapiAWMT.pool.forEach(function(w) {
					var _a, _b;
					(_a = w._emnapiAWMTListener) === null || _a === void 0 || _a.dispose();
					(_b = w._emnapiTSFNListener) === null || _b === void 0 || _b.dispose();
					w.terminate();
				});
				emnapiAWMT.pool.length = 0;
			},
			initWorkers: function(n) {
				if (ENVIRONMENT_IS_PTHREAD) return emnapiAWMT.workerReady || (emnapiAWMT.workerReady = Promise.resolve());
				if (emnapiAWMT.workerReady) return emnapiAWMT.workerReady;
				if (!("emnapi_async_worker_create" in wasmInstance.exports)) throw new TypeError("`emnapi_async_worker_create` is not exported, please try to add `--export=emnapi_async_worker_create` to linker flags");
				var emnapi_async_worker_create = wasmInstance.exports.emnapi_async_worker_create;
				var args = [];
				emnapiAWMT.initGlobal();
				for (var i = 0; i < n; ++i) args.push(emnapi_async_worker_create(1, emnapiAWMT.globalAddress));
				var promises = args.map(function(index) {
					if (index === 0) return Promise.reject(/* @__PURE__ */ new Error("Failed to create async worker"));
					var worker;
					if (index < 0) {
						worker = emnapiAWMT.pool[-index - 1];
						if (worker) return worker.whenLoaded;
					}
					index >>>= 0;
					var tidOffset = 20;
					var tid = new DataView(emnapiAWMT.ensureBufferFor(index + tidOffset + 4)).getInt32(index + tidOffset, true);
					worker = PThread.pthreads[tid];
					return worker.whenLoaded;
				});
				emnapiAWMT.workerReady = Promise.all(promises);
				return emnapiAWMT.workerReady;
			},
			getResource: function(work) {
				emnapiAWMT.ensureBufferFor(work + emnapiAWMT.offset.resource + 4);
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				return GET_HEAP_DATA_VIEW().getUint32(work + emnapiAWMT.offset.resource, true);
			},
			getExecute: function(work) {
				emnapiAWMT.ensureBufferFor(work + emnapiAWMT.offset.execute + 4);
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				return GET_HEAP_DATA_VIEW().getUint32(work + emnapiAWMT.offset.execute, true);
			},
			getComplete: function(work) {
				emnapiAWMT.ensureBufferFor(work + emnapiAWMT.offset.complete + 4);
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				return GET_HEAP_DATA_VIEW().getUint32(work + emnapiAWMT.offset.complete, true);
			},
			getEnv: function(work) {
				emnapiAWMT.ensureBufferFor(work + emnapiAWMT.offset.env + 4);
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				return GET_HEAP_DATA_VIEW().getUint32(work + emnapiAWMT.offset.env, true);
			},
			getData: function(work) {
				emnapiAWMT.ensureBufferFor(work + emnapiAWMT.offset.data + 4);
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				return GET_HEAP_DATA_VIEW().getUint32(work + emnapiAWMT.offset.data, true);
			},
			getMutex: function() {
				var index = emnapiAWMT.globalAddress + emnapiAWMT.globalOffset.mutex;
				var mutex = {
					lock: function() {
						var isBrowserMain = typeof window !== "undefined" && typeof document !== "undefined" && !ENVIRONMENT_IS_NODE;
						var i32a = new Int32Array(emnapiAWMT.ensureBufferFor(index + 4), index, 1);
						if (isBrowserMain) while (true) {
							var oldValue = Atomics.compareExchange(i32a, 0, 0, 10);
							if (oldValue === 0) return;
						}
						else while (true) {
							var oldValue = Atomics.compareExchange(i32a, 0, 0, 10);
							if (oldValue === 0) return;
							Atomics.wait(i32a, 0, 10);
						}
					},
					unlock: function() {
						var i32a = new Int32Array(emnapiAWMT.ensureBufferFor(index + 4), index, 1);
						if (Atomics.compareExchange(i32a, 0, 10, 0) !== 10) throw new Error("Tried to unlock while not holding the mutex");
						Atomics.notify(i32a, 0, 1);
					},
					execute: function(fn) {
						mutex.lock();
						try {
							return fn();
						} finally {
							mutex.unlock();
						}
					}
				};
				return mutex;
			},
			getCond: function() {
				var index = emnapiAWMT.globalAddress + emnapiAWMT.globalOffset.cond;
				var mutex = emnapiAWMT.getMutex();
				return {
					wait: function() {
						var i32a = new Int32Array(emnapiAWMT.ensureBufferFor(index + 4), index, 1);
						var value = Atomics.load(i32a, 0);
						mutex.unlock();
						Atomics.wait(i32a, 0, value);
						mutex.lock();
					},
					signal: function() {
						var i32a = new Int32Array(emnapiAWMT.ensureBufferFor(index + 4), index, 1);
						Atomics.add(i32a, 0, 1);
						Atomics.notify(i32a, 0, 1);
					}
				};
			},
			queueInit: function(q) {
				emnapiAWMT.ensureBufferFor(q + 4 + 4);
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(q, q, true);
				GET_HEAP_DATA_VIEW().setUint32(q + 4, q, true);
			},
			queueInsertTail: function(h, q) {
				emnapiAWMT.ensureBufferFor(h + 4 + 4);
				emnapiAWMT.ensureBufferFor(q + 4 + 4);
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(q, h, true);
				var tempValue = GET_HEAP_DATA_VIEW().getUint32(h + 4, true);
				GET_HEAP_DATA_VIEW().setUint32(q + 4, tempValue, true);
				var qprev = GET_HEAP_DATA_VIEW().getUint32(q + 4, true);
				GET_HEAP_DATA_VIEW().setUint32(qprev, q, true);
				GET_HEAP_DATA_VIEW().setUint32(h + 4, q, true);
			},
			queueRemove: function(q) {
				emnapiAWMT.ensureBufferFor(q + 4 + 4);
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				var qprev = GET_HEAP_DATA_VIEW().getUint32(q + 4, true);
				var qnext = GET_HEAP_DATA_VIEW().getUint32(q, true);
				GET_HEAP_DATA_VIEW().setUint32(qprev, qnext, true);
				GET_HEAP_DATA_VIEW().setUint32(qnext + 4, qprev, true);
			},
			queueEmpty: function(q) {
				emnapiAWMT.ensureBufferFor(q + 4);
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				return q == GET_HEAP_DATA_VIEW().getUint32(q, true);
			},
			scheduleWork: function(work) {
				var _a;
				if (!((_a = emnapiAWMT.workerReady) === null || _a === void 0 ? void 0 : _a.ready)) emnapiAWMT.initWorkers(_emnapi_async_work_pool_size()).then(function() {
					emnapiAWMT.workerReady.ready = true;
				}).catch(function(err) {
					emnapiAWMT.workerReady = null;
					throw err;
				});
				emnapiCtx.increaseWaitingRequestCounter();
				var statusBuffer = new Int32Array(emnapiAWMT.ensureBufferFor(work + emnapiAWMT.offset.status + 4), work + emnapiAWMT.offset.status, 1);
				Atomics.store(statusBuffer, 0, 0);
				var mutex = emnapiAWMT.getMutex();
				var cond = emnapiAWMT.getCond();
				mutex.lock();
				try {
					emnapiAWMT.queueInsertTail(emnapiAWMT.globalAddress + emnapiAWMT.globalOffset.q, work + emnapiAWMT.offset.queue);
				} catch (err) {
					emnapiCtx.decreaseWaitingRequestCounter();
					mutex.unlock();
					throw err;
				}
				emnapiAWMT.ensureBufferFor(emnapiAWMT.globalAddress + emnapiAWMT.globalOffset.idle_threads + 4);
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				if (GET_HEAP_DATA_VIEW().getUint32(emnapiAWMT.globalAddress + emnapiAWMT.globalOffset.idle_threads, true) > 0) cond.signal();
				mutex.unlock();
			},
			cancelWork: function(work) {
				var cancelled = false;
				emnapiAWMT.getMutex().execute(function() {
					emnapiAWMT.ensureBufferFor(work + emnapiAWMT.offset.status + 4);
					var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
						return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
					};
					cancelled = !emnapiAWMT.queueEmpty(work + emnapiAWMT.offset.queue) && GET_HEAP_DATA_VIEW().getInt32(work + emnapiAWMT.offset.status, true) !== 2;
					if (cancelled) emnapiAWMT.queueRemove(work + emnapiAWMT.offset.queue);
				});
				if (!cancelled) return 9;
				if (Atomics.compareExchange(new Int32Array(emnapiAWMT.ensureBufferFor(work + emnapiAWMT.offset.status + 4), work + emnapiAWMT.offset.status, 1), 0, 0, 1) !== 0) return 9;
				emnapiCtx.feature.setImmediate(function() {
					emnapiAWMT.callComplete(work, 11);
				});
				return 0;
			},
			callComplete: function(work, status) {
				emnapiCtx.decreaseWaitingRequestCounter();
				var complete = emnapiAWMT.getComplete(work);
				var env = emnapiAWMT.getEnv(work);
				var data = emnapiAWMT.getData(work);
				var envObject = emnapiCtx.envStore.get(env);
				var scope = emnapiCtx.openScope(envObject);
				var callback = function() {
					if (!complete) return;
					envObject.callbackIntoModule(true, function() {
						wasmTable.get(complete)(env, status, data);
					});
				};
				try {
					if (emnapiNodeBinding) {
						var resource = emnapiAWMT.getResource(work);
						var resource_value = emnapiCtx.refStore.get(resource).get();
						var resourceObject = emnapiCtx.handleStore.get(resource_value).value;
						var view = new DataView(emnapiAWMT.ensureBufferFor(work + emnapiAWMT.offset.trigger_async_id + 8));
						var asyncId = view.getFloat64(work + emnapiAWMT.offset.async_id, true);
						var triggerAsyncId = view.getFloat64(work + emnapiAWMT.offset.trigger_async_id, true);
						emnapiNodeBinding.node.makeCallback(resourceObject, callback, [], {
							asyncId,
							triggerAsyncId
						});
					} else callback();
				} finally {
					emnapiCtx.closeScope(envObject, scope);
				}
			}
		};
		emnapiAWMT.init();
		/** @__sig ippppppp */
		var napi_create_async_work = singleThreadAsyncWork ? function(env, resource, resource_name, execute, complete, data, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!execute) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			var resourceObject;
			if (resource) resourceObject = Object(emnapiCtx.handleStore.get(resource).value);
			else resourceObject = {};
			if (!resource_name) return envObject.setLastError(1);
			var resourceName = String(emnapiCtx.handleStore.get(resource_name).value);
			var id = emnapiAWST.create(env, resourceObject, resourceName, execute, complete, data);
			result >>>= 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(result, id, true);
			return envObject.clearLastError();
		} : function(env, resource, resource_name, execute, complete, data, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!execute) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			var resourceObject;
			if (resource) resourceObject = Object(emnapiCtx.handleStore.get(resource).value);
			else resourceObject = {};
			if (!resource_name) return envObject.setLastError(1);
			var sizeofAW = emnapiAWMT.offset.end;
			var aw = _malloc(sizeofAW);
			if (!aw) return envObject.setLastError(9);
			aw >>>= 0;
			new Uint8Array(emnapiAWMT.ensureBufferFor(aw + sizeofAW)).subarray(aw, aw + sizeofAW).fill(0);
			var s = envObject.ensureHandleId(resourceObject);
			var resource_ = emnapiCtx.createReference(envObject, s, 1, 1).id;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(aw, resource_, true);
			_emnapi_node_emit_async_init(s, resource_name, -1, aw + emnapiAWMT.offset.async_id);
			GET_HEAP_DATA_VIEW().setUint32(aw + emnapiAWMT.offset.env, env, true);
			GET_HEAP_DATA_VIEW().setUint32(aw + emnapiAWMT.offset.execute, execute, true);
			GET_HEAP_DATA_VIEW().setUint32(aw + emnapiAWMT.offset.complete, complete, true);
			GET_HEAP_DATA_VIEW().setUint32(aw + emnapiAWMT.offset.data, data, true);
			emnapiAWMT.queueInit(aw + emnapiAWMT.offset.queue);
			result >>>= 0;
			GET_HEAP_DATA_VIEW().setUint32(result, aw, true);
			return envObject.clearLastError();
		};
		/** @__sig ipp */
		var napi_delete_async_work = singleThreadAsyncWork ? function(env, work) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!work) return envObject.setLastError(1);
			work >>>= 0;
			emnapiAWST.remove(work);
			return envObject.clearLastError();
		} : function(env, work) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!work) return envObject.setLastError(1);
			work >>>= 0;
			var resource = emnapiAWMT.getResource(work);
			emnapiCtx.refStore.get(resource).dispose();
			if (emnapiNodeBinding) {
				var view = new DataView(emnapiAWMT.ensureBufferFor(work + emnapiAWMT.offset.trigger_async_id + 8));
				_emnapi_node_emit_async_destroy(view.getFloat64(work + emnapiAWMT.offset.async_id, true), view.getFloat64(work + emnapiAWMT.offset.trigger_async_id, true));
			}
			_free(work);
			return envObject.clearLastError();
		};
		/** @__sig ipp */
		var napi_queue_async_work = singleThreadAsyncWork ? function(env, work) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			if (!work) return envObject.setLastError(1);
			work >>>= 0;
			emnapiAWST.queue(work);
			return envObject.clearLastError();
		} : function(env, work) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			if (!work) return envObject.setLastError(1);
			work >>>= 0;
			emnapiAWMT.scheduleWork(work);
			return envObject.clearLastError();
		};
		/** @__sig ipp */
		var napi_cancel_async_work = singleThreadAsyncWork ? function(env, work) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			if (!work) return envObject.setLastError(1);
			work >>>= 0;
			var status = emnapiAWST.cancel(work);
			if (status === 0) return envObject.clearLastError();
			return envObject.setLastError(status);
		} : function(env, work) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			if (!work) return envObject.setLastError(1);
			work >>>= 0;
			var status = emnapiAWMT.cancelWork(work);
			if (status === 0) return envObject.clearLastError();
			return envObject.setLastError(status);
		};
		/** @__sig pp */
		function _emnapi_async_worker(globalAddress) {
			globalAddress >>>= 0;
			emnapiAWMT.globalAddress = globalAddress;
			var mutex = emnapiAWMT.getMutex();
			var cond = emnapiAWMT.getCond();
			mutex.lock();
			var exitMessageAddr = globalAddress + emnapiAWMT.globalOffset.exit_message;
			var idleThreadsAddr = globalAddress + emnapiAWMT.globalOffset.idle_threads;
			var workerQueueAddr = globalAddress + emnapiAWMT.globalOffset.q;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			for (;;) {
				emnapiAWMT.ensureBufferFor(workerQueueAddr + 4);
				while (emnapiAWMT.queueEmpty(workerQueueAddr)) {
					Atomics.add(new Int32Array(emnapiAWMT.ensureBufferFor(idleThreadsAddr + 4), idleThreadsAddr, 1), 0, 1);
					cond.wait();
					Atomics.sub(new Int32Array(emnapiAWMT.ensureBufferFor(idleThreadsAddr + 4), idleThreadsAddr, 1), 0, 1);
				}
				var q = GET_HEAP_DATA_VIEW().getUint32(workerQueueAddr, true);
				if (q === exitMessageAddr) {
					cond.signal();
					mutex.unlock();
					break;
				}
				var work = q - emnapiAWMT.offset.queue;
				emnapiAWMT.queueRemove(q);
				emnapiAWMT.queueInit(q);
				mutex.unlock();
				var statusBuffer = new Int32Array(emnapiAWMT.ensureBufferFor(work + emnapiAWMT.offset.status + 4), work + emnapiAWMT.offset.status, 1);
				if (Atomics.load(statusBuffer, 0) === 1) abort("unreachable");
				var execute = emnapiAWMT.getExecute(work);
				var env = emnapiAWMT.getEnv(work);
				var data = emnapiAWMT.getData(work);
				wasmTable.get(execute)(env, data);
				Atomics.store(statusBuffer, 0, 2);
				(0, napiModule.postMessage)({ __emnapi__: {
					type: "async-work-complete",
					payload: { work }
				} });
				mutex.lock();
			}
			return 0;
		}
		/** @__sig ipp */
		function _emnapi_spawn_worker(f, globalAddress) {
			if (typeof onCreateWorker !== "function") throw new TypeError("`options.onCreateWorker` is not a function");
			var promises = [];
			var args = [];
			if (!("emnapi_async_worker_create" in wasmInstance.exports)) throw new TypeError("`emnapi_async_worker_create` is not exported, please try to add `--export=emnapi_async_worker_create` to linker flags");
			args.push(wasmInstance.exports.emnapi_async_worker_create(0, 0));
			var handleError = function(e) {
				if ("message" in e && (e.message.indexOf("RuntimeError") !== -1 || e.message.indexOf("unreachable") !== -1)) emnapiAWMT.terminateWorkers();
			};
			var ret;
			try {
				var worker_1 = onCreateWorker({
					type: "async-work",
					name: "emnapi-async-worker"
				});
				var p = PThread.loadWasmModuleToWorker(worker_1);
				if (ENVIRONMENT_IS_NODE) worker_1.on("error", handleError);
				else worker_1.addEventListener("error", handleError, false);
				emnapiAWMT.addListener(worker_1);
				emnapiTSFN.addListener(worker_1);
				promises.push(p.then(function() {
					if (typeof worker_1.unref === "function") worker_1.unref();
				}));
				ret = emnapiAWMT.pool.push(worker_1) - 1;
				var arg = args[0];
				worker_1.threadBlockBase = arg;
				worker_1.postMessage({ __emnapi__: {
					type: "async-worker-init",
					payload: {
						arg,
						func: [f, globalAddress]
					}
				} });
			} catch (err) {
				var arg = args[0];
				_free(arg);
				throw err;
			}
			return ret;
		}
		function initWorker(startArg, func) {
			if (napiModule.childThread) {
				if (typeof wasmInstance.exports.emnapi_async_worker_init !== "function") throw new TypeError("`emnapi_async_worker_init` is not exported, please try to add `--export=emnapi_async_worker_init` to linker flags");
				wasmInstance.exports.emnapi_async_worker_init(startArg);
				wasmTable.get(func[0])(func[1]);
			} else throw new Error("startThread is only available in child threads");
		}
		napiModule.initWorker = initWorker;
		var asyncWorkMod = /*#__PURE__*/ Object.freeze({
			__proto__: null,
			_emnapi_async_worker,
			_emnapi_spawn_worker,
			napi_cancel_async_work,
			napi_create_async_work,
			napi_delete_async_work,
			napi_queue_async_work
		});
		/**
		* @__deps malloc
		* @__deps free
		* @__postset
		* ```
		* emnapiExternalMemory.init();
		* ```
		*/
		var emnapiExternalMemory = {
			registry: typeof FinalizationRegistry === "function" ? new FinalizationRegistry(function(_pointer) {
				_free(_pointer);
			}) : void 0,
			table: /* @__PURE__ */ new WeakMap(),
			wasmMemoryViewTable: /* @__PURE__ */ new WeakMap(),
			intrinsics: void 0,
			init: function() {
				var _a;
				emnapiExternalMemory.registry = typeof FinalizationRegistry === "function" ? new FinalizationRegistry(function(_pointer) {
					_free(_pointer);
				}) : void 0;
				emnapiExternalMemory.table = /* @__PURE__ */ new WeakMap();
				emnapiExternalMemory.wasmMemoryViewTable = /* @__PURE__ */ new WeakMap();
				var sharedArrayBufferByteLength = typeof SharedArrayBuffer === "function" ? (_a = Object.getOwnPropertyDescriptor(SharedArrayBuffer.prototype, "byteLength")) === null || _a === void 0 ? void 0 : _a.get : void 0;
				emnapiExternalMemory.intrinsics = {
					isView: ArrayBuffer.isView,
					typedArray: {
						buffer: Object.getOwnPropertyDescriptor(Object.getPrototypeOf(Uint8Array.prototype), "buffer").get,
						byteOffset: Object.getOwnPropertyDescriptor(Object.getPrototypeOf(Uint8Array.prototype), "byteOffset").get,
						length: Object.getOwnPropertyDescriptor(Object.getPrototypeOf(Uint8Array.prototype), "length").get,
						tag: Object.getOwnPropertyDescriptor(Object.getPrototypeOf(Uint8Array.prototype), Symbol.toStringTag).get
					},
					dataView: {
						buffer: Object.getOwnPropertyDescriptor(DataView.prototype, "buffer").get,
						byteOffset: Object.getOwnPropertyDescriptor(DataView.prototype, "byteOffset").get,
						byteLength: Object.getOwnPropertyDescriptor(DataView.prototype, "byteLength").get
					},
					arrayBuffer: { byteLength: Object.getOwnPropertyDescriptor(ArrayBuffer.prototype, "byteLength").get },
					sharedArrayBuffer: sharedArrayBufferByteLength !== void 0 ? { byteLength: sharedArrayBufferByteLength } : void 0,
					ctors: {
						Int8Array,
						Uint8Array,
						Uint8ClampedArray,
						Int16Array,
						Uint16Array,
						Int32Array,
						Uint32Array,
						Float16Array: typeof Float16Array === "function" ? Float16Array : void 0,
						Float32Array,
						Float64Array,
						BigInt64Array,
						BigUint64Array
					},
					bufferFrom: void 0
				};
			},
			isSharedArrayBuffer: function(value) {
				var sharedArrayBuffer = emnapiExternalMemory.intrinsics.sharedArrayBuffer;
				if (sharedArrayBuffer !== void 0) {
					if (typeof SharedArrayBuffer === "function" && value instanceof SharedArrayBuffer) return true;
					try {
						sharedArrayBuffer.byteLength.call(value);
						return true;
					} catch (_) {
						return false;
					}
				}
				try {
					emnapiExternalMemory.intrinsics.arrayBuffer.byteLength.call(value);
					return false;
				} catch (_) {}
				return Object.prototype.toString.call(value) === "[object SharedArrayBuffer]";
			},
			isDetachedArrayBuffer: function(arrayBuffer) {
				if (emnapiExternalMemory.bufferByteLength(arrayBuffer) === 0) try {
					new Uint8Array(arrayBuffer);
				} catch (_) {
					return true;
				}
				return false;
			},
			bufferByteLength: function(buffer) {
				var _a;
				var intrinsics = emnapiExternalMemory.intrinsics;
				try {
					return intrinsics.arrayBuffer.byteLength.call(buffer);
				} catch (err) {
					var sharedArrayBuffer = intrinsics.sharedArrayBuffer;
					if (sharedArrayBuffer !== void 0) return sharedArrayBuffer.byteLength.call(buffer);
					var proto = Object.getPrototypeOf(buffer);
					var get = proto !== null && proto !== void 0 ? (_a = Object.getOwnPropertyDescriptor(proto, "byteLength")) === null || _a === void 0 ? void 0 : _a.get : void 0;
					if (get === void 0) throw err;
					return get.call(buffer);
				}
			},
			viewBuffer: function(view) {
				var intrinsics = emnapiExternalMemory.intrinsics;
				return intrinsics.typedArray.tag.call(view) === void 0 ? intrinsics.dataView.buffer.call(view) : intrinsics.typedArray.buffer.call(view);
			},
			viewByteOffset: function(view) {
				var intrinsics = emnapiExternalMemory.intrinsics;
				return intrinsics.typedArray.tag.call(view) === void 0 ? intrinsics.dataView.byteOffset.call(view) : intrinsics.typedArray.byteOffset.call(view);
			},
			viewLength: function(view) {
				var intrinsics = emnapiExternalMemory.intrinsics;
				return intrinsics.typedArray.tag.call(view) === void 0 ? intrinsics.dataView.byteLength.call(view) : intrinsics.typedArray.length.call(view);
			},
			getBufferFrom: function() {
				var _a;
				return (_a = emnapiExternalMemory.intrinsics.bufferFrom) !== null && _a !== void 0 ? _a : emnapiExternalMemory.intrinsics.bufferFrom = emnapiCtx.feature.Buffer.from;
			},
			getArrayBufferPointer: function(arrayBuffer, shouldCopy) {
				var _a;
				var info = {
					address: 0,
					ownership: 0,
					runtimeAllocated: 0
				};
				if (arrayBuffer === wasmMemory.buffer) return info;
				var isDetached = emnapiExternalMemory.isDetachedArrayBuffer(arrayBuffer);
				if (emnapiExternalMemory.table.has(arrayBuffer)) {
					var cachedInfo = emnapiExternalMemory.table.get(arrayBuffer);
					if (isDetached) {
						cachedInfo.address = 0;
						return cachedInfo;
					}
					if (shouldCopy && cachedInfo.ownership === 0 && cachedInfo.runtimeAllocated === 1) new Uint8Array(wasmMemory.buffer).set(new Uint8Array(arrayBuffer), cachedInfo.address);
					return cachedInfo;
				}
				var byteLength = emnapiExternalMemory.bufferByteLength(arrayBuffer);
				if (isDetached || byteLength === 0) return info;
				if (!shouldCopy) return info;
				var pointer = _malloc(byteLength);
				if (!pointer) throw new Error("Out of memory");
				pointer >>>= 0;
				new Uint8Array(wasmMemory.buffer).set(new Uint8Array(arrayBuffer), pointer);
				info.address = pointer;
				info.ownership = emnapiExternalMemory.registry ? 0 : 1;
				info.runtimeAllocated = 1;
				emnapiExternalMemory.table.set(arrayBuffer, info);
				(_a = emnapiExternalMemory.registry) === null || _a === void 0 || _a.register(arrayBuffer, pointer);
				return info;
			},
			getOrUpdateMemoryView: function(view) {
				var intrinsics = emnapiExternalMemory.intrinsics;
				var tag = intrinsics.typedArray.tag.call(view);
				var isDataView = tag === void 0;
				var buffer = isDataView ? intrinsics.dataView.buffer.call(view) : intrinsics.typedArray.buffer.call(view);
				if (buffer === wasmMemory.buffer) {
					if (!emnapiExternalMemory.wasmMemoryViewTable.has(view)) emnapiExternalMemory.wasmMemoryViewTable.set(view, {
						Ctor: isDataView ? DataView : intrinsics.ctors[tag],
						address: isDataView ? intrinsics.dataView.byteOffset.call(view) : intrinsics.typedArray.byteOffset.call(view),
						length: isDataView ? intrinsics.dataView.byteLength.call(view) : intrinsics.typedArray.length.call(view),
						ownership: 1,
						runtimeAllocated: 0
					});
					return view;
				}
				if ((emnapiExternalMemory.isDetachedArrayBuffer(buffer) || emnapiExternalMemory.isSharedArrayBuffer(buffer)) && emnapiExternalMemory.wasmMemoryViewTable.has(view)) {
					var info = emnapiExternalMemory.wasmMemoryViewTable.get(view);
					var Ctor = info.Ctor;
					var newView = void 0;
					var Buffer_1 = emnapiCtx.feature.Buffer;
					if (typeof Buffer_1 === "function" && Ctor === Buffer_1) newView = emnapiExternalMemory.getBufferFrom()(wasmMemory.buffer, info.address, info.length);
					else newView = new Ctor(wasmMemory.buffer, info.address, info.length);
					emnapiExternalMemory.wasmMemoryViewTable.set(newView, info);
					return newView;
				}
				return view;
			},
			getViewPointer: function(view, shouldCopy) {
				view = emnapiExternalMemory.getOrUpdateMemoryView(view);
				var buffer = emnapiExternalMemory.viewBuffer(view);
				if (buffer === wasmMemory.buffer) {
					if (emnapiExternalMemory.wasmMemoryViewTable.has(view)) {
						var _a = emnapiExternalMemory.wasmMemoryViewTable.get(view);
						return {
							address: _a.address,
							ownership: _a.ownership,
							runtimeAllocated: _a.runtimeAllocated,
							view
						};
					}
					return {
						address: emnapiExternalMemory.viewByteOffset(view),
						ownership: 1,
						runtimeAllocated: 0,
						view
					};
				}
				var _b = emnapiExternalMemory.getArrayBufferPointer(buffer, shouldCopy), address = _b.address, ownership = _b.ownership, runtimeAllocated = _b.runtimeAllocated;
				return {
					address: address === 0 ? 0 : address + emnapiExternalMemory.viewByteOffset(view),
					ownership,
					runtimeAllocated,
					view
				};
			}
		};
		/**
		* Metadata layout in wasm shared memory (allocated with _malloc):
		*   offset 0:              refcount     (int32, 4 bytes - for Atomics)
		*   offset POINTER_SIZE:   external_data (pointer)
		*   offset 2*POINTER_SIZE: byte_length   (size_t)
		*   offset 3*POINTER_SIZE: finalize_cb   (pointer)
		*   offset 4*POINTER_SIZE: finalize_data (pointer)
		*   offset 5*POINTER_SIZE: finalize_hint (pointer)
		*   Total: 6 * POINTER_SIZE bytes
		*/
		/**
		* @__postset
		* ```
		* emnapiExternalSAB.init();
		* ```
		*/
		var emnapiExternalSAB = {
			registry: void 0,
			handleTable: /* @__PURE__ */ new WeakMap(),
			init: function() {
				emnapiExternalSAB.handleTable = /* @__PURE__ */ new WeakMap();
				emnapiExternalSAB.registry = typeof FinalizationRegistry === "function" ? new FinalizationRegistry(function(metaPtr) {
					emnapiExternalSAB.release(metaPtr);
				}) : void 0;
			},
			allocMeta: function(external_data, byte_length, finalize_cb, finalize_data, finalize_hint) {
				var metaPtr = _malloc(24);
				if (!metaPtr) throw new Error("Out of memory");
				metaPtr >>>= 0;
				Atomics.store(new Int32Array(wasmMemory.buffer, metaPtr, 1), 0, 1);
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(metaPtr + 4, external_data, true);
				GET_HEAP_DATA_VIEW().setUint32(metaPtr + 8, byte_length, true);
				GET_HEAP_DATA_VIEW().setUint32(metaPtr + 12, finalize_cb, true);
				GET_HEAP_DATA_VIEW().setUint32(metaPtr + 16, finalize_data, true);
				GET_HEAP_DATA_VIEW().setUint32(metaPtr + 20, finalize_hint, true);
				return metaPtr;
			},
			readMeta: function(metaPtr) {
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				return {
					external_data: GET_HEAP_DATA_VIEW().getUint32(metaPtr + 4, true),
					byte_length: GET_HEAP_DATA_VIEW().getUint32(metaPtr + 8, true),
					finalize_cb: GET_HEAP_DATA_VIEW().getUint32(metaPtr + 12, true),
					finalize_data: GET_HEAP_DATA_VIEW().getUint32(metaPtr + 16, true),
					finalize_hint: GET_HEAP_DATA_VIEW().getUint32(metaPtr + 20, true)
				};
			},
			release: function(metaPtr) {
				if (Atomics.sub(new Int32Array(wasmMemory.buffer, metaPtr, 1), 0, 1) === 1) {
					var info = emnapiExternalSAB.readMeta(metaPtr);
					var finalize_cb = info.finalize_cb;
					if (finalize_cb) {
						var finalize_data = info.finalize_data;
						var finalize_hint = info.finalize_hint;
						wasmTable.get(finalize_cb)(finalize_data, finalize_hint);
					}
					_free(metaPtr);
				}
			}
		};
		/**
		* @__postset
		* ```
		* emnapiString.init();
		* ```
		*/
		var emnapiString = {
			utf8Decoder: void 0,
			utf16Decoder: void 0,
			init: function() {
				emnapiString.utf8Decoder = typeof TextDecoder === "function" ? new TextDecoder() : { decode: function(bytes) {
					var inputIndex = 0;
					var pendingSize = Math.min(4096, bytes.length + 1);
					var pending = new Uint16Array(pendingSize);
					var chunks = [];
					var pendingIndex = 0;
					for (;;) {
						var more = inputIndex < bytes.length;
						if (!more || pendingIndex >= pendingSize - 1) {
							var arraylike = pending.subarray(0, pendingIndex);
							chunks.push(String.fromCharCode.apply(null, arraylike));
							if (!more) return chunks.join("");
							bytes = bytes.subarray(inputIndex);
							inputIndex = 0;
							pendingIndex = 0;
						}
						var byte1 = bytes[inputIndex++];
						if ((byte1 & 128) === 0) pending[pendingIndex++] = byte1;
						else if ((byte1 & 224) === 192) {
							var byte2 = bytes[inputIndex++] & 63;
							pending[pendingIndex++] = (byte1 & 31) << 6 | byte2;
						} else if ((byte1 & 240) === 224) {
							var byte2 = bytes[inputIndex++] & 63;
							var byte3 = bytes[inputIndex++] & 63;
							pending[pendingIndex++] = (byte1 & 31) << 12 | byte2 << 6 | byte3;
						} else if ((byte1 & 248) === 240) {
							var byte2 = bytes[inputIndex++] & 63;
							var byte3 = bytes[inputIndex++] & 63;
							var byte4 = bytes[inputIndex++] & 63;
							var codepoint = (byte1 & 7) << 18 | byte2 << 12 | byte3 << 6 | byte4;
							if (codepoint > 65535) {
								codepoint -= 65536;
								pending[pendingIndex++] = codepoint >>> 10 & 1023 | 55296;
								codepoint = 56320 | codepoint & 1023;
							}
							pending[pendingIndex++] = codepoint;
						}
					}
				} };
				emnapiString.utf16Decoder = typeof TextDecoder === "function" ? new TextDecoder("utf-16le") : { decode: function(input) {
					var bytes = new Uint16Array(input.buffer, input.byteOffset, input.byteLength / 2);
					if (bytes.length <= 4096) return String.fromCharCode.apply(null, bytes);
					var chunks = [];
					var i = 0;
					var len = 0;
					for (; i < bytes.length; i += len) {
						len = Math.min(4096, bytes.length - i);
						chunks.push(String.fromCharCode.apply(null, bytes.subarray(i, i + len)));
					}
					return chunks.join("");
				} };
			},
			lengthBytesUTF8: function(str) {
				var c;
				var len = 0;
				for (var i = 0; i < str.length; ++i) {
					c = str.charCodeAt(i);
					if (c <= 127) len++;
					else if (c <= 2047) len += 2;
					else if (c >= 55296 && c <= 57343) {
						len += 4;
						++i;
					} else len += 3;
				}
				return len;
			},
			UTF8ToString: function(ptr, length) {
				if (!ptr || !length) return "";
				ptr >>>= 0;
				var HEAPU8 = new Uint8Array(wasmMemory.buffer);
				var end = ptr;
				if (length === -1 || length === 0xffffffff) for (; HEAPU8[end];) ++end;
				else end = ptr + (length >>> 0);
				length = end - ptr;
				if (length <= 16) {
					var idx = ptr;
					var str = "";
					while (idx < end) {
						var u0 = HEAPU8[idx++];
						if (!(u0 & 128)) {
							str += String.fromCharCode(u0);
							continue;
						}
						var u1 = HEAPU8[idx++] & 63;
						if ((u0 & 224) === 192) {
							str += String.fromCharCode((u0 & 31) << 6 | u1);
							continue;
						}
						var u2 = HEAPU8[idx++] & 63;
						if ((u0 & 240) === 224) u0 = (u0 & 15) << 12 | u1 << 6 | u2;
						else u0 = (u0 & 7) << 18 | u1 << 12 | u2 << 6 | HEAPU8[idx++] & 63;
						if (u0 < 65536) str += String.fromCharCode(u0);
						else {
							var ch = u0 - 65536;
							str += String.fromCharCode(55296 | ch >> 10, 56320 | ch & 1023);
						}
					}
					return str;
				}
				return emnapiString.utf8Decoder.decode(typeof SharedArrayBuffer === "function" && HEAPU8.buffer instanceof SharedArrayBuffer || Object.prototype.toString.call(HEAPU8.buffer) === "[object SharedArrayBuffer]" ? HEAPU8.slice(ptr, end) : HEAPU8.subarray(ptr, end));
			},
			stringToUTF8: function(str, outPtr, maxBytesToWrite) {
				var HEAPU8 = new Uint8Array(wasmMemory.buffer);
				var outIdx = outPtr;
				outIdx >>>= 0;
				if (!(maxBytesToWrite > 0)) return 0;
				var startIdx = outIdx;
				var endIdx = outIdx + maxBytesToWrite - 1;
				for (var i = 0; i < str.length; ++i) {
					var u = str.charCodeAt(i);
					if (u >= 55296 && u <= 57343) {
						var u1 = str.charCodeAt(++i);
						u = 65536 + ((u & 1023) << 10) | u1 & 1023;
					}
					if (u <= 127) {
						if (outIdx >= endIdx) break;
						HEAPU8[outIdx++] = u;
					} else if (u <= 2047) {
						if (outIdx + 1 >= endIdx) break;
						HEAPU8[outIdx++] = 192 | u >> 6;
						HEAPU8[outIdx++] = 128 | u & 63;
					} else if (u <= 65535) {
						if (outIdx + 2 >= endIdx) break;
						HEAPU8[outIdx++] = 224 | u >> 12;
						HEAPU8[outIdx++] = 128 | u >> 6 & 63;
						HEAPU8[outIdx++] = 128 | u & 63;
					} else {
						if (outIdx + 3 >= endIdx) break;
						HEAPU8[outIdx++] = 240 | u >> 18;
						HEAPU8[outIdx++] = 128 | u >> 12 & 63;
						HEAPU8[outIdx++] = 128 | u >> 6 & 63;
						HEAPU8[outIdx++] = 128 | u & 63;
					}
				}
				HEAPU8[outIdx] = 0;
				return outIdx - startIdx;
			},
			UTF16ToString: function(ptr, length) {
				if (!ptr || !length) return "";
				ptr >>>= 0;
				var end = ptr;
				if (length === -1 || length === 0xffffffff) {
					var idx = end >>> 1;
					var HEAPU16 = new Uint16Array(wasmMemory.buffer);
					while (HEAPU16[idx]) ++idx;
					end = idx << 1 >>> 0;
				} else end = ptr + (length >>> 0) * 2;
				length = end - ptr;
				if (length <= 32) return String.fromCharCode.apply(null, new Uint16Array(wasmMemory.buffer, ptr, length / 2));
				var HEAPU8 = new Uint8Array(wasmMemory.buffer);
				return emnapiString.utf16Decoder.decode(typeof SharedArrayBuffer === "function" && HEAPU8.buffer instanceof SharedArrayBuffer || Object.prototype.toString.call(HEAPU8.buffer) === "[object SharedArrayBuffer]" ? HEAPU8.slice(ptr, end) : HEAPU8.subarray(ptr, end));
			},
			stringToUTF16: function(str, outPtr, maxBytesToWrite) {
				if (maxBytesToWrite === void 0) maxBytesToWrite = 0x7fffffff;
				if (maxBytesToWrite < 2) return 0;
				maxBytesToWrite -= 2;
				var startPtr = outPtr;
				var numCharsToWrite = maxBytesToWrite < str.length * 2 ? maxBytesToWrite / 2 : str.length;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				for (var i = 0; i < numCharsToWrite; ++i) {
					var codeUnit = str.charCodeAt(i);
					GET_HEAP_DATA_VIEW().setInt16(outPtr, codeUnit, true);
					outPtr += 2;
				}
				GET_HEAP_DATA_VIEW().setInt16(outPtr, 0, true);
				return outPtr - startPtr;
			},
			newString: function(env, str, length, result, stringMaker) {
				length >>>= 0;
				if (!env) return 1;
				var envObject = emnapiCtx.envStore.get(env);
				envObject.checkGCAccess();
				var autoLength = length === -1 || length === 0xffffffff;
				var sizelength = length >>> 0;
				if (length !== 0) {
					if (!str) return envObject.setLastError(1);
				}
				if (!result) return envObject.setLastError(1);
				if (!(autoLength || sizelength <= 0x7fffffff)) return envObject.setLastError(1);
				str >>>= 0;
				var strValue = stringMaker(str, autoLength, sizelength);
				result >>>= 0;
				var value = emnapiCtx.addToCurrentScope(strValue).id;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(result, value, true);
				return envObject.clearLastError();
			},
			newExternalString: function(env, str, length, finalize_callback, finalize_hint, result, copied, createApi, stringMaker) {
				length >>>= 0;
				if (!env) return 1;
				var envObject = emnapiCtx.envStore.get(env);
				envObject.checkGCAccess();
				var autoLength = length === -1 || length === 0xffffffff;
				var sizelength = length >>> 0;
				if (length !== 0) {
					if (!str) return envObject.setLastError(1);
				}
				if (!result) return envObject.setLastError(1);
				if (!(autoLength || sizelength <= 0x7fffffff)) return envObject.setLastError(1);
				var status = createApi(env, str, length, result);
				if (status === 0) {
					if (copied) {
						var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
							return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
						};
						GET_HEAP_DATA_VIEW().setInt8(copied, 1, true);
					}
					if (finalize_callback) envObject.callFinalizer(finalize_callback, str, finalize_hint);
				}
				return status;
			}
		};
		/**
		* @__sig ippp
		*/
		function napi_get_array_length(env, value, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!value) return envObject.setLastError(1);
				if (!result) return envObject.setLastError(1);
				var handle = emnapiCtx.handleStore.get(value);
				if (!handle.isArray()) return envObject.setLastError(8);
				result >>>= 0;
				var v = handle.value.length >>> 0;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(result, v, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/**
		* @__sig ipppp
		*/
		function napi_get_arraybuffer_info(env, arraybuffer, data, byte_length) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!arraybuffer) return envObject.setLastError(1);
			var handle = emnapiCtx.handleStore.get(arraybuffer);
			if (!handle.isArrayBuffer() && !emnapiExternalMemory.isSharedArrayBuffer(handle.value)) return envObject.setLastError(1);
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			if (data) {
				data >>>= 0;
				var p = emnapiExternalMemory.getArrayBufferPointer(handle.value, true).address;
				GET_HEAP_DATA_VIEW().setUint32(data, p, true);
			}
			if (byte_length) {
				byte_length >>>= 0;
				var len = emnapiExternalMemory.bufferByteLength(handle.value);
				GET_HEAP_DATA_VIEW().setUint32(byte_length, len, true);
			}
			return envObject.clearLastError();
		}
		/**
		* @__sig ippp
		*/
		function node_api_set_prototype(env, object, value) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!value) return envObject.setLastError(1);
				var obj = emnapiCtx.handleStore.get(object).value;
				if (obj == null) throw new TypeError("Cannot convert undefined or null to object");
				var type = typeof obj;
				var v = void 0;
				try {
					v = type === "object" && obj !== null || type === "function" ? obj : Object(obj);
				} catch (_) {
					return envObject.setLastError(2);
				}
				var val = emnapiCtx.handleStore.get(value).value;
				Object.setPrototypeOf(v, val);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/**
		* @__sig ippp
		*/
		function napi_get_prototype(env, value, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!value) return envObject.setLastError(1);
				if (!result) return envObject.setLastError(1);
				var handle = emnapiCtx.handleStore.get(value);
				if (handle.value == null) throw new TypeError("Cannot convert undefined or null to object");
				var v = void 0;
				try {
					v = handle.isObject() || handle.isFunction() ? handle.value : Object(handle.value);
				} catch (_) {
					return envObject.setLastError(2);
				}
				result >>>= 0;
				var p = envObject.ensureHandleId(Object.getPrototypeOf(v));
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(result, p, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/**
		* @__sig ippppppp
		*/
		function napi_get_typedarray_info(env, typedarray, type, length, data, arraybuffer, byte_offset) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!typedarray) return envObject.setLastError(1);
			var handle = emnapiCtx.handleStore.get(typedarray);
			if (!emnapiExternalMemory.intrinsics.isView(handle.value)) return envObject.setLastError(1);
			var v = handle.value;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			if (type) {
				type >>>= 0;
				var t = void 0;
				switch (emnapiExternalMemory.intrinsics.typedArray.tag.call(v)) {
					case "Int8Array":
						t = 0;
						break;
					case "Uint8Array":
						t = 1;
						break;
					case "Uint8ClampedArray":
						t = 2;
						break;
					case "Int16Array":
						t = 3;
						break;
					case "Uint16Array":
						t = 4;
						break;
					case "Int32Array":
						t = 5;
						break;
					case "Uint32Array":
						t = 6;
						break;
					case "Float16Array":
						t = 11;
						break;
					case "Float32Array":
						t = 7;
						break;
					case "Float64Array":
						t = 8;
						break;
					case "BigInt64Array":
						t = 9;
						break;
					case "BigUint64Array":
						t = 10;
						break;
					default: return envObject.setLastError(9);
				}
				GET_HEAP_DATA_VIEW().setInt32(type, t, true);
			}
			v = emnapiExternalMemory.getOrUpdateMemoryView(v);
			if (length) {
				length >>>= 0;
				var len = emnapiExternalMemory.viewLength(v);
				GET_HEAP_DATA_VIEW().setUint32(length, len, true);
			}
			if (data || arraybuffer) {
				if (data) {
					data >>>= 0;
					var vp = emnapiExternalMemory.getViewPointer(v, true);
					v = vp.view;
					var p = vp.address;
					GET_HEAP_DATA_VIEW().setUint32(data, p, true);
				}
				if (arraybuffer) {
					arraybuffer >>>= 0;
					var ab = envObject.ensureHandleId(emnapiExternalMemory.viewBuffer(v));
					GET_HEAP_DATA_VIEW().setUint32(arraybuffer, ab, true);
				}
			}
			if (byte_offset) {
				byte_offset >>>= 0;
				var offset = emnapiExternalMemory.viewByteOffset(v);
				GET_HEAP_DATA_VIEW().setUint32(byte_offset, offset, true);
			}
			return envObject.clearLastError();
		}
		/**
		* @__sig ipppp
		*/
		function napi_get_buffer_info(env, buffer, data, length) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!buffer) return envObject.setLastError(1);
			var handle = emnapiCtx.handleStore.get(buffer);
			var Buffer = emnapiCtx.feature.Buffer;
			if (!(emnapiExternalMemory.intrinsics.isView(handle.value) || typeof Buffer === "function" && Buffer.isBuffer(handle.value))) return envObject.setLastError(1);
			if (emnapiExternalMemory.intrinsics.typedArray.tag.call(handle.value) === void 0) return napi_get_dataview_info(env, buffer, length, data, 0, 0);
			return napi_get_typedarray_info(env, buffer, 0, length, data, 0, 0);
		}
		/**
		* @__sig ipppppp
		*/
		function napi_get_dataview_info(env, dataview, byte_length, data, arraybuffer, byte_offset) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!dataview) return envObject.setLastError(1);
			var handle = emnapiCtx.handleStore.get(dataview);
			if (!emnapiExternalMemory.intrinsics.isView(handle.value) || emnapiExternalMemory.intrinsics.typedArray.tag.call(handle.value) !== void 0) return envObject.setLastError(1);
			var v = emnapiExternalMemory.getOrUpdateMemoryView(handle.value);
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			if (byte_length) {
				byte_length >>>= 0;
				var len = emnapiExternalMemory.viewLength(v);
				GET_HEAP_DATA_VIEW().setUint32(byte_length, len, true);
			}
			if (data || arraybuffer) {
				if (data) {
					data >>>= 0;
					var vp = emnapiExternalMemory.getViewPointer(v, true);
					v = vp.view;
					var p = vp.address;
					GET_HEAP_DATA_VIEW().setUint32(data, p, true);
				}
				if (arraybuffer) {
					arraybuffer >>>= 0;
					var ab = envObject.ensureHandleId(emnapiExternalMemory.viewBuffer(v));
					GET_HEAP_DATA_VIEW().setUint32(arraybuffer, ab, true);
				}
			}
			if (byte_offset) {
				byte_offset >>>= 0;
				var offset = emnapiExternalMemory.viewByteOffset(v);
				GET_HEAP_DATA_VIEW().setUint32(byte_offset, offset, true);
			}
			return envObject.clearLastError();
		}
		/**
		* @__sig ippp
		*/
		function napi_get_date_value(env, value, result) {
			var v;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!value) return envObject.setLastError(1);
				if (!result) return envObject.setLastError(1);
				var handle = emnapiCtx.handleStore.get(value);
				if (!handle.isDate()) return envObject.setLastError(1);
				result >>>= 0;
				v = handle.value.valueOf();
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setFloat64(result, v, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/**
		* @__sig ippp
		*/
		function napi_get_value_bool(env, value, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!value) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			var handle = emnapiCtx.handleStore.get(value);
			if (typeof handle.value !== "boolean") return envObject.setLastError(7);
			result >>>= 0;
			var r = handle.value ? 1 : 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setInt8(result, r, true);
			return envObject.clearLastError();
		}
		/**
		* @__sig ippp
		*/
		function napi_get_value_double(env, value, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!value) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			var handle = emnapiCtx.handleStore.get(value);
			if (typeof handle.value !== "number") return envObject.setLastError(6);
			result >>>= 0;
			var r = handle.value;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setFloat64(result, r, true);
			return envObject.clearLastError();
		}
		/**
		* @__sig ipppp
		*/
		function napi_get_value_bigint_int64(env, value, result, lossless) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!emnapiCtx.feature.supportBigInt) return envObject.setLastError(9);
			if (!value) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			if (!lossless) return envObject.setLastError(1);
			var numberValue = emnapiCtx.handleStore.get(value).value;
			if (typeof numberValue !== "bigint") return envObject.setLastError(6);
			lossless >>>= 0;
			result >>>= 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			if (numberValue >= BigInt(-1) * (BigInt(1) << BigInt(63)) && numberValue < BigInt(1) << BigInt(63)) GET_HEAP_DATA_VIEW().setInt8(lossless, 1, true);
			else {
				GET_HEAP_DATA_VIEW().setInt8(lossless, 0, true);
				numberValue = numberValue & (BigInt(1) << BigInt(64)) - BigInt(1);
				if (numberValue >= BigInt(1) << BigInt(63)) numberValue = numberValue - (BigInt(1) << BigInt(64));
			}
			var low = Number(numberValue & BigInt(0xffffffff));
			var high = Number(numberValue >> BigInt(32));
			GET_HEAP_DATA_VIEW().setInt32(result, low, true);
			GET_HEAP_DATA_VIEW().setInt32(result + 4, high, true);
			return envObject.clearLastError();
		}
		/**
		* @__sig ipppp
		*/
		function napi_get_value_bigint_uint64(env, value, result, lossless) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!emnapiCtx.feature.supportBigInt) return envObject.setLastError(9);
			if (!value) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			if (!lossless) return envObject.setLastError(1);
			var numberValue = emnapiCtx.handleStore.get(value).value;
			if (typeof numberValue !== "bigint") return envObject.setLastError(6);
			lossless >>>= 0;
			result >>>= 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			if (numberValue >= BigInt(0) && numberValue < BigInt(1) << BigInt(64)) GET_HEAP_DATA_VIEW().setInt8(lossless, 1, true);
			else {
				GET_HEAP_DATA_VIEW().setInt8(lossless, 0, true);
				numberValue = numberValue & (BigInt(1) << BigInt(64)) - BigInt(1);
			}
			var low = Number(numberValue & BigInt(0xffffffff));
			var high = Number(numberValue >> BigInt(32));
			GET_HEAP_DATA_VIEW().setUint32(result, low, true);
			GET_HEAP_DATA_VIEW().setUint32(result + 4, high, true);
			return envObject.clearLastError();
		}
		/**
		* @__sig ippppp
		*/
		function napi_get_value_bigint_words(env, value, sign_bit, word_count, words) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!emnapiCtx.feature.supportBigInt) return envObject.setLastError(9);
			if (!value) return envObject.setLastError(1);
			if (!word_count) return envObject.setLastError(1);
			var handle = emnapiCtx.handleStore.get(value);
			if (!handle.isBigInt()) return envObject.setLastError(17);
			var isMinus = handle.value < BigInt(0);
			sign_bit >>>= 0;
			words >>>= 0;
			word_count >>>= 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			var word_count_int = GET_HEAP_DATA_VIEW().getUint32(word_count, true);
			word_count_int >>>= 0;
			var wordCount = 0;
			var bigintValue = isMinus ? handle.value * BigInt(-1) : handle.value;
			while (bigintValue !== BigInt(0)) {
				wordCount++;
				bigintValue = bigintValue >> BigInt(64);
			}
			bigintValue = isMinus ? handle.value * BigInt(-1) : handle.value;
			if (!sign_bit && !words) {
				word_count_int = wordCount;
				GET_HEAP_DATA_VIEW().setUint32(word_count, word_count_int, true);
			} else {
				if (!sign_bit) return envObject.setLastError(1);
				if (!words) return envObject.setLastError(1);
				var wordsArr = [];
				while (bigintValue !== BigInt(0)) {
					var uint64 = bigintValue & (BigInt(1) << BigInt(64)) - BigInt(1);
					wordsArr.push(uint64);
					bigintValue = bigintValue >> BigInt(64);
				}
				var len = Math.min(word_count_int, wordsArr.length);
				for (var i = 0; i < len; i++) {
					var low = Number(wordsArr[i] & BigInt(0xffffffff));
					var high = Number(wordsArr[i] >> BigInt(32));
					GET_HEAP_DATA_VIEW().setUint32(words + i * 8, low, true);
					GET_HEAP_DATA_VIEW().setUint32(words + (i * 8 + 4), high, true);
				}
				GET_HEAP_DATA_VIEW().setInt32(sign_bit, isMinus ? 1 : 0, true);
				GET_HEAP_DATA_VIEW().setUint32(word_count, len, true);
			}
			return envObject.clearLastError();
		}
		/**
		* @__sig ippp
		*/
		function napi_get_value_external(env, value, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!value) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			var handle = emnapiCtx.handleStore.get(value);
			if (!handle.isExternal()) return envObject.setLastError(1);
			result >>>= 0;
			var p = handle.data();
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(result, p, true);
			return envObject.clearLastError();
		}
		/**
		* @__sig ippp
		*/
		function napi_get_value_int32(env, value, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!value) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			var handle = emnapiCtx.handleStore.get(value);
			if (typeof handle.value !== "number") return envObject.setLastError(6);
			result >>>= 0;
			var v = new Int32Array([handle.value])[0];
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setInt32(result, v, true);
			return envObject.clearLastError();
		}
		/**
		* @__sig ippp
		*/
		function napi_get_value_int64(env, value, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!value) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			var handle = emnapiCtx.handleStore.get(value);
			if (typeof handle.value !== "number") return envObject.setLastError(6);
			var numberValue = handle.value;
			result >>>= 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			if (numberValue === Number.POSITIVE_INFINITY || numberValue === Number.NEGATIVE_INFINITY || isNaN(numberValue)) {
				GET_HEAP_DATA_VIEW().setInt32(result, 0, true);
				GET_HEAP_DATA_VIEW().setInt32(result + 4, 0, true);
			} else if (numberValue < -0x8000000000000000) {
				GET_HEAP_DATA_VIEW().setInt32(result, 0, true);
				GET_HEAP_DATA_VIEW().setInt32(result + 4, 0x80000000, true);
			} else if (numberValue >= 0x8000000000000000) {
				GET_HEAP_DATA_VIEW().setUint32(result, 0xffffffff, true);
				GET_HEAP_DATA_VIEW().setUint32(result + 4, 0x7fffffff, true);
			} else $emnapiSetValueI64(result, Math.trunc(numberValue));
			return envObject.clearLastError();
		}
		/**
		* @__sig ippppp
		*/
		function napi_get_value_string_latin1(env, value, buf, buf_size, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!value) return envObject.setLastError(1);
			result >>>= 0;
			buf >>>= 0;
			buf_size >>>= 0;
			buf_size = buf_size >>> 0;
			var handle = emnapiCtx.handleStore.get(value);
			if (typeof handle.value !== "string") return envObject.setLastError(3);
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			if (!buf) {
				if (!result) return envObject.setLastError(1);
				GET_HEAP_DATA_VIEW().setUint32(result, handle.value.length, true);
			} else if (buf_size !== 0) {
				var copied = 0;
				var v = void 0;
				for (var i = 0; i < buf_size - 1; ++i) {
					v = handle.value.charCodeAt(i) & 255;
					GET_HEAP_DATA_VIEW().setUint8(buf + i, v, true);
					copied++;
				}
				GET_HEAP_DATA_VIEW().setUint8(buf + copied, 0, true);
				if (result) GET_HEAP_DATA_VIEW().setUint32(result, copied, true);
			} else if (result) GET_HEAP_DATA_VIEW().setUint32(result, 0, true);
			return envObject.clearLastError();
		}
		/**
		* @__sig ippppp
		*/
		function napi_get_value_string_utf8(env, value, buf, buf_size, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!value) return envObject.setLastError(1);
			result >>>= 0;
			buf >>>= 0;
			buf_size >>>= 0;
			buf_size = buf_size >>> 0;
			var handle = emnapiCtx.handleStore.get(value);
			if (typeof handle.value !== "string") return envObject.setLastError(3);
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			if (!buf) {
				if (!result) return envObject.setLastError(1);
				var strLength = emnapiString.lengthBytesUTF8(handle.value);
				GET_HEAP_DATA_VIEW().setUint32(result, strLength, true);
			} else if (buf_size !== 0) {
				var copied = emnapiString.stringToUTF8(handle.value, buf, buf_size);
				if (result) GET_HEAP_DATA_VIEW().setUint32(result, copied, true);
			} else if (result) GET_HEAP_DATA_VIEW().setUint32(result, 0, true);
			return envObject.clearLastError();
		}
		/**
		* @__sig ippppp
		*/
		function napi_get_value_string_utf16(env, value, buf, buf_size, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!value) return envObject.setLastError(1);
			result >>>= 0;
			buf >>>= 0;
			buf_size >>>= 0;
			buf_size = buf_size >>> 0;
			var handle = emnapiCtx.handleStore.get(value);
			if (typeof handle.value !== "string") return envObject.setLastError(3);
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			if (!buf) {
				if (!result) return envObject.setLastError(1);
				GET_HEAP_DATA_VIEW().setUint32(result, handle.value.length, true);
			} else if (buf_size !== 0) {
				var copied = emnapiString.stringToUTF16(handle.value, buf, buf_size * 2);
				if (result) GET_HEAP_DATA_VIEW().setUint32(result, copied / 2, true);
			} else if (result) GET_HEAP_DATA_VIEW().setUint32(result, 0, true);
			return envObject.clearLastError();
		}
		/**
		* @__sig ippp
		*/
		function napi_get_value_uint32(env, value, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!value) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			var handle = emnapiCtx.handleStore.get(value);
			if (typeof handle.value !== "number") return envObject.setLastError(6);
			result >>>= 0;
			var v = new Uint32Array([handle.value])[0];
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(result, v, true);
			return envObject.clearLastError();
		}
		var convert2cMod = /*#__PURE__*/ Object.freeze({
			__proto__: null,
			napi_get_array_length,
			napi_get_arraybuffer_info,
			napi_get_buffer_info,
			napi_get_dataview_info,
			napi_get_date_value,
			napi_get_prototype,
			napi_get_typedarray_info,
			napi_get_value_bigint_int64,
			napi_get_value_bigint_uint64,
			napi_get_value_bigint_words,
			napi_get_value_bool,
			napi_get_value_double,
			napi_get_value_external,
			napi_get_value_int32,
			napi_get_value_int64,
			napi_get_value_string_latin1,
			napi_get_value_string_utf16,
			napi_get_value_string_utf8,
			napi_get_value_uint32,
			node_api_set_prototype
		});
		/**
		* @__sig ipip
		*/
		function napi_create_int32(env, value, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!result) return envObject.setLastError(1);
			result >>>= 0;
			var v = emnapiCtx.addToCurrentScope(value).id;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(result, v, true);
			return envObject.clearLastError();
		}
		/**
		* @__sig ipip
		*/
		function napi_create_uint32(env, value, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!result) return envObject.setLastError(1);
			result >>>= 0;
			var v = emnapiCtx.addToCurrentScope(value >>> 0).id;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(result, v, true);
			return envObject.clearLastError();
		}
		/**
		* @__sig ipjp
		*/
		function napi_create_int64(env, low, high, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			var value;
			if (!high) return envObject.setLastError(1);
			value = Number(low);
			var v1 = emnapiCtx.addToCurrentScope(value).id;
			high >>>= 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(high, v1, true);
			return envObject.clearLastError();
		}
		/**
		* @__sig ipdp
		*/
		function napi_create_double(env, value, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!result) return envObject.setLastError(1);
			result >>>= 0;
			var v = emnapiCtx.addToCurrentScope(value).id;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(result, v, true);
			return envObject.clearLastError();
		}
		/**
		* @__sig ipppp
		*/
		function napi_create_string_latin1(env, str, length, result) {
			return emnapiString.newString(env, str, length, result, function(str, autoLength, sizeLength) {
				var latin1String = "";
				var len = 0;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				if (autoLength) while (true) {
					var ch = GET_HEAP_DATA_VIEW().getUint8(str, true);
					if (!ch) break;
					latin1String += String.fromCharCode(ch);
					str++;
				}
				else while (len < sizeLength) {
					var ch = GET_HEAP_DATA_VIEW().getUint8(str, true);
					if (!ch) break;
					latin1String += String.fromCharCode(ch);
					len++;
					str++;
				}
				return latin1String;
			});
		}
		/**
		* @__sig ipppp
		*/
		function napi_create_string_utf16(env, str, length, result) {
			return emnapiString.newString(env, str, length, result, function(str) {
				return emnapiString.UTF16ToString(str, length);
			});
		}
		/**
		* @__sig ipppp
		*/
		function napi_create_string_utf8(env, str, length, result) {
			return emnapiString.newString(env, str, length, result, function(str) {
				return emnapiString.UTF8ToString(str, length);
			});
		}
		/**
		* @__sig ippppppp
		*/
		function node_api_create_external_string_latin1(env, str, length, finalize_callback, finalize_hint, result, copied) {
			return emnapiString.newExternalString(env, str, length, finalize_callback, finalize_hint, result, copied, napi_create_string_latin1, void 0);
		}
		/**
		* @__sig ippppppp
		*/
		function node_api_create_external_string_utf16(env, str, length, finalize_callback, finalize_hint, result, copied) {
			return emnapiString.newExternalString(env, str, length, finalize_callback, finalize_hint, result, copied, napi_create_string_utf16, void 0);
		}
		/**
		* @__sig ipppp
		*/
		function node_api_create_property_key_latin1(env, str, length, result) {
			return napi_create_string_latin1(env, str, length, result);
		}
		/**
		* @__sig ipppp
		*/
		function node_api_create_property_key_utf8(env, str, length, result) {
			return napi_create_string_utf8(env, str, length, result);
		}
		/**
		* @__sig ipppp
		*/
		function node_api_create_property_key_utf16(env, str, length, result) {
			return napi_create_string_utf16(env, str, length, result);
		}
		/**
		* @__sig ipjp
		*/
		function napi_create_bigint_int64(env, low, high, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!emnapiCtx.feature.supportBigInt) return envObject.setLastError(9);
			var value;
			if (!high) return envObject.setLastError(1);
			value = low;
			var v1 = emnapiCtx.addToCurrentScope(value).id;
			high >>>= 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(high, v1, true);
			return envObject.clearLastError();
		}
		/**
		* @__sig ipjp
		*/
		function napi_create_bigint_uint64(env, low, high, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!emnapiCtx.feature.supportBigInt) return envObject.setLastError(9);
			var value;
			if (!high) return envObject.setLastError(1);
			value = low & (BigInt(1) << BigInt(64)) - BigInt(1);
			var v1 = emnapiCtx.addToCurrentScope(value).id;
			high >>>= 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(high, v1, true);
			return envObject.clearLastError();
		}
		/**
		* @__sig ipippp
		*/
		function napi_create_bigint_words(env, sign_bit, word_count, words, result) {
			var v, i;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!emnapiCtx.feature.supportBigInt) return envObject.setLastError(9);
				if (!result) return envObject.setLastError(1);
				words >>>= 0;
				word_count >>>= 0;
				word_count = word_count >>> 0;
				if (word_count > 0x7fffffff) return envObject.setLastError(1);
				if (word_count > 16384) throw new RangeError("Maximum BigInt size exceeded");
				var value = BigInt(0);
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				for (i = 0; i < word_count; i++) {
					var low = GET_HEAP_DATA_VIEW().getUint32(words + i * 8, true);
					var high = GET_HEAP_DATA_VIEW().getUint32(words + (i * 8 + 4), true);
					var wordi = BigInt(low) | BigInt(high) << BigInt(32);
					value += wordi << BigInt(64 * i);
				}
				value *= BigInt(sign_bit) % BigInt(2) === BigInt(0) ? BigInt(1) : BigInt(-1);
				result >>>= 0;
				v = emnapiCtx.addToCurrentScope(value).id;
				GET_HEAP_DATA_VIEW().setUint32(result, v, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		var convert2napiMod = /*#__PURE__*/ Object.freeze({
			__proto__: null,
			napi_create_bigint_int64,
			napi_create_bigint_uint64,
			napi_create_bigint_words,
			napi_create_double,
			napi_create_int32,
			napi_create_int64,
			napi_create_string_latin1,
			napi_create_string_utf16,
			napi_create_string_utf8,
			napi_create_uint32,
			node_api_create_external_string_latin1,
			node_api_create_external_string_utf16,
			node_api_create_property_key_latin1,
			node_api_create_property_key_utf16,
			node_api_create_property_key_utf8
		});
		function emnapiCreateFunction(envObject, utf8name, length, cb, data) {
			utf8name >>>= 0;
			var functionName = !utf8name || !length ? "" : emnapiString.UTF8ToString(utf8name, length);
			var f;
			var napiCallback = wasmTable.get(cb);
			var callback = function(envObject) {
				return napiCallback(envObject.id, envObject.ctx.scopeStore.currentScope.id);
			};
			var makeFunction = function(envObject, callback) {
				return function() {
					var scope = envObject.ctx.openScope(envObject);
					var callbackInfo = scope.callbackInfo;
					callbackInfo.data = data;
					callbackInfo.args = arguments;
					callbackInfo.thiz = this;
					callbackInfo.fn = f;
					try {
						var napiValue = envObject.callIntoModule(callback);
						return !napiValue ? void 0 : envObject.ctx.handleStore.get(napiValue).value;
					} finally {
						callbackInfo.data = 0;
						callbackInfo.args = void 0;
						callbackInfo.thiz = void 0;
						callbackInfo.fn = void 0;
						envObject.ctx.closeScope(envObject, scope);
					}
				};
			};
			if (functionName === "") {
				f = makeFunction(envObject, callback);
				return {
					status: 0,
					f
				};
			}
			if (!/^[_$a-zA-Z][_$a-zA-Z0-9]*$/.test(functionName)) return {
				status: 1,
				f: void 0
			};
			if (emnapiCtx.feature.supportNewFunction) {
				var _ = makeFunction(envObject, callback);
				try {
					f = new Function("_", "return function " + functionName + "(){\"use strict\";return _.apply(this,arguments);};")(_);
				} catch (_err) {
					f = makeFunction(envObject, callback);
					if (emnapiCtx.feature.canSetFunctionName) Object.defineProperty(f, "name", { value: functionName });
				}
			} else {
				f = makeFunction(envObject, callback);
				if (emnapiCtx.feature.canSetFunctionName) Object.defineProperty(f, "name", { value: functionName });
			}
			return {
				status: 0,
				f
			};
		}
		function emnapiDefineProperty(envObject, obj, propertyName, method, getter, setter, value, attributes, data) {
			if (getter || setter) {
				var localGetter = void 0;
				var localSetter = void 0;
				if (getter) localGetter = emnapiCreateFunction(envObject, 0, 0, getter, data).f;
				if (setter) localSetter = emnapiCreateFunction(envObject, 0, 0, setter, data).f;
				var desc = {
					configurable: (attributes & 4) !== 0,
					enumerable: (attributes & 2) !== 0,
					get: localGetter,
					set: localSetter
				};
				Object.defineProperty(obj, propertyName, desc);
			} else if (method) {
				var localMethod = emnapiCreateFunction(envObject, 0, 0, method, data).f;
				var desc = {
					configurable: (attributes & 4) !== 0,
					enumerable: (attributes & 2) !== 0,
					writable: (attributes & 1) !== 0,
					value: localMethod
				};
				Object.defineProperty(obj, propertyName, desc);
			} else {
				var desc = {
					configurable: (attributes & 4) !== 0,
					enumerable: (attributes & 2) !== 0,
					writable: (attributes & 1) !== 0,
					value: emnapiCtx.handleStore.get(value).value
				};
				Object.defineProperty(obj, propertyName, desc);
			}
		}
		function emnapiGetHandle(js_object) {
			var handle = emnapiCtx.handleStore.get(js_object);
			if (!(handle.isObject() || handle.isFunction())) return { status: 1 };
			if (typeof emnapiExternalMemory !== "undefined" && ArrayBuffer.isView(handle.value)) {
				if (emnapiExternalMemory.wasmMemoryViewTable.has(handle.value)) handle = emnapiCtx.addToCurrentScope(emnapiExternalMemory.wasmMemoryViewTable.get(handle.value));
			}
			return {
				status: 0,
				handle
			};
		}
		function emnapiWrap(env, js_object, native_object, finalize_cb, finalize_hint, result) {
			var referenceId;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!emnapiCtx.feature.supportFinalizer) {
					if (finalize_cb) throw emnapiCtx.createNotSupportWeakRefError("napi_wrap", "Parameter \"finalize_cb\" must be 0(NULL)");
					if (result) throw emnapiCtx.createNotSupportWeakRefError("napi_wrap", "Parameter \"result\" must be 0(NULL)");
				}
				if (!js_object) return envObject.setLastError(1);
				var handleResult = emnapiGetHandle(js_object);
				if (handleResult.status !== 0) return envObject.setLastError(handleResult.status);
				var handle = handleResult.handle;
				if (envObject.getObjectBinding(handle.value).wrapped !== 0) return envObject.setLastError(1);
				var reference = void 0;
				if (result) {
					if (!finalize_cb) return envObject.setLastError(1);
					reference = emnapiCtx.createReferenceWithFinalizer(envObject, handle.id, 0, 1, finalize_cb, native_object, finalize_hint);
					result >>>= 0;
					referenceId = reference.id;
					var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
						return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
					};
					GET_HEAP_DATA_VIEW().setUint32(result, referenceId, true);
				} else if (finalize_cb) reference = emnapiCtx.createReferenceWithFinalizer(envObject, handle.id, 0, 0, finalize_cb, native_object, finalize_hint);
				else reference = emnapiCtx.createReferenceWithData(envObject, handle.id, 0, 0, native_object);
				envObject.getObjectBinding(handle.value).wrapped = reference.id;
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		function emnapiUnwrap(env, js_object, result, action) {
			var data;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!js_object) return envObject.setLastError(1);
				if (action === 0) {
					if (!result) return envObject.setLastError(1);
				}
				var value = emnapiCtx.handleStore.get(js_object);
				if (!(value.isObject() || value.isFunction())) return envObject.setLastError(1);
				var binding = envObject.getObjectBinding(value.value);
				var referenceId = binding.wrapped;
				var ref = emnapiCtx.refStore.get(referenceId);
				if (!ref) return envObject.setLastError(1);
				if (result) {
					result >>>= 0;
					data = ref.data();
					var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
						return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
					};
					GET_HEAP_DATA_VIEW().setUint32(result, data, true);
				}
				if (action === 1) {
					binding.wrapped = 0;
					if (ref.ownership() === 1) ref.resetFinalizer();
					else ref.dispose();
				}
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/**
		* @__sig ipppppppp
		*/
		function napi_define_class(env, utf8name, length, constructor, callback_data, property_count, properties, result) {
			var propPtr, valueHandleId, attributes;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!result) return envObject.setLastError(1);
				if (!constructor) return envObject.setLastError(1);
				length >>>= 0;
				properties >>>= 0;
				property_count >>>= 0;
				property_count = property_count >>> 0;
				if (property_count > 0) {
					if (!properties) return envObject.setLastError(1);
				}
				if (!(length >= -1 && length <= 0x7fffffff || length === 0xffffffff) || !utf8name) return envObject.setLastError(1);
				var fresult = emnapiCreateFunction(envObject, utf8name, length, constructor, callback_data);
				if (fresult.status !== 0) return envObject.setLastError(fresult.status);
				var F = fresult.f;
				var propertyName = void 0;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				for (var i = 0; i < property_count; i++) {
					propPtr = properties + i * 32;
					var utf8Name = GET_HEAP_DATA_VIEW().getUint32(propPtr, true);
					var name_1 = GET_HEAP_DATA_VIEW().getUint32(propPtr + 4, true);
					var method = GET_HEAP_DATA_VIEW().getUint32(propPtr + 8, true);
					var getter = GET_HEAP_DATA_VIEW().getUint32(propPtr + 12, true);
					var setter = GET_HEAP_DATA_VIEW().getUint32(propPtr + 16, true);
					var value = GET_HEAP_DATA_VIEW().getUint32(propPtr + 20, true);
					attributes = GET_HEAP_DATA_VIEW().getInt32(propPtr + 24, true);
					attributes >>>= 0;
					var data = GET_HEAP_DATA_VIEW().getUint32(propPtr + 28, true);
					if (utf8Name) propertyName = emnapiString.UTF8ToString(utf8Name, -1);
					else {
						if (!name_1) return envObject.setLastError(4);
						propertyName = emnapiCtx.handleStore.get(name_1).value;
						if (typeof propertyName !== "string" && typeof propertyName !== "symbol") return envObject.setLastError(4);
					}
					if ((attributes & 1024) !== 0) {
						emnapiDefineProperty(envObject, F, propertyName, method, getter, setter, value, attributes, data);
						continue;
					}
					emnapiDefineProperty(envObject, F.prototype, propertyName, method, getter, setter, value, attributes, data);
				}
				valueHandleId = emnapiCtx.addToCurrentScope(F).id;
				result >>>= 0;
				GET_HEAP_DATA_VIEW().setUint32(result, valueHandleId, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/**
		* @__sig ipppppp
		*/
		function napi_wrap(env, js_object, native_object, finalize_cb, finalize_hint, result) {
			return emnapiWrap(env, js_object, native_object, finalize_cb, finalize_hint, result);
		}
		/**
		* @__sig ippp
		*/
		function napi_unwrap(env, js_object, result) {
			return emnapiUnwrap(env, js_object, result, 0);
		}
		/**
		* @__sig ippp
		*/
		function napi_remove_wrap(env, js_object, result) {
			return emnapiUnwrap(env, js_object, result, 1);
		}
		/**
		* @__sig ippp
		*/
		function napi_type_tag_object(env, object, type_tag) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!object) return envObject.setLastError(envObject.tryCatch.hasCaught() ? 10 : 1);
				var value = emnapiCtx.handleStore.get(object);
				if (!(value.isObject() || value.isFunction())) return envObject.setLastError(envObject.tryCatch.hasCaught() ? 10 : 2);
				type_tag >>>= 0;
				if (!type_tag) return envObject.setLastError(envObject.tryCatch.hasCaught() ? 10 : 1);
				var binding = envObject.getObjectBinding(value.value);
				if (binding.tag !== null) return envObject.setLastError(envObject.tryCatch.hasCaught() ? 10 : 1);
				var tag = /* @__PURE__ */ new Uint8Array(16);
				tag.set(new Uint8Array(wasmMemory.buffer, type_tag, 16));
				binding.tag = new Uint32Array(tag.buffer);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/**
		* @__sig ipppp
		*/
		function napi_check_object_type_tag(env, object, type_tag, result) {
			var ret = true;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!object) return envObject.setLastError(envObject.tryCatch.hasCaught() ? 10 : 1);
				var value = emnapiCtx.handleStore.get(object);
				if (!(value.isObject() || value.isFunction())) return envObject.setLastError(envObject.tryCatch.hasCaught() ? 10 : 2);
				if (!type_tag) return envObject.setLastError(envObject.tryCatch.hasCaught() ? 10 : 1);
				if (!result) return envObject.setLastError(envObject.tryCatch.hasCaught() ? 10 : 1);
				var binding = envObject.getObjectBinding(value.value);
				if (binding.tag !== null) {
					type_tag >>>= 0;
					var tag = binding.tag;
					var typeTag = new Uint32Array(wasmMemory.buffer, type_tag, 4);
					ret = tag[0] === typeTag[0] && tag[1] === typeTag[1] && tag[2] === typeTag[2] && tag[3] === typeTag[3];
				} else ret = false;
				result >>>= 0;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setInt8(result, ret ? 1 : 0, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/**
		* @__sig ipppppp
		*/
		function napi_add_finalizer(env, js_object, finalize_data, finalize_cb, finalize_hint, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!emnapiCtx.feature.supportFinalizer) return envObject.setLastError(9);
			if (!js_object) return envObject.setLastError(1);
			if (!finalize_cb) return envObject.setLastError(1);
			var handleResult = emnapiGetHandle(js_object);
			if (handleResult.status !== 0) return envObject.setLastError(handleResult.status);
			var handle = handleResult.handle;
			var ownership = !result ? 0 : 1;
			finalize_data >>>= 0;
			finalize_cb >>>= 0;
			finalize_hint >>>= 0;
			var reference = emnapiCtx.createReferenceWithFinalizer(envObject, handle.id, 0, ownership, finalize_cb, finalize_data, finalize_hint);
			if (result) {
				result >>>= 0;
				var referenceId = reference.id;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(result, referenceId, true);
			}
			return envObject.clearLastError();
		}
		/**
		* @__sig ipppp
		*/
		function node_api_post_finalizer(env, finalize_cb, finalize_data, finalize_hint) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.enqueueFinalizer(emnapiCtx.createTrackedFinalizer(envObject, finalize_cb, finalize_data, finalize_hint));
			return envObject.clearLastError();
		}
		var wrapMod = /*#__PURE__*/ Object.freeze({
			__proto__: null,
			napi_add_finalizer,
			napi_check_object_type_tag,
			napi_define_class,
			napi_remove_wrap,
			napi_type_tag_object,
			napi_unwrap,
			napi_wrap,
			node_api_post_finalizer
		});
		/**
		* @__sig ipippppp
		*/
		function emnapi_create_memory_view(env, typedarray_type, external_data, byte_length, finalize_cb, finalize_hint, result) {
			var value;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!result) return envObject.setLastError(1);
				byte_length >>>= 0;
				external_data >>>= 0;
				result >>>= 0;
				byte_length = byte_length >>> 0;
				if (!external_data) byte_length = 0;
				if (byte_length > 0x7fffffff) throw new RangeError("Cannot create a memory view larger than 2147483647 bytes");
				if (external_data + byte_length > wasmMemory.buffer.byteLength) throw new RangeError("Memory out of range");
				if (!emnapiCtx.feature.supportFinalizer && finalize_cb) throw emnapiCtx.createNotSupportWeakRefError("emnapi_create_memory_view", "Parameter \"finalize_cb\" must be 0(NULL)");
				var viewDescriptor = void 0;
				switch (typedarray_type) {
					case 0:
						viewDescriptor = {
							Ctor: Int8Array,
							address: external_data,
							length: byte_length,
							ownership: 1,
							runtimeAllocated: 0
						};
						break;
					case 1:
						viewDescriptor = {
							Ctor: Uint8Array,
							address: external_data,
							length: byte_length,
							ownership: 1,
							runtimeAllocated: 0
						};
						break;
					case 2:
						viewDescriptor = {
							Ctor: Uint8ClampedArray,
							address: external_data,
							length: byte_length,
							ownership: 1,
							runtimeAllocated: 0
						};
						break;
					case 3:
						viewDescriptor = {
							Ctor: Int16Array,
							address: external_data,
							length: byte_length >> 1,
							ownership: 1,
							runtimeAllocated: 0
						};
						break;
					case 4:
						viewDescriptor = {
							Ctor: Uint16Array,
							address: external_data,
							length: byte_length >> 1,
							ownership: 1,
							runtimeAllocated: 0
						};
						break;
					case 5:
						viewDescriptor = {
							Ctor: Int32Array,
							address: external_data,
							length: byte_length >> 2,
							ownership: 1,
							runtimeAllocated: 0
						};
						break;
					case 6:
						viewDescriptor = {
							Ctor: Uint32Array,
							address: external_data,
							length: byte_length >> 2,
							ownership: 1,
							runtimeAllocated: 0
						};
						break;
					case 7:
						viewDescriptor = {
							Ctor: Float32Array,
							address: external_data,
							length: byte_length >> 2,
							ownership: 1,
							runtimeAllocated: 0
						};
						break;
					case 8:
						viewDescriptor = {
							Ctor: Float64Array,
							address: external_data,
							length: byte_length >> 3,
							ownership: 1,
							runtimeAllocated: 0
						};
						break;
					case 9:
						viewDescriptor = {
							Ctor: BigInt64Array,
							address: external_data,
							length: byte_length >> 3,
							ownership: 1,
							runtimeAllocated: 0
						};
						break;
					case 10:
						viewDescriptor = {
							Ctor: BigUint64Array,
							address: external_data,
							length: byte_length >> 3,
							ownership: 1,
							runtimeAllocated: 0
						};
						break;
					case -1:
						viewDescriptor = {
							Ctor: DataView,
							address: external_data,
							length: byte_length,
							ownership: 1,
							runtimeAllocated: 0
						};
						break;
					case 11:
						if (typeof Float16Array !== "function") return envObject.setLastError(1);
						viewDescriptor = {
							Ctor: Float16Array,
							address: external_data,
							length: byte_length >> 1,
							ownership: 1,
							runtimeAllocated: 0
						};
						break;
					case -2:
						if (!emnapiCtx.feature.Buffer) throw emnapiCtx.createNotSupportBufferError("emnapi_create_memory_view", "");
						viewDescriptor = {
							Ctor: emnapiCtx.feature.Buffer,
							address: external_data,
							length: byte_length,
							ownership: 1,
							runtimeAllocated: 0
						};
						break;
					default: return envObject.setLastError(1);
				}
				var Ctor = viewDescriptor.Ctor;
				var typedArray = typedarray_type === -2 ? emnapiExternalMemory.getBufferFrom()(wasmMemory.buffer, viewDescriptor.address, viewDescriptor.length) : new Ctor(wasmMemory.buffer, viewDescriptor.address, viewDescriptor.length);
				var handle = emnapiCtx.addToCurrentScope(typedArray);
				emnapiExternalMemory.wasmMemoryViewTable.set(typedArray, viewDescriptor);
				if (finalize_cb) {
					var status_1 = napi_add_finalizer(env, handle.id, external_data, finalize_cb, finalize_hint, 0);
					if (status_1 === 10) {
						var err = envObject.tryCatch.extractException();
						envObject.clearLastError();
						throw err;
					} else if (status_1 !== 0) return envObject.setLastError(status_1);
				}
				value = handle.id;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(result, value, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/**
		* @__sig i
		*/
		function emnapi_is_support_weakref() {
			return emnapiCtx.feature.supportFinalizer ? 1 : 0;
		}
		/**
		* @__sig i
		*/
		function emnapi_is_support_bigint() {
			return emnapiCtx.feature.supportBigInt ? 1 : 0;
		}
		/**
		* @__sig i
		*/
		function emnapi_is_node_binding_available() {
			return emnapiNodeBinding ? 1 : 0;
		}
		/**
		* Synchronize data between the wasm memory and an ArrayBuffer / view.
		*
		* Note: when a non-shared wasm memory has grown, a view over the detached
		* old buffer is re-created over the current buffer through the intrinsic
		* base-class constructor, so for view inputs the returned view may be a
		* base-class instance (e.g. `Uint8Array` or `DataView`) instead of the
		* subclass that was passed in.
		*/
		function $emnapiSyncMemory(js_to_wasm, arrayBufferOrView, offset, len) {
			offset = offset !== null && offset !== void 0 ? offset : 0;
			offset = offset >>> 0;
			var view;
			if (arrayBufferOrView instanceof ArrayBuffer || emnapiExternalMemory.isSharedArrayBuffer(arrayBufferOrView)) {
				var pointer = emnapiExternalMemory.getArrayBufferPointer(arrayBufferOrView, false).address;
				if (!pointer) throw new Error("Unknown ArrayBuffer address");
				if (typeof len !== "number" || len === -1 || len === 0xffffffff) len = arrayBufferOrView.byteLength - offset;
				len = len >>> 0;
				if (len === 0) return arrayBufferOrView;
				view = new Uint8Array(arrayBufferOrView, offset, len);
				var wasmMemoryU8 = new Uint8Array(wasmMemory.buffer);
				if (!js_to_wasm) view.set(wasmMemoryU8.subarray(pointer, pointer + len));
				else wasmMemoryU8.set(view, pointer);
				return arrayBufferOrView;
			}
			if (ArrayBuffer.isView(arrayBufferOrView)) {
				var viewPointerInfo = emnapiExternalMemory.getViewPointer(arrayBufferOrView, false);
				var latestView = viewPointerInfo.view;
				var pointer = viewPointerInfo.address;
				if (!pointer) throw new Error("Unknown ArrayBuffer address");
				if (typeof len !== "number" || len === -1 || len === 0xffffffff) len = latestView.byteLength - offset;
				len = len >>> 0;
				if (len === 0) return latestView;
				view = new Uint8Array(latestView.buffer, latestView.byteOffset + offset, len);
				var wasmMemoryU8 = new Uint8Array(wasmMemory.buffer);
				if (!js_to_wasm) view.set(wasmMemoryU8.subarray(pointer, pointer + len));
				else wasmMemoryU8.set(view, pointer);
				return latestView;
			}
			throw new TypeError("emnapiSyncMemory expect ArrayBuffer or ArrayBufferView as first parameter");
		}
		/**
		* @__sig ipippp
		*/
		function emnapi_sync_memory(env, js_to_wasm, arraybuffer_or_view, offset, len) {
			var v;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!arraybuffer_or_view) return envObject.setLastError(1);
				arraybuffer_or_view >>>= 0;
				offset >>>= 0;
				len >>>= 0;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				var handleId = GET_HEAP_DATA_VIEW().getUint32(arraybuffer_or_view, true);
				var handle = envObject.ctx.handleStore.get(handleId);
				if (!handle.isArrayBuffer() && !handle.isTypedArray() && !handle.isDataView() && !emnapiExternalMemory.isSharedArrayBuffer(handle.value)) return envObject.setLastError(1);
				var ret = $emnapiSyncMemory(Boolean(js_to_wasm), handle.value, offset, len);
				if (handle.value !== ret) {
					arraybuffer_or_view >>>= 0;
					v = envObject.ensureHandleId(ret);
					GET_HEAP_DATA_VIEW().setUint32(arraybuffer_or_view, v, true);
				}
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		function $emnapiGetMemoryAddress(arrayBufferOrView) {
			var isArrayBuffer = arrayBufferOrView instanceof ArrayBuffer;
			var isDataView = arrayBufferOrView instanceof DataView;
			if (!isArrayBuffer && !(ArrayBuffer.isView(arrayBufferOrView) && !isDataView) && !isDataView && !emnapiExternalMemory.isSharedArrayBuffer(arrayBufferOrView)) throw new TypeError("emnapiGetMemoryAddress expect ArrayBuffer or ArrayBufferView as first parameter");
			var info;
			if (isArrayBuffer) info = emnapiExternalMemory.getArrayBufferPointer(arrayBufferOrView, false);
			else info = emnapiExternalMemory.getViewPointer(arrayBufferOrView, false);
			return {
				address: info.address,
				ownership: info.ownership,
				runtimeAllocated: info.runtimeAllocated
			};
		}
		/**
		* @__sig ipppp
		*/
		function emnapi_get_memory_address(env, arraybuffer_or_view, address, ownership, runtime_allocated) {
			var p, runtimeAllocated, ownershipOut;
			var info;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!arraybuffer_or_view) return envObject.setLastError(1);
				if (!address && !ownership && !runtime_allocated) return envObject.setLastError(1);
				info = $emnapiGetMemoryAddress(envObject.ctx.handleStore.get(arraybuffer_or_view).value);
				p = info.address;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				if (address) {
					address >>>= 0;
					GET_HEAP_DATA_VIEW().setUint32(address, p, true);
				}
				if (ownership) {
					ownership >>>= 0;
					ownershipOut = info.ownership;
					GET_HEAP_DATA_VIEW().setInt32(ownership, ownershipOut, true);
				}
				if (runtime_allocated) {
					runtime_allocated >>>= 0;
					runtimeAllocated = info.runtimeAllocated;
					GET_HEAP_DATA_VIEW().setInt8(runtime_allocated, runtimeAllocated, true);
				}
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/**
		* @__sig ipp
		*/
		function emnapi_get_runtime_version(env, version) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			if (!version) return envObject.setLastError(1);
			var runtimeVersion;
			try {
				runtimeVersion = emnapiCtx.getRuntimeVersions().version;
			} catch (_) {
				return envObject.setLastError(9);
			}
			var versions = runtimeVersion.split(".").map(function(n) {
				return Number(n);
			});
			version >>>= 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(version, versions[0], true);
			GET_HEAP_DATA_VIEW().setUint32(version + 4, versions[1], true);
			GET_HEAP_DATA_VIEW().setUint32(version + 8, versions[2], true);
			return envObject.clearLastError();
		}
		/**
		* Get the handle for an external SharedArrayBuffer.
		* Must be called on the emnapi main thread.
		* @__sig ippp
		*/
		function emnapi_get_external_sharedarraybuffer_handle(env, sharedarraybuffer, handle) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!sharedarraybuffer) return envObject.setLastError(1);
				if (!handle) return envObject.setLastError(1);
				handle >>>= 0;
				var jsValue = emnapiCtx.handleStore.get(sharedarraybuffer).value;
				if (!emnapiExternalMemory.isSharedArrayBuffer(jsValue)) return envObject.setLastError(1);
				var metaPtr = emnapiExternalSAB.handleTable.get(jsValue);
				if (metaPtr === void 0) return envObject.setLastError(1);
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(handle, metaPtr, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/**
		* Acquire a reference to an external SharedArrayBuffer.
		* Can be called on any thread.
		* @__sig vp
		*/
		function emnapi_acquire_external_sharedarraybuffer(handle) {
			handle >>>= 0;
			Atomics.add(new Int32Array(wasmMemory.buffer, handle, 1), 0, 1);
		}
		/**
		* Release a reference to an external SharedArrayBuffer.
		* Can be called on any thread.
		* @__sig vp
		*/
		function emnapi_release_external_sharedarraybuffer(handle) {
			handle >>>= 0;
			emnapiExternalSAB.release(handle);
		}
		/**
		* Acquire a reference to an external SharedArrayBuffer on the current thread.
		* Can be called on any thread. Increments refcount and registers in
		* the current thread's FinalizationRegistry.
		* Exposed as Module.emnapiAcquireExternalSharedArrayBuffer (emscripten) or
		* napiModule.emnapi.acquireExternalSharedArrayBuffer (core).
		*/
		function $emnapiAcquireExternalSharedArrayBuffer(handle, sab) {
			if (sab != null && !emnapiExternalMemory.isSharedArrayBuffer(sab)) throw new TypeError("Expected a SharedArrayBuffer");
			if (!emnapiExternalSAB.registry) throw new Error("FinalizationRegistry is not supported in this environment");
			handle >>>= 0;
			var meta = emnapiExternalSAB.readMeta(handle);
			var external_data = meta.external_data;
			external_data >>>= 0;
			if (sab == null) {
				sab = new SharedArrayBuffer(meta.byte_length);
				new Uint8Array(sab).set(new Uint8Array(wasmMemory.buffer, external_data, meta.byte_length));
			} else if (emnapiExternalSAB.handleTable.has(sab)) return sab;
			Atomics.add(new Int32Array(wasmMemory.buffer, handle, 1), 0, 1);
			if (!emnapiExternalMemory.table.has(sab)) {
				if (external_data) emnapiExternalMemory.table.set(sab, {
					address: external_data,
					ownership: 1,
					runtimeAllocated: 0
				});
			}
			emnapiExternalSAB.handleTable.set(sab, handle);
			emnapiExternalSAB.registry.register(sab, handle);
			return sab;
		}
		var emnapiMod = /*#__PURE__*/ Object.freeze({
			__proto__: null,
			$emnapiAcquireExternalSharedArrayBuffer,
			$emnapiGetMemoryAddress,
			$emnapiSyncMemory,
			emnapi_acquire_external_sharedarraybuffer,
			emnapi_create_memory_view,
			emnapi_get_external_sharedarraybuffer_handle,
			emnapi_get_memory_address,
			emnapi_get_runtime_version,
			emnapi_is_node_binding_available,
			emnapi_is_support_bigint,
			emnapi_is_support_weakref,
			emnapi_release_external_sharedarraybuffer,
			emnapi_sync_memory
		});
		/**
		* @__sig ipp
		*/
		function napi_create_array(env, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!result) return envObject.setLastError(1);
			result >>>= 0;
			var value = emnapiCtx.addToCurrentScope([]).id;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(result, value, true);
			return envObject.clearLastError();
		}
		/**
		* @__sig ippp
		*/
		function napi_create_array_with_length(env, length, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!result) return envObject.setLastError(1);
			length >>>= 0;
			result >>>= 0;
			length = length >>> 0;
			var value = emnapiCtx.addToCurrentScope(new Array(length)).id;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(result, value, true);
			return envObject.clearLastError();
		}
		function emnapiCreateArrayBuffer(byte_length, data, shared) {
			byte_length >>>= 0;
			byte_length = byte_length >>> 0;
			var arrayBuffer = shared ? new SharedArrayBuffer(byte_length) : new ArrayBuffer(byte_length);
			if (data) {
				data >>>= 0;
				var p = emnapiExternalMemory.getArrayBufferPointer(arrayBuffer, true).address;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(data, p, true);
			}
			return arrayBuffer;
		}
		/**
		* @__sig ipppp
		*/
		function napi_create_arraybuffer(env, byte_length, data, result) {
			var value;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!result) return envObject.setLastError(1);
				result >>>= 0;
				var arrayBuffer = emnapiCreateArrayBuffer(byte_length, data, false);
				value = emnapiCtx.addToCurrentScope(arrayBuffer).id;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(result, value, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/**
		* @__sig ipppp
		*/
		function node_api_create_sharedarraybuffer(env, byte_length, data, result) {
			var value;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!result) return envObject.setLastError(1);
				result >>>= 0;
				var arrayBuffer = emnapiCreateArrayBuffer(byte_length, data, true);
				value = emnapiCtx.addToCurrentScope(arrayBuffer).id;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(result, value, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/**
		* @__sig ipdp
		*/
		function napi_create_date(env, time, result) {
			var value;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!result) return envObject.setLastError(1);
				result >>>= 0;
				value = emnapiCtx.addToCurrentScope(new Date(time)).id;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(result, value, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/**
		* @__sig ippppp
		*/
		function napi_create_external(env, data, finalize_cb, finalize_hint, result) {
			var value;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!result) return envObject.setLastError(1);
				if (!emnapiCtx.feature.supportFinalizer && finalize_cb) throw emnapiCtx.createNotSupportWeakRefError("napi_create_external", "Parameter \"finalize_cb\" must be 0(NULL)");
				var externalHandle = emnapiCtx.getCurrentScope().addExternal(data);
				if (finalize_cb) emnapiCtx.createReferenceWithFinalizer(envObject, externalHandle.id, 0, 0, finalize_cb, data, finalize_hint);
				result >>>= 0;
				value = externalHandle.id;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(result, value, true);
				return envObject.clearLastError();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/**
		* @__sig ipppppp
		*/
		function napi_create_external_arraybuffer(env, external_data, byte_length, finalize_cb, finalize_hint, result) {
			var value;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!result) return envObject.setLastError(1);
				byte_length >>>= 0;
				external_data >>>= 0;
				result >>>= 0;
				byte_length = byte_length >>> 0;
				if (!external_data) byte_length = 0;
				if (external_data + byte_length > wasmMemory.buffer.byteLength) throw new RangeError("Memory out of range");
				if (!emnapiCtx.feature.supportFinalizer && finalize_cb) throw emnapiCtx.createNotSupportWeakRefError("napi_create_external_arraybuffer", "Parameter \"finalize_cb\" must be 0(NULL)");
				var arrayBuffer = new ArrayBuffer(byte_length);
				if (byte_length === 0) try {
					new emnapiCtx.feature.MessageChannel().port1.postMessage(arrayBuffer, [arrayBuffer]);
				} catch (_) {}
				else {
					new Uint8Array(arrayBuffer).set(new Uint8Array(wasmMemory.buffer).subarray(external_data, external_data + byte_length));
					emnapiExternalMemory.table.set(arrayBuffer, {
						address: external_data,
						ownership: 1,
						runtimeAllocated: 0
					});
				}
				var handle = emnapiCtx.addToCurrentScope(arrayBuffer);
				if (finalize_cb) {
					var status_1 = napi_add_finalizer(env, handle.id, external_data, finalize_cb, finalize_hint, 0);
					if (status_1 === 10) {
						var err = envObject.tryCatch.extractException();
						envObject.clearLastError();
						throw err;
					} else if (status_1 !== 0) return envObject.setLastError(status_1);
				}
				value = handle.id;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(result, value, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/**
		* @__sig ipppppp
		*/
		function node_api_create_external_sharedarraybuffer(env, external_data, byte_length, finalize_cb, finalize_hint, result) {
			var value;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!result) return envObject.setLastError(1);
				byte_length >>>= 0;
				external_data >>>= 0;
				result >>>= 0;
				byte_length = byte_length >>> 0;
				if (!external_data) byte_length = 0;
				if (external_data + byte_length > wasmMemory.buffer.byteLength) throw new RangeError("Memory out of range");
				if (!emnapiExternalSAB.registry && finalize_cb) throw emnapiCtx.createNotSupportWeakRefError("node_api_create_external_sharedarraybuffer", "Parameter \"finalize_cb\" must be 0(NULL)");
				var sharedArrayBuffer = new SharedArrayBuffer(byte_length);
				if (byte_length !== 0) {
					new Uint8Array(sharedArrayBuffer).set(new Uint8Array(wasmMemory.buffer).subarray(external_data, external_data + byte_length));
					emnapiExternalMemory.table.set(sharedArrayBuffer, {
						address: external_data,
						ownership: 1,
						runtimeAllocated: 0
					});
				}
				value = emnapiCtx.addToCurrentScope(sharedArrayBuffer).id;
				if (finalize_cb) {
					finalize_cb >>>= 0;
					finalize_hint >>>= 0;
					var metaPtr = emnapiExternalSAB.allocMeta(external_data, byte_length, finalize_cb, external_data, finalize_hint);
					emnapiExternalSAB.handleTable.set(sharedArrayBuffer, metaPtr);
					emnapiExternalSAB.registry.register(sharedArrayBuffer, metaPtr);
				}
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(result, value, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/**
		* @__sig ipp
		*/
		function napi_create_object(env, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!result) return envObject.setLastError(1);
			result >>>= 0;
			var value = emnapiCtx.addToCurrentScope({}).id;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(result, value, true);
			return envObject.clearLastError();
		}
		/**
		* @__sig ipppppp
		*/
		function node_api_create_object_with_properties(env, prototype_or_null, property_names, property_values, property_count, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!result) return envObject.setLastError(1);
			property_count >>>= 0;
			property_count = property_count >>> 0;
			if (property_count > 0) {
				if (!property_names) return envObject.setLastError(1);
				if (!property_values) return envObject.setLastError(1);
			}
			var v8_prototype_or_null = prototype_or_null ? emnapiCtx.handleStore.get(prototype_or_null).value : null;
			var properties = {};
			property_names >>>= 0;
			property_values >>>= 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			for (var i = 0; i < property_count; i++) {
				var name_value = emnapiCtx.handleStore.get(GET_HEAP_DATA_VIEW().getUint32(property_names + i * 4, true)).value;
				if (!(typeof name_value === "string" || typeof name_value === "symbol")) return envObject.setLastError(4);
				properties[name_value] = {
					value: emnapiCtx.handleStore.get(GET_HEAP_DATA_VIEW().getUint32(property_values + i * 4, true)).value,
					writable: true,
					enumerable: true,
					configurable: true
				};
			}
			var obj;
			try {
				obj = Object.defineProperties(Object.create(v8_prototype_or_null), properties);
			} catch (_) {
				return envObject.setLastError(9);
			}
			var value = emnapiCtx.addToCurrentScope(obj).id;
			result >>>= 0;
			GET_HEAP_DATA_VIEW().setUint32(result, value, true);
			return envObject.clearLastError();
		}
		/**
		* @__sig ippp
		*/
		function napi_create_symbol(env, description, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!result) return envObject.setLastError(1);
			result >>>= 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			if (!description) {
				var value = emnapiCtx.addToCurrentScope(Symbol()).id;
				GET_HEAP_DATA_VIEW().setUint32(result, value, true);
			} else {
				var desc = emnapiCtx.handleStore.get(description).value;
				if (typeof desc !== "string") return envObject.setLastError(3);
				var v = emnapiCtx.addToCurrentScope(Symbol(desc)).id;
				GET_HEAP_DATA_VIEW().setUint32(result, v, true);
			}
			return envObject.clearLastError();
		}
		/**
		* @__sig ipipppp
		*/
		function napi_create_typedarray(env, type, length, arraybuffer, byte_offset, result) {
			var value;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!arraybuffer) return envObject.setLastError(1);
				if (!result) return envObject.setLastError(1);
				var buffer = emnapiCtx.handleStore.get(arraybuffer).value;
				byte_offset >>>= 0;
				length >>>= 0;
				var createTypedArray = function(envObject, Type, size_of_element, buffer, byte_offset, length) {
					var _a;
					byte_offset = byte_offset >>> 0;
					length = length >>> 0;
					if (size_of_element > 1) {
						if (byte_offset % size_of_element !== 0) {
							var err = new RangeError("start offset of ".concat((_a = Type.name) !== null && _a !== void 0 ? _a : "", " should be a multiple of ").concat(size_of_element));
							err.code = "ERR_NAPI_INVALID_TYPEDARRAY_ALIGNMENT";
							envObject.tryCatch.setError(err);
							return envObject.setLastError(9);
						}
					}
					if (length * size_of_element + byte_offset > buffer.byteLength) {
						var err = /* @__PURE__ */ new RangeError("Invalid typed array length");
						err.code = "ERR_NAPI_INVALID_TYPEDARRAY_LENGTH";
						envObject.tryCatch.setError(err);
						return envObject.setLastError(9);
					}
					var out = new Type(buffer, byte_offset, length);
					if (buffer === wasmMemory.buffer) {
						if (!emnapiExternalMemory.wasmMemoryViewTable.has(out)) emnapiExternalMemory.wasmMemoryViewTable.set(out, {
							Ctor: Type,
							address: byte_offset,
							length,
							ownership: 1,
							runtimeAllocated: 0
						});
					}
					result >>>= 0;
					value = emnapiCtx.addToCurrentScope(out).id;
					var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
						return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
					};
					GET_HEAP_DATA_VIEW().setUint32(result, value, true);
					return envObject.getReturnStatus();
				};
				if (buffer instanceof ArrayBuffer || emnapiExternalMemory.isSharedArrayBuffer(buffer)) switch (type) {
					case 0: return createTypedArray(envObject, Int8Array, 1, buffer, byte_offset, length);
					case 1: return createTypedArray(envObject, Uint8Array, 1, buffer, byte_offset, length);
					case 2: return createTypedArray(envObject, Uint8ClampedArray, 1, buffer, byte_offset, length);
					case 3: return createTypedArray(envObject, Int16Array, 2, buffer, byte_offset, length);
					case 4: return createTypedArray(envObject, Uint16Array, 2, buffer, byte_offset, length);
					case 5: return createTypedArray(envObject, Int32Array, 4, buffer, byte_offset, length);
					case 6: return createTypedArray(envObject, Uint32Array, 4, buffer, byte_offset, length);
					case 7: return createTypedArray(envObject, Float32Array, 4, buffer, byte_offset, length);
					case 8: return createTypedArray(envObject, Float64Array, 8, buffer, byte_offset, length);
					case 9: return createTypedArray(envObject, BigInt64Array, 8, buffer, byte_offset, length);
					case 10: return createTypedArray(envObject, BigUint64Array, 8, buffer, byte_offset, length);
					case 11:
						if (typeof Float16Array !== "function") return envObject.setLastError(1);
						return createTypedArray(envObject, Float16Array, 2, buffer, byte_offset, length);
					default: return envObject.setLastError(1);
				}
				else return envObject.setLastError(1);
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/**
		* @__deps malloc
		* @__sig ippp
		*/
		function napi_create_buffer(env, size, data, result) {
			var _a;
			var value, pointer;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!result) return envObject.setLastError(1);
				var Buffer = emnapiCtx.feature.Buffer;
				if (!Buffer) throw emnapiCtx.createNotSupportBufferError("napi_create_buffer", "");
				result >>>= 0;
				var buffer = void 0;
				size >>>= 0;
				size = size >>> 0;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				if (!data || size === 0) {
					buffer = Buffer.alloc(size);
					value = emnapiCtx.addToCurrentScope(buffer).id;
					GET_HEAP_DATA_VIEW().setUint32(result, value, true);
				} else {
					pointer = _malloc(size);
					if (!pointer) throw new Error("Out of memory");
					pointer >>>= 0;
					new Uint8Array(wasmMemory.buffer).subarray(pointer, pointer + size).fill(0);
					var buffer_1 = emnapiExternalMemory.getBufferFrom()(wasmMemory.buffer, pointer, size);
					var viewDescriptor = {
						Ctor: Buffer,
						address: pointer,
						length: size,
						ownership: emnapiExternalMemory.registry ? 0 : 1,
						runtimeAllocated: 1
					};
					emnapiExternalMemory.wasmMemoryViewTable.set(buffer_1, viewDescriptor);
					(_a = emnapiExternalMemory.registry) === null || _a === void 0 || _a.register(viewDescriptor, pointer);
					value = emnapiCtx.addToCurrentScope(buffer_1).id;
					GET_HEAP_DATA_VIEW().setUint32(result, value, true);
					data >>>= 0;
					GET_HEAP_DATA_VIEW().setUint32(data, pointer, true);
				}
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/**
		* @__sig ippppp
		*/
		function napi_create_buffer_copy(env, length, data, result_data, result) {
			var value;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!result) return envObject.setLastError(1);
				var Buffer = emnapiCtx.feature.Buffer;
				if (!Buffer) throw emnapiCtx.createNotSupportBufferError("napi_create_buffer_copy", "");
				var arrayBuffer = emnapiCreateArrayBuffer(length, result_data, false);
				var buffer = Buffer.from(arrayBuffer);
				data >>>= 0;
				length >>>= 0;
				buffer.set(new Uint8Array(wasmMemory.buffer).subarray(data, data + length));
				value = emnapiCtx.addToCurrentScope(buffer).id;
				result >>>= 0;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(result, value, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/**
		* @__sig ipppppp
		*/
		function napi_create_external_buffer(env, length, data, finalize_cb, finalize_hint, result) {
			return emnapi_create_memory_view(env, -2, data, length, finalize_cb, finalize_hint, result);
		}
		/**
		* @__sig ippppp
		*/
		function node_api_create_buffer_from_arraybuffer(env, arraybuffer, byte_offset, byte_length, result) {
			var value;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!arraybuffer) return envObject.setLastError(1);
				if (!result) return envObject.setLastError(1);
				byte_offset >>>= 0;
				byte_length >>>= 0;
				byte_offset = byte_offset >>> 0;
				byte_length = byte_length >>> 0;
				var handle = emnapiCtx.handleStore.get(arraybuffer);
				if (!handle.isArrayBuffer()) return envObject.setLastError(1);
				var buffer = handle.value;
				if (byte_length + byte_offset > buffer.byteLength) {
					var err = /* @__PURE__ */ new RangeError("The byte offset + length is out of range");
					err.code = "ERR_OUT_OF_RANGE";
					throw err;
				}
				var Buffer = emnapiCtx.feature.Buffer;
				if (!Buffer) throw emnapiCtx.createNotSupportBufferError("node_api_create_buffer_from_arraybuffer", "");
				var out = emnapiExternalMemory.getBufferFrom()(buffer, byte_offset, byte_length);
				if (buffer === wasmMemory.buffer) {
					if (!emnapiExternalMemory.wasmMemoryViewTable.has(out)) emnapiExternalMemory.wasmMemoryViewTable.set(out, {
						Ctor: Buffer,
						address: byte_offset,
						length: byte_length,
						ownership: 1,
						runtimeAllocated: 0
					});
				}
				result >>>= 0;
				value = emnapiCtx.addToCurrentScope(out).id;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(result, value, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/**
		* @__sig ippppp
		*/
		function napi_create_dataview(env, byte_length, arraybuffer, byte_offset, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!arraybuffer) return envObject.setLastError(1);
				if (!result) return envObject.setLastError(1);
				byte_length >>>= 0;
				byte_offset >>>= 0;
				byte_length = byte_length >>> 0;
				byte_offset = byte_offset >>> 0;
				var value = emnapiCtx.handleStore.get(arraybuffer).value;
				var createDataview = function(buffer) {
					if (byte_length + byte_offset > buffer.byteLength) {
						var err = /* @__PURE__ */ new RangeError("byte_offset + byte_length should be less than or equal to the size in bytes of the array passed in");
						err.code = "ERR_NAPI_INVALID_DATAVIEW_ARGS";
						throw err;
					}
					var dataview = new DataView(buffer, byte_offset, byte_length);
					if (buffer === wasmMemory.buffer) {
						if (!emnapiExternalMemory.wasmMemoryViewTable.has(dataview)) emnapiExternalMemory.wasmMemoryViewTable.set(dataview, {
							Ctor: DataView,
							address: byte_offset,
							length: byte_length,
							ownership: 1,
							runtimeAllocated: 0
						});
					}
					result >>>= 0;
					var v = emnapiCtx.addToCurrentScope(dataview).id;
					var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
						return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
					};
					GET_HEAP_DATA_VIEW().setUint32(result, v, true);
					return envObject.getReturnStatus();
				};
				if (value instanceof ArrayBuffer || emnapiExternalMemory.isSharedArrayBuffer(value)) return createDataview(value);
				else return envObject.setLastError(1);
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/**
		* @__sig ipppp
		*/
		function node_api_symbol_for(env, utf8description, length, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!result) return envObject.setLastError(1);
			length >>>= 0;
			utf8description >>>= 0;
			result >>>= 0;
			var autoLength = length === -1 || length === 0xffffffff;
			var sizelength = length >>> 0;
			if (length !== 0) {
				if (!utf8description) return envObject.setLastError(1);
			}
			if (!(autoLength || sizelength <= 0x7fffffff)) return envObject.setLastError(1);
			var descriptionString = emnapiString.UTF8ToString(utf8description, length);
			var value = emnapiCtx.addToCurrentScope(Symbol.for(descriptionString)).id;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(result, value, true);
			return envObject.clearLastError();
		}
		var createMod = /*#__PURE__*/ Object.freeze({
			__proto__: null,
			napi_create_array,
			napi_create_array_with_length,
			napi_create_arraybuffer,
			napi_create_buffer,
			napi_create_buffer_copy,
			napi_create_dataview,
			napi_create_date,
			napi_create_external,
			napi_create_external_arraybuffer,
			napi_create_external_buffer,
			napi_create_object,
			napi_create_symbol,
			napi_create_typedarray,
			node_api_create_buffer_from_arraybuffer,
			node_api_create_external_sharedarraybuffer,
			node_api_create_object_with_properties,
			node_api_create_sharedarraybuffer,
			node_api_symbol_for
		});
		/** @__sig ipip */
		function napi_get_boolean(env, value, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!result) return envObject.setLastError(1);
			result >>>= 0;
			var v = value === 0 ? 3 : 4;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(result, v, true);
			return envObject.clearLastError();
		}
		/** @__sig ipp */
		function napi_get_global(env, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!result) return envObject.setLastError(1);
			result >>>= 0;
			var value = 5;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(result, value, true);
			return envObject.clearLastError();
		}
		/** @__sig ipp */
		function napi_get_null(env, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!result) return envObject.setLastError(1);
			result >>>= 0;
			var value = 2;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(result, value, true);
			return envObject.clearLastError();
		}
		/** @__sig ipp */
		function napi_get_undefined(env, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!result) return envObject.setLastError(1);
			result >>>= 0;
			var value = 1;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(result, value, true);
			return envObject.clearLastError();
		}
		var globalMod = /*#__PURE__*/ Object.freeze({
			__proto__: null,
			napi_get_boolean,
			napi_get_global,
			napi_get_null,
			napi_get_undefined
		});
		/** @__sig ipppp */
		function napi_set_instance_data(env, data, finalize_cb, finalize_hint) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			data >>>= 0;
			finalize_cb >>>= 0;
			finalize_hint >>>= 0;
			envObject.setInstanceData(data, finalize_cb, finalize_hint);
			return envObject.clearLastError();
		}
		/** @__sig ipp */
		function napi_get_instance_data(env, data) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			if (!data) return envObject.setLastError(1);
			data >>>= 0;
			var value = envObject.getInstanceData();
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(data, value, true);
			return envObject.clearLastError();
		}
		var envMod = /*#__PURE__*/ Object.freeze({
			__proto__: null,
			napi_get_instance_data,
			napi_set_instance_data
		});
		/** @__sig vpppp */
		function _emnapi_get_last_error_info(env, error_code, engine_error_code, engine_reserved) {
			error_code >>>= 0;
			engine_error_code >>>= 0;
			engine_reserved >>>= 0;
			var lastError = emnapiCtx.envStore.get(env).lastError;
			var errorCode = lastError.errorCode;
			var engineErrorCode = lastError.engineErrorCode >>> 0;
			var engineReserved = lastError.engineReserved;
			engineReserved >>>= 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setInt32(error_code, errorCode, true);
			GET_HEAP_DATA_VIEW().setUint32(engine_error_code, engineErrorCode, true);
			GET_HEAP_DATA_VIEW().setUint32(engine_reserved, engineReserved, true);
		}
		/** @__sig ipp */
		function napi_throw(env, error) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!error) return envObject.setLastError(1);
				envObject.tryCatch.setError(emnapiCtx.handleStore.get(error).value);
				return envObject.clearLastError();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ippp */
		function napi_throw_error(env, code, msg) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!msg) return envObject.setLastError(1);
				code >>>= 0;
				msg >>>= 0;
				var error = new Error(emnapiString.UTF8ToString(msg, -1));
				if (code) error.code = emnapiString.UTF8ToString(code, -1);
				envObject.tryCatch.setError(error);
				return envObject.clearLastError();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ippp */
		function napi_throw_type_error(env, code, msg) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!msg) return envObject.setLastError(1);
				code >>>= 0;
				msg >>>= 0;
				var error = new TypeError(emnapiString.UTF8ToString(msg, -1));
				if (code) error.code = emnapiString.UTF8ToString(code, -1);
				envObject.tryCatch.setError(error);
				return envObject.clearLastError();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ippp */
		function napi_throw_range_error(env, code, msg) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!msg) return envObject.setLastError(1);
				code >>>= 0;
				msg >>>= 0;
				var error = new RangeError(emnapiString.UTF8ToString(msg, -1));
				if (code) error.code = emnapiString.UTF8ToString(code, -1);
				envObject.tryCatch.setError(error);
				return envObject.clearLastError();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ippp */
		function node_api_throw_syntax_error(env, code, msg) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!msg) return envObject.setLastError(1);
				code >>>= 0;
				msg >>>= 0;
				var error = new SyntaxError(emnapiString.UTF8ToString(msg, -1));
				if (code) error.code = emnapiString.UTF8ToString(code, -1);
				envObject.tryCatch.setError(error);
				return envObject.clearLastError();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ipp */
		function napi_is_exception_pending(env, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!result) return envObject.setLastError(1);
			var r = envObject.tryCatch.hasCaught();
			result >>>= 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setInt8(result, r ? 1 : 0, true);
			return envObject.clearLastError();
		}
		/** @__sig ipppp */
		function napi_create_error(env, code, msg, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!msg) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			var msgValue = emnapiCtx.handleStore.get(msg).value;
			if (typeof msgValue !== "string") return envObject.setLastError(3);
			var error = new Error(msgValue);
			if (code) {
				var codeValue = emnapiCtx.handleStore.get(code).value;
				if (typeof codeValue !== "string") return envObject.setLastError(3);
				error.code = codeValue;
			}
			result >>>= 0;
			var value = emnapiCtx.addToCurrentScope(error).id;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(result, value, true);
			return envObject.clearLastError();
		}
		/** @__sig ipppp */
		function napi_create_type_error(env, code, msg, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!msg) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			var msgValue = emnapiCtx.handleStore.get(msg).value;
			if (typeof msgValue !== "string") return envObject.setLastError(3);
			var error = new TypeError(msgValue);
			if (code) {
				var codeValue = emnapiCtx.handleStore.get(code).value;
				if (typeof codeValue !== "string") return envObject.setLastError(3);
				error.code = codeValue;
			}
			result >>>= 0;
			var value = emnapiCtx.addToCurrentScope(error).id;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(result, value, true);
			return envObject.clearLastError();
		}
		/** @__sig ipppp */
		function napi_create_range_error(env, code, msg, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!msg) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			var msgValue = emnapiCtx.handleStore.get(msg).value;
			if (typeof msgValue !== "string") return envObject.setLastError(3);
			var error = new RangeError(msgValue);
			if (code) {
				var codeValue = emnapiCtx.handleStore.get(code).value;
				if (typeof codeValue !== "string") return envObject.setLastError(3);
				error.code = codeValue;
			}
			result >>>= 0;
			var value = emnapiCtx.addToCurrentScope(error).id;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(result, value, true);
			return envObject.clearLastError();
		}
		/** @__sig ipppp */
		function node_api_create_syntax_error(env, code, msg, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!msg) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			var msgValue = emnapiCtx.handleStore.get(msg).value;
			if (typeof msgValue !== "string") return envObject.setLastError(3);
			var error = new SyntaxError(msgValue);
			if (code) {
				var codeValue = emnapiCtx.handleStore.get(code).value;
				if (typeof codeValue !== "string") return envObject.setLastError(3);
				error.code = codeValue;
			}
			result >>>= 0;
			var value = emnapiCtx.addToCurrentScope(error).id;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(result, value, true);
			return envObject.clearLastError();
		}
		/** @__sig ipp */
		function napi_get_and_clear_last_exception(env, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!result) return envObject.setLastError(1);
			result >>>= 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			if (!envObject.tryCatch.hasCaught()) {
				GET_HEAP_DATA_VIEW().setUint32(result, 1, true);
				return envObject.clearLastError();
			} else {
				var err = envObject.tryCatch.exception();
				var value = envObject.ensureHandleId(err);
				GET_HEAP_DATA_VIEW().setUint32(result, value, true);
				envObject.tryCatch.reset();
			}
			return envObject.clearLastError();
		}
		/** @__sig vpppp */
		function napi_fatal_error(location, location_len, message, message_len) {
			location >>>= 0;
			location_len >>>= 0;
			message >>>= 0;
			message_len >>>= 0;
			var locationStr = emnapiString.UTF8ToString(location, location_len);
			var messageStr = emnapiString.UTF8ToString(message, message_len);
			if (emnapiNodeBinding) emnapiNodeBinding.napi.fatalError(locationStr, messageStr);
			else abort("FATAL ERROR: " + locationStr + " " + messageStr);
		}
		/** @__sig ipp */
		function napi_fatal_exception(env, err) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!err) return envObject.setLastError(1);
				var error = envObject.ctx.handleStore.get(err);
				try {
					envObject.triggerFatalException(error.value);
				} catch (_) {
					return envObject.setLastError(9);
				}
				return envObject.clearLastError();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		var errorMod = /*#__PURE__*/ Object.freeze({
			__proto__: null,
			_emnapi_get_last_error_info,
			napi_create_error,
			napi_create_range_error,
			napi_create_type_error,
			napi_fatal_error,
			napi_fatal_exception,
			napi_get_and_clear_last_exception,
			napi_is_exception_pending,
			napi_throw,
			napi_throw_error,
			napi_throw_range_error,
			napi_throw_type_error,
			node_api_create_syntax_error,
			node_api_throw_syntax_error
		});
		/** @__sig ipppppp */
		function _emnapi_create_function(env, utf8name, length, cb, data, result) {
			length >>>= 0;
			var envObject = emnapiCtx.envStore.get(env);
			var fresult = emnapiCreateFunction(envObject, utf8name, length, cb, data);
			if (fresult.status !== 0) return envObject.setLastError(fresult.status);
			var f = fresult.f;
			var valueHandle = emnapiCtx.addToCurrentScope(f);
			result >>>= 0;
			var value = valueHandle.id;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(result, value, true);
			return envObject.getReturnStatus();
		}
		/** @__sig ipppppp */
		function napi_create_function(env, utf8name, length, cb, data, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!result) return envObject.setLastError(1);
				if (!cb) return envObject.setLastError(1);
				return _emnapi_create_function(env, utf8name, length, cb, data, result);
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ipppppp */
		function napi_get_cb_info(env, cbinfo, argc, argv, this_arg, data) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			if (!cbinfo) return envObject.setLastError(1);
			var cbinfoValue = emnapiCtx.scopeStore.get(cbinfo).callbackInfo;
			argc >>>= 0;
			argv >>>= 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			if (argv) {
				if (!argc) return envObject.setLastError(1);
				var argcValue = GET_HEAP_DATA_VIEW().getUint32(argc, true);
				argcValue >>>= 0;
				var len = cbinfoValue.args.length;
				var arrlen = argcValue < len ? argcValue : len;
				var i = 0;
				for (; i < arrlen; i++) {
					var argVal = envObject.ensureHandleId(cbinfoValue.args[i]);
					GET_HEAP_DATA_VIEW().setUint32(argv + i * 4, argVal, true);
				}
				if (i < argcValue) for (; i < argcValue; i++) GET_HEAP_DATA_VIEW().setUint32(argv + i * 4, 1, true);
			}
			if (argc) GET_HEAP_DATA_VIEW().setUint32(argc, cbinfoValue.args.length, true);
			if (this_arg) {
				this_arg >>>= 0;
				var v = envObject.ensureHandleId(cbinfoValue.thiz);
				GET_HEAP_DATA_VIEW().setUint32(this_arg, v, true);
			}
			if (data) {
				data >>>= 0;
				GET_HEAP_DATA_VIEW().setUint32(data, cbinfoValue.data, true);
			}
			return envObject.clearLastError();
		}
		/** @__sig ipppppp */
		function napi_call_function(env, recv, func, argc, argv, result) {
			var i = 0;
			var v;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!recv) return envObject.setLastError(1);
				argc >>>= 0;
				argv >>>= 0;
				result >>>= 0;
				argc = argc >>> 0;
				if (argc > 0) {
					if (!argv) return envObject.setLastError(1);
				}
				var v8recv = emnapiCtx.handleStore.get(recv).value;
				if (!func) return envObject.setLastError(1);
				var v8func = emnapiCtx.handleStore.get(func).value;
				if (typeof v8func !== "function") return envObject.setLastError(1);
				var args = [];
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				for (; i < argc; i++) {
					var argVal = GET_HEAP_DATA_VIEW().getUint32(argv + i * 4, true);
					args.push(emnapiCtx.handleStore.get(argVal).value);
				}
				var ret = v8func.apply(v8recv, args);
				if (result) {
					v = envObject.ensureHandleId(ret);
					GET_HEAP_DATA_VIEW().setUint32(result, v, true);
				}
				return envObject.clearLastError();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ippppp */
		function napi_new_instance(env, constructor, argc, argv, result) {
			var i;
			var v;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!constructor) return envObject.setLastError(1);
				argc >>>= 0;
				argv >>>= 0;
				result >>>= 0;
				argc = argc >>> 0;
				if (argc > 0) {
					if (!argv) return envObject.setLastError(1);
				}
				if (!result) return envObject.setLastError(1);
				var Ctor = emnapiCtx.handleStore.get(constructor).value;
				if (typeof Ctor !== "function") return envObject.setLastError(1);
				var ret = void 0;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				if (emnapiCtx.feature.supportReflect) {
					var argList = Array(argc);
					for (i = 0; i < argc; i++) {
						var argVal = GET_HEAP_DATA_VIEW().getUint32(argv + i * 4, true);
						argList[i] = emnapiCtx.handleStore.get(argVal).value;
					}
					ret = Reflect.construct(Ctor, argList, Ctor);
				} else {
					var args = Array(argc + 1);
					args[0] = void 0;
					for (i = 0; i < argc; i++) {
						var argVal = GET_HEAP_DATA_VIEW().getUint32(argv + i * 4, true);
						args[i + 1] = emnapiCtx.handleStore.get(argVal).value;
					}
					ret = new (Ctor.bind.apply(Ctor, args))();
				}
				if (result) {
					v = envObject.ensureHandleId(ret);
					GET_HEAP_DATA_VIEW().setUint32(result, v, true);
				}
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ippp */
		function napi_get_new_target(env, cbinfo, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!cbinfo) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			result >>>= 0;
			var cbinfoValue = emnapiCtx.scopeStore.get(cbinfo).callbackInfo;
			var thiz = cbinfoValue.thiz, fn = cbinfoValue.fn;
			var value = thiz == null || thiz.constructor == null ? 0 : thiz instanceof fn ? envObject.ensureHandleId(thiz.constructor) : 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(result, value, true);
			return envObject.clearLastError();
		}
		var functionMod = /*#__PURE__*/ Object.freeze({
			__proto__: null,
			_emnapi_create_function,
			napi_call_function,
			napi_create_function,
			napi_get_cb_info,
			napi_get_new_target,
			napi_new_instance
		});
		/** @__sig ipp */
		function napi_open_handle_scope(env, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!result) return envObject.setLastError(1);
			var scope = emnapiCtx.openScope(envObject);
			result >>>= 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(result, scope.id, true);
			return envObject.clearLastError();
		}
		/** @__sig ipp */
		function napi_close_handle_scope(env, scope) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!scope) return envObject.setLastError(1);
			if (envObject.openHandleScopes === 0) return 13;
			emnapiCtx.closeScope(envObject);
			return envObject.clearLastError();
		}
		/** @__sig ipp */
		function napi_open_escapable_handle_scope(env, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!result) return envObject.setLastError(1);
			var scope = emnapiCtx.openScope(envObject);
			result >>>= 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(result, scope.id, true);
			return envObject.clearLastError();
		}
		/** @__sig ipp */
		function napi_close_escapable_handle_scope(env, scope) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!scope) return envObject.setLastError(1);
			if (envObject.openHandleScopes === 0) return 13;
			emnapiCtx.closeScope(envObject);
			return envObject.clearLastError();
		}
		/** @__sig ipppp */
		function napi_escape_handle(env, scope, escapee, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!scope) return envObject.setLastError(1);
			if (!escapee) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			var scopeObject = emnapiCtx.scopeStore.get(scope);
			if (!scopeObject.escapeCalled()) {
				escapee >>>= 0;
				result >>>= 0;
				var newHandle = scopeObject.escape(escapee);
				var value = newHandle ? newHandle.id : 0;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(result, value, true);
				return envObject.clearLastError();
			}
			return envObject.setLastError(12);
		}
		/** @__sig ippip */
		function napi_create_reference(env, value, initial_refcount, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!value) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			var handle = emnapiCtx.handleStore.get(value);
			if (envObject.moduleApiVersion < 10) {
				if (!(handle.isObject() || handle.isFunction() || handle.isSymbol())) return envObject.setLastError(1);
			}
			var ref = emnapiCtx.createReference(envObject, handle.id, initial_refcount >>> 0, 1);
			result >>>= 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(result, ref.id, true);
			return envObject.clearLastError();
		}
		/** @__sig ipp */
		function napi_delete_reference(env, ref) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			if (!ref) return envObject.setLastError(1);
			emnapiCtx.refStore.get(ref).dispose();
			return envObject.clearLastError();
		}
		/** @__sig ippp */
		function napi_reference_ref(env, ref, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!ref) return envObject.setLastError(1);
			var count = emnapiCtx.refStore.get(ref).ref();
			if (result) {
				result >>>= 0;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(result, count, true);
			}
			return envObject.clearLastError();
		}
		/** @__sig ippp */
		function napi_reference_unref(env, ref, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!ref) return envObject.setLastError(1);
			var reference = emnapiCtx.refStore.get(ref);
			if (reference.refcount() === 0) return envObject.setLastError(9);
			var count = reference.unref();
			if (result) {
				result >>>= 0;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(result, count, true);
			}
			return envObject.clearLastError();
		}
		/** @__sig ippp */
		function napi_get_reference_value(env, ref, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!ref) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			var handleId = emnapiCtx.refStore.get(ref).get(envObject);
			result >>>= 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(result, handleId, true);
			return envObject.clearLastError();
		}
		/** @__sig ippp */
		function napi_add_env_cleanup_hook(env, fun, arg) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			if (!fun) return envObject.setLastError(1);
			fun >>>= 0;
			arg >>>= 0;
			emnapiCtx.addCleanupHook(envObject, fun, arg);
			return 0;
		}
		/** @__sig ippp */
		function napi_remove_env_cleanup_hook(env, fun, arg) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			if (!fun) return envObject.setLastError(1);
			fun >>>= 0;
			arg >>>= 0;
			emnapiCtx.removeCleanupHook(envObject, fun, arg);
			return 0;
		}
		/** @__sig vp */
		function _emnapi_env_ref(env) {
			emnapiCtx.envStore.get(env).ref();
		}
		/** @__sig vp */
		function _emnapi_env_unref(env) {
			emnapiCtx.envStore.get(env).unref();
		}
		var lifeMod = /*#__PURE__*/ Object.freeze({
			__proto__: null,
			_emnapi_env_ref,
			_emnapi_env_unref,
			napi_add_env_cleanup_hook,
			napi_close_escapable_handle_scope,
			napi_close_handle_scope,
			napi_create_reference,
			napi_delete_reference,
			napi_escape_handle,
			napi_get_reference_value,
			napi_open_escapable_handle_scope,
			napi_open_handle_scope,
			napi_reference_ref,
			napi_reference_unref,
			napi_remove_env_cleanup_hook
		});
		/** @__sig ippi */
		function _emnapi_get_filename(env, buf, len) {
			var filename = emnapiCtx.envStore.get(env).filename;
			if (!buf) return emnapiString.lengthBytesUTF8(filename);
			return emnapiString.stringToUTF8(filename, buf, len);
		}
		var miscellaneousMod = /*#__PURE__*/ Object.freeze({
			__proto__: null,
			_emnapi_get_filename
		});
		/** @__sig ippp */
		function napi_create_promise(env, deferred, promise) {
			var deferredObjectId, value;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!deferred) return envObject.setLastError(1);
				if (!promise) return envObject.setLastError(1);
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				var p = new Promise(function(resolve, reject) {
					deferredObjectId = emnapiCtx.createDeferred({
						resolve,
						reject
					}).id;
					deferred >>>= 0;
					GET_HEAP_DATA_VIEW().setUint32(deferred, deferredObjectId, true);
				});
				promise >>>= 0;
				value = emnapiCtx.addToCurrentScope(p).id;
				GET_HEAP_DATA_VIEW().setUint32(promise, value, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ippp */
		function napi_resolve_deferred(env, deferred, resolution) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!deferred) return envObject.setLastError(1);
				if (!resolution) return envObject.setLastError(1);
				emnapiCtx.deferredStore.get(deferred).resolve(emnapiCtx.handleStore.get(resolution).value);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ippp */
		function napi_reject_deferred(env, deferred, resolution) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!deferred) return envObject.setLastError(1);
				if (!resolution) return envObject.setLastError(1);
				emnapiCtx.deferredStore.get(deferred).reject(emnapiCtx.handleStore.get(resolution).value);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ippp */
		function napi_is_promise(env, value, is_promise) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!value) return envObject.setLastError(1);
			if (!is_promise) return envObject.setLastError(1);
			var h = emnapiCtx.handleStore.get(value);
			is_promise >>>= 0;
			var r = h.isPromise() ? 1 : 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setInt8(is_promise, r, true);
			return envObject.clearLastError();
		}
		var promiseMod = /*#__PURE__*/ Object.freeze({
			__proto__: null,
			napi_create_promise,
			napi_is_promise,
			napi_reject_deferred,
			napi_resolve_deferred
		});
		/** @__sig ippiiip */
		function napi_get_all_property_names(env, object, key_mode, key_filter, key_conversion, result) {
			var value;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!result) return envObject.setLastError(1);
				if (!object) return envObject.setLastError(1);
				var h = emnapiCtx.handleStore.get(object);
				if (h.value == null) throw new TypeError("Cannot convert undefined or null to object");
				var obj = void 0;
				try {
					obj = h.isObject() || h.isFunction() ? h.value : Object(h.value);
				} catch (_) {
					return envObject.setLastError(2);
				}
				if (key_mode !== 0 && key_mode !== 1) return envObject.setLastError(1);
				if (key_conversion !== 0 && key_conversion !== 1) return envObject.setLastError(1);
				var props = [];
				var names = void 0;
				var symbols = void 0;
				var i = void 0;
				var own = true;
				var integerIndiceRegex = /^(0|[1-9][0-9]*)$/;
				do {
					names = Object.getOwnPropertyNames(obj);
					symbols = Object.getOwnPropertySymbols(obj);
					for (i = 0; i < names.length; i++) props.push({
						name: integerIndiceRegex.test(names[i]) ? Number(names[i]) : names[i],
						desc: Object.getOwnPropertyDescriptor(obj, names[i]),
						own
					});
					for (i = 0; i < symbols.length; i++) props.push({
						name: symbols[i],
						desc: Object.getOwnPropertyDescriptor(obj, symbols[i]),
						own
					});
					if (key_mode === 1) break;
					obj = Object.getPrototypeOf(obj);
					own = false;
				} while (obj);
				var ret = [];
				var addName = function(ret, name, key_filter, conversion_mode) {
					if (ret.indexOf(name) !== -1) return;
					if (conversion_mode === 0) ret.push(name);
					else if (conversion_mode === 1) {
						var realName = typeof name === "number" ? String(name) : name;
						if (typeof realName === "string") {
							if (!(key_filter & 8)) ret.push(realName);
						} else ret.push(realName);
					}
				};
				for (i = 0; i < props.length; i++) {
					var prop = props[i];
					var name_1 = prop.name;
					var desc = prop.desc;
					if (key_filter === 0) addName(ret, name_1, key_filter, key_conversion);
					else {
						if (key_filter & 8 && typeof name_1 === "string") continue;
						if (key_filter & 16 && typeof name_1 === "symbol") continue;
						var shouldAdd = true;
						switch (key_filter & 7) {
							case 1:
								shouldAdd = Boolean(desc.writable);
								break;
							case 2:
								shouldAdd = Boolean(desc.enumerable);
								break;
							case 3:
								shouldAdd = Boolean(desc.writable && desc.enumerable);
								break;
							case 4:
								shouldAdd = Boolean(desc.configurable);
								break;
							case 5:
								shouldAdd = Boolean(desc.configurable && desc.writable);
								break;
							case 6:
								shouldAdd = Boolean(desc.configurable && desc.enumerable);
								break;
							case 7: shouldAdd = Boolean(desc.configurable && desc.enumerable && desc.writable);
						}
						if (shouldAdd) addName(ret, name_1, key_filter, key_conversion);
					}
				}
				result >>>= 0;
				value = emnapiCtx.addToCurrentScope(ret).id;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(result, value, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ippp */
		function napi_get_property_names(env, object, result) {
			return napi_get_all_property_names(env, object, 0, 18, 1, result);
		}
		/** @__sig ipppp */
		function napi_set_property(env, object, key, value) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!key) return envObject.setLastError(1);
				if (!value) return envObject.setLastError(1);
				if (!object) return envObject.setLastError(1);
				var h = emnapiCtx.handleStore.get(object);
				if (!(h.isObject() || h.isFunction())) return envObject.setLastError(2);
				h.value[emnapiCtx.handleStore.get(key).value] = emnapiCtx.handleStore.get(value).value;
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ipppp */
		function napi_has_property(env, object, key, result) {
			var r;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!key) return envObject.setLastError(1);
				if (!result) return envObject.setLastError(1);
				if (!object) return envObject.setLastError(1);
				var h = emnapiCtx.handleStore.get(object);
				if (h.value == null) throw new TypeError("Cannot convert undefined or null to object");
				var v = void 0;
				try {
					v = h.isObject() || h.isFunction() ? h.value : Object(h.value);
				} catch (_) {
					return envObject.setLastError(2);
				}
				result >>>= 0;
				r = emnapiCtx.handleStore.get(key).value in v ? 1 : 0;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setInt8(result, r, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ipppp */
		function napi_get_property(env, object, key, result) {
			var value;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!key) return envObject.setLastError(1);
				if (!result) return envObject.setLastError(1);
				if (!object) return envObject.setLastError(1);
				var h = emnapiCtx.handleStore.get(object);
				if (h.value == null) throw new TypeError("Cannot convert undefined or null to object");
				var v = void 0;
				try {
					v = h.isObject() || h.isFunction() ? h.value : Object(h.value);
				} catch (_) {
					return envObject.setLastError(2);
				}
				result >>>= 0;
				value = envObject.ensureHandleId(v[emnapiCtx.handleStore.get(key).value]);
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(result, value, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ipppp */
		function napi_delete_property(env, object, key, result) {
			var r;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!key) return envObject.setLastError(1);
				if (!object) return envObject.setLastError(1);
				var h = emnapiCtx.handleStore.get(object);
				if (!(h.isObject() || h.isFunction())) return envObject.setLastError(2);
				var propertyKey = emnapiCtx.handleStore.get(key).value;
				if (emnapiCtx.feature.supportReflect) r = Reflect.deleteProperty(h.value, propertyKey);
				else try {
					r = delete h.value[propertyKey];
				} catch (_) {
					r = false;
				}
				if (result) {
					result >>>= 0;
					var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
						return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
					};
					GET_HEAP_DATA_VIEW().setInt8(result, r ? 1 : 0, true);
				}
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ipppp */
		function napi_has_own_property(env, object, key, result) {
			var r;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!key) return envObject.setLastError(1);
				if (!result) return envObject.setLastError(1);
				if (!object) return envObject.setLastError(1);
				var h = emnapiCtx.handleStore.get(object);
				if (h.value == null) throw new TypeError("Cannot convert undefined or null to object");
				var v = void 0;
				try {
					v = h.isObject() || h.isFunction() ? h.value : Object(h.value);
				} catch (_) {
					return envObject.setLastError(2);
				}
				var prop = emnapiCtx.handleStore.get(key).value;
				if (typeof prop !== "string" && typeof prop !== "symbol") return envObject.setLastError(4);
				r = Object.prototype.hasOwnProperty.call(v, emnapiCtx.handleStore.get(key).value);
				result >>>= 0;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setInt8(result, r ? 1 : 0, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ipppp */
		function napi_set_named_property(env, object, cname, value) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!value) return envObject.setLastError(1);
				if (!object) return envObject.setLastError(1);
				var h = emnapiCtx.handleStore.get(object);
				if (!(h.isObject() || h.isFunction())) return envObject.setLastError(2);
				if (!cname) return envObject.setLastError(1);
				cname >>>= 0;
				emnapiCtx.handleStore.get(object).value[emnapiString.UTF8ToString(cname, -1)] = emnapiCtx.handleStore.get(value).value;
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ipppp */
		function napi_has_named_property(env, object, utf8name, result) {
			var r;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!result) return envObject.setLastError(1);
				if (!object) return envObject.setLastError(1);
				if (!utf8name) return envObject.setLastError(1);
				var h = emnapiCtx.handleStore.get(object);
				if (h.value == null) throw new TypeError("Cannot convert undefined or null to object");
				var v = void 0;
				try {
					v = h.isObject() || h.isFunction() ? h.value : Object(h.value);
				} catch (_) {
					return envObject.setLastError(2);
				}
				utf8name >>>= 0;
				result >>>= 0;
				r = emnapiString.UTF8ToString(utf8name, -1) in v;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setInt8(result, r ? 1 : 0, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ipppp */
		function napi_get_named_property(env, object, utf8name, result) {
			var value;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!result) return envObject.setLastError(1);
				if (!object) return envObject.setLastError(1);
				if (!utf8name) return envObject.setLastError(1);
				var h = emnapiCtx.handleStore.get(object);
				if (h.value == null) throw new TypeError("Cannot convert undefined or null to object");
				var v = void 0;
				try {
					v = h.isObject() || h.isFunction() ? h.value : Object(h.value);
				} catch (_) {
					return envObject.setLastError(2);
				}
				utf8name >>>= 0;
				result >>>= 0;
				value = envObject.ensureHandleId(v[emnapiString.UTF8ToString(utf8name, -1)]);
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(result, value, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ippip */
		function napi_set_element(env, object, index, value) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!value) return envObject.setLastError(1);
				if (!object) return envObject.setLastError(1);
				var h = emnapiCtx.handleStore.get(object);
				if (!(h.isObject() || h.isFunction())) return envObject.setLastError(2);
				h.value[index >>> 0] = emnapiCtx.handleStore.get(value).value;
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ippip */
		function napi_has_element(env, object, index, result) {
			var r;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!result) return envObject.setLastError(1);
				if (!object) return envObject.setLastError(1);
				var h = emnapiCtx.handleStore.get(object);
				if (h.value == null) throw new TypeError("Cannot convert undefined or null to object");
				var v = void 0;
				try {
					v = h.isObject() || h.isFunction() ? h.value : Object(h.value);
				} catch (_) {
					return envObject.setLastError(2);
				}
				result >>>= 0;
				r = index >>> 0 in v ? 1 : 0;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setInt8(result, r, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ippip */
		function napi_get_element(env, object, index, result) {
			var value;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!result) return envObject.setLastError(1);
				if (!object) return envObject.setLastError(1);
				var h = emnapiCtx.handleStore.get(object);
				if (h.value == null) throw new TypeError("Cannot convert undefined or null to object");
				var v = void 0;
				try {
					v = h.isObject() || h.isFunction() ? h.value : Object(h.value);
				} catch (_) {
					return envObject.setLastError(2);
				}
				result >>>= 0;
				value = envObject.ensureHandleId(v[index >>> 0]);
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(result, value, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ippip */
		function napi_delete_element(env, object, index, result) {
			var r;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!object) return envObject.setLastError(1);
				var h = emnapiCtx.handleStore.get(object);
				if (!(h.isObject() || h.isFunction())) return envObject.setLastError(2);
				if (emnapiCtx.feature.supportReflect) r = Reflect.deleteProperty(h.value, index >>> 0);
				else try {
					r = delete h.value[index >>> 0];
				} catch (_) {
					r = false;
				}
				if (result) {
					result >>>= 0;
					var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
						return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
					};
					GET_HEAP_DATA_VIEW().setInt8(result, r ? 1 : 0, true);
				}
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ipppp */
		function napi_define_properties(env, object, property_count, properties) {
			var propPtr, attributes;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				properties >>>= 0;
				property_count >>>= 0;
				property_count = property_count >>> 0;
				if (property_count > 0) {
					if (!properties) return envObject.setLastError(1);
				}
				if (!object) return envObject.setLastError(1);
				var h = emnapiCtx.handleStore.get(object);
				var maybeObject = h.value;
				if (!(h.isObject() || h.isFunction())) return envObject.setLastError(2);
				var propertyName = void 0;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				for (var i = 0; i < property_count; i++) {
					propPtr = properties + i * 32;
					var utf8Name = GET_HEAP_DATA_VIEW().getUint32(propPtr, true);
					var name_2 = GET_HEAP_DATA_VIEW().getUint32(propPtr + 4, true);
					var method = GET_HEAP_DATA_VIEW().getUint32(propPtr + 8, true);
					var getter = GET_HEAP_DATA_VIEW().getUint32(propPtr + 12, true);
					var setter = GET_HEAP_DATA_VIEW().getUint32(propPtr + 16, true);
					var value = GET_HEAP_DATA_VIEW().getUint32(propPtr + 20, true);
					attributes = GET_HEAP_DATA_VIEW().getInt32(propPtr + 24, true);
					attributes >>>= 0;
					var data = GET_HEAP_DATA_VIEW().getUint32(propPtr + 28, true);
					if (utf8Name) propertyName = emnapiString.UTF8ToString(utf8Name, -1);
					else {
						if (!name_2) return envObject.setLastError(4);
						propertyName = emnapiCtx.handleStore.get(name_2).value;
						if (typeof propertyName !== "string" && typeof propertyName !== "symbol") return envObject.setLastError(4);
					}
					emnapiDefineProperty(envObject, maybeObject, propertyName, method, getter, setter, value, attributes, data);
				}
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ipp */
		function napi_object_freeze(env, object) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!object) return envObject.setLastError(1);
				var h = emnapiCtx.handleStore.get(object);
				var maybeObject = h.value;
				if (!(h.isObject() || h.isFunction())) return envObject.setLastError(2);
				Object.freeze(maybeObject);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ipp */
		function napi_object_seal(env, object) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!object) return envObject.setLastError(1);
				var h = emnapiCtx.handleStore.get(object);
				var maybeObject = h.value;
				if (!(h.isObject() || h.isFunction())) return envObject.setLastError(2);
				Object.seal(maybeObject);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		var propertyMod = /*#__PURE__*/ Object.freeze({
			__proto__: null,
			napi_define_properties,
			napi_delete_element,
			napi_delete_property,
			napi_get_all_property_names,
			napi_get_element,
			napi_get_named_property,
			napi_get_property,
			napi_get_property_names,
			napi_has_element,
			napi_has_named_property,
			napi_has_own_property,
			napi_has_property,
			napi_object_freeze,
			napi_object_seal,
			napi_set_element,
			napi_set_named_property,
			napi_set_property
		});
		/** @__sig ippp */
		function napi_run_script(env, script, result) {
			var status;
			var value;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!script) return envObject.setLastError(1);
				if (!result) return envObject.setLastError(1);
				var v8Script = emnapiCtx.handleStore.get(script);
				if (!v8Script.isString()) return envObject.setLastError(3);
				var ret = emnapiCtx.handleStore.get(5).value.eval(v8Script.value);
				result >>>= 0;
				value = envObject.ensureHandleId(ret);
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(result, value, true);
				status = envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
			return status;
		}
		var scriptMod = /*#__PURE__*/ Object.freeze({
			__proto__: null,
			napi_run_script
		});
		/** @__sig ippp */
		function napi_typeof(env, value, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!value) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			var v = emnapiCtx.handleStore.get(value);
			result >>>= 0;
			var r;
			if (v.isNumber()) r = 3;
			else if (v.isBigInt()) r = 9;
			else if (v.isString()) r = 4;
			else if (v.isFunction()) r = 7;
			else if (v.isExternal()) r = 8;
			else if (v.isObject()) r = 6;
			else if (v.isBoolean()) r = 2;
			else if (v.isUndefined()) r = 0;
			else if (v.isSymbol()) r = 5;
			else if (v.isNull()) r = 1;
			else return envObject.setLastError(1);
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setInt32(result, r, true);
			return envObject.clearLastError();
		}
		/** @__sig ippp */
		function napi_coerce_to_bool(env, value, result) {
			var v;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!value) return envObject.setLastError(1);
				if (!result) return envObject.setLastError(1);
				var handle = emnapiCtx.handleStore.get(value);
				result >>>= 0;
				v = handle.value ? 4 : 3;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(result, v, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ippp */
		function napi_coerce_to_number(env, value, result) {
			var v;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!value) return envObject.setLastError(1);
				if (!result) return envObject.setLastError(1);
				var handle = emnapiCtx.handleStore.get(value);
				if (handle.isBigInt()) throw new TypeError("Cannot convert a BigInt value to a number");
				result >>>= 0;
				v = emnapiCtx.addToCurrentScope(Number(handle.value)).id;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(result, v, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ippp */
		function napi_coerce_to_object(env, value, result) {
			var v;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!value) return envObject.setLastError(1);
				if (!result) return envObject.setLastError(1);
				var handle = emnapiCtx.handleStore.get(value);
				if (handle.value == null) throw new TypeError("Cannot convert undefined or null to object");
				result >>>= 0;
				v = envObject.ensureHandleId(Object(handle.value));
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(result, v, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ippp */
		function napi_coerce_to_string(env, value, result) {
			var v;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!value) return envObject.setLastError(1);
				if (!result) return envObject.setLastError(1);
				var handle = emnapiCtx.handleStore.get(value);
				if (handle.isSymbol()) throw new TypeError("Cannot convert a Symbol value to a string");
				result >>>= 0;
				v = emnapiCtx.addToCurrentScope(String(handle.value)).id;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setUint32(result, v, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ipppp */
		function napi_instanceof(env, object, constructor, result) {
			var r;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!object) return envObject.setLastError(1);
				if (!result) return envObject.setLastError(1);
				if (!constructor) return envObject.setLastError(1);
				result >>>= 0;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setInt8(result, 0, true);
				var ctor = emnapiCtx.handleStore.get(constructor);
				if (!ctor.isFunction()) return envObject.setLastError(5);
				r = emnapiCtx.handleStore.get(object).value instanceof ctor.value ? 1 : 0;
				GET_HEAP_DATA_VIEW().setInt8(result, r, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ippp */
		function napi_is_array(env, value, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!value) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			var h = emnapiCtx.handleStore.get(value);
			result >>>= 0;
			var r = h.isArray() ? 1 : 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setInt8(result, r, true);
			return envObject.clearLastError();
		}
		/** @__sig ippp */
		function napi_is_arraybuffer(env, value, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!value) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			var h = emnapiCtx.handleStore.get(value);
			result >>>= 0;
			var r = h.isArrayBuffer() ? 1 : 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setInt8(result, r, true);
			return envObject.clearLastError();
		}
		/** @__sig ippp */
		function node_api_is_sharedarraybuffer(env, value, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!value) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			var h = emnapiCtx.handleStore.get(value);
			result >>>= 0;
			var r = typeof SharedArrayBuffer === "function" && h.value instanceof SharedArrayBuffer || Object.prototype.toString.call(h.value) === "[object SharedArrayBuffer]" ? 1 : 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setInt8(result, r, true);
			return envObject.clearLastError();
		}
		/** @__sig ippp */
		function napi_is_date(env, value, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!value) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			var h = emnapiCtx.handleStore.get(value);
			result >>>= 0;
			var r = h.isDate() ? 1 : 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setInt8(result, r, true);
			return envObject.clearLastError();
		}
		/** @__sig ippp */
		function napi_is_error(env, value, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!value) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			var val = emnapiCtx.handleStore.get(value).value;
			result >>>= 0;
			var r = val instanceof Error ? 1 : 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setInt8(result, r, true);
			return envObject.clearLastError();
		}
		/** @__sig ippp */
		function napi_is_typedarray(env, value, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!value) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			var h = emnapiCtx.handleStore.get(value);
			result >>>= 0;
			var r = h.isTypedArray() ? 1 : 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setInt8(result, r, true);
			return envObject.clearLastError();
		}
		/** @__sig ippp */
		function napi_is_buffer(env, value, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!value) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			var h = emnapiCtx.handleStore.get(value);
			result >>>= 0;
			var r = h.isBuffer(emnapiCtx.feature.Buffer) ? 1 : 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setInt8(result, r, true);
			return envObject.clearLastError();
		}
		/** @__sig ippp */
		function napi_is_dataview(env, value, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!value) return envObject.setLastError(1);
			if (!result) return envObject.setLastError(1);
			var h = emnapiCtx.handleStore.get(value);
			result >>>= 0;
			var r = h.isDataView() ? 1 : 0;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setInt8(result, r, true);
			return envObject.clearLastError();
		}
		/** @__sig ipppp */
		function napi_strict_equals(env, lhs, rhs, result) {
			var r;
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!lhs) return envObject.setLastError(1);
				if (!rhs) return envObject.setLastError(1);
				if (!result) return envObject.setLastError(1);
				var lv = emnapiCtx.handleStore.get(lhs).value;
				var rv = emnapiCtx.handleStore.get(rhs).value;
				result >>>= 0;
				r = lv === rv ? 1 : 0;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				GET_HEAP_DATA_VIEW().setInt8(result, r, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		/** @__sig ipp */
		function napi_detach_arraybuffer(env, arraybuffer) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!arraybuffer) return envObject.setLastError(1);
			var value = emnapiCtx.handleStore.get(arraybuffer).value;
			if (!(value instanceof ArrayBuffer)) {
				if (typeof SharedArrayBuffer === "function" && value instanceof SharedArrayBuffer) return envObject.setLastError(20);
				return envObject.setLastError(19);
			}
			try {
				new emnapiCtx.feature.MessageChannel().port1.postMessage(value, [value]);
			} catch (_) {
				return envObject.setLastError(9);
			}
			return envObject.clearLastError();
		}
		/** @__sig ippp */
		function napi_is_detached_arraybuffer(env, arraybuffer, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			envObject.checkGCAccess();
			if (!envObject.tryCatch.isEmpty()) return envObject.setLastError(10);
			if (!envObject.canCallIntoJs()) return envObject.setLastError(envObject.moduleApiVersion >= 10 ? 23 : 10);
			envObject.clearLastError();
			try {
				if (!arraybuffer) return envObject.setLastError(1);
				if (!result) return envObject.setLastError(1);
				var h = emnapiCtx.handleStore.get(arraybuffer);
				result >>>= 0;
				var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
					return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
				};
				if (h.isArrayBuffer() && h.value.byteLength === 0) try {
					new Uint8Array(h.value);
				} catch (_) {
					GET_HEAP_DATA_VIEW().setInt8(result, 1, true);
					return envObject.getReturnStatus();
				}
				GET_HEAP_DATA_VIEW().setInt8(result, 0, true);
				return envObject.getReturnStatus();
			} catch (err) {
				envObject.tryCatch.setError(err);
				return envObject.setLastError(10);
			}
		}
		var valueOperationMod = /*#__PURE__*/ Object.freeze({
			__proto__: null,
			napi_coerce_to_bool,
			napi_coerce_to_number,
			napi_coerce_to_object,
			napi_coerce_to_string,
			napi_detach_arraybuffer,
			napi_instanceof,
			napi_is_array,
			napi_is_arraybuffer,
			napi_is_buffer,
			napi_is_dataview,
			napi_is_date,
			napi_is_detached_arraybuffer,
			napi_is_error,
			napi_is_typedarray,
			napi_strict_equals,
			napi_typeof,
			node_api_is_sharedarraybuffer
		});
		/** @__sig ipp */
		function napi_get_version(env, result) {
			if (!env) return 1;
			var envObject = emnapiCtx.envStore.get(env);
			if (!result) return envObject.setLastError(1);
			var NODE_API_SUPPORTED_VERSION_MAX = 10;
			var HEAP_DATA_VIEW = new DataView(wasmMemory.buffer), GET_HEAP_DATA_VIEW = function() {
				return HEAP_DATA_VIEW.buffer === wasmMemory.buffer ? HEAP_DATA_VIEW : HEAP_DATA_VIEW = new DataView(wasmMemory.buffer);
			};
			GET_HEAP_DATA_VIEW().setUint32(result, NODE_API_SUPPORTED_VERSION_MAX, true);
			return envObject.clearLastError();
		}
		var versionMod = /*#__PURE__*/ Object.freeze({
			__proto__: null,
			napi_get_version
		});
		emnapiAWST.init();
		emnapiExternalMemory.init();
		emnapiExternalSAB.init();
		emnapiString.init();
		emnapiTSFN.init();
		PThread.init();
		napiModule.emnapi.syncMemory = $emnapiSyncMemory;
		napiModule.emnapi.getMemoryAddress = $emnapiGetMemoryAddress;
		napiModule.emnapi.acquireExternalSharedArrayBuffer = $emnapiAcquireExternalSharedArrayBuffer;
		function addImports(mod) {
			var keys = Object.keys(mod);
			for (var i = 0; i < keys.length; ++i) {
				var k = keys[i];
				if (k.indexOf("$") === 0) continue;
				if (k.indexOf("emnapi_") === 0) napiModule.imports.emnapi[k] = mod[k];
				else if (k.indexOf("_emnapi_") === 0 || k === "napi_set_last_error" || k === "napi_clear_last_error") napiModule.imports.env[k] = mod[k];
				else napiModule.imports.napi[k] = mod[k];
			}
		}
		addImports(asyncMod);
		addImports(memoryMod);
		addImports(asyncWorkMod);
		addImports(utilMod);
		addImports(convert2cMod);
		addImports(convert2napiMod);
		addImports(createMod);
		addImports(globalMod);
		addImports(wrapMod);
		addImports(envMod);
		addImports(emnapiMod);
		addImports(errorMod);
		addImports(functionMod);
		addImports(lifeMod);
		addImports(miscellaneousMod);
		addImports(nodeMod);
		addImports(promiseMod);
		addImports(propertyMod);
		addImports(scriptMod);
		addImports(valueOperationMod);
		addImports(versionMod);
		napiModule.imports.napi.napi_create_threadsafe_function = napi_create_threadsafe_function;
		napiModule.imports.napi.napi_get_threadsafe_function_context = napi_get_threadsafe_function_context;
		napiModule.imports.napi.napi_call_threadsafe_function = napi_call_threadsafe_function;
		napiModule.imports.napi.napi_acquire_threadsafe_function = napi_acquire_threadsafe_function;
		napiModule.imports.napi.napi_release_threadsafe_function = napi_release_threadsafe_function;
		napiModule.imports.napi.napi_unref_threadsafe_function = napi_unref_threadsafe_function;
		napiModule.imports.napi.napi_ref_threadsafe_function = napi_ref_threadsafe_function;
		return napiModule;
	})();
}
function loadNapiModuleImpl(loadFn, userNapiModule, wasmInput, options) {
	options = options !== null && options !== void 0 ? options : {};
	var getMemory = options.getMemory;
	var getTable = options.getTable;
	var beforeInit = options.beforeInit;
	if (getMemory != null && typeof getMemory !== "function") throw new TypeError("options.getMemory is not a function");
	if (getTable != null && typeof getTable !== "function") throw new TypeError("options.getTable is not a function");
	if (beforeInit != null && typeof beforeInit !== "function") throw new TypeError("options.beforeInit is not a function");
	var napiModule;
	var isLoad = typeof userNapiModule === "object" && userNapiModule !== null;
	if (isLoad) {
		if (userNapiModule.loaded) throw new Error("napiModule has already loaded");
		napiModule = userNapiModule;
	} else napiModule = createNapiModule(options);
	var wasi = options.wasi;
	var wasiThreads;
	var importObject = {
		env: napiModule.imports.env,
		napi: napiModule.imports.napi,
		emnapi: napiModule.imports.emnapi
	};
	if (wasi) {
		wasiThreads = new WASIThreads(napiModule.childThread ? {
			wasi,
			childThread: true,
			postMessage: napiModule.postMessage
		} : {
			wasi,
			threadManager: napiModule.PThread,
			waitThreadStart: napiModule.waitThreadStart
		});
		Object.assign(importObject, typeof wasi.getImportObject === "function" ? wasi.getImportObject() : { wasi_snapshot_preview1: wasi.wasiImport });
		Object.assign(importObject, wasiThreads.getImportObject());
	}
	var overwriteImports = options.overwriteImports;
	if (typeof overwriteImports === "function") {
		var newImportObject = overwriteImports(importObject);
		if (typeof newImportObject === "object" && newImportObject !== null) importObject = newImportObject;
	}
	return loadFn(wasmInput, importObject, function(err, source) {
		if (err) throw err;
		var originalInstance = source.instance;
		var instance = originalInstance;
		var originalExports = originalInstance.exports;
		var exportMemory = "memory" in originalExports;
		var importMemory = "memory" in importObject.env;
		var memory = getMemory ? getMemory(originalExports) : exportMemory ? originalExports.memory : importMemory ? importObject.env.memory : void 0;
		if (!memory) throw new Error("memory is neither exported nor imported");
		var table = getTable ? getTable(originalExports) : originalExports.__indirect_function_table;
		if (wasi && !exportMemory) {
			var exports_1 = Object.create(null);
			Object.assign(exports_1, originalExports, { memory });
			instance = { exports: exports_1 };
		}
		var module = source.module;
		if (wasi) instance = wasiThreads.initialize(instance, module, memory);
		else napiModule.PThread.setup(module, memory);
		var emnapiInit = function() {
			if (beforeInit) beforeInit({
				instance: originalInstance,
				module
			});
			napiModule.init({
				instance,
				module,
				memory,
				table
			});
			var ret = {
				instance: originalInstance,
				module,
				usedInstance: instance
			};
			if (!isLoad) ret.napiModule = napiModule;
			return ret;
		};
		if (napiModule.PThread.shouldPreloadWorkers()) {
			var poolReady = napiModule.PThread.loadWasmModuleToAllWorkers();
			if (loadFn === loadCallback) return poolReady.then(emnapiInit);
			else throw new Error("Synchronous loading is not supported with worker pool (reuseWorker.size > 0)");
		}
		return emnapiInit();
	});
}
function loadCallback(wasmInput, importObject, callback) {
	return load(wasmInput, importObject).then(function(source) {
		return callback(null, source);
	}, function(err) {
		return callback(err);
	});
}
function loadSyncCallback(wasmInput, importObject, callback) {
	var source;
	try {
		source = loadSync(wasmInput, importObject);
	} catch (err) {
		return callback(err);
	}
	return callback(null, source);
}
/** @public */
function loadNapiModule(napiModule, wasmInput, options) {
	if (typeof napiModule !== "object" || napiModule === null) throw new TypeError("Invalid napiModule");
	return loadNapiModuleImpl(loadCallback, napiModule, wasmInput, options);
}
/** @public */
function loadNapiModuleSync(napiModule, wasmInput, options) {
	if (typeof napiModule !== "object" || napiModule === null) throw new TypeError("Invalid napiModule");
	return loadNapiModuleImpl(loadSyncCallback, napiModule, wasmInput, options);
}
/** @public */
function instantiateNapiModule$1(wasmInput, options) {
	return loadNapiModuleImpl(loadCallback, void 0, wasmInput, options);
}
/** @public */
function instantiateNapiModuleSync$1(wasmInput, options) {
	return loadNapiModuleImpl(loadSyncCallback, void 0, wasmInput, options);
}
/** @public */
var MessageHandler$1 = /* @__PURE__ */ function(_super) {
	__extends(MessageHandler, _super);
	function MessageHandler(options) {
		var _this = this;
		if (typeof options.onLoad !== "function") throw new TypeError("options.onLoad is not a function");
		var userOnError = options.onError;
		_this = _super.call(this, __assign(__assign({}, options), { onError: function(err, type) {
			var _a;
			var emnapi_thread_crashed = (_a = _this.instance) === null || _a === void 0 ? void 0 : _a.exports.emnapi_thread_crashed;
			if (typeof emnapi_thread_crashed === "function") emnapi_thread_crashed();
			if (typeof userOnError === "function") userOnError(err, type);
			else throw err;
		} })) || this;
		_this.napiModule = void 0;
		return _this;
	}
	MessageHandler.prototype.instantiate = function(data) {
		var _this = this;
		var source = this.onLoad(data);
		if (typeof source.then === "function") return source.then(function(result) {
			_this.napiModule = result.napiModule;
			return result;
		});
		this.napiModule = source.napiModule;
		return source;
	};
	MessageHandler.prototype.handle = function(e) {
		var _this = this;
		var _a;
		_super.prototype.handle.call(this, e);
		if ((_a = e === null || e === void 0 ? void 0 : e.data) === null || _a === void 0 ? void 0 : _a.__emnapi__) {
			var type = e.data.__emnapi__.type;
			var payload_1 = e.data.__emnapi__.payload;
			try {
				if (type === "async-worker-init") this.handleAfterLoad(e, function() {
					_this.napiModule.initWorker(payload_1.arg, payload_1.func);
				});
			} catch (err) {
				this.onError(err, type);
			}
		}
	};
	return MessageHandler;
}(ThreadMessageHandler);
var version$1 = "1.11.3";
// node_modules/@emnapi/runtime/dist/emnapi.esm-bundler.js
var emnapi_esm_bundler_exports = /* @__PURE__ */ __exportAll({
	ConstHandle: () => ConstHandle,
	Context: () => Context,
	Deferred: () => Deferred,
	EmnapiError: () => EmnapiError,
	Env: () => Env,
	External: () => External,
	Finalizer: () => Finalizer,
	Handle: () => Handle,
	HandleScope: () => HandleScope,
	HandleStore: () => HandleStore,
	NAPI_VERSION_EXPERIMENTAL: () => NAPI_VERSION_EXPERIMENTAL,
	NODE_API_DEFAULT_MODULE_API_VERSION: () => 8,
	NODE_API_SUPPORTED_VERSION_MAX: () => 10,
	NODE_API_SUPPORTED_VERSION_MIN: () => 1,
	NodeEnv: () => NodeEnv,
	NotSupportBufferError: () => NotSupportBufferError,
	NotSupportWeakRefError: () => NotSupportWeakRefError,
	Persistent: () => Persistent,
	RefTracker: () => RefTracker,
	Reference: () => Reference,
	ReferenceOwnership: () => ReferenceOwnership,
	ReferenceWithData: () => ReferenceWithData,
	ReferenceWithFinalizer: () => ReferenceWithFinalizer,
	ScopeStore: () => ScopeStore,
	Store: () => Store,
	TrackedFinalizer: () => TrackedFinalizer,
	TryCatch: () => TryCatch,
	createContext: () => createContext$1,
	getDefaultContext: () => getDefaultContext$1,
	getExternalValue: () => getExternalValue,
	isExternal: () => isExternal,
	isReferenceType: () => isReferenceType,
	version: () => version
});
var externalValue = /* @__PURE__ */ new WeakMap();
/** @public */
function isExternal(object) {
	return externalValue.has(object);
}
/** @public */ var External = (function() {
	function External(value) {
		Object.setPrototypeOf(this, null);
		externalValue.set(this, value);
	}
	External.prototype = null;
	return External;
})();
/** @public */
function getExternalValue(external) {
	if (!isExternal(external)) throw new TypeError("not external");
	return externalValue.get(external);
}
var supportNewFunction = /*#__PURE__*/ (function() {
	var f;
	try {
		f = new Function();
	} catch (_) {
		return false;
	}
	return typeof f === "function";
})();
var _global = /*#__PURE__*/ (function() {
	if (typeof globalThis !== "undefined") return globalThis;
	var g = (function() {
		return this;
	})();
	if (!g && supportNewFunction) try {
		g = new Function("return this")();
	} catch (_) {}
	if (!g) {
		if (typeof __webpack_public_path__ === "undefined") {
			if (typeof global !== "undefined") return global;
		}
		if (typeof window !== "undefined") return window;
		if (typeof self !== "undefined") return self;
	}
	return g;
})();
var TryCatch = /* @__PURE__ */ function() {
	function TryCatch() {
		this._exception = void 0;
		this._caught = false;
	}
	TryCatch.prototype.isEmpty = function() {
		return !this._caught;
	};
	TryCatch.prototype.hasCaught = function() {
		return this._caught;
	};
	TryCatch.prototype.exception = function() {
		return this._exception;
	};
	TryCatch.prototype.setError = function(err) {
		this._caught = true;
		this._exception = err;
	};
	TryCatch.prototype.reset = function() {
		this._caught = false;
		this._exception = void 0;
	};
	TryCatch.prototype.extractException = function() {
		var e = this._exception;
		this.reset();
		return e;
	};
	return TryCatch;
}();
var canSetFunctionName = /*#__PURE__*/ (function() {
	var _a;
	try {
		return Boolean((_a = Object.getOwnPropertyDescriptor(Function.prototype, "name")) === null || _a === void 0 ? void 0 : _a.configurable);
	} catch (_) {
		return false;
	}
})();
var supportReflect = typeof Reflect === "object";
var supportFinalizer = typeof FinalizationRegistry !== "undefined" && typeof WeakRef !== "undefined";
var supportWeakSymbol = /*#__PURE__*/ (function() {
	try {
		var sym = Symbol();
		new WeakRef(sym);
		(/* @__PURE__ */ new WeakMap()).set(sym, void 0);
	} catch (_) {
		return false;
	}
	return true;
})();
var supportBigInt = typeof BigInt !== "undefined";
function isReferenceType(v) {
	return typeof v === "object" && v !== null || typeof v === "function";
}
var _require = /*#__PURE__*/ (function() {
	var nativeRequire;
	if (typeof __webpack_public_path__ !== "undefined") nativeRequire = (function() {
		return typeof __non_webpack_require__ !== "undefined" ? __non_webpack_require__ : void 0;
	})();
	else nativeRequire = (function() {
		return typeof __webpack_public_path__ !== "undefined" ? typeof __non_webpack_require__ !== "undefined" ? __non_webpack_require__ : void 0 : typeof __require !== "undefined" ? __require : void 0;
	})();
	return nativeRequire;
})();
var _MessageChannel = typeof MessageChannel === "function" ? MessageChannel : /*#__PURE__*/ (function() {
	try {
		return _require("worker_threads").MessageChannel;
	} catch (_) {}
})();
var _setImmediate = typeof setImmediate === "function" ? setImmediate.bind(_global) : function(callback) {
	if (typeof callback !== "function") throw new TypeError("The \"callback\" argument must be of type function");
	if (_MessageChannel) {
		var channel_1 = new _MessageChannel();
		channel_1.port1.onmessage = function() {
			channel_1.port1.onmessage = null;
			channel_1 = void 0;
			callback();
		};
		channel_1.port2.postMessage(null);
	} else setTimeout(callback, 0);
};
var _Buffer = typeof Buffer === "function" ? Buffer : /*#__PURE__*/ (function() {
	try {
		return _require("buffer").Buffer;
	} catch (_) {}
})();
var version = "1.11.3";
var NAPI_VERSION_EXPERIMENTAL = 0x7fffffff;
var Handle = /* @__PURE__ */ function() {
	function Handle(id, value) {
		this.id = id;
		this.value = value;
	}
	Handle.prototype.data = function() {
		return getExternalValue(this.value);
	};
	Handle.prototype.isNumber = function() {
		return typeof this.value === "number";
	};
	Handle.prototype.isBigInt = function() {
		return typeof this.value === "bigint";
	};
	Handle.prototype.isString = function() {
		return typeof this.value === "string";
	};
	Handle.prototype.isFunction = function() {
		return typeof this.value === "function";
	};
	Handle.prototype.isExternal = function() {
		return isExternal(this.value);
	};
	Handle.prototype.isObject = function() {
		return typeof this.value === "object" && this.value !== null;
	};
	Handle.prototype.isArray = function() {
		return Array.isArray(this.value);
	};
	Handle.prototype.isArrayBuffer = function() {
		return this.value instanceof ArrayBuffer;
	};
	Handle.prototype.isTypedArray = function() {
		return ArrayBuffer.isView(this.value) && !(this.value instanceof DataView);
	};
	Handle.prototype.isBuffer = function(BufferConstructor) {
		if (ArrayBuffer.isView(this.value)) return true;
		BufferConstructor !== null && BufferConstructor !== void 0 || (BufferConstructor = _Buffer);
		return typeof BufferConstructor === "function" && BufferConstructor.isBuffer(this.value);
	};
	Handle.prototype.isDataView = function() {
		return this.value instanceof DataView;
	};
	Handle.prototype.isDate = function() {
		return this.value instanceof Date;
	};
	Handle.prototype.isPromise = function() {
		return this.value instanceof Promise;
	};
	Handle.prototype.isBoolean = function() {
		return typeof this.value === "boolean";
	};
	Handle.prototype.isUndefined = function() {
		return this.value === void 0;
	};
	Handle.prototype.isSymbol = function() {
		return typeof this.value === "symbol";
	};
	Handle.prototype.isNull = function() {
		return this.value === null;
	};
	Handle.prototype.dispose = function() {
		this.value = void 0;
	};
	return Handle;
}();
var ConstHandle = /* @__PURE__ */ function(_super) {
	__extends(ConstHandle, _super);
	function ConstHandle(id, value) {
		return _super.call(this, id, value) || this;
	}
	ConstHandle.prototype.dispose = function() {};
	return ConstHandle;
}(Handle);
var HandleStore = /* @__PURE__ */ function() {
	function HandleStore() {
		this._values = [
			void 0,
			HandleStore.UNDEFINED,
			HandleStore.NULL,
			HandleStore.FALSE,
			HandleStore.TRUE,
			HandleStore.GLOBAL
		];
		this._next = HandleStore.MIN_ID;
	}
	HandleStore.prototype.push = function(value) {
		var h;
		var next = this._next;
		var values = this._values;
		if (next < values.length) {
			h = values[next];
			h.value = value;
		} else {
			h = new Handle(next, value);
			values[next] = h;
		}
		this._next++;
		return h;
	};
	HandleStore.prototype.erase = function(start, end) {
		this._next = start;
		var values = this._values;
		for (var i = start; i < end; ++i) values[i].dispose();
	};
	HandleStore.prototype.get = function(id) {
		return this._values[id];
	};
	HandleStore.prototype.swap = function(a, b) {
		var values = this._values;
		var h = values[a];
		values[a] = values[b];
		values[a].id = Number(a);
		values[b] = h;
		h.id = Number(b);
	};
	HandleStore.prototype.dispose = function() {
		this._values.length = HandleStore.MIN_ID;
		this._next = HandleStore.MIN_ID;
	};
	HandleStore.UNDEFINED = new ConstHandle(1, void 0);
	HandleStore.NULL = new ConstHandle(2, null);
	HandleStore.FALSE = new ConstHandle(3, false);
	HandleStore.TRUE = new ConstHandle(4, true);
	HandleStore.GLOBAL = new ConstHandle(5, _global);
	HandleStore.MIN_ID = 6;
	return HandleStore;
}();
var HandleScope = /* @__PURE__ */ function() {
	function HandleScope(handleStore, id, parentScope, start, end) {
		if (end === void 0) end = start;
		this.handleStore = handleStore;
		this.id = id;
		this.parent = parentScope;
		this.child = null;
		if (parentScope !== null) parentScope.child = this;
		this.start = start;
		this.end = end;
		this._escapeCalled = false;
		this.callbackInfo = {
			thiz: void 0,
			data: 0,
			args: void 0,
			fn: void 0
		};
	}
	HandleScope.prototype.add = function(value) {
		var h = this.handleStore.push(value);
		this.end++;
		return h;
	};
	HandleScope.prototype.addExternal = function(data) {
		return this.add(new External(data));
	};
	HandleScope.prototype.dispose = function() {
		if (this._escapeCalled) this._escapeCalled = false;
		if (this.start === this.end) return;
		this.handleStore.erase(this.start, this.end);
	};
	HandleScope.prototype.escape = function(handle) {
		if (this._escapeCalled) return null;
		this._escapeCalled = true;
		if (handle < this.start || handle >= this.end) return null;
		this.handleStore.swap(handle, this.start);
		var h = this.handleStore.get(this.start);
		this.start++;
		this.parent.end++;
		return h;
	};
	HandleScope.prototype.escapeCalled = function() {
		return this._escapeCalled;
	};
	return HandleScope;
}();
var ScopeStore = /* @__PURE__ */ function() {
	function ScopeStore() {
		this._rootScope = new HandleScope(null, 0, null, 1, HandleStore.MIN_ID);
		this.currentScope = this._rootScope;
		this._values = [void 0];
	}
	ScopeStore.prototype.get = function(id) {
		return this._values[id];
	};
	ScopeStore.prototype.openScope = function(handleStore) {
		var currentScope = this.currentScope;
		var scope = currentScope.child;
		if (scope !== null) scope.start = scope.end = currentScope.end;
		else {
			var id = currentScope.id + 1;
			scope = new HandleScope(handleStore, id, currentScope, currentScope.end);
			this._values[id] = scope;
		}
		this.currentScope = scope;
		return scope;
	};
	ScopeStore.prototype.closeScope = function() {
		var scope = this.currentScope;
		this.currentScope = scope.parent;
		scope.dispose();
	};
	ScopeStore.prototype.dispose = function() {
		this.currentScope = this._rootScope;
		this._values.length = 1;
	};
	return ScopeStore;
}();
var RefTracker = /* @__PURE__ */ function() {
	function RefTracker() {
		this._next = null;
		this._prev = null;
	}
	/** @virtual */
	RefTracker.prototype.dispose = function() {};
	/** @virtual */
	RefTracker.prototype.finalize = function() {};
	RefTracker.prototype.link = function(list) {
		this._prev = list;
		this._next = list._next;
		if (this._next !== null) this._next._prev = this;
		list._next = this;
	};
	RefTracker.prototype.unlink = function() {
		if (this._prev !== null) this._prev._next = this._next;
		if (this._next !== null) this._next._prev = this._prev;
		this._prev = null;
		this._next = null;
	};
	RefTracker.finalizeAll = function(list) {
		while (list._next !== null) list._next.finalize();
	};
	return RefTracker;
}();
var Finalizer = /* @__PURE__ */ function() {
	function Finalizer(envObject, _finalizeCallback, _finalizeData, _finalizeHint) {
		if (_finalizeCallback === void 0) _finalizeCallback = 0;
		if (_finalizeData === void 0) _finalizeData = 0;
		if (_finalizeHint === void 0) _finalizeHint = 0;
		this.envObject = envObject;
		this._finalizeCallback = _finalizeCallback;
		this._finalizeData = _finalizeData;
		this._finalizeHint = _finalizeHint;
		this._makeDynCall_vppp = envObject.makeDynCall_vppp;
	}
	Finalizer.prototype.callback = function() {
		return this._finalizeCallback;
	};
	Finalizer.prototype.data = function() {
		return this._finalizeData;
	};
	Finalizer.prototype.hint = function() {
		return this._finalizeHint;
	};
	Finalizer.prototype.resetEnv = function() {
		this.envObject = void 0;
	};
	Finalizer.prototype.resetFinalizer = function() {
		this._finalizeCallback = 0;
		this._finalizeData = 0;
		this._finalizeHint = 0;
	};
	Finalizer.prototype.callFinalizer = function() {
		var finalize_callback = this._finalizeCallback;
		var finalize_data = this._finalizeData;
		var finalize_hint = this._finalizeHint;
		this.resetFinalizer();
		if (!finalize_callback) return;
		var fini = Number(finalize_callback);
		if (!this.envObject) this._makeDynCall_vppp(fini)(0, finalize_data, finalize_hint);
		else this.envObject.callFinalizer(fini, finalize_data, finalize_hint);
	};
	Finalizer.prototype.dispose = function() {
		this.envObject = void 0;
		this._makeDynCall_vppp = void 0;
	};
	return Finalizer;
}();
var TrackedFinalizer = /* @__PURE__ */ function(_super) {
	__extends(TrackedFinalizer, _super);
	function TrackedFinalizer(envObject, finalize_callback, finalize_data, finalize_hint) {
		var _this = _super.call(this) || this;
		_this._finalizer = new Finalizer(envObject, finalize_callback, finalize_data, finalize_hint);
		return _this;
	}
	TrackedFinalizer.create = function(envObject, finalize_callback, finalize_data, finalize_hint) {
		var finalizer = new TrackedFinalizer(envObject, finalize_callback, finalize_data, finalize_hint);
		finalizer.link(envObject.finalizing_reflist);
		return finalizer;
	};
	TrackedFinalizer.prototype.data = function() {
		return this._finalizer.data();
	};
	TrackedFinalizer.prototype.dispose = function() {
		if (!this._finalizer) return;
		this.unlink();
		this._finalizer.envObject.dequeueFinalizer(this);
		this._finalizer.dispose();
		this._finalizer = void 0;
		_super.prototype.dispose.call(this);
	};
	TrackedFinalizer.prototype.finalize = function() {
		this.unlink();
		var error;
		var caught = false;
		try {
			this._finalizer.callFinalizer();
		} catch (err) {
			caught = true;
			error = err;
		}
		this.dispose();
		if (caught) throw error;
	};
	return TrackedFinalizer;
}(RefTracker);
function throwNodeApiVersionError(moduleName, moduleApiVersion) {
	var errorMessage = "".concat(moduleName, " requires Node-API version ").concat(moduleApiVersion, ", but this version of Node.js only supports version ").concat(10, " add-ons.");
	throw new Error(errorMessage);
}
function handleThrow(envObject, value) {
	if (envObject.terminatedOrTerminating()) return;
	throw value;
}
var Env = /* @__PURE__ */ function() {
	function Env(ctx, moduleApiVersion, makeDynCall_vppp, makeDynCall_vp, abort) {
		this.ctx = ctx;
		this.moduleApiVersion = moduleApiVersion;
		this.makeDynCall_vppp = makeDynCall_vppp;
		this.makeDynCall_vp = makeDynCall_vp;
		this.abort = abort;
		this.openHandleScopes = 0;
		this.instanceData = null;
		this.tryCatch = new TryCatch();
		this.refs = 1;
		this.reflist = new RefTracker();
		this.finalizing_reflist = new RefTracker();
		this.pendingFinalizers = [];
		this.lastError = {
			errorCode: 0,
			engineErrorCode: 0,
			engineReserved: 0
		};
		this.inGcFinalizer = false;
		this._bindingMap = /* @__PURE__ */ new WeakMap();
		this.id = 0;
	}
	/** @virtual */
	Env.prototype.canCallIntoJs = function() {
		return true;
	};
	Env.prototype.terminatedOrTerminating = function() {
		return !this.canCallIntoJs();
	};
	Env.prototype.ref = function() {
		this.refs++;
	};
	Env.prototype.unref = function() {
		this.refs--;
		if (this.refs === 0) this.dispose();
	};
	Env.prototype.ensureHandle = function(value) {
		return this.ctx.ensureHandle(value);
	};
	Env.prototype.ensureHandleId = function(value) {
		return this.ensureHandle(value).id;
	};
	Env.prototype.clearLastError = function() {
		var lastError = this.lastError;
		if (lastError.errorCode !== 0) lastError.errorCode = 0;
		if (lastError.engineErrorCode !== 0) lastError.engineErrorCode = 0;
		if (lastError.engineReserved !== 0) lastError.engineReserved = 0;
		return 0;
	};
	Env.prototype.setLastError = function(error_code, engine_error_code, engine_reserved) {
		if (engine_error_code === void 0) engine_error_code = 0;
		if (engine_reserved === void 0) engine_reserved = 0;
		var lastError = this.lastError;
		if (lastError.errorCode !== error_code) lastError.errorCode = error_code;
		if (lastError.engineErrorCode !== engine_error_code) lastError.engineErrorCode = engine_error_code;
		if (lastError.engineReserved !== engine_reserved) lastError.engineReserved = engine_reserved;
		return error_code;
	};
	Env.prototype.getReturnStatus = function() {
		return !this.tryCatch.hasCaught() ? 0 : this.setLastError(10);
	};
	Env.prototype.callIntoModule = function(fn, handleException) {
		if (handleException === void 0) handleException = handleThrow;
		var openHandleScopesBefore = this.openHandleScopes;
		this.clearLastError();
		var r = fn(this);
		if (openHandleScopesBefore !== this.openHandleScopes) this.abort("open_handle_scopes != open_handle_scopes_before");
		if (this.tryCatch.hasCaught()) {
			var err = this.tryCatch.extractException();
			handleException(this, err);
		}
		return r;
	};
	Env.prototype.invokeFinalizerFromGC = function(finalizer) {
		if (this.moduleApiVersion !== 0x7fffffff) this.enqueueFinalizer(finalizer);
		else {
			var saved = this.inGcFinalizer;
			this.inGcFinalizer = true;
			try {
				finalizer.finalize();
			} finally {
				this.inGcFinalizer = saved;
			}
		}
	};
	Env.prototype.checkGCAccess = function() {
		if (this.moduleApiVersion === 0x7fffffff && this.inGcFinalizer) this.abort("Finalizer is calling a function that may affect GC state.\nThe finalizers are run directly from GC and must not affect GC state.\nUse `node_api_post_finalizer` from inside of the finalizer to work around this issue.\nIt schedules the call as a new task in the event loop.");
	};
	/** @virtual */
	Env.prototype.enqueueFinalizer = function(finalizer) {
		if (this.pendingFinalizers.indexOf(finalizer) === -1) this.pendingFinalizers.push(finalizer);
	};
	/** @virtual */
	Env.prototype.dequeueFinalizer = function(finalizer) {
		var index = this.pendingFinalizers.indexOf(finalizer);
		if (index !== -1) this.pendingFinalizers.splice(index, 1);
	};
	/** @virtual */
	Env.prototype.deleteMe = function() {
		RefTracker.finalizeAll(this.finalizing_reflist);
		RefTracker.finalizeAll(this.reflist);
		this.tryCatch.extractException();
		this.ctx.envStore.remove(this.id);
	};
	Env.prototype.dispose = function() {
		if (this.id === 0) return;
		this.deleteMe();
		this.finalizing_reflist.dispose();
		this.reflist.dispose();
		this.id = 0;
	};
	Env.prototype.initObjectBinding = function(value) {
		var binding = {
			wrapped: 0,
			tag: null
		};
		this._bindingMap.set(value, binding);
		return binding;
	};
	Env.prototype.getObjectBinding = function(value) {
		if (this._bindingMap.has(value)) return this._bindingMap.get(value);
		return this.initObjectBinding(value);
	};
	Env.prototype.setInstanceData = function(data, finalize_cb, finalize_hint) {
		if (this.instanceData) this.instanceData.dispose();
		this.instanceData = TrackedFinalizer.create(this, finalize_cb, data, finalize_hint);
	};
	Env.prototype.getInstanceData = function() {
		return this.instanceData ? this.instanceData.data() : 0;
	};
	return Env;
}();
var NodeEnv = /* @__PURE__ */ function(_super) {
	__extends(NodeEnv, _super);
	function NodeEnv(ctx, filename, moduleApiVersion, makeDynCall_vppp, makeDynCall_vp, abort, nodeBinding) {
		var _this = _super.call(this, ctx, moduleApiVersion, makeDynCall_vppp, makeDynCall_vp, abort) || this;
		_this.filename = filename;
		_this.nodeBinding = nodeBinding;
		_this.destructing = false;
		_this.finalizationScheduled = false;
		return _this;
	}
	NodeEnv.prototype.deleteMe = function() {
		this.destructing = true;
		this.drainFinalizerQueue();
		_super.prototype.deleteMe.call(this);
	};
	NodeEnv.prototype.canCallIntoJs = function() {
		return _super.prototype.canCallIntoJs.call(this) && this.ctx.canCallIntoJs();
	};
	NodeEnv.prototype.triggerFatalException = function(err) {
		if (this.nodeBinding) this.nodeBinding.napi.fatalException(err);
		else if (typeof process === "object" && process !== null && typeof process._fatalException === "function") {
			if (!process._fatalException(err)) {
				console.error(err);
				process.exit(1);
			}
		} else throw err;
	};
	NodeEnv.prototype.callbackIntoModule = function(enforceUncaughtExceptionPolicy, fn) {
		return this.callIntoModule(fn, function(envObject, err) {
			if (envObject.terminatedOrTerminating()) return;
			var hasProcess = typeof process === "object" && process !== null;
			var hasForceFlag = hasProcess ? Boolean(process.execArgv && process.execArgv.indexOf("--force-node-api-uncaught-exceptions-policy") !== -1) : false;
			if (envObject.moduleApiVersion < 10 && !hasForceFlag && !enforceUncaughtExceptionPolicy) {
				(hasProcess && typeof process.emitWarning === "function" ? process.emitWarning : function(warning, type, code) {
					if (warning instanceof Error) console.warn(warning.toString());
					else {
						var prefix = code ? "[".concat(code, "] ") : "";
						console.warn("".concat(prefix).concat(type || "Warning", ": ").concat(warning));
					}
				})("Uncaught Node-API callback exception detected, please run node with option --force-node-api-uncaught-exceptions-policy=true to handle those exceptions properly.", "DeprecationWarning", "DEP0168");
				return;
			}
			envObject.triggerFatalException(err);
		});
	};
	NodeEnv.prototype.callFinalizer = function(cb, data, hint) {
		this.callFinalizerInternal(1, cb, data, hint);
	};
	NodeEnv.prototype.callFinalizerInternal = function(forceUncaught, cb, data, hint) {
		var f = this.makeDynCall_vppp(cb);
		var env = this.id;
		var scope = this.ctx.openScope(this);
		try {
			this.callbackIntoModule(Boolean(forceUncaught), function() {
				f(env, data, hint);
			});
		} finally {
			this.ctx.closeScope(this, scope);
		}
	};
	NodeEnv.prototype.enqueueFinalizer = function(finalizer) {
		var _this = this;
		_super.prototype.enqueueFinalizer.call(this, finalizer);
		if (!this.finalizationScheduled && !this.destructing) {
			this.finalizationScheduled = true;
			this.ref();
			_setImmediate(function() {
				_this.finalizationScheduled = false;
				_this.unref();
				_this.drainFinalizerQueue();
			});
		}
	};
	NodeEnv.prototype.drainFinalizerQueue = function() {
		while (this.pendingFinalizers.length > 0) this.pendingFinalizers.shift().finalize();
	};
	return NodeEnv;
}(Env);
function newEnv(ctx, filename, moduleApiVersion, makeDynCall_vppp, makeDynCall_vp, abort, nodeBinding) {
	moduleApiVersion = typeof moduleApiVersion !== "number" ? 8 : moduleApiVersion;
	if (moduleApiVersion < 8) moduleApiVersion = 8;
	else if (moduleApiVersion > 10 && moduleApiVersion !== 0x7fffffff) throwNodeApiVersionError(filename, moduleApiVersion);
	var env = new NodeEnv(ctx, filename, moduleApiVersion, makeDynCall_vppp, makeDynCall_vp, abort, nodeBinding);
	ctx.envStore.add(env);
	ctx.addCleanupHook(env, function() {
		env.unref();
	}, 0);
	return env;
}
var EmnapiError = /* @__PURE__ */ function(_super) {
	__extends(EmnapiError, _super);
	function EmnapiError(message) {
		var _newTarget = this.constructor;
		var _this = _super.call(this, message) || this;
		var ErrorConstructor = _newTarget;
		var proto = ErrorConstructor.prototype;
		if (!(_this instanceof EmnapiError)) {
			var setPrototypeOf = Object.setPrototypeOf;
			if (typeof setPrototypeOf === "function") setPrototypeOf.call(Object, _this, proto);
			else _this.__proto__ = proto;
			if (typeof Error.captureStackTrace === "function") Error.captureStackTrace(_this, ErrorConstructor);
		}
		return _this;
	}
	return EmnapiError;
}(Error);
Object.defineProperty(EmnapiError.prototype, "name", {
	configurable: true,
	writable: true,
	value: "EmnapiError"
});
var NotSupportWeakRefError = /* @__PURE__ */ function(_super) {
	__extends(NotSupportWeakRefError, _super);
	function NotSupportWeakRefError(api, message) {
		return _super.call(this, "".concat(api, ": The current runtime does not support \"FinalizationRegistry\" and \"WeakRef\".").concat(message ? " ".concat(message) : "")) || this;
	}
	return NotSupportWeakRefError;
}(EmnapiError);
Object.defineProperty(NotSupportWeakRefError.prototype, "name", {
	configurable: true,
	writable: true,
	value: "NotSupportWeakRefError"
});
var NotSupportBufferError = /* @__PURE__ */ function(_super) {
	__extends(NotSupportBufferError, _super);
	function NotSupportBufferError(api, message) {
		return _super.call(this, "".concat(api, ": The current runtime does not support \"Buffer\". Consider using buffer polyfill to make sure `globalThis.Buffer` is defined.").concat(message ? " ".concat(message) : "")) || this;
	}
	return NotSupportBufferError;
}(EmnapiError);
Object.defineProperty(NotSupportBufferError.prototype, "name", {
	configurable: true,
	writable: true,
	value: "NotSupportBufferError"
});
var StrongRef = /* @__PURE__ */ function() {
	function StrongRef(value) {
		this._value = value;
	}
	StrongRef.prototype.deref = function() {
		return this._value;
	};
	StrongRef.prototype.dispose = function() {
		this._value = void 0;
	};
	return StrongRef;
}();
var Persistent = /* @__PURE__ */ function() {
	function Persistent(value) {
		this._ref = new StrongRef(value);
	}
	Persistent.prototype.setWeak = function(param, callback) {
		if (!supportFinalizer || this._ref === void 0 || this._ref instanceof WeakRef) return;
		var value = this._ref.deref();
		try {
			Persistent._registry.register(value, this, this);
			var weakRef = new WeakRef(value);
			this._ref.dispose();
			this._ref = weakRef;
			this._param = param;
			this._callback = callback;
		} catch (err) {
			if (typeof value === "symbol");
			else throw err;
		}
	};
	Persistent.prototype.clearWeak = function() {
		if (!supportFinalizer || this._ref === void 0) return;
		if (this._ref instanceof WeakRef) {
			try {
				Persistent._registry.unregister(this);
			} catch (_) {}
			this._param = void 0;
			this._callback = void 0;
			var value = this._ref.deref();
			if (value === void 0) this._ref = value;
			else this._ref = new StrongRef(value);
		}
	};
	Persistent.prototype.reset = function() {
		if (supportFinalizer) try {
			Persistent._registry.unregister(this);
		} catch (_) {}
		this._param = void 0;
		this._callback = void 0;
		if (this._ref instanceof StrongRef) this._ref.dispose();
		this._ref = void 0;
	};
	Persistent.prototype.isEmpty = function() {
		return this._ref === void 0;
	};
	Persistent.prototype.deref = function() {
		if (this._ref === void 0) return void 0;
		return this._ref.deref();
	};
	Persistent._registry = supportFinalizer ? new FinalizationRegistry(function(value) {
		value._ref = void 0;
		var callback = value._callback;
		var param = value._param;
		value._callback = void 0;
		value._param = void 0;
		if (typeof callback === "function") callback(param);
	}) : void 0;
	return Persistent;
}();
var ReferenceOwnership;
(function(ReferenceOwnership) {
	ReferenceOwnership[ReferenceOwnership["kRuntime"] = 0] = "kRuntime";
	ReferenceOwnership[ReferenceOwnership["kUserland"] = 1] = "kUserland";
})(ReferenceOwnership || (ReferenceOwnership = {}));
function canBeHeldWeakly(value) {
	return value.isObject() || value.isFunction() || value.isSymbol();
}
var Reference = /* @__PURE__ */ function(_super) {
	__extends(Reference, _super);
	function Reference(envObject, handle_id, initialRefcount, ownership) {
		var _this = _super.call(this) || this;
		_this.envObject = envObject;
		_this._refcount = initialRefcount;
		_this._ownership = ownership;
		var handle = envObject.ctx.handleStore.get(handle_id);
		_this.canBeWeak = canBeHeldWeakly(handle);
		_this.persistent = new Persistent(handle.value);
		_this.id = 0;
		if (initialRefcount === 0) _this._setWeak();
		return _this;
	}
	Reference.weakCallback = function(ref) {
		ref.persistent.reset();
		ref.invokeFinalizerFromGC();
	};
	Reference.create = function(envObject, handle_id, initialRefcount, ownership, _unused1, _unused2, _unused3) {
		var ref = new Reference(envObject, handle_id, initialRefcount, ownership);
		envObject.ctx.refStore.add(ref);
		ref.link(envObject.reflist);
		return ref;
	};
	Reference.prototype.ref = function() {
		if (this.persistent.isEmpty()) return 0;
		if (++this._refcount === 1 && this.canBeWeak) this.persistent.clearWeak();
		return this._refcount;
	};
	Reference.prototype.unref = function() {
		if (this.persistent.isEmpty() || this._refcount === 0) return 0;
		if (--this._refcount === 0) this._setWeak();
		return this._refcount;
	};
	Reference.prototype.get = function(envObject) {
		if (envObject === void 0) envObject = this.envObject;
		if (this.persistent.isEmpty()) return 0;
		var obj = this.persistent.deref();
		return envObject.ensureHandle(obj).id;
	};
	/** @virtual */
	Reference.prototype.resetFinalizer = function() {};
	/** @virtual */
	Reference.prototype.data = function() {
		return 0;
	};
	Reference.prototype.refcount = function() {
		return this._refcount;
	};
	Reference.prototype.ownership = function() {
		return this._ownership;
	};
	/** @virtual */
	Reference.prototype.callUserFinalizer = function() {};
	/** @virtual */
	Reference.prototype.invokeFinalizerFromGC = function() {
		this.finalize();
	};
	Reference.prototype._setWeak = function() {
		if (this.canBeWeak) this.persistent.setWeak(this, Reference.weakCallback);
		else this.persistent.reset();
	};
	Reference.prototype.finalize = function() {
		this.persistent.reset();
		var deleteMe = this._ownership === ReferenceOwnership.kRuntime;
		this.unlink();
		this.callUserFinalizer();
		if (deleteMe) this.dispose();
	};
	Reference.prototype.dispose = function() {
		if (this.id === 0) return;
		this.unlink();
		this.persistent.reset();
		this.envObject.ctx.refStore.remove(this.id);
		_super.prototype.dispose.call(this);
		this.envObject = void 0;
		this.id = 0;
	};
	return Reference;
}(RefTracker);
var ReferenceWithData = /* @__PURE__ */ function(_super) {
	__extends(ReferenceWithData, _super);
	function ReferenceWithData(envObject, value, initialRefcount, ownership, _data) {
		var _this = _super.call(this, envObject, value, initialRefcount, ownership) || this;
		_this._data = _data;
		return _this;
	}
	ReferenceWithData.create = function(envObject, value, initialRefcount, ownership, data) {
		var reference = new ReferenceWithData(envObject, value, initialRefcount, ownership, data);
		envObject.ctx.refStore.add(reference);
		reference.link(envObject.reflist);
		return reference;
	};
	ReferenceWithData.prototype.data = function() {
		return this._data;
	};
	return ReferenceWithData;
}(Reference);
var ReferenceWithFinalizer = /* @__PURE__ */ function(_super) {
	__extends(ReferenceWithFinalizer, _super);
	function ReferenceWithFinalizer(envObject, value, initialRefcount, ownership, finalize_callback, finalize_data, finalize_hint) {
		var _this = _super.call(this, envObject, value, initialRefcount, ownership) || this;
		_this._finalizer = new Finalizer(envObject, finalize_callback, finalize_data, finalize_hint);
		return _this;
	}
	ReferenceWithFinalizer.create = function(envObject, value, initialRefcount, ownership, finalize_callback, finalize_data, finalize_hint) {
		var reference = new ReferenceWithFinalizer(envObject, value, initialRefcount, ownership, finalize_callback, finalize_data, finalize_hint);
		envObject.ctx.refStore.add(reference);
		reference.link(envObject.finalizing_reflist);
		return reference;
	};
	ReferenceWithFinalizer.prototype.resetFinalizer = function() {
		this._finalizer.resetFinalizer();
	};
	ReferenceWithFinalizer.prototype.data = function() {
		return this._finalizer.data();
	};
	ReferenceWithFinalizer.prototype.callUserFinalizer = function() {
		this._finalizer.callFinalizer();
	};
	ReferenceWithFinalizer.prototype.invokeFinalizerFromGC = function() {
		this._finalizer.envObject.invokeFinalizerFromGC(this);
	};
	ReferenceWithFinalizer.prototype.dispose = function() {
		if (!this._finalizer) return;
		this._finalizer.envObject.dequeueFinalizer(this);
		this._finalizer.dispose();
		_super.prototype.dispose.call(this);
		this._finalizer = void 0;
	};
	return ReferenceWithFinalizer;
}(Reference);
var Deferred = /* @__PURE__ */ function() {
	function Deferred(ctx, value) {
		Object.defineProperties(this, {
			id: {
				configurable: true,
				enumerable: true,
				value: 0,
				writable: true
			},
			ctx: {
				configurable: true,
				enumerable: true,
				value: ctx,
				writable: true
			},
			value: {
				configurable: true,
				enumerable: true,
				value,
				writable: true
			}
		});
	}
	Deferred.create = function(ctx, value) {
		var deferred = new Deferred(ctx, value);
		ctx.deferredStore.add(deferred);
		return deferred;
	};
	Deferred.prototype.resolve = function(value) {
		this.value.resolve(value);
		this.dispose();
	};
	Deferred.prototype.reject = function(reason) {
		this.value.reject(reason);
		this.dispose();
	};
	Deferred.prototype.dispose = function() {
		this.ctx.deferredStore.remove(this.id);
		this.id = 0;
		this.value = null;
		this.ctx = null;
	};
	return Deferred;
}();
var Store = /* @__PURE__ */ function() {
	function Store() {
		this._values = [void 0];
		this._values.length = 4;
		this._size = 1;
		this._freeList = [];
	}
	Store.prototype.add = function(value) {
		var id;
		if (this._freeList.length) id = this._freeList.shift();
		else {
			id = this._size;
			this._size++;
			var capacity = this._values.length;
			if (id >= capacity) this._values.length = capacity + (capacity >> 1) + 16;
		}
		value.id = id;
		this._values[id] = value;
	};
	Store.prototype.get = function(id) {
		return this._values[id];
	};
	Store.prototype.has = function(id) {
		return this._values[id] !== void 0;
	};
	Store.prototype.remove = function(id) {
		var value = this._values[id];
		if (value) {
			value.id = 0;
			this._values[id] = void 0;
			this._freeList.push(Number(id));
		}
	};
	Store.prototype.dispose = function() {
		for (var i = 1; i < this._size; ++i) {
			var value = this._values[i];
			value === null || value === void 0 || value.dispose();
		}
		this._values = [void 0];
		this._size = 1;
		this._freeList = [];
	};
	return Store;
}();
var kMaxReasonableBytes = BigInt(1) << BigInt(60);
var kMinReasonableBytes = -kMaxReasonableBytes;
var ExternalMemory = /* @__PURE__ */ function() {
	function ExternalMemory(onChange) {
		this.total = BigInt(0);
		this.onChange = onChange !== null && onChange !== void 0 ? onChange : null;
	}
	ExternalMemory.prototype.adjust = function(changeInBytes) {
		changeInBytes = BigInt(changeInBytes);
		if (!(kMinReasonableBytes <= changeInBytes && changeInBytes < kMaxReasonableBytes)) throw new RangeError("changeInBytes ".concat(changeInBytes, " is out of reasonable range"));
		var old = this.total;
		this.total += changeInBytes;
		var amount = this.total;
		var onChange = this.onChange;
		if (changeInBytes) onChange === null || onChange === void 0 || onChange(amount, old, changeInBytes);
		return amount;
	};
	return ExternalMemory;
}();
var CleanupHookCallback = /* @__PURE__ */ function() {
	function CleanupHookCallback(envObject, fn, arg, order) {
		this.envObject = envObject;
		this.fn = fn;
		this.arg = arg;
		this.order = order;
	}
	return CleanupHookCallback;
}();
var CleanupQueue = /* @__PURE__ */ function() {
	function CleanupQueue() {
		this._cleanupHooks = [];
		this._cleanupHookCounter = 0;
	}
	CleanupQueue.prototype.empty = function() {
		return this._cleanupHooks.length === 0;
	};
	CleanupQueue.prototype.add = function(envObject, fn, arg) {
		if (this._cleanupHooks.filter(function(hook) {
			return hook.envObject === envObject && hook.fn === fn && hook.arg === arg;
		}).length > 0) throw new Error("Can not add same fn and arg twice");
		this._cleanupHooks.push(new CleanupHookCallback(envObject, fn, arg, this._cleanupHookCounter++));
	};
	CleanupQueue.prototype.remove = function(envObject, fn, arg) {
		for (var i = 0; i < this._cleanupHooks.length; ++i) {
			var hook = this._cleanupHooks[i];
			if (hook.envObject === envObject && hook.fn === fn && hook.arg === arg) {
				this._cleanupHooks.splice(i, 1);
				return;
			}
		}
	};
	CleanupQueue.prototype.drain = function() {
		var hooks = this._cleanupHooks.slice();
		hooks.sort(function(a, b) {
			return b.order - a.order;
		});
		for (var i = 0; i < hooks.length; ++i) {
			var cb = hooks[i];
			if (typeof cb.fn === "number") cb.envObject.makeDynCall_vp(cb.fn)(cb.arg);
			else cb.fn(cb.arg);
			this._cleanupHooks.splice(this._cleanupHooks.indexOf(cb), 1);
		}
	};
	CleanupQueue.prototype.dispose = function() {
		this._cleanupHooks.length = 0;
		this._cleanupHookCounter = 0;
	};
	return CleanupQueue;
}();
var NodejsWaitingRequestCounter = /* @__PURE__ */ function() {
	function NodejsWaitingRequestCounter() {
		this.refHandle = new _MessageChannel().port1;
		this.count = 0;
	}
	NodejsWaitingRequestCounter.prototype.increase = function() {
		if (this.count === 0) {
			if (this.refHandle.ref) this.refHandle.ref();
		}
		this.count++;
	};
	NodejsWaitingRequestCounter.prototype.decrease = function() {
		if (this.count === 0) return;
		if (this.count === 1) {
			if (this.refHandle.unref) this.refHandle.unref();
		}
		this.count--;
	};
	return NodejsWaitingRequestCounter;
}();
var Context = /* @__PURE__ */ function() {
	function Context(options) {
		var _this = this;
		this._isStopping = false;
		this._canCallIntoJs = true;
		this._suppressDestroy = false;
		this.envStore = new Store();
		this.scopeStore = new ScopeStore();
		this.refStore = new Store();
		this.deferredStore = new Store();
		this.handleStore = new HandleStore();
		this.feature = {
			supportReflect,
			supportFinalizer,
			supportWeakSymbol,
			supportBigInt,
			supportNewFunction,
			canSetFunctionName,
			setImmediate: _setImmediate,
			Buffer: _Buffer,
			MessageChannel: _MessageChannel
		};
		this.cleanupQueue = new CleanupQueue();
		this._externalMemory = new ExternalMemory(options === null || options === void 0 ? void 0 : options.onExternalMemoryChange);
		if (typeof process === "object" && process !== null && typeof process.once === "function") {
			this.refCounter = new NodejsWaitingRequestCounter();
			process.once("beforeExit", function() {
				if (!_this._suppressDestroy) _this.destroy();
			});
		}
	}
	/**
	* Suppress the destroy on `beforeExit` event in Node.js.
	* Call this method if you want to keep the context and
	* all associated {@link Env | Env} alive,
	* this also means that cleanup hooks will not be called.
	* After call this method, you should call
	* {@link Context.destroy | `Context.prototype.destroy`} method manually.
	*/
	Context.prototype.suppressDestroy = function() {
		this._suppressDestroy = true;
	};
	Context.prototype.getRuntimeVersions = function() {
		return {
			version,
			NODE_API_SUPPORTED_VERSION_MAX: 10,
			NAPI_VERSION_EXPERIMENTAL,
			NODE_API_DEFAULT_MODULE_API_VERSION: 8
		};
	};
	Context.prototype.createNotSupportWeakRefError = function(api, message) {
		return new NotSupportWeakRefError(api, message);
	};
	Context.prototype.createNotSupportBufferError = function(api, message) {
		return new NotSupportBufferError(api, message);
	};
	Context.prototype.createReference = function(envObject, handle_id, initialRefcount, ownership) {
		return Reference.create(envObject, handle_id, initialRefcount, ownership);
	};
	Context.prototype.createReferenceWithData = function(envObject, handle_id, initialRefcount, ownership, data) {
		return ReferenceWithData.create(envObject, handle_id, initialRefcount, ownership, data);
	};
	Context.prototype.createReferenceWithFinalizer = function(envObject, handle_id, initialRefcount, ownership, finalize_callback, finalize_data, finalize_hint) {
		if (finalize_callback === void 0) finalize_callback = 0;
		if (finalize_data === void 0) finalize_data = 0;
		if (finalize_hint === void 0) finalize_hint = 0;
		return ReferenceWithFinalizer.create(envObject, handle_id, initialRefcount, ownership, finalize_callback, finalize_data, finalize_hint);
	};
	Context.prototype.createDeferred = function(value) {
		return Deferred.create(this, value);
	};
	Context.prototype.adjustAmountOfExternalAllocatedMemory = function(changeInBytes) {
		return this._externalMemory.adjust(changeInBytes);
	};
	Context.prototype.createEnv = function(filename, moduleApiVersion, makeDynCall_vppp, makeDynCall_vp, abort, nodeBinding) {
		return newEnv(this, filename, moduleApiVersion, makeDynCall_vppp, makeDynCall_vp, abort, nodeBinding);
	};
	Context.prototype.createTrackedFinalizer = function(envObject, finalize_callback, finalize_data, finalize_hint) {
		return TrackedFinalizer.create(envObject, finalize_callback, finalize_data, finalize_hint);
	};
	Context.prototype.getCurrentScope = function() {
		return this.scopeStore.currentScope;
	};
	Context.prototype.addToCurrentScope = function(value) {
		return this.scopeStore.currentScope.add(value);
	};
	Context.prototype.openScope = function(envObject) {
		var scope = this.scopeStore.openScope(this.handleStore);
		if (envObject) envObject.openHandleScopes++;
		return scope;
	};
	Context.prototype.closeScope = function(envObject, _scope) {
		if (envObject && envObject.openHandleScopes === 0) return;
		this.scopeStore.closeScope();
		if (envObject) envObject.openHandleScopes--;
	};
	Context.prototype.ensureHandle = function(value) {
		switch (value) {
			case void 0: return HandleStore.UNDEFINED;
			case null: return HandleStore.NULL;
			case true: return HandleStore.TRUE;
			case false: return HandleStore.FALSE;
			case _global: return HandleStore.GLOBAL;
		}
		return this.addToCurrentScope(value);
	};
	Context.prototype.addCleanupHook = function(envObject, fn, arg) {
		this.cleanupQueue.add(envObject, fn, arg);
	};
	Context.prototype.removeCleanupHook = function(envObject, fn, arg) {
		this.cleanupQueue.remove(envObject, fn, arg);
	};
	Context.prototype.runCleanup = function() {
		while (!this.cleanupQueue.empty()) this.cleanupQueue.drain();
	};
	Context.prototype.increaseWaitingRequestCounter = function() {
		var _a;
		(_a = this.refCounter) === null || _a === void 0 || _a.increase();
	};
	Context.prototype.decreaseWaitingRequestCounter = function() {
		var _a;
		(_a = this.refCounter) === null || _a === void 0 || _a.decrease();
	};
	Context.prototype.setCanCallIntoJs = function(value) {
		this._canCallIntoJs = value;
	};
	Context.prototype.setStopping = function(value) {
		this._isStopping = value;
	};
	Context.prototype.canCallIntoJs = function() {
		return this._canCallIntoJs && !this._isStopping;
	};
	/**
	* Destroy the context and call cleanup hooks.
	* Associated {@link Env | Env} will be destroyed.
	*/
	Context.prototype.destroy = function() {
		this.setStopping(true);
		this.setCanCallIntoJs(false);
		this.runCleanup();
	};
	return Context;
}();
var defaultContext;
function createContext$1(options) {
	return new Context(options);
}
function getDefaultContext$1() {
	if (!defaultContext) defaultContext = createContext$1();
	return defaultContext;
}
// node_modules/@tybys/wasm-util/dist/wasm-util.esm-bundler.js
const _WebAssembly = typeof WebAssembly !== "undefined" ? WebAssembly : typeof WXWebAssembly !== "undefined" ? WXWebAssembly : void 0;
if (!_WebAssembly) throw new Error("WebAssembly is not supported in this environment");
function validateObject(value, name) {
	if (value === null || typeof value !== "object") throw new TypeError(`${name} must be an object. Received ${value === null ? "null" : typeof value}`);
}
function validateArray(value, name) {
	if (!Array.isArray(value)) throw new TypeError(`${name} must be an array. Received ${value === null ? "null" : typeof value}`);
}
function validateBoolean(value, name) {
	if (typeof value !== "boolean") throw new TypeError(`${name} must be a boolean. Received ${value === null ? "null" : typeof value}`);
}
function validateString(value, name) {
	if (typeof value !== "string") throw new TypeError(`${name} must be a string. Received ${value === null ? "null" : typeof value}`);
}
function validateFunction(value, name) {
	if (typeof value !== "function") throw new TypeError(`${name} must be a function. Received ${value === null ? "null" : typeof value}`);
}
function validateUndefined(value, name) {
	if (value !== void 0) throw new TypeError(`${name} must be undefined. Received ${value === null ? "null" : typeof value}`);
}
function isPromiseLike(obj) {
	return !!(obj && (typeof obj === "object" || typeof obj === "function") && typeof obj.then === "function");
}
function sleepBreakIf(delay, breakIf) {
	const end = Date.now() + delay;
	let ret = false;
	while (Date.now() < end) if (breakIf()) {
		ret = true;
		break;
	}
	return ret;
}
function unsharedSlice(view, start, end) {
	return typeof SharedArrayBuffer === "function" && view.buffer instanceof SharedArrayBuffer || Object.prototype.toString.call(view.buffer.constructor) === "[object SharedArrayBuffer]" ? view.slice(start, end) : view.subarray(start, end);
}
const CHAR_DOT = 46;
const CHAR_FORWARD_SLASH = 47;
const CHAR_BACKWARD_SLASH = 92;
const CHAR_COLON = 58;
const CHAR_UPPERCASE_A = 65;
const CHAR_UPPERCASE_Z = 90;
const CHAR_LOWERCASE_A = 97;
const CHAR_LOWERCASE_Z = 122;
function isPathSeparatorWin(code) {
	return code === CHAR_FORWARD_SLASH || code === CHAR_BACKWARD_SLASH;
}
function isWindowsDeviceRoot(code) {
	return code >= CHAR_UPPERCASE_A && code <= CHAR_UPPERCASE_Z || code >= CHAR_LOWERCASE_A && code <= CHAR_LOWERCASE_Z;
}
const _isWin32 = typeof process !== "undefined" && process.platform === "win32";
/**
* Windows variant of `resolve()`. Mirrors Node's `path.win32.resolve`
* semantics enough for the WASI shim's needs (drive-letter absolutes
* + UNC paths + per-drive cwd lookups via `process.env`/`process.cwd`).
*
* Why this is needed: on Windows, `FileDescriptor.realPath` is the
* host realpath returned by `fs.realpathSync(realPath, 'utf8')` — the
* backslash form `D:\…`. The POSIX-only `resolveImpl` below only
* treats `/` as a separator, reads `D` as non-`/`, decides realPath
* is "relative", and produces a garbage joined path. Downstream
* `fs.openSync(garbage)` then returns `EINVAL` and the WASI caller
* sees a permanent failure. This function gives us the correct
* Windows-style resolution without taking a Node-only `path` import
* (which would break browser bundles of this package).
*
* Cribbed from Node's `lib/path.js` `win32.resolve()` (MIT-licensed),
* trimmed to what WASI realpath resolution actually exercises.
*/
function resolveWin32(args) {
	let resolvedDevice = "";
	let resolvedTail = "";
	let resolvedAbsolute = false;
	for (let i = args.length - 1; i >= -1; i--) {
		let path;
		if (i >= 0) {
			path = args[i];
			validateString(path, "path");
			if (path.length === 0) continue;
		} else if (resolvedDevice.length === 0) path = typeof process !== "undefined" && typeof process.cwd === "function" ? process.cwd() : "";
		else {
			const envKey = `=${resolvedDevice}`;
			const env = typeof process !== "undefined" ? process.env : void 0;
			path = env && typeof env[envKey] === "string" ? env[envKey] : typeof process !== "undefined" && typeof process.cwd === "function" ? process.cwd() : "";
			if (path === void 0 || path.slice(0, 2).toLowerCase() !== resolvedDevice.toLowerCase() && path.charCodeAt(2) === CHAR_BACKWARD_SLASH) path = `${resolvedDevice}\\`;
		}
		const len = path.length;
		let rootEnd = 0;
		let device = "";
		let isAbsolute = false;
		const code = path.charCodeAt(0);
		if (len === 1) {
			if (isPathSeparatorWin(code)) {
				rootEnd = 1;
				isAbsolute = true;
			}
		} else if (isPathSeparatorWin(code)) {
			isAbsolute = true;
			if (isPathSeparatorWin(path.charCodeAt(1))) {
				let j = 2;
				let last = j;
				while (j < len && !isPathSeparatorWin(path.charCodeAt(j))) j++;
				if (j < len && j !== last) {
					const firstPart = path.slice(last, j);
					last = j;
					while (j < len && isPathSeparatorWin(path.charCodeAt(j))) j++;
					if (j < len && j !== last) {
						last = j;
						while (j < len && !isPathSeparatorWin(path.charCodeAt(j))) j++;
						if (j === len || j !== last) {
							device = `\\\\${firstPart}\\${path.slice(last, j)}`;
							rootEnd = j;
						}
					}
				}
			} else rootEnd = 1;
		} else if (isWindowsDeviceRoot(code) && path.charCodeAt(1) === CHAR_COLON) {
			device = path.slice(0, 2);
			rootEnd = 2;
			if (len > 2 && isPathSeparatorWin(path.charCodeAt(2))) {
				isAbsolute = true;
				rootEnd = 3;
			}
		}
		if (device.length > 0) {
			if (resolvedDevice.length > 0) {
				if (device.toLowerCase() !== resolvedDevice.toLowerCase()) continue;
			} else resolvedDevice = device;
		}
		if (resolvedAbsolute) {
			if (resolvedDevice.length > 0) break;
		} else {
			resolvedTail = `${path.slice(rootEnd)}\\${resolvedTail}`;
			resolvedAbsolute = isAbsolute;
			if (isAbsolute && resolvedDevice.length > 0) break;
		}
	}
	resolvedTail = normalizeString(resolvedTail, !resolvedAbsolute, "\\", isPathSeparatorWin);
	return resolvedDevice + (resolvedAbsolute ? "\\" : "") + resolvedTail || ".";
}
function isPosixPathSeparator(code) {
	return code === CHAR_FORWARD_SLASH;
}
function normalizeString(path, allowAboveRoot, separator, isPathSeparator) {
	let res = "";
	let lastSegmentLength = 0;
	let lastSlash = -1;
	let dots = 0;
	let code = 0;
	for (let i = 0; i <= path.length; ++i) {
		if (i < path.length) code = path.charCodeAt(i);
		else if (isPathSeparator(code)) break;
		else code = CHAR_FORWARD_SLASH;
		if (isPathSeparator(code)) {
			if (lastSlash === i - 1 || dots === 1);
			else if (dots === 2) {
				if (res.length < 2 || lastSegmentLength !== 2 || res.charCodeAt(res.length - 1) !== CHAR_DOT || res.charCodeAt(res.length - 2) !== CHAR_DOT) {
					if (res.length > 2) {
						const lastSlashIndex = res.indexOf(separator);
						if (lastSlashIndex === -1) {
							res = "";
							lastSegmentLength = 0;
						} else {
							res = res.slice(0, lastSlashIndex);
							lastSegmentLength = res.length - 1 - res.indexOf(separator);
						}
						lastSlash = i;
						dots = 0;
						continue;
					} else if (res.length !== 0) {
						res = "";
						lastSegmentLength = 0;
						lastSlash = i;
						dots = 0;
						continue;
					}
				}
				if (allowAboveRoot) {
					res += res.length > 0 ? `${separator}..` : "..";
					lastSegmentLength = 2;
				}
			} else {
				if (res.length > 0) res += `${separator}${path.slice(lastSlash + 1, i)}`;
				else res = path.slice(lastSlash + 1, i);
				lastSegmentLength = i - lastSlash - 1;
			}
			lastSlash = i;
			dots = 0;
		} else if (code === CHAR_DOT && dots !== -1) ++dots;
		else dots = -1;
	}
	return res;
}
function resolve(...args) {
	if (_isWin32) return resolveWin32(args);
	let resolvedPath = "";
	let resolvedAbsolute = false;
	for (let i = args.length - 1; i >= -1 && !resolvedAbsolute; i--) {
		const path = i >= 0 ? args[i] : "/";
		validateString(path, "path");
		if (path.length === 0) continue;
		resolvedPath = `${path}/${resolvedPath}`;
		resolvedAbsolute = path.charCodeAt(0) === CHAR_FORWARD_SLASH;
	}
	resolvedPath = normalizeString(resolvedPath, !resolvedAbsolute, "/", isPosixPathSeparator);
	if (resolvedAbsolute) return `/${resolvedPath}`;
	return resolvedPath.length > 0 ? resolvedPath : ".";
}
const WasiRights = {
	FD_DATASYNC: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(0),
	FD_READ: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(1),
	FD_SEEK: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(2),
	FD_FDSTAT_SET_FLAGS: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(3),
	FD_SYNC: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(4),
	FD_TELL: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(5),
	FD_WRITE: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(6),
	FD_ADVISE: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(7),
	FD_ALLOCATE: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(8),
	PATH_CREATE_DIRECTORY: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(9),
	PATH_CREATE_FILE: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(10),
	PATH_LINK_SOURCE: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(11),
	PATH_LINK_TARGET: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(12),
	PATH_OPEN: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(13),
	FD_READDIR: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(14),
	PATH_READLINK: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(15),
	PATH_RENAME_SOURCE: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(16),
	PATH_RENAME_TARGET: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(17),
	PATH_FILESTAT_GET: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(18),
	PATH_FILESTAT_SET_SIZE: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(19),
	PATH_FILESTAT_SET_TIMES: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(20),
	FD_FILESTAT_GET: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(21),
	FD_FILESTAT_SET_SIZE: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(22),
	FD_FILESTAT_SET_TIMES: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(23),
	PATH_SYMLINK: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(24),
	PATH_REMOVE_DIRECTORY: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(25),
	PATH_UNLINK_FILE: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(26),
	POLL_FD_READWRITE: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(27),
	SOCK_SHUTDOWN: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(28),
	SOCK_ACCEPT: /*#__PURE__*/ BigInt(1) << /*#__PURE__*/ BigInt(29)
};
function strerror(errno) {
	switch (errno) {
		case 0: return "Success";
		case 1: return "Argument list too long";
		case 2: return "Permission denied";
		case 3: return "Address in use";
		case 4: return "Address not available";
		case 5: return "Address family not supported by protocol";
		case 6: return "Resource temporarily unavailable";
		case 7: return "Operation already in progress";
		case 8: return "Bad file descriptor";
		case 9: return "Bad message";
		case 10: return "Resource busy";
		case 11: return "Operation canceled";
		case 12: return "No child process";
		case 13: return "Connection aborted";
		case 14: return "Connection refused";
		case 15: return "Connection reset by peer";
		case 16: return "Resource deadlock would occur";
		case 17: return "Destination address required";
		case 18: return "Domain error";
		case 19: return "Quota exceeded";
		case 20: return "File exists";
		case 21: return "Bad address";
		case 22: return "File too large";
		case 23: return "Host is unreachable";
		case 24: return "Identifier removed";
		case 25: return "Illegal byte sequence";
		case 26: return "Operation in progress";
		case 27: return "Interrupted system call";
		case 28: return "Invalid argument";
		case 29: return "I/O error";
		case 30: return "Socket is connected";
		case 31: return "Is a directory";
		case 32: return "Symbolic link loop";
		case 33: return "No file descriptors available";
		case 34: return "Too many links";
		case 35: return "Message too large";
		case 36: return "Multihop attempted";
		case 37: return "Filename too long";
		case 38: return "Network is down";
		case 39: return "Connection reset by network";
		case 40: return "Network unreachable";
		case 41: return "Too many files open in system";
		case 42: return "No buffer space available";
		case 43: return "No such device";
		case 44: return "No such file or directory";
		case 45: return "Exec format error";
		case 46: return "No locks available";
		case 47: return "Link has been severed";
		case 48: return "Out of memory";
		case 49: return "No message of the desired type";
		case 50: return "Protocol not available";
		case 51: return "No space left on device";
		case 52: return "Function not implemented";
		case 53: return "Socket not connected";
		case 54: return "Not a directory";
		case 55: return "Directory not empty";
		case 56: return "State not recoverable";
		case 57: return "Not a socket";
		case 58: return "Not supported";
		case 59: return "Not a tty";
		case 60: return "No such device or address";
		case 61: return "Value too large for data type";
		case 62: return "Previous owner died";
		case 63: return "Operation not permitted";
		case 64: return "Broken pipe";
		case 65: return "Protocol error";
		case 66: return "Protocol not supported";
		case 67: return "Protocol wrong type for socket";
		case 68: return "Result not representable";
		case 69: return "Read-only file system";
		case 70: return "Invalid seek";
		case 71: return "No such process";
		case 72: return "Stale file handle";
		case 73: return "Operation timed out";
		case 74: return "Text file busy";
		case 75: return "Cross-device link";
		case 76: return "Capabilities insufficient";
		default: return "Unknown error";
	}
}
var WasiError = class extends Error {
	constructor(message, errno) {
		super(message);
		this.errno = errno;
	}
	getErrorMessage() {
		return strerror(this.errno);
	}
};
Object.defineProperty(WasiError.prototype, "name", {
	configurable: true,
	writable: true,
	value: "WasiError"
});
const RIGHTS_ALL = WasiRights.FD_DATASYNC | WasiRights.FD_READ | WasiRights.FD_SEEK | WasiRights.FD_FDSTAT_SET_FLAGS | WasiRights.FD_SYNC | WasiRights.FD_TELL | WasiRights.FD_WRITE | WasiRights.FD_ADVISE | WasiRights.FD_ALLOCATE | WasiRights.PATH_CREATE_DIRECTORY | WasiRights.PATH_CREATE_FILE | WasiRights.PATH_LINK_SOURCE | WasiRights.PATH_LINK_TARGET | WasiRights.PATH_OPEN | WasiRights.FD_READDIR | WasiRights.PATH_READLINK | WasiRights.PATH_RENAME_SOURCE | WasiRights.PATH_RENAME_TARGET | WasiRights.PATH_FILESTAT_GET | WasiRights.PATH_FILESTAT_SET_SIZE | WasiRights.PATH_FILESTAT_SET_TIMES | WasiRights.FD_FILESTAT_GET | WasiRights.FD_FILESTAT_SET_TIMES | WasiRights.FD_FILESTAT_SET_SIZE | WasiRights.PATH_SYMLINK | WasiRights.PATH_UNLINK_FILE | WasiRights.PATH_REMOVE_DIRECTORY | WasiRights.POLL_FD_READWRITE | WasiRights.SOCK_SHUTDOWN | WasiRights.SOCK_ACCEPT;
const BLOCK_DEVICE_BASE = RIGHTS_ALL;
const BLOCK_DEVICE_INHERITING = RIGHTS_ALL;
const CHARACTER_DEVICE_BASE = RIGHTS_ALL;
const CHARACTER_DEVICE_INHERITING = RIGHTS_ALL;
const REGULAR_FILE_BASE = WasiRights.FD_DATASYNC | WasiRights.FD_READ | WasiRights.FD_SEEK | WasiRights.FD_FDSTAT_SET_FLAGS | WasiRights.FD_SYNC | WasiRights.FD_TELL | WasiRights.FD_WRITE | WasiRights.FD_ADVISE | WasiRights.FD_ALLOCATE | WasiRights.FD_FILESTAT_GET | WasiRights.FD_FILESTAT_SET_SIZE | WasiRights.FD_FILESTAT_SET_TIMES | WasiRights.POLL_FD_READWRITE;
const REGULAR_FILE_INHERITING = /*#__PURE__*/ BigInt(0);
const DIRECTORY_BASE = WasiRights.FD_FDSTAT_SET_FLAGS | WasiRights.FD_SYNC | WasiRights.FD_ADVISE | WasiRights.PATH_CREATE_DIRECTORY | WasiRights.PATH_CREATE_FILE | WasiRights.PATH_LINK_SOURCE | WasiRights.PATH_LINK_TARGET | WasiRights.PATH_OPEN | WasiRights.FD_READDIR | WasiRights.PATH_READLINK | WasiRights.PATH_RENAME_SOURCE | WasiRights.PATH_RENAME_TARGET | WasiRights.PATH_FILESTAT_GET | WasiRights.PATH_FILESTAT_SET_SIZE | WasiRights.PATH_FILESTAT_SET_TIMES | WasiRights.FD_FILESTAT_GET | WasiRights.FD_FILESTAT_SET_TIMES | WasiRights.PATH_SYMLINK | WasiRights.PATH_UNLINK_FILE | WasiRights.PATH_REMOVE_DIRECTORY | WasiRights.POLL_FD_READWRITE;
const DIRECTORY_INHERITING = DIRECTORY_BASE | REGULAR_FILE_BASE;
const SOCKET_BASE = WasiRights.FD_READ | WasiRights.FD_FDSTAT_SET_FLAGS | WasiRights.FD_WRITE | WasiRights.FD_FILESTAT_GET | WasiRights.POLL_FD_READWRITE | WasiRights.SOCK_SHUTDOWN;
const SOCKET_INHERITING = RIGHTS_ALL;
const TTY_BASE = WasiRights.FD_READ | WasiRights.FD_FDSTAT_SET_FLAGS | WasiRights.FD_WRITE | WasiRights.FD_FILESTAT_GET | WasiRights.POLL_FD_READWRITE;
const TTY_INHERITING = /*#__PURE__*/ BigInt(0);
function getRights(stdio, fd, flags, type) {
	const ret = {
		base: BigInt(0),
		inheriting: BigInt(0)
	};
	if (type === 0) throw new WasiError("Unknown file type", 28);
	switch (type) {
		case 4:
			ret.base = REGULAR_FILE_BASE;
			ret.inheriting = REGULAR_FILE_INHERITING;
			break;
		case 3:
			ret.base = DIRECTORY_BASE;
			ret.inheriting = DIRECTORY_INHERITING;
			break;
		case 6:
		case 5:
			ret.base = SOCKET_BASE;
			ret.inheriting = SOCKET_INHERITING;
			break;
		case 2:
			if (stdio.indexOf(fd) !== -1) {
				ret.base = TTY_BASE;
				ret.inheriting = TTY_INHERITING;
			} else {
				ret.base = CHARACTER_DEVICE_BASE;
				ret.inheriting = CHARACTER_DEVICE_INHERITING;
			}
			break;
		case 1:
			ret.base = BLOCK_DEVICE_BASE;
			ret.inheriting = BLOCK_DEVICE_INHERITING;
			break;
		default:
			ret.base = BigInt(0);
			ret.inheriting = BigInt(0);
	}
	const read_or_write_only = flags & 3;
	if (read_or_write_only === 0) ret.base &= ~WasiRights.FD_WRITE;
	else if (read_or_write_only === 1) ret.base &= ~WasiRights.FD_READ;
	return ret;
}
function concatBuffer(buffers, size) {
	let total = 0;
	if (typeof size === "number" && size >= 0) total = size;
	else for (let i = 0; i < buffers.length; i++) {
		const buffer = buffers[i];
		total += buffer.length;
	}
	let pos = 0;
	const ret = new Uint8Array(total);
	for (let i = 0; i < buffers.length; i++) {
		const buffer = buffers[i];
		ret.set(buffer, pos);
		pos += buffer.length;
	}
	return ret;
}
var FileDescriptor = class {
	constructor(id, fd, path, realPath, type, rightsBase, rightsInheriting, preopen) {
		this.id = id;
		this.fd = fd;
		this.path = path;
		this.realPath = realPath;
		this.type = type;
		this.rightsBase = rightsBase;
		this.rightsInheriting = rightsInheriting;
		this.preopen = preopen;
		this.pos = BigInt(0);
		this.size = BigInt(0);
	}
	seek(offset, whence) {
		let seekOffset;
		try {
			seekOffset = BigInt(offset);
		} catch (_) {
			throw new WasiError("Invalid seek offset", 28);
		}
		let position;
		if (whence === 0) position = seekOffset;
		else if (whence === 1) position = BigInt(this.pos) + seekOffset;
		else if (whence === 2) position = BigInt(this.size) + seekOffset;
		else throw new WasiError("Unknown whence", 29);
		if (position < BigInt(0)) throw new WasiError("Invalid seek offset", 28);
		this.pos = position;
		return position;
	}
};
var StandardOutput = class extends FileDescriptor {
	constructor(log, id, fd, path, realPath, type, rightsBase, rightsInheriting, preopen) {
		super(id, fd, path, realPath, type, rightsBase, rightsInheriting, preopen);
		this._log = log;
		this._buf = null;
	}
	write(buffer) {
		const originalBuffer = buffer;
		if (this._buf) {
			buffer = concatBuffer([this._buf, buffer]);
			this._buf = null;
		}
		if (buffer.indexOf(10) === -1) {
			this._buf = buffer;
			return originalBuffer.byteLength;
		}
		let written = 0;
		let lastBegin = 0;
		let index;
		while ((index = buffer.indexOf(10, written)) !== -1) {
			const str = new TextDecoder().decode(buffer.subarray(lastBegin, index));
			this._log(str);
			written += index - lastBegin + 1;
			lastBegin = index + 1;
		}
		if (written < buffer.length) this._buf = buffer.slice(written);
		return originalBuffer.byteLength;
	}
};
function toFileType(stat) {
	if (stat.isBlockDevice()) return 1;
	if (stat.isCharacterDevice()) return 2;
	if (stat.isDirectory()) return 3;
	if (stat.isSocket()) return 6;
	if (stat.isFile()) return 4;
	if (stat.isSymbolicLink()) return 7;
	return 0;
}
function toFileStat(view, buf, stat) {
	view.setBigUint64(buf, stat.dev, true);
	view.setBigUint64(buf + 8, stat.ino, true);
	view.setBigUint64(buf + 16, BigInt(toFileType(stat)), true);
	view.setBigUint64(buf + 24, stat.nlink, true);
	view.setBigUint64(buf + 32, stat.size, true);
	view.setBigUint64(buf + 40, stat.atimeMs * BigInt(1e6), true);
	view.setBigUint64(buf + 48, stat.mtimeMs * BigInt(1e6), true);
	view.setBigUint64(buf + 56, stat.ctimeMs * BigInt(1e6), true);
}
var FileDescriptorTable = class {
	constructor(options) {
		this.used = 0;
		this.size = options.size;
		this.fds = Array(options.size);
		this.stdio = [
			options.in,
			options.out,
			options.err
		];
		this.print = options.print;
		this.printErr = options.printErr;
		this.insertStdio(options.in, 0, "<stdin>");
		this.insertStdio(options.out, 1, "<stdout>");
		this.insertStdio(options.err, 2, "<stderr>");
	}
	insertStdio(fd, expected, name) {
		const type = 2;
		const { base, inheriting } = getRights(this.stdio, fd, 2, type);
		const wrap = this.insert(fd, name, name, type, base, inheriting, 0);
		if (wrap.id !== expected) throw new WasiError(`id: ${wrap.id} !== expected: ${expected}`, 8);
		return wrap;
	}
	insert(fd, mappedPath, realPath, type, rightsBase, rightsInheriting, preopen) {
		var _a, _b;
		let index = -1;
		if (this.used >= this.size) {
			const newSize = this.size * 2;
			this.fds.length = newSize;
			index = this.size;
			this.size = newSize;
		} else for (let i = 0; i < this.size; ++i) if (this.fds[i] == null) {
			index = i;
			break;
		}
		let entry;
		if (mappedPath === "<stdout>") entry = new StandardOutput((_a = this.print) !== null && _a !== void 0 ? _a : console.log, index, fd, mappedPath, realPath, type, rightsBase, rightsInheriting, preopen);
		else if (mappedPath === "<stderr>") entry = new StandardOutput((_b = this.printErr) !== null && _b !== void 0 ? _b : console.error, index, fd, mappedPath, realPath, type, rightsBase, rightsInheriting, preopen);
		else entry = new FileDescriptor(index, fd, mappedPath, realPath, type, rightsBase, rightsInheriting, preopen);
		this.fds[index] = entry;
		this.used++;
		return entry;
	}
	get(id, base, inheriting) {
		if (id >= this.size) throw new WasiError("Invalid fd", 8);
		const entry = this.fds[id];
		if (!entry || entry.id !== id) throw new WasiError("Bad file descriptor", 8);
		if ((~entry.rightsBase & base) !== BigInt(0) || (~entry.rightsInheriting & inheriting) !== BigInt(0)) throw new WasiError("Capabilities insufficient", 76);
		return entry;
	}
	remove(id) {
		if (id >= this.size) throw new WasiError("Invalid fd", 8);
		const entry = this.fds[id];
		if (!entry || entry.id !== id) throw new WasiError("Bad file descriptor", 8);
		this.fds[id] = void 0;
		this.used--;
	}
};
var SyncTable = class extends FileDescriptorTable {
	constructor(options) {
		super(options);
		this.fs = options.fs;
	}
	getFileTypeByFd(fd) {
		return toFileType(this.fs.fstatSync(fd, { bigint: true }));
	}
	insertPreopen(fd, mappedPath, realPath) {
		const type = this.getFileTypeByFd(fd);
		if (type !== 3) throw new WasiError(`Preopen not dir: ["${mappedPath}", "${realPath}"]`, 54);
		const result = getRights(this.stdio, fd, 0, type);
		return this.insert(fd, mappedPath, realPath, type, result.base, result.inheriting, 1);
	}
	renumber(dst, src) {
		if (dst === src) return;
		if (dst >= this.size || src >= this.size) throw new WasiError("Invalid fd", 8);
		const dstEntry = this.fds[dst];
		const srcEntry = this.fds[src];
		if (!dstEntry || !srcEntry || dstEntry.id !== dst || srcEntry.id !== src) throw new WasiError("Invalid fd", 8);
		this.fs.closeSync(dstEntry.fd);
		this.fds[dst] = this.fds[src];
		this.fds[dst].id = dst;
		this.fds[src] = void 0;
		this.used--;
	}
};
var AsyncTable = class extends FileDescriptorTable {
	constructor(options) {
		super(options);
	}
	async getFileTypeByFd(fd) {
		return toFileType(await fd.stat({ bigint: true }));
	}
	async insertPreopen(fd, mappedPath, realPath) {
		const type = await this.getFileTypeByFd(fd);
		if (type !== 3) throw new WasiError(`Preopen not dir: ["${mappedPath}", "${realPath}"]`, 54);
		const result = getRights(this.stdio, fd.fd, 0, type);
		return this.insert(fd, mappedPath, realPath, type, result.base, result.inheriting, 1);
	}
	async renumber(dst, src) {
		if (dst === src) return;
		if (dst >= this.size || src >= this.size) throw new WasiError("Invalid fd", 8);
		const dstEntry = this.fds[dst];
		const srcEntry = this.fds[src];
		if (!dstEntry || !srcEntry || dstEntry.id !== dst || srcEntry.id !== src) throw new WasiError("Invalid fd", 8);
		await dstEntry.fd.close();
		this.fds[dst] = this.fds[src];
		this.fds[dst].id = dst;
		this.fds[src] = void 0;
		this.used--;
	}
};
/** @public */
const WebAssemblyMemory = /*#__PURE__*/ (function() {
	return _WebAssembly.Memory;
})();
/** @public */
var Memory = class extends WebAssemblyMemory {
	constructor(descriptor) {
		super(descriptor);
	}
	get HEAP8() {
		return new Int8Array(super.buffer);
	}
	get HEAPU8() {
		return new Uint8Array(super.buffer);
	}
	get HEAP16() {
		return new Int16Array(super.buffer);
	}
	get HEAPU16() {
		return new Uint16Array(super.buffer);
	}
	get HEAP32() {
		return new Int32Array(super.buffer);
	}
	get HEAPU32() {
		return new Uint32Array(super.buffer);
	}
	get HEAP64() {
		return new BigInt64Array(super.buffer);
	}
	get HEAPU64() {
		return new BigUint64Array(super.buffer);
	}
	get HEAPF32() {
		return new Float32Array(super.buffer);
	}
	get HEAPF64() {
		return new Float64Array(super.buffer);
	}
	get view() {
		return new DataView(super.buffer);
	}
};
/** @public */
function extendMemory(memory) {
	if (Object.getPrototypeOf(memory) === _WebAssembly.Memory.prototype) Object.setPrototypeOf(memory, Memory.prototype);
	return memory;
}
function checkWebAssemblyFunction() {
	const WebAssemblyFunction = _WebAssembly.Function;
	if (typeof WebAssemblyFunction !== "function") throw new Error("WebAssembly.Function is not supported in this environment. If you are using V8 based browser like Chrome, try to specify --js-flags=\"--wasm-staging --experimental-wasm-stack-switching\"");
	return WebAssemblyFunction;
}
/** @public */
function wrapAsyncImport(f, parameterType, returnType) {
	const WebAssemblyFunction = checkWebAssemblyFunction();
	if (typeof f !== "function") throw new TypeError("Function required");
	const parameters = parameterType.slice(0);
	parameters.unshift("externref");
	return new WebAssemblyFunction({
		parameters,
		results: returnType
	}, f, { suspending: "first" });
}
const _isWin32Flags = typeof process !== "undefined" && process.platform === "win32";
function _toWinOpenFlags(f) {
	let r = f & 3;
	if ((f & 64) !== 0) r |= 256;
	if ((f & 128) !== 0) r |= 1024;
	if ((f & 512) !== 0) r |= 512;
	if ((f & 1024) !== 0) r |= 8;
	return r;
}
function copyMemory(targets, src) {
	if (targets.length === 0 || src.length === 0) return 0;
	let copied = 0;
	let left = src.length - copied;
	for (let i = 0; i < targets.length; ++i) {
		const target = targets[i];
		if (left < target.length) {
			target.set(src.subarray(copied, copied + left), 0);
			copied += left;
			left = 0;
			return copied;
		}
		target.set(src.subarray(copied, copied + target.length), 0);
		copied += target.length;
		left -= target.length;
	}
	return copied;
}
const _memory = /* @__PURE__ */ new WeakMap();
const _wasi = /* @__PURE__ */ new WeakMap();
const _fs = /* @__PURE__ */ new WeakMap();
function ensureMemoryFor(memory, end) {
	let buffer = memory.buffer;
	if (end > buffer.byteLength) {
		memory.grow(0);
		buffer = memory.buffer;
	}
	return buffer;
}
function getMemory(wasi, end = 0) {
	const memory = _memory.get(wasi);
	ensureMemoryFor(memory, end);
	return memory;
}
function getFs(wasi) {
	const fs = _fs.get(wasi);
	if (!fs) throw new Error("filesystem is unavailable");
	return fs;
}
function handleError(err) {
	if (err instanceof WasiError) return err.errno;
	switch (err.code) {
		case "ENOENT": return 44;
		case "EBADF": return 8;
		case "EINVAL": return 28;
		case "EPERM": return 63;
		case "EPROTO": return 65;
		case "EEXIST": return 20;
		case "ENOTDIR": return 54;
		case "EMFILE": return 33;
		case "EACCES": return 2;
		case "EISDIR": return 31;
		case "ENOTEMPTY": return 55;
		case "ENOSYS": return 52;
	}
	throw err;
}
function defineName(name, f) {
	Object.defineProperty(f, "name", { value: name });
	return f;
}
function tryCall(f, wasi, args) {
	let r;
	try {
		r = f.apply(wasi, args);
	} catch (err) {
		return handleError(err);
	}
	if (isPromiseLike(r)) return r.then((_) => _, handleError);
	return r;
}
function syscallWrap(self, name, f) {
	let debug = false;
	const NODE_DEBUG_NATIVE = (() => {
		try {
			return process.env.NODE_DEBUG_NATIVE;
		} catch (_) {
			return;
		}
	})();
	if (typeof NODE_DEBUG_NATIVE === "string" && NODE_DEBUG_NATIVE.split(",").includes("wasi")) debug = true;
	return debug ? defineName(name, function() {
		const args = Array.prototype.slice.call(arguments);
		let debugArgs = [`${name}(${Array.from({ length: arguments.length }).map(() => "%d").join(", ")})`];
		debugArgs = debugArgs.concat(args);
		console.debug.apply(console, debugArgs);
		return tryCall(f, self, args);
	}) : defineName(name, function() {
		return tryCall(f, self, arguments);
	});
}
function resolvePathSync(fs, fileDescriptor, path, flags) {
	let resolvedPath = resolve(fileDescriptor.realPath, path);
	if ((flags & 1) === 1) try {
		resolvedPath = fs.readlinkSync(resolvedPath);
	} catch (err) {
		if (err.code !== "EINVAL" && err.code !== "ENOENT") throw err;
	}
	return resolvedPath;
}
async function resolvePathAsync(fs, fileDescriptor, path, flags) {
	let resolvedPath = resolve(fileDescriptor.realPath, path);
	if ((flags & 1) === 1) try {
		resolvedPath = await fs.promises.readlink(resolvedPath);
	} catch (err) {
		if (err.code !== "EINVAL" && err.code !== "ENOENT") throw err;
	}
	return resolvedPath;
}
const encoder = /*#__PURE__*/ new TextEncoder();
const decoder = /*#__PURE__*/ new TextDecoder();
const INT64_MAX = (BigInt(1) << BigInt(63)) - BigInt(1);
const NANOSECONDS_PER_SECOND = BigInt(1e9);
function nanosecondsToSeconds(nanoseconds) {
	const seconds = nanoseconds / NANOSECONDS_PER_SECOND;
	const remainder = nanoseconds % NANOSECONDS_PER_SECOND;
	return Number(seconds) + Number(remainder) / Number(NANOSECONDS_PER_SECOND);
}
function readStdin() {
	const value = window.prompt();
	if (value === null) return /* @__PURE__ */ new Uint8Array();
	return new TextEncoder().encode(value + "\n");
}
function validateFstFlagsOrReturn(flags) {
	return Boolean(flags & -16) || (flags & 3) === 3 || (flags & 12) === 12;
}
var WASI$1 = class WASI$1 {
	constructor(args, env, fds, asyncFs, fs, asyncify) {
		this.args_get = syscallWrap(this, "args_get", function(argv, argv_buf) {
			argv = Number(argv);
			argv_buf = Number(argv_buf);
			if (argv === 0 || argv_buf === 0) return 28;
			const args = _wasi.get(this).args;
			const argsSize = encoder.encode(args.join("\0") + "\0").length;
			const { HEAPU8, view } = getMemory(this, Math.max(argv + args.length * 4, argv_buf + argsSize));
			for (let i = 0; i < args.length; ++i) {
				const arg = args[i];
				view.setInt32(argv, argv_buf, true);
				argv += 4;
				const data = encoder.encode(arg + "\0");
				HEAPU8.set(data, argv_buf);
				argv_buf += data.length;
			}
			return 0;
		});
		this.args_sizes_get = syscallWrap(this, "args_sizes_get", function(argc, argv_buf_size) {
			argc = Number(argc);
			argv_buf_size = Number(argv_buf_size);
			if (argc === 0 || argv_buf_size === 0) return 28;
			const { view } = getMemory(this, Math.max(argc + 4, argv_buf_size + 4));
			const args = _wasi.get(this).args;
			view.setUint32(argc, args.length, true);
			view.setUint32(argv_buf_size, encoder.encode(args.join("\0") + "\0").length, true);
			return 0;
		});
		this.environ_get = syscallWrap(this, "environ_get", function(environ, environ_buf) {
			environ = Number(environ);
			environ_buf = Number(environ_buf);
			if (environ === 0 || environ_buf === 0) return 28;
			const env = _wasi.get(this).env;
			const envSize = encoder.encode(env.join("\0") + "\0").length;
			const { HEAPU8, view } = getMemory(this, Math.max(environ + env.length * 4, environ_buf + envSize));
			for (let i = 0; i < env.length; ++i) {
				const pair = env[i];
				view.setInt32(environ, environ_buf, true);
				environ += 4;
				const data = encoder.encode(pair + "\0");
				HEAPU8.set(data, environ_buf);
				environ_buf += data.length;
			}
			return 0;
		});
		this.environ_sizes_get = syscallWrap(this, "environ_sizes_get", function(len, buflen) {
			len = Number(len);
			buflen = Number(buflen);
			if (len === 0 || buflen === 0) return 28;
			const { view } = getMemory(this, Math.max(len + 4, buflen + 4));
			const wasi = _wasi.get(this);
			view.setUint32(len, wasi.env.length, true);
			view.setUint32(buflen, encoder.encode(wasi.env.join("\0") + "\0").length, true);
			return 0;
		});
		this.clock_res_get = syscallWrap(this, "clock_res_get", function(id, resolution) {
			resolution = Number(resolution);
			if (resolution === 0) return 28;
			const { view } = getMemory(this, resolution + 8);
			switch (id) {
				case 0:
					view.setBigUint64(resolution, BigInt(1e6), true);
					return 0;
				case 1:
				case 2:
				case 3:
					view.setBigUint64(resolution, BigInt(1e3), true);
					return 0;
				default: return 28;
			}
		});
		this.clock_time_get = syscallWrap(this, "clock_time_get", function(id, _percision, time) {
			time = Number(time);
			if (time === 0) return 28;
			const { view } = getMemory(this, time + 8);
			switch (id) {
				case 0:
					view.setBigUint64(time, BigInt(Date.now()) * BigInt(1e6), true);
					return 0;
				case 1:
				case 2:
				case 3: {
					const t = performance.now() / 1e3;
					const s = Math.trunc(t);
					const ms = Math.floor((t - s) * 1e3);
					const result = BigInt(s) * BigInt(1e9) + BigInt(ms) * BigInt(1e6);
					view.setBigUint64(time, result, true);
					return 0;
				}
				default: return 28;
			}
		});
		this.fd_advise = syscallWrap(this, "fd_advise", function(_fd, _offset, _len, _advice) {
			return 52;
		});
		this.fd_fdstat_get = syscallWrap(this, "fd_fdstat_get", function(fd, fdstat) {
			fdstat = Number(fdstat);
			if (fdstat === 0) return 28;
			const fileDescriptor = _wasi.get(this).fds.get(fd, BigInt(0), BigInt(0));
			const { view } = getMemory(this, fdstat + 24);
			view.setUint16(fdstat, fileDescriptor.type, true);
			view.setUint16(fdstat + 2, 0, true);
			view.setBigUint64(fdstat + 8, fileDescriptor.rightsBase, true);
			view.setBigUint64(fdstat + 16, fileDescriptor.rightsInheriting, true);
			return 0;
		});
		this.fd_fdstat_set_flags = syscallWrap(this, "fd_fdstat_set_flags", function(_fd, _flags) {
			return 52;
		});
		this.fd_fdstat_set_rights = syscallWrap(this, "fd_fdstat_set_rights", function(fd, rightsBase, rightsInheriting) {
			const fileDescriptor = _wasi.get(this).fds.get(fd, BigInt(0), BigInt(0));
			if ((rightsBase | fileDescriptor.rightsBase) > fileDescriptor.rightsBase) return 76;
			if ((rightsInheriting | fileDescriptor.rightsInheriting) > fileDescriptor.rightsInheriting) return 76;
			fileDescriptor.rightsBase = rightsBase;
			fileDescriptor.rightsInheriting = rightsInheriting;
			return 0;
		});
		this.fd_prestat_get = syscallWrap(this, "fd_prestat_get", function(fd, prestat) {
			prestat = Number(prestat);
			if (prestat === 0) return 28;
			const wasi = _wasi.get(this);
			let fileDescriptor;
			try {
				fileDescriptor = wasi.fds.get(fd, BigInt(0), BigInt(0));
			} catch (err) {
				if (err instanceof WasiError) return err.errno;
				throw err;
			}
			if (fileDescriptor.preopen !== 1) return 28;
			const { view } = getMemory(this, prestat + 8);
			view.setUint32(prestat, 0, true);
			view.setUint32(prestat + 4, encoder.encode(fileDescriptor.path).length, true);
			return 0;
		});
		this.fd_prestat_dir_name = syscallWrap(this, "fd_prestat_dir_name", function(fd, path, path_len) {
			path = Number(path);
			path_len = Number(path_len);
			if (path === 0) return 28;
			const fileDescriptor = _wasi.get(this).fds.get(fd, BigInt(0), BigInt(0));
			if (fileDescriptor.preopen !== 1) return 8;
			const buffer = encoder.encode(fileDescriptor.path);
			const size = buffer.length;
			if (size > path_len) return 42;
			const { HEAPU8 } = getMemory(this, path + size);
			HEAPU8.set(buffer, path);
			return 0;
		});
		this.fd_seek = syscallWrap(this, "fd_seek", function(fd, offset, whence, newOffset) {
			newOffset = Number(newOffset);
			if (newOffset === 0) return 28;
			if (fd === 0 || fd === 1 || fd === 2) return 0;
			const r = _wasi.get(this).fds.get(fd, WasiRights.FD_SEEK, BigInt(0)).seek(offset, whence);
			const { view } = getMemory(this, newOffset + 8);
			view.setBigUint64(newOffset, r, true);
			return 0;
		});
		this.fd_tell = syscallWrap(this, "fd_tell", function(fd, offset) {
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.FD_TELL, BigInt(0));
			const pos = BigInt(fileDescriptor.pos);
			const { view } = getMemory(this, Number(offset) + 8);
			view.setBigUint64(Number(offset), pos, true);
			return 0;
		});
		this.poll_oneoff = syscallWrap(this, "poll_oneoff", function(in_ptr, out_ptr, nsubscriptions, nevents) {
			in_ptr = Number(in_ptr);
			out_ptr = Number(out_ptr);
			nevents = Number(nevents);
			nsubscriptions = Number(nsubscriptions);
			nsubscriptions = nsubscriptions >>> 0;
			if (in_ptr === 0 || out_ptr === 0 || nsubscriptions === 0 || nevents === 0) return 28;
			const { view } = getMemory(this, Math.max(in_ptr + nsubscriptions * 48, out_ptr + nsubscriptions * 32, nevents + 4));
			view.setUint32(nevents, 0, true);
			let i = 0;
			let timer_userdata = BigInt(0);
			let cur_timeout = BigInt(0);
			let has_timeout = 0;
			let min_timeout = BigInt(0);
			let sub;
			const subscriptions = Array(nsubscriptions);
			for (i = 0; i < nsubscriptions; i++) {
				sub = in_ptr + i * 48;
				const userdata = view.getBigUint64(sub, true);
				const type = view.getUint8(sub + 8);
				const clockIdOrFd = view.getUint32(sub + 16, true);
				const timeout = view.getBigUint64(sub + 24, true);
				const precision = view.getBigUint64(sub + 32, true);
				const flags = view.getUint16(sub + 40, true);
				subscriptions[i] = {
					userdata,
					type,
					u: {
						clock: {
							clock_id: clockIdOrFd,
							timeout,
							precision,
							flags
						},
						fd_readwrite: { fd: clockIdOrFd }
					}
				};
			}
			const fdevents = [];
			for (i = 0; i < nsubscriptions; i++) {
				sub = subscriptions[i];
				switch (sub.type) {
					case 0:
						if (sub.u.clock.flags === 1) {
							const now = BigInt(Date.now()) * BigInt(1e6);
							cur_timeout = sub.u.clock.timeout - now;
						} else cur_timeout = sub.u.clock.timeout;
						if (has_timeout === 0 || cur_timeout < min_timeout) {
							min_timeout = cur_timeout;
							timer_userdata = sub.userdata;
							has_timeout = 1;
						}
						break;
					case 1:
					case 2:
						fdevents.push(sub);
						break;
					default: return 28;
				}
			}
			if (fdevents.length > 0) {
				for (i = 0; i < fdevents.length; i++) {
					const fdevent = fdevents[i];
					const event = out_ptr + 32 * i;
					view.setBigUint64(event, fdevent.userdata, true);
					view.setUint32(event + 8, 52, true);
					view.setUint32(event + 12, fdevent.type, true);
					view.setBigUint64(event + 16, BigInt(0), true);
					view.setUint16(event + 24, 0, true);
					view.setUint32(nevents, 1, true);
				}
				view.setUint32(nevents, fdevents.length, true);
				return 0;
			}
			if (has_timeout) {
				sleepBreakIf(Number(min_timeout / BigInt(1e6)), () => false);
				const event = out_ptr;
				view.setBigUint64(event, timer_userdata, true);
				view.setUint32(event + 8, 0, true);
				view.setUint32(event + 12, 0, true);
				view.setUint32(nevents, 1, true);
			}
			return 0;
		});
		this.proc_exit = syscallWrap(this, "proc_exit", function(rval) {
			if (typeof process === "object" && process !== null && typeof process.exit === "function") process.exit(rval);
			return 0;
		});
		this.proc_raise = syscallWrap(this, "proc_raise", function(_sig) {
			return 52;
		});
		this.sched_yield = syscallWrap(this, "sched_yield", function() {
			return 0;
		});
		this.random_get = typeof crypto !== "undefined" && typeof crypto.getRandomValues === "function" ? syscallWrap(this, "random_get", function(buf, buf_len) {
			buf = Number(buf);
			if (buf === 0) return 28;
			buf_len = Number(buf_len);
			const { HEAPU8, view } = getMemory(this, buf + buf_len);
			if (typeof SharedArrayBuffer === "function" && HEAPU8.buffer instanceof SharedArrayBuffer || Object.prototype.toString.call(HEAPU8.buffer) === "[object SharedArrayBuffer]") {
				for (let i = buf; i < buf + buf_len; ++i) view.setUint8(i, Math.floor(Math.random() * 256));
				return 0;
			}
			let pos;
			const stride = 65536;
			for (pos = 0; pos + stride < buf_len; pos += stride) crypto.getRandomValues(HEAPU8.subarray(buf + pos, buf + pos + stride));
			crypto.getRandomValues(HEAPU8.subarray(buf + pos, buf + buf_len));
			return 0;
		}) : syscallWrap(this, "random_get", function(buf, buf_len) {
			buf = Number(buf);
			if (buf === 0) return 28;
			buf_len = Number(buf_len);
			const { view } = getMemory(this, buf + buf_len);
			for (let i = buf; i < buf + buf_len; ++i) view.setUint8(i, Math.floor(Math.random() * 256));
			return 0;
		});
		this.sock_recv = syscallWrap(this, "sock_recv", function() {
			return 58;
		});
		this.sock_send = syscallWrap(this, "sock_send", function() {
			return 58;
		});
		this.sock_shutdown = syscallWrap(this, "sock_shutdown", function() {
			return 58;
		});
		this.sock_accept = syscallWrap(this, "sock_accept", function() {
			return 58;
		});
		_wasi.set(this, {
			fds,
			args,
			env
		});
		if (fs) _fs.set(this, fs);
		const _this = this;
		function defineImport(name, syncVersion, asyncVersion, parameterType, returnType) {
			if (asyncFs) {
				if (asyncify) _this[name] = asyncify.wrapImportFunction(syscallWrap(_this, name, asyncVersion));
				else _this[name] = wrapAsyncImport(syscallWrap(_this, name, asyncVersion), parameterType, returnType);
			} else _this[name] = syscallWrap(_this, name, syncVersion);
		}
		defineImport("fd_allocate", function fd_allocate(fd, offset, len) {
			const wasi = _wasi.get(this);
			const fs = getFs(this);
			const fileDescriptor = wasi.fds.get(fd, WasiRights.FD_ALLOCATE, BigInt(0));
			if (fs.fstatSync(fileDescriptor.fd, { bigint: true }).size < offset + len) fs.ftruncateSync(fileDescriptor.fd, Number(offset + len));
			return 0;
		}, async function fd_allocate(fd, offset, len) {
			const h = _wasi.get(this).fds.get(fd, WasiRights.FD_ALLOCATE, BigInt(0)).fd;
			if ((await h.stat({ bigint: true })).size < offset + len) await h.truncate(Number(offset + len));
			return 0;
		}, [
			"i32",
			"i64",
			"f64"
		], ["i32"]);
		defineImport("fd_close", function fd_close(fd) {
			const wasi = _wasi.get(this);
			const fileDescriptor = wasi.fds.get(fd, BigInt(0), BigInt(0));
			getFs(this).closeSync(fileDescriptor.fd);
			wasi.fds.remove(fd);
			return 0;
		}, async function fd_close(fd) {
			const wasi = _wasi.get(this);
			await wasi.fds.get(fd, BigInt(0), BigInt(0)).fd.close();
			wasi.fds.remove(fd);
			return 0;
		}, ["i32"], ["i32"]);
		defineImport("fd_datasync", function fd_datasync(fd) {
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.FD_DATASYNC, BigInt(0));
			getFs(this).fdatasyncSync(fileDescriptor.fd);
			return 0;
		}, async function fd_datasync(fd) {
			await _wasi.get(this).fds.get(fd, WasiRights.FD_DATASYNC, BigInt(0)).fd.datasync();
			return 0;
		}, ["i32"], ["i32"]);
		defineImport("fd_filestat_get", function fd_filestat_get(fd, buf) {
			buf = Number(buf);
			if (buf === 0) return 28;
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.FD_FILESTAT_GET, BigInt(0));
			const stat = getFs(this).fstatSync(fileDescriptor.fd, { bigint: true });
			const { view } = getMemory(this, buf + 64);
			toFileStat(view, buf, stat);
			return 0;
		}, async function fd_filestat_get(fd, buf) {
			buf = Number(buf);
			if (buf === 0) return 28;
			const stat = await _wasi.get(this).fds.get(fd, WasiRights.FD_FILESTAT_GET, BigInt(0)).fd.stat({ bigint: true });
			const { view } = getMemory(this, buf + 64);
			toFileStat(view, buf, stat);
			return 0;
		}, ["i32", "i32"], ["i32"]);
		defineImport("fd_filestat_set_size", function fd_filestat_set_size(fd, size) {
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.FD_FILESTAT_SET_SIZE, BigInt(0));
			getFs(this).ftruncateSync(fileDescriptor.fd, Number(size));
			return 0;
		}, async function fd_filestat_set_size(fd, size) {
			await _wasi.get(this).fds.get(fd, WasiRights.FD_FILESTAT_SET_SIZE, BigInt(0)).fd.truncate(Number(size));
			return 0;
		}, ["i32", "i64"], ["i32"]);
		function fdFilestatGetTimes(fd, atim, mtim, flags) {
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.FD_FILESTAT_SET_TIMES, BigInt(0));
			if ((flags & 2) === 2) atim = BigInt(Date.now()) * BigInt(1e6);
			if ((flags & 8) === 8) mtim = BigInt(Date.now()) * BigInt(1e6);
			return {
				fileDescriptor,
				atim,
				mtim
			};
		}
		defineImport("fd_filestat_set_times", function fd_filestat_set_times(fd, atim, mtim, flags) {
			if (validateFstFlagsOrReturn(flags)) return 28;
			const { fileDescriptor, atim: atimRes, mtim: mtimRes } = fdFilestatGetTimes.call(this, fd, atim, mtim, flags);
			getFs(this).futimesSync(fileDescriptor.fd, nanosecondsToSeconds(atimRes), nanosecondsToSeconds(mtimRes));
			return 0;
		}, async function fd_filestat_set_times(fd, atim, mtim, flags) {
			if (validateFstFlagsOrReturn(flags)) return 28;
			const { fileDescriptor, atim: atimRes, mtim: mtimRes } = fdFilestatGetTimes.call(this, fd, atim, mtim, flags);
			await fileDescriptor.fd.utimes(nanosecondsToSeconds(atimRes), nanosecondsToSeconds(mtimRes));
			return 0;
		}, [
			"i32",
			"i64",
			"i64",
			"i32"
		], ["i32"]);
		defineImport("fd_pread", function fd_pread(fd, iovs, iovslen, offset, size) {
			iovs = Number(iovs);
			size = Number(size);
			if (iovs === 0 && iovslen || size === 0 || offset > INT64_MAX) return 28;
			const { HEAPU8, view } = getMemory(this, Math.max(iovs + Number(iovslen) * 8, size + 4));
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.FD_READ | WasiRights.FD_SEEK, BigInt(0));
			if (!iovslen) {
				view.setUint32(size, 0, true);
				return 0;
			}
			let totalSize = 0;
			const ioVecs = Array.from({ length: Number(iovslen) }, (_, i) => {
				const offset = iovs + i * 8;
				const buf = view.getInt32(offset, true);
				const bufLen = view.getUint32(offset + 4, true);
				totalSize += bufLen;
				return HEAPU8.subarray(buf, buf + bufLen);
			});
			let nread = 0;
			const buffer = (() => {
				try {
					return new Uint8Array(new SharedArrayBuffer(totalSize));
				} catch (_) {
					return new Uint8Array(totalSize);
				}
			})();
			buffer._isBuffer = true;
			const bytesRead = getFs(this).readSync(fileDescriptor.fd, buffer, 0, buffer.length, Number(offset));
			nread = buffer ? copyMemory(ioVecs, buffer.subarray(0, bytesRead)) : 0;
			view.setUint32(size, nread, true);
			return 0;
		}, async function(fd, iovs, iovslen, offset, size) {
			iovs = Number(iovs);
			size = Number(size);
			if (iovs === 0 && iovslen || size === 0 || offset > INT64_MAX) return 28;
			const { HEAPU8, view } = getMemory(this, Math.max(iovs + Number(iovslen) * 8, size + 4));
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.FD_READ | WasiRights.FD_SEEK, BigInt(0));
			if (!iovslen) {
				view.setUint32(size, 0, true);
				return 0;
			}
			let totalSize = 0;
			const ioVecs = Array.from({ length: Number(iovslen) }, (_, i) => {
				const offset = iovs + i * 8;
				const buf = view.getInt32(offset, true);
				const bufLen = view.getUint32(offset + 4, true);
				totalSize += bufLen;
				return HEAPU8.subarray(buf, buf + bufLen);
			});
			let nread = 0;
			const buffer = new Uint8Array(totalSize);
			buffer._isBuffer = true;
			const { bytesRead } = await fileDescriptor.fd.read(buffer, 0, buffer.length, Number(offset));
			nread = buffer ? copyMemory(ioVecs, buffer.subarray(0, bytesRead)) : 0;
			view.setUint32(size, nread, true);
			return 0;
		}, [
			"i32",
			"i32",
			"i32",
			"i64",
			"i32"
		], ["i32"]);
		defineImport("fd_pwrite", function fd_pwrite(fd, iovs, iovslen, offset, size) {
			iovs = Number(iovs);
			size = Number(size);
			if (iovs === 0 && iovslen || size === 0 || offset > INT64_MAX) return 28;
			const { HEAPU8, view } = getMemory(this, Math.max(iovs + Number(iovslen) * 8, size + 4));
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.FD_WRITE | WasiRights.FD_SEEK, BigInt(0));
			if (!iovslen) {
				view.setUint32(size, 0, true);
				return 0;
			}
			const buffer = concatBuffer(Array.from({ length: Number(iovslen) }, (_, i) => {
				const offset = iovs + i * 8;
				const buf = view.getInt32(offset, true);
				const bufLen = view.getUint32(offset + 4, true);
				return HEAPU8.subarray(buf, buf + bufLen);
			}));
			const nwritten = getFs(this).writeSync(fileDescriptor.fd, buffer, 0, buffer.length, Number(offset));
			view.setUint32(size, nwritten, true);
			return 0;
		}, async function fd_pwrite(fd, iovs, iovslen, offset, size) {
			iovs = Number(iovs);
			size = Number(size);
			if (iovs === 0 && iovslen || size === 0 || offset > INT64_MAX) return 28;
			const { HEAPU8, view } = getMemory(this, Math.max(iovs + Number(iovslen) * 8, size + 4));
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.FD_WRITE | WasiRights.FD_SEEK, BigInt(0));
			if (!iovslen) {
				view.setUint32(size, 0, true);
				return 0;
			}
			const buffer = concatBuffer(Array.from({ length: Number(iovslen) }, (_, i) => {
				const offset = iovs + i * 8;
				const buf = view.getInt32(offset, true);
				const bufLen = view.getUint32(offset + 4, true);
				return HEAPU8.subarray(buf, buf + bufLen);
			}));
			const { bytesWritten } = await fileDescriptor.fd.write(buffer, 0, buffer.length, Number(offset));
			view.setUint32(size, bytesWritten, true);
			return 0;
		}, [
			"i32",
			"i32",
			"i32",
			"i64",
			"i32"
		], ["i32"]);
		defineImport("fd_read", function fd_read(fd, iovs, iovslen, size) {
			iovs = Number(iovs);
			size = Number(size);
			if (iovs === 0 && iovslen || size === 0) return 28;
			const { HEAPU8, view } = getMemory(this, Math.max(iovs + Number(iovslen) * 8, size + 4));
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.FD_READ, BigInt(0));
			if (!iovslen) {
				view.setUint32(size, 0, true);
				return 0;
			}
			let totalSize = 0;
			const ioVecs = Array.from({ length: Number(iovslen) }, (_, i) => {
				const offset = iovs + i * 8;
				const buf = view.getInt32(offset, true);
				const bufLen = view.getUint32(offset + 4, true);
				totalSize += bufLen;
				return HEAPU8.subarray(buf, buf + bufLen);
			});
			let buffer;
			let nread = 0;
			if (fd === 0) {
				if (typeof window === "undefined" || typeof window.prompt !== "function") return 58;
				buffer = readStdin();
				nread = buffer ? copyMemory(ioVecs, buffer) : 0;
			} else {
				buffer = (() => {
					try {
						return new Uint8Array(new SharedArrayBuffer(totalSize));
					} catch (_) {
						return new Uint8Array(totalSize);
					}
				})();
				buffer._isBuffer = true;
				const bytesRead = getFs(this).readSync(fileDescriptor.fd, buffer, 0, buffer.length, Number(fileDescriptor.pos));
				nread = buffer ? copyMemory(ioVecs, buffer.subarray(0, bytesRead)) : 0;
				fileDescriptor.pos += BigInt(nread);
			}
			view.setUint32(size, nread, true);
			return 0;
		}, async function fd_read(fd, iovs, iovslen, size) {
			iovs = Number(iovs);
			size = Number(size);
			if (iovs === 0 && iovslen || size === 0) return 28;
			const { HEAPU8, view } = getMemory(this, Math.max(iovs + Number(iovslen) * 8, size + 4));
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.FD_READ, BigInt(0));
			if (!iovslen) {
				view.setUint32(size, 0, true);
				return 0;
			}
			let totalSize = 0;
			const ioVecs = Array.from({ length: Number(iovslen) }, (_, i) => {
				const offset = iovs + i * 8;
				const buf = view.getInt32(offset, true);
				const bufLen = view.getUint32(offset + 4, true);
				totalSize += bufLen;
				return HEAPU8.subarray(buf, buf + bufLen);
			});
			let buffer;
			let nread = 0;
			if (fd === 0) {
				if (typeof window === "undefined" || typeof window.prompt !== "function") return 58;
				buffer = readStdin();
				nread = buffer ? copyMemory(ioVecs, buffer) : 0;
			} else {
				buffer = new Uint8Array(totalSize);
				buffer._isBuffer = true;
				const { bytesRead } = await fileDescriptor.fd.read(buffer, 0, buffer.length, Number(fileDescriptor.pos));
				nread = buffer ? copyMemory(ioVecs, buffer.subarray(0, bytesRead)) : 0;
				fileDescriptor.pos += BigInt(nread);
			}
			view.setUint32(size, nread, true);
			return 0;
		}, [
			"i32",
			"i32",
			"i32",
			"i32"
		], ["i32"]);
		defineImport("fd_readdir", function fd_readdir(fd, buf, buf_len, cookie, bufused) {
			buf = Number(buf);
			buf_len = Number(buf_len);
			bufused = Number(bufused);
			if (buf === 0 || bufused === 0) return 0;
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.FD_READDIR, BigInt(0));
			const fs = getFs(this);
			const entries = fs.readdirSync(fileDescriptor.realPath, { withFileTypes: true });
			const { HEAPU8, view } = getMemory(this, Math.max(buf + buf_len, bufused + 4));
			let bufferUsed = 0;
			for (let i = Number(cookie); i < entries.length; i++) {
				const nameData = encoder.encode(entries[i].name);
				const entryInfo = fs.statSync(resolve(fileDescriptor.realPath, entries[i].name), { bigint: true });
				const entryData = new Uint8Array(24 + nameData.byteLength);
				const entryView = new DataView(entryData.buffer);
				entryView.setBigUint64(0, BigInt(i + 1), true);
				entryView.setBigUint64(8, BigInt(entryInfo.ino ? entryInfo.ino : 0), true);
				entryView.setUint32(16, nameData.byteLength, true);
				let type;
				if (entries[i].isFile()) type = 4;
				else if (entries[i].isDirectory()) type = 3;
				else if (entries[i].isSymbolicLink()) type = 7;
				else if (entries[i].isCharacterDevice()) type = 2;
				else if (entries[i].isBlockDevice()) type = 1;
				else if (entries[i].isSocket()) type = 6;
				else type = 0;
				entryView.setUint8(20, type);
				entryData.set(nameData, 24);
				const data = entryData.slice(0, Math.min(entryData.length, buf_len - bufferUsed));
				HEAPU8.set(data, buf + bufferUsed);
				bufferUsed += data.byteLength;
			}
			view.setUint32(bufused, bufferUsed, true);
			return 0;
		}, async function fd_readdir(fd, buf, buf_len, cookie, bufused) {
			buf = Number(buf);
			buf_len = Number(buf_len);
			bufused = Number(bufused);
			if (buf === 0 || bufused === 0) return 0;
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.FD_READDIR, BigInt(0));
			const fs = getFs(this);
			const entries = await fs.promises.readdir(fileDescriptor.realPath, { withFileTypes: true });
			const { HEAPU8, view } = getMemory(this, Math.max(buf + buf_len, bufused + 4));
			let bufferUsed = 0;
			for (let i = Number(cookie); i < entries.length; i++) {
				const nameData = encoder.encode(entries[i].name);
				const entryInfo = await fs.promises.stat(resolve(fileDescriptor.realPath, entries[i].name), { bigint: true });
				const entryData = new Uint8Array(24 + nameData.byteLength);
				const entryView = new DataView(entryData.buffer);
				entryView.setBigUint64(0, BigInt(i + 1), true);
				entryView.setBigUint64(8, BigInt(entryInfo.ino ? entryInfo.ino : 0), true);
				entryView.setUint32(16, nameData.byteLength, true);
				let type;
				if (entries[i].isFile()) type = 4;
				else if (entries[i].isDirectory()) type = 3;
				else if (entries[i].isSymbolicLink()) type = 7;
				else if (entries[i].isCharacterDevice()) type = 2;
				else if (entries[i].isBlockDevice()) type = 1;
				else if (entries[i].isSocket()) type = 6;
				else type = 0;
				entryView.setUint8(20, type);
				entryData.set(nameData, 24);
				const data = entryData.slice(0, Math.min(entryData.length, buf_len - bufferUsed));
				HEAPU8.set(data, buf + bufferUsed);
				bufferUsed += data.byteLength;
			}
			view.setUint32(bufused, bufferUsed, true);
			return 0;
		}, [
			"i32",
			"i32",
			"i32",
			"i64",
			"i32"
		], ["i32"]);
		defineImport("fd_renumber", function fd_renumber(from, to) {
			_wasi.get(this).fds.renumber(to, from);
			return 0;
		}, async function fd_renumber(from, to) {
			await _wasi.get(this).fds.renumber(to, from);
			return 0;
		}, ["i32", "i32"], ["i32"]);
		defineImport("fd_sync", function fd_sync(fd) {
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.FD_SYNC, BigInt(0));
			getFs(this).fsyncSync(fileDescriptor.fd);
			return 0;
		}, async function fd_sync(fd) {
			await _wasi.get(this).fds.get(fd, WasiRights.FD_SYNC, BigInt(0)).fd.sync();
			return 0;
		}, ["i32"], ["i32"]);
		defineImport("fd_write", function fd_write(fd, iovs, iovslen, size) {
			iovs = Number(iovs);
			size = Number(size);
			if (iovs === 0 && iovslen || size === 0) return 28;
			const { HEAPU8, view } = getMemory(this, Math.max(iovs + Number(iovslen) * 8, size + 4));
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.FD_WRITE, BigInt(0));
			if (!iovslen) {
				view.setUint32(size, 0, true);
				return 0;
			}
			const buffer = concatBuffer(Array.from({ length: Number(iovslen) }, (_, i) => {
				const offset = iovs + i * 8;
				const buf = view.getInt32(offset, true);
				const bufLen = view.getUint32(offset + 4, true);
				return HEAPU8.subarray(buf, buf + bufLen);
			}));
			let nwritten;
			if (fd === 1 || fd === 2) nwritten = fileDescriptor.write(buffer);
			else {
				nwritten = getFs(this).writeSync(fileDescriptor.fd, buffer, 0, buffer.length, Number(fileDescriptor.pos));
				fileDescriptor.pos += BigInt(nwritten);
			}
			view.setUint32(size, nwritten, true);
			return 0;
		}, async function fd_write(fd, iovs, iovslen, size) {
			iovs = Number(iovs);
			size = Number(size);
			if (iovs === 0 && iovslen || size === 0) return 28;
			const { HEAPU8, view } = getMemory(this, Math.max(iovs + Number(iovslen) * 8, size + 4));
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.FD_WRITE, BigInt(0));
			if (!iovslen) {
				view.setUint32(size, 0, true);
				return 0;
			}
			const buffer = concatBuffer(Array.from({ length: Number(iovslen) }, (_, i) => {
				const offset = iovs + i * 8;
				const buf = view.getInt32(offset, true);
				const bufLen = view.getUint32(offset + 4, true);
				return HEAPU8.subarray(buf, buf + bufLen);
			}));
			let nwritten;
			if (fd === 1 || fd === 2) nwritten = fileDescriptor.write(buffer);
			else {
				nwritten = await (await fileDescriptor.fd.write(buffer, 0, buffer.length, Number(fileDescriptor.pos))).bytesWritten;
				fileDescriptor.pos += BigInt(nwritten);
			}
			view.setUint32(size, nwritten, true);
			return 0;
		}, [
			"i32",
			"i32",
			"i32",
			"i32"
		], ["i32"]);
		defineImport("path_create_directory", function path_create_directory(fd, path, path_len) {
			path = Number(path);
			path_len = Number(path_len);
			if (path === 0) return 28;
			const { HEAPU8 } = getMemory(this, path + path_len);
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.PATH_CREATE_DIRECTORY, BigInt(0));
			let pathString = decoder.decode(unsharedSlice(HEAPU8, path, path + path_len));
			pathString = resolve(fileDescriptor.realPath, pathString);
			getFs(this).mkdirSync(pathString);
			return 0;
		}, async function path_create_directory(fd, path, path_len) {
			path = Number(path);
			path_len = Number(path_len);
			if (path === 0) return 28;
			const { HEAPU8 } = getMemory(this, path + path_len);
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.PATH_CREATE_DIRECTORY, BigInt(0));
			let pathString = decoder.decode(unsharedSlice(HEAPU8, path, path + path_len));
			pathString = resolve(fileDescriptor.realPath, pathString);
			await getFs(this).promises.mkdir(pathString);
			return 0;
		}, [
			"i32",
			"i32",
			"i32"
		], ["i32"]);
		defineImport("path_filestat_get", function path_filestat_get(fd, flags, path, path_len, filestat) {
			path = Number(path);
			path_len = Number(path_len);
			filestat = Number(filestat);
			if (path === 0 || filestat === 0) return 28;
			const { HEAPU8, view } = getMemory(this, Math.max(path + path_len, filestat + 64));
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.PATH_FILESTAT_GET, BigInt(0));
			let pathString = decoder.decode(unsharedSlice(HEAPU8, path, path + path_len));
			const fs = getFs(this);
			pathString = resolve(fileDescriptor.realPath, pathString);
			let stat;
			if ((flags & 1) === 1) stat = fs.statSync(pathString, { bigint: true });
			else stat = fs.lstatSync(pathString, { bigint: true });
			toFileStat(view, filestat, stat);
			return 0;
		}, async function path_filestat_get(fd, flags, path, path_len, filestat) {
			path = Number(path);
			path_len = Number(path_len);
			filestat = Number(filestat);
			if (path === 0 || filestat === 0) return 28;
			const { HEAPU8, view } = getMemory(this, Math.max(path + path_len, filestat + 64));
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.PATH_FILESTAT_GET, BigInt(0));
			let pathString = decoder.decode(unsharedSlice(HEAPU8, path, path + path_len));
			const fs = getFs(this);
			pathString = resolve(fileDescriptor.realPath, pathString);
			let stat;
			if ((flags & 1) === 1) stat = await fs.promises.stat(pathString, { bigint: true });
			else stat = await fs.promises.lstat(pathString, { bigint: true });
			toFileStat(view, filestat, stat);
			return 0;
		}, [
			"i32",
			"i32",
			"i32",
			"i32",
			"i32"
		], ["i32"]);
		defineImport("path_filestat_set_times", function path_filestat_set_times(fd, flags, path, path_len, atim, mtim, fst_flags) {
			path = Number(path);
			path_len = Number(path_len);
			if (path === 0) return 28;
			const { HEAPU8 } = getMemory(this, path + path_len);
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.PATH_FILESTAT_SET_TIMES, BigInt(0));
			if (validateFstFlagsOrReturn(fst_flags)) return 28;
			const fs = getFs(this);
			const resolvedPath = resolvePathSync(fs, fileDescriptor, decoder.decode(unsharedSlice(HEAPU8, path, path + path_len)), flags);
			if ((fst_flags & 2) === 2) atim = BigInt(Date.now()) * BigInt(1e6);
			if ((fst_flags & 8) === 8) mtim = BigInt(Date.now()) * BigInt(1e6);
			fs.utimesSync(resolvedPath, nanosecondsToSeconds(atim), nanosecondsToSeconds(mtim));
			return 0;
		}, async function path_filestat_set_times(fd, flags, path, path_len, atim, mtim, fst_flags) {
			path = Number(path);
			path_len = Number(path_len);
			if (path === 0) return 28;
			const { HEAPU8 } = getMemory(this, path + path_len);
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.PATH_FILESTAT_SET_TIMES, BigInt(0));
			if (validateFstFlagsOrReturn(fst_flags)) return 28;
			const fs = getFs(this);
			const resolvedPath = await resolvePathAsync(fs, fileDescriptor, decoder.decode(unsharedSlice(HEAPU8, path, path + path_len)), flags);
			if ((fst_flags & 2) === 2) atim = BigInt(Date.now()) * BigInt(1e6);
			if ((fst_flags & 8) === 8) mtim = BigInt(Date.now()) * BigInt(1e6);
			await fs.promises.utimes(resolvedPath, nanosecondsToSeconds(atim), nanosecondsToSeconds(mtim));
			return 0;
		}, [
			"i32",
			"i32",
			"i32",
			"i32",
			"i64",
			"i64",
			"i32"
		], ["i32"]);
		defineImport("path_link", function path_link(old_fd, old_flags, old_path, old_path_len, new_fd, new_path, new_path_len) {
			old_path = Number(old_path);
			old_path_len = Number(old_path_len);
			new_path = Number(new_path);
			new_path_len = Number(new_path_len);
			if (old_path === 0 || new_path === 0) return 28;
			const wasi = _wasi.get(this);
			let oldWrap;
			let newWrap;
			if (old_fd === new_fd) oldWrap = newWrap = wasi.fds.get(old_fd, WasiRights.PATH_LINK_SOURCE | WasiRights.PATH_LINK_TARGET, BigInt(0));
			else {
				oldWrap = wasi.fds.get(old_fd, WasiRights.PATH_LINK_SOURCE, BigInt(0));
				newWrap = wasi.fds.get(new_fd, WasiRights.PATH_LINK_TARGET, BigInt(0));
			}
			const { HEAPU8 } = getMemory(this, Math.max(old_path + old_path_len, new_path + new_path_len));
			const fs = getFs(this);
			const resolvedOldPath = resolvePathSync(fs, oldWrap, decoder.decode(unsharedSlice(HEAPU8, old_path, old_path + old_path_len)), old_flags);
			const resolvedNewPath = resolve(newWrap.realPath, decoder.decode(unsharedSlice(HEAPU8, new_path, new_path + new_path_len)));
			fs.linkSync(resolvedOldPath, resolvedNewPath);
			return 0;
		}, async function path_link(old_fd, old_flags, old_path, old_path_len, new_fd, new_path, new_path_len) {
			old_path = Number(old_path);
			old_path_len = Number(old_path_len);
			new_path = Number(new_path);
			new_path_len = Number(new_path_len);
			if (old_path === 0 || new_path === 0) return 28;
			const wasi = _wasi.get(this);
			let oldWrap;
			let newWrap;
			if (old_fd === new_fd) oldWrap = newWrap = wasi.fds.get(old_fd, WasiRights.PATH_LINK_SOURCE | WasiRights.PATH_LINK_TARGET, BigInt(0));
			else {
				oldWrap = wasi.fds.get(old_fd, WasiRights.PATH_LINK_SOURCE, BigInt(0));
				newWrap = wasi.fds.get(new_fd, WasiRights.PATH_LINK_TARGET, BigInt(0));
			}
			const { HEAPU8 } = getMemory(this, Math.max(old_path + old_path_len, new_path + new_path_len));
			const fs = getFs(this);
			const resolvedOldPath = await resolvePathAsync(fs, oldWrap, decoder.decode(unsharedSlice(HEAPU8, old_path, old_path + old_path_len)), old_flags);
			const resolvedNewPath = resolve(newWrap.realPath, decoder.decode(unsharedSlice(HEAPU8, new_path, new_path + new_path_len)));
			await fs.promises.link(resolvedOldPath, resolvedNewPath);
			return 0;
		}, [
			"i32",
			"i32",
			"i32",
			"i32",
			"i32",
			"i32",
			"i32"
		], ["i32"]);
		function pathOpen(o_flags, fs_rights_base, fs_rights_inheriting, fs_flags) {
			const read = (fs_rights_base & (WasiRights.FD_READ | WasiRights.FD_READDIR)) !== BigInt(0);
			const write = (fs_rights_base & (WasiRights.FD_DATASYNC | WasiRights.FD_WRITE | WasiRights.FD_ALLOCATE | WasiRights.FD_FILESTAT_SET_SIZE)) !== BigInt(0);
			let flags = write ? read ? 2 : 1 : 0;
			let needed_base = WasiRights.PATH_OPEN;
			let needed_inheriting = fs_rights_base | fs_rights_inheriting;
			if ((o_flags & 1) !== 0) {
				flags |= 64;
				needed_base |= WasiRights.PATH_CREATE_FILE;
			}
			if ((o_flags & 2) !== 0) flags |= 65536;
			if ((o_flags & 4) !== 0) flags |= 128;
			if ((o_flags & 8) !== 0) {
				flags |= 512;
				needed_base |= WasiRights.PATH_FILESTAT_SET_SIZE;
			}
			if ((fs_flags & 1) !== 0) flags |= 1024;
			if ((fs_flags & 2) !== 0) needed_inheriting |= WasiRights.FD_DATASYNC;
			if ((fs_flags & 4) !== 0) flags |= 2048;
			if ((fs_flags & 8) !== 0) {
				flags |= 1052672;
				needed_inheriting |= WasiRights.FD_SYNC;
			}
			if ((fs_flags & 16) !== 0) {
				flags |= 1052672;
				needed_inheriting |= WasiRights.FD_SYNC;
			}
			if (write && (flags & 1536) === 0) needed_inheriting |= WasiRights.FD_SEEK;
			return {
				flags,
				needed_base,
				needed_inheriting
			};
		}
		defineImport("path_open", function path_open(dirfd, dirflags, path, path_len, o_flags, fs_rights_base, fs_rights_inheriting, fs_flags, fd) {
			path = Number(path);
			fd = Number(fd);
			if (path === 0 || fd === 0) return 28;
			path_len = Number(path_len);
			fs_rights_base = BigInt(fs_rights_base);
			fs_rights_inheriting = BigInt(fs_rights_inheriting);
			const { flags: flagsRes, needed_base: neededBase, needed_inheriting: neededInheriting } = pathOpen(o_flags, fs_rights_base, fs_rights_inheriting, fs_flags);
			const wasi = _wasi.get(this);
			const fileDescriptor = wasi.fds.get(dirfd, neededBase, neededInheriting);
			const memory = getMemory(this, Math.max(path + path_len, fd + 4));
			const HEAPU8 = memory.HEAPU8;
			const pathString = decoder.decode(unsharedSlice(HEAPU8, path, path + path_len));
			const fs = getFs(this);
			const resolved_path = resolvePathSync(fs, fileDescriptor, pathString, dirflags);
			const r = fs.openSync(resolved_path, _isWin32Flags ? _toWinOpenFlags(flagsRes) : flagsRes, 438);
			const filetype = wasi.fds.getFileTypeByFd(r);
			if (filetype !== 3 && ((o_flags & 2) !== 0 || resolved_path.endsWith("/"))) return 54;
			const { base: max_base, inheriting: max_inheriting } = getRights(wasi.fds.stdio, r, flagsRes, filetype);
			const wrap = wasi.fds.insert(r, resolved_path, resolved_path, filetype, fs_rights_base & max_base, fs_rights_inheriting & max_inheriting, 0);
			const stat = fs.fstatSync(r, { bigint: true });
			if (stat.isFile()) {
				wrap.size = stat.size;
				if ((flagsRes & 1024) !== 0) wrap.pos = stat.size;
			}
			memory.view.setInt32(fd, wrap.id, true);
			return 0;
		}, async function path_open(dirfd, dirflags, path, path_len, o_flags, fs_rights_base, fs_rights_inheriting, fs_flags, fd) {
			path = Number(path);
			fd = Number(fd);
			if (path === 0 || fd === 0) return 28;
			path_len = Number(path_len);
			fs_rights_base = BigInt(fs_rights_base);
			fs_rights_inheriting = BigInt(fs_rights_inheriting);
			const { flags: flagsRes, needed_base: neededBase, needed_inheriting: neededInheriting } = pathOpen(o_flags, fs_rights_base, fs_rights_inheriting, fs_flags);
			const wasi = _wasi.get(this);
			const fileDescriptor = wasi.fds.get(dirfd, neededBase, neededInheriting);
			const memory = getMemory(this, Math.max(path + path_len, fd + 4));
			const HEAPU8 = memory.HEAPU8;
			const pathString = decoder.decode(unsharedSlice(HEAPU8, path, path + path_len));
			const fs = getFs(this);
			const resolved_path = await resolvePathAsync(fs, fileDescriptor, pathString, dirflags);
			const r = await fs.promises.open(resolved_path, _isWin32Flags ? _toWinOpenFlags(flagsRes) : flagsRes, 438);
			const filetype = await wasi.fds.getFileTypeByFd(r);
			if ((o_flags & 2) !== 0 && filetype !== 3) return 54;
			const { base: max_base, inheriting: max_inheriting } = getRights(wasi.fds.stdio, r.fd, flagsRes, filetype);
			const wrap = wasi.fds.insert(r, resolved_path, resolved_path, filetype, fs_rights_base & max_base, fs_rights_inheriting & max_inheriting, 0);
			const stat = await r.stat({ bigint: true });
			if (stat.isFile()) {
				wrap.size = stat.size;
				if ((flagsRes & 1024) !== 0) wrap.pos = stat.size;
			}
			memory.view.setInt32(fd, wrap.id, true);
			return 0;
		}, [
			"i32",
			"i32",
			"i32",
			"i32",
			"i32",
			"i64",
			"i64",
			"i32",
			"i32"
		], ["i32"]);
		defineImport("path_readlink", function path_readlink(fd, path, path_len, buf, buf_len, bufused) {
			path = Number(path);
			path_len = Number(path_len);
			buf = Number(buf);
			buf_len = Number(buf_len);
			bufused = Number(bufused);
			if (path === 0 || buf === 0 || bufused === 0) return 28;
			const { HEAPU8, view } = getMemory(this, Math.max(path + path_len, buf + buf_len, bufused + 4));
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.PATH_READLINK, BigInt(0));
			let pathString = decoder.decode(unsharedSlice(HEAPU8, path, path + path_len));
			pathString = resolve(fileDescriptor.realPath, pathString);
			const link = getFs(this).readlinkSync(pathString);
			const linkData = encoder.encode(link);
			const len = Math.min(linkData.length, buf_len);
			if (len >= buf_len) return 42;
			HEAPU8.set(linkData.subarray(0, len), buf);
			HEAPU8[buf + len] = 0;
			view.setUint32(bufused, len, true);
			return 0;
		}, async function path_readlink(fd, path, path_len, buf, buf_len, bufused) {
			path = Number(path);
			path_len = Number(path_len);
			buf = Number(buf);
			buf_len = Number(buf_len);
			bufused = Number(bufused);
			if (path === 0 || buf === 0 || bufused === 0) return 28;
			const { HEAPU8, view } = getMemory(this, Math.max(path + path_len, buf + buf_len, bufused + 4));
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.PATH_READLINK, BigInt(0));
			let pathString = decoder.decode(unsharedSlice(HEAPU8, path, path + path_len));
			pathString = resolve(fileDescriptor.realPath, pathString);
			const link = await getFs(this).promises.readlink(pathString);
			const linkData = encoder.encode(link);
			const len = Math.min(linkData.length, buf_len);
			if (len >= buf_len) return 42;
			HEAPU8.set(linkData.subarray(0, len), buf);
			HEAPU8[buf + len] = 0;
			view.setUint32(bufused, len, true);
			return 0;
		}, [
			"i32",
			"i32",
			"i32",
			"i32",
			"i32",
			"i32"
		], ["i32"]);
		defineImport("path_remove_directory", function path_remove_directory(fd, path, path_len) {
			path = Number(path);
			path_len = Number(path_len);
			if (path === 0) return 28;
			const { HEAPU8 } = getMemory(this, path + path_len);
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.PATH_REMOVE_DIRECTORY, BigInt(0));
			let pathString = decoder.decode(unsharedSlice(HEAPU8, path, path + path_len));
			pathString = resolve(fileDescriptor.realPath, pathString);
			getFs(this).rmdirSync(pathString);
			return 0;
		}, async function path_remove_directory(fd, path, path_len) {
			path = Number(path);
			path_len = Number(path_len);
			if (path === 0) return 28;
			const { HEAPU8 } = getMemory(this, path + path_len);
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.PATH_REMOVE_DIRECTORY, BigInt(0));
			let pathString = decoder.decode(unsharedSlice(HEAPU8, path, path + path_len));
			pathString = resolve(fileDescriptor.realPath, pathString);
			await getFs(this).promises.rmdir(pathString);
			return 0;
		}, [
			"i32",
			"i32",
			"i32"
		], ["i32"]);
		defineImport("path_rename", function path_rename(old_fd, old_path, old_path_len, new_fd, new_path, new_path_len) {
			old_path = Number(old_path);
			old_path_len = Number(old_path_len);
			new_path = Number(new_path);
			new_path_len = Number(new_path_len);
			if (old_path === 0 || new_path === 0) return 28;
			const wasi = _wasi.get(this);
			let oldWrap;
			let newWrap;
			if (old_fd === new_fd) oldWrap = newWrap = wasi.fds.get(old_fd, WasiRights.PATH_RENAME_SOURCE | WasiRights.PATH_RENAME_TARGET, BigInt(0));
			else {
				oldWrap = wasi.fds.get(old_fd, WasiRights.PATH_RENAME_SOURCE, BigInt(0));
				newWrap = wasi.fds.get(new_fd, WasiRights.PATH_RENAME_TARGET, BigInt(0));
			}
			const { HEAPU8 } = getMemory(this, Math.max(old_path + old_path_len, new_path + new_path_len));
			const resolvedOldPath = resolve(oldWrap.realPath, decoder.decode(unsharedSlice(HEAPU8, old_path, old_path + old_path_len)));
			const resolvedNewPath = resolve(newWrap.realPath, decoder.decode(unsharedSlice(HEAPU8, new_path, new_path + new_path_len)));
			getFs(this).renameSync(resolvedOldPath, resolvedNewPath);
			return 0;
		}, async function path_rename(old_fd, old_path, old_path_len, new_fd, new_path, new_path_len) {
			old_path = Number(old_path);
			old_path_len = Number(old_path_len);
			new_path = Number(new_path);
			new_path_len = Number(new_path_len);
			if (old_path === 0 || new_path === 0) return 28;
			const wasi = _wasi.get(this);
			let oldWrap;
			let newWrap;
			if (old_fd === new_fd) oldWrap = newWrap = wasi.fds.get(old_fd, WasiRights.PATH_RENAME_SOURCE | WasiRights.PATH_RENAME_TARGET, BigInt(0));
			else {
				oldWrap = wasi.fds.get(old_fd, WasiRights.PATH_RENAME_SOURCE, BigInt(0));
				newWrap = wasi.fds.get(new_fd, WasiRights.PATH_RENAME_TARGET, BigInt(0));
			}
			const { HEAPU8 } = getMemory(this, Math.max(old_path + old_path_len, new_path + new_path_len));
			const resolvedOldPath = resolve(oldWrap.realPath, decoder.decode(unsharedSlice(HEAPU8, old_path, old_path + old_path_len)));
			const resolvedNewPath = resolve(newWrap.realPath, decoder.decode(unsharedSlice(HEAPU8, new_path, new_path + new_path_len)));
			await getFs(this).promises.rename(resolvedOldPath, resolvedNewPath);
			return 0;
		}, [
			"i32",
			"i32",
			"i32",
			"i32",
			"i32",
			"i32"
		], ["i32"]);
		defineImport("path_symlink", function path_symlink(old_path, old_path_len, fd, new_path, new_path_len) {
			old_path = Number(old_path);
			old_path_len = Number(old_path_len);
			new_path = Number(new_path);
			new_path_len = Number(new_path_len);
			if (old_path === 0 || new_path === 0) return 28;
			const { HEAPU8 } = getMemory(this, Math.max(old_path + old_path_len, new_path + new_path_len));
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.PATH_SYMLINK, BigInt(0));
			const oldPath = decoder.decode(unsharedSlice(HEAPU8, old_path, old_path + old_path_len));
			if (oldPath.length > 0 && oldPath[0] === "/") return 63;
			let newPath = decoder.decode(unsharedSlice(HEAPU8, new_path, new_path + new_path_len));
			newPath = resolve(fileDescriptor.realPath, newPath);
			getFs(this).symlinkSync(oldPath, newPath);
			return 0;
		}, async function path_symlink(old_path, old_path_len, fd, new_path, new_path_len) {
			old_path = Number(old_path);
			old_path_len = Number(old_path_len);
			new_path = Number(new_path);
			new_path_len = Number(new_path_len);
			if (old_path === 0 || new_path === 0) return 28;
			const { HEAPU8 } = getMemory(this, Math.max(old_path + old_path_len, new_path + new_path_len));
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.PATH_SYMLINK, BigInt(0));
			const oldPath = decoder.decode(unsharedSlice(HEAPU8, old_path, old_path + old_path_len));
			let newPath = decoder.decode(unsharedSlice(HEAPU8, new_path, new_path + new_path_len));
			newPath = resolve(fileDescriptor.realPath, newPath);
			await getFs(this).promises.symlink(oldPath, newPath);
			return 0;
		}, [
			"i32",
			"i32",
			"i32",
			"i32",
			"i32"
		], ["i32"]);
		defineImport("path_unlink_file", function path_unlink_file(fd, path, path_len) {
			path = Number(path);
			path_len = Number(path_len);
			if (path === 0) return 28;
			const { HEAPU8 } = getMemory(this, path + path_len);
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.PATH_UNLINK_FILE, BigInt(0));
			let pathString = decoder.decode(unsharedSlice(HEAPU8, path, path + path_len));
			pathString = resolve(fileDescriptor.realPath, pathString);
			getFs(this).unlinkSync(pathString);
			return 0;
		}, async function path_unlink_file(fd, path, path_len) {
			path = Number(path);
			path_len = Number(path_len);
			if (path === 0) return 28;
			const { HEAPU8 } = getMemory(this, path + path_len);
			const fileDescriptor = _wasi.get(this).fds.get(fd, WasiRights.PATH_UNLINK_FILE, BigInt(0));
			let pathString = decoder.decode(unsharedSlice(HEAPU8, path, path + path_len));
			pathString = resolve(fileDescriptor.realPath, pathString);
			await getFs(this).promises.unlink(pathString);
			return 0;
		}, [
			"i32",
			"i32",
			"i32"
		], ["i32"]);
		this._setMemory = function setMemory(m) {
			if (!(m instanceof _WebAssembly.Memory)) throw new TypeError("\"instance.exports.memory\" property must be a WebAssembly.Memory");
			_memory.set(_this, extendMemory(m));
		};
	}
	static createSync(args, env, preopens, stdio, fs, print, printErr) {
		const fds = new SyncTable({
			size: 3,
			in: stdio[0],
			out: stdio[1],
			err: stdio[2],
			fs,
			print,
			printErr
		});
		const _this = new WASI$1(args, env, fds, false, fs);
		if (preopens.length > 0) for (let i = 0; i < preopens.length; ++i) {
			const realPath = fs.realpathSync(preopens[i].realPath, "utf8");
			const fd = fs.openSync(realPath, "r", 438);
			fds.insertPreopen(fd, preopens[i].mappedPath, realPath);
		}
		return _this;
	}
	static async createAsync(args, env, preopens, stdio, fs, print, printErr, asyncify) {
		const fds = new AsyncTable({
			size: 3,
			in: stdio[0],
			out: stdio[1],
			err: stdio[2],
			print,
			printErr
		});
		const _this = new WASI$1(args, env, fds, true, fs, asyncify);
		if (preopens.length > 0) for (let i = 0; i < preopens.length; ++i) {
			const entry = preopens[i];
			const realPath = await fs.promises.realpath(entry.realPath);
			const fd = await fs.promises.open(realPath, "r", 438);
			await fds.insertPreopen(fd, entry.mappedPath, realPath);
		}
		return _this;
	}
};
const kEmptyObject = /*#__PURE__*/ Object.freeze(/*#__PURE__*/ Object.create(null));
const kExitCode = Symbol("kExitCode");
const kSetMemory = Symbol("kSetMemory");
const kStarted = Symbol("kStarted");
const kInstance = Symbol("kInstance");
const kBindingName = Symbol("kBindingName");
function validateOptions(options) {
	var _a;
	validateObject(options, "options");
	let _WASI;
	if (options.version !== void 0) {
		validateString(options.version, "options.version");
		switch (options.version) {
			case "unstable":
				_WASI = WASI$1;
				this[kBindingName] = "wasi_unstable";
				break;
			case "preview1":
				_WASI = WASI$1;
				this[kBindingName] = "wasi_snapshot_preview1";
				break;
			default: throw new TypeError(`unsupported WASI version "${options.version}"`);
		}
	} else {
		_WASI = WASI$1;
		this[kBindingName] = "wasi_snapshot_preview1";
	}
	if (options.args !== void 0) validateArray(options.args, "options.args");
	const args = ((_a = options.args) !== null && _a !== void 0 ? _a : []).map(String);
	const env = [];
	if (options.env !== void 0) {
		validateObject(options.env, "options.env");
		Object.entries(options.env).forEach(({ 0: key, 1: value }) => {
			if (value !== void 0) env.push(`${key}=${value}`);
		});
	}
	const preopens = [];
	if (options.preopens !== void 0) {
		validateObject(options.preopens, "options.preopens");
		Object.entries(options.preopens).forEach(({ 0: key, 1: value }) => preopens.push({
			mappedPath: String(key),
			realPath: String(value)
		}));
	}
	if (preopens.length > 0) {
		if (options.fs === void 0) throw new Error("filesystem is disabled, can not preopen directory");
		try {
			validateObject(options.fs, "options.fs");
		} catch (_) {
			throw new TypeError("Node.js fs like implementation is not provided");
		}
	}
	if (options.print !== void 0) validateFunction(options.print, "options.print");
	if (options.printErr !== void 0) validateFunction(options.printErr, "options.printErr");
	if (options.returnOnExit !== void 0) validateBoolean(options.returnOnExit, "options.returnOnExit");
	return {
		args,
		env,
		preopens,
		stdio: [
			0,
			1,
			2
		],
		_WASI
	};
}
function initWASI(setMemory, wrap) {
	this[kSetMemory] = setMemory;
	this.wasiImport = wrap;
	this[kStarted] = false;
	this[kExitCode] = 0;
	this[kInstance] = void 0;
}
/** @public */
var WASI = class {
	constructor(options = kEmptyObject) {
		const { args, env, preopens, stdio, _WASI } = validateOptions.call(this, options);
		const wrap = _WASI.createSync(args, env, preopens, stdio, options.fs, options.print, options.printErr);
		const setMemory = wrap._setMemory;
		delete wrap._setMemory;
		initWASI.call(this, setMemory, wrap);
		if (options.returnOnExit) wrap.proc_exit = wasiReturnOnProcExit.bind(this);
	}
	finalizeBindings(instance, _a) {
		var _b;
		var { memory = (_b = instance === null || instance === void 0 ? void 0 : instance.exports) === null || _b === void 0 ? void 0 : _b.memory } = _a === void 0 ? {} : _a;
		if (this[kStarted]) throw new Error("WASI instance has already started");
		validateObject(instance, "instance");
		validateObject(instance.exports, "instance.exports");
		this[kSetMemory](memory);
		this[kInstance] = instance;
		this[kStarted] = true;
	}
	start(instance) {
		this.finalizeBindings(instance);
		const { _start, _initialize } = this[kInstance].exports;
		validateFunction(_start, "instance.exports._start");
		validateUndefined(_initialize, "instance.exports._initialize");
		let ret;
		try {
			ret = _start();
		} catch (err) {
			if (err !== kExitCode) throw err;
		}
		if (ret instanceof Promise) return ret.then(() => this[kExitCode], (err) => {
			if (err !== kExitCode) throw err;
			return this[kExitCode];
		});
		return this[kExitCode];
	}
	initialize(instance) {
		this.finalizeBindings(instance);
		const { _start, _initialize } = this[kInstance].exports;
		validateUndefined(_start, "instance.exports._start");
		if (_initialize !== void 0) {
			validateFunction(_initialize, "instance.exports._initialize");
			return _initialize();
		}
	}
	getImportObject() {
		return { [this[kBindingName]]: this.wasiImport };
	}
};
function wasiReturnOnProcExit(rval) {
	this[kExitCode] = rval;
	throw kExitCode;
}
// node_modules/@napi-rs/wasm-runtime/runtime.js
const { instantiateNapiModuleSync, instantiateNapiModule, MessageHandler } = emnapi_core_esm_bundler_exports;
const { createContext, getDefaultContext } = emnapi_esm_bundler_exports;
// node_modules/@astrojs/astro2tsx/index.js
const wasi = new WASI({
	version: "preview1",
	env: process.env
});
const memory = new WebAssembly.Memory({
	initial: 4e3,
	maximum: 65536,
	shared: true
});
const { napiModule } = instantiateNapiModuleSync(readFileSync(new URL("./astro2tsx.wasm32-wasi.wasm", import.meta.url)), {
	context: getDefaultContext(),
	wasi,
	overwriteImports(importObject) {
		importObject.env = {
			...importObject.env,
			...importObject.napi,
			...importObject.emnapi,
			memory
		};
		return importObject;
	},
	beforeInit({ instance }) {
		for (const name of Object.keys(instance.exports)) if (name.startsWith("__napi_register__")) instance.exports[name]();
	}
});
const { AstroFrontmatterStatus, convertToTsx, DiagnosticSeverity, ExtractedScriptType, ExtractedStyleType } = napiModule.exports;
// node_modules/@astrojs/ts-content-mapper/dist/transform.js
const VIRTUAL_EXTENSION = ".tsx";
const PARSE_ERROR_CODE = 1e3;
const INTERNAL_ERROR_CODE = 1001;
/** Importers of a broken component still resolve a default export, so their errors stay local. */
const FALLBACK_TSX = "export default function (_props: Record<string, any>): any {}\n";
function toSupplementalScripts(tsx) {
	const modules = tsx.scripts.flatMap((script) => {
		let extension;
		switch (script.type) {
			case "processed-module":
				extension = ".mts";
				break;
			case "module":
				extension = ".mjs";
				break;
			case "inline":
			case "event-attribute":
			case "json":
			case "raw":
			case "unknown": return [];
		}
		return [{
			text: script.content,
			extension,
			mappings: [[
				0,
				script.content.length,
				script.position.start,
				script.content.length,
				0
			]]
		}];
	});
	const inlineScripts = tsx.scripts.filter((script) => script.type === "inline" || script.type === "event-attribute" || script.type === "unknown").sort((a, b) => a.position.start - b.position.start);
	if (inlineScripts.length === 0) return modules;
	let text = "";
	const mappings = [];
	for (const script of inlineScripts) {
		mappings.push([
			text.length,
			script.content.length,
			script.position.start,
			script.content.length,
			0
		]);
		text += `${script.content};`;
	}
	return [...modules, {
		text,
		extension: ".mjs",
		mappings
	}];
}
function toMapperDiagnostics(tsx, source) {
	const diagnostics = [];
	for (const diagnostic of tsx.diagnostics) {
		if (diagnostic.severity !== 1) continue;
		const start = Math.max(0, Math.min(diagnostic.position.start, source.length));
		const end = Math.max(start, Math.min(diagnostic.position.end, source.length));
		diagnostics.push({
			messageText: diagnostic.message,
			start,
			length: end - start,
			code: PARSE_ERROR_CODE
		});
	}
	return diagnostics;
}
function transform({ content, fileName }) {
	try {
		const tsx = convertToTsx(content, {
			filename: fileName,
			ambientTypes: true
		});
		return {
			text: tsx.code,
			extension: VIRTUAL_EXTENSION,
			mappings: tsx.mappings,
			diagnostics: toMapperDiagnostics(tsx, content),
			supplemental: toSupplementalScripts(tsx)
		};
	} catch (error) {
		return {
			text: FALLBACK_TSX,
			extension: VIRTUAL_EXTENSION,
			mappings: [],
			diagnostics: [{
				messageText: `The Astro compiler failed to transform this file to TSX: ${error instanceof Error ? error.message : String(error)}`,
				start: 0,
				length: content.length,
				code: INTERNAL_ERROR_CODE
			}]
		};
	}
}
// node_modules/@astrojs/ts-content-mapper/dist/server.js
const POSITION_ENCODING = "utf-16";
function initialize(params) {
	if (!params.positionEncodings.includes(POSITION_ENCODING)) throw new import_api.ResponseError(import_api.ErrorCodes.InvalidParams, `The Astro content mapper requires the ${POSITION_ENCODING} position encoding.`);
	return {
		positionEncoding: POSITION_ENCODING,
		diagnosticSource: "astro"
	};
}
function startServer() {
	const connection = (0, import_api.createMessageConnection)(new import_main.StreamMessageReader(process.stdin), new import_main.StreamMessageWriter(process.stdout));
	connection.onRequest("initialize", initialize);
	connection.onRequest("openProject", () => ({}));
	connection.onRequest("closeProject", () => void 0);
	connection.onRequest("transform", transform);
	connection.listen();
}
startServer();
export { startServer };
