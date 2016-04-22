package {
public class int extends Number {}
public class uint extends Number {}
public native function eval(x:*):Object;
public native function parseInt(string:*, radix:int = 10):Number;
public native function parseFloat(string:*):Number;
public native function isNaN(number:Number):Boolean;
public native function get NaN():Number;
public native function isFinite(number:Number):Boolean;
public native function decodeURI(encodedURI:String):String;
public native function decodeURIComponent(encodedURIComponent:String):String;
public native function get Infinity():Number;
public native function encodeURI(uri:String):String;
public native function encodeURIComponent(uriComponent:String):String;
public native var undefined:*;

public dynamic class Object {
    public native function get constructor():*;
    public native function set constructor(o:*):void;
    public native function get length():Number;
    public native function set length(n:Number):void;
    public native function get prototype():Object;
    public native function set prototype(o:Object):void;

    public native function toLocaleString():Object;

    public native function toSource():Object;
    public native function unwatch(prop:String):void;
    public native function watch(prop:String, handler:Object):void;

    public native function hasOwnProperty(propertyName:String):Boolean;
    public native function isPrototypeOf(o:Object):Boolean;
    public native function propertyIsEnumerable(propertyName:String):Boolean;

    public native function toString():String;
}

public dynamic class Array extends Object {
    native public function Array(... rest):*;
    public native function get index():Number;
    public native function get input():Number;
    public native function concat(... items):Array;
    public native function join(separator:String):String;
    public native function pop():*;
    public native function push(... items):void;
    public native function reverse():Array;
    public native function shift():*;

    native function slice(start:Number = 0, end:Number = Number.MAX_VALUE):Array;
    public native function sort(comparefn:Function = null):Array;
    public native function splice(start:Number,deleteCount:Number = 0, ... items):Array;
    public native function unshift(... items):uint;
    public native function get length():uint;
    private static const AS3:* = "http://adobe.com/AS3/2006/builtin";
}

public class Boolean extends Object {
    public native function valueOf():Boolean;
}

public dynamic class Date extends Object {
    public native function Date(... rest);
    public native function getDate():Number;
    public native function getDay():Number;
    public native function getMonth():Number;
    public native function getFullYear():Number;
    public native function getHours():Number;

    public native function getMilliseconds():Number;
    public native function getMinutes():Number;
    public native function getSeconds():Number;
    public native function getTime():Number;
    public native function getTimezoneOffset():Number;

    public native function getUTCDate():Number;
    public native function getUTCDay():Number;
    public native function getUTCFullYear():Number;
    public native function getUTCHours():Number;
    public native function getUTCMilliseconds():Number;
    public native function getUTCMinutes():Number;

    public native function getUTCMonth():Number;
    public native function getUTCSeconds():Number;
    public native function getYear():Number;
    public static native function parse(s:*):Date;
    public native function setDate(date:*):void;

    public native function setFullYear(year:Number, month: Number = 1, date: Number = 1):void;
    public native function setHours(hour:Number, min: Number = 1, sec: Number = 1, ms:Number = 1):void;
    public native function setMilliseconds(ms: Number):void;

    public native function setMinutes(min: Number, sec: Number = 1, ms:Number = 1):void;
    public native function setMonth(month: Number, date: Number = 1):void;
    public native function setSeconds(sec: Number, ms:Number = 1):void;

    public native function setTime(time: Number):void;
    public native function setUTCDate(date: Number):void;
    public native function setUTCFullYear(year:Number, month: Number = 1, date: Number = 1):void;
    public native function setUTCHours(hour:Number, min: Number = 1, sec: Number = 1, ms:Number = 1):void;

    public native function setUTCMilliseconds(ms: Number):void;
    public native function setUTCMinutes(min: Number, sec: Number = 1, ms:Number = 1):void;
    public native function setUTCMonth(month: Number, date: Number = 1):void;
    public native function setUTCSeconds(sec: Number, ms:Number = 1):void;

    public native function setYear(year: Number):void;
    public native function toGMTString():String;
    public native function toLocaleDateString():String;
    public native function toLocaleTimeString():String;
    public native function toDateString():String;

    public native function toTimeString():String;
    public native function toUTCString():String;
    public native static function UTC(year:Number, month:Number, date:Number = 1, hours:Number = 1, minutes:Number = 1, seconds:Number = 1, ms:Number = 1):String;
    public native function valueOf():Date;
}

class Arguments extends Array {
    public native function get callee():Function;
    public native function set callee(value:Function):void;
    [Deprecated(message="Property caller is obsolete")]
    public native function get caller():Function;
}

public dynamic class Function extends Object {
    public native function get arguments():Arguments;
    public native function get arity():Number;
    public native function apply(thisArg:Object, argArray:Array = null):Object;
    public native function call(thisArg:Object, ...args):Object;
}

public class Math extends Object {
    public native static const E:Number;
    public native static const LN10:Number;
    public native static const LN2:Number;
    public native static const LOG10E:Number;
    public native static const LOG2E:Number;

    public native static const PI:Number;
    public native static const SQRT1_2:Number;
    public native static const SQRT2:Number;

    public native static function abs(x:Number):Number;
    public native static function acos(x:Number):Number;
    public native static function asin(x:Number):Number;
    public native static function atan(x:Number):Number;
    public native static function atan2(y:Number, x:Number):Number;

    public native static function ceil(value:Number):Number;
    public native static function cos(x:Number):Number;
    public native static function exp(x:Number):Number;
    public native static function floor(x:Number):Number;
    public native static function log(x:Number):Number;
    public native static function max(... x/*Number*/):Number;
    public native static function min(... x/*Number*/):Number;

    public native static function pow(x:Number, y:Number):Number;
    public native static function random():Number;
    public native static function round(x:Number):Number;
    public native static function sin(x:Number):Number;
    public native static function sqrt(x:Number):Number;
    public native static function tan(x:Number):Number;
}

public class Number extends Object {
    public static native function get MAX_VALUE():Number
    public static native function get MIN_VALUE():Number
    public static native function get NaN():Number
    public static native function get Infinity():Number
    public static native function get NEGATIVE_INFINITY():Number
    public static native function get POSITIVE_INFINITY():Number

    public native function toExponential(fractionalDigits:Number):String;
    public native function toFixed(fractionalDigits:Number):String;
    public native function toPrecision(precision:Number):String;
    public native function toString(radix:Number = 10):String;
    public native function valueOf():Number;
}

public dynamic class RegExp extends Object {
    public native function RegExp(pattern:String = null, options:String = null);
    public native function get global():Boolean;
    public native function get ignoreCase():Boolean;
    public native function get lastIndex():Number;
    public native function set lastIndex(n:Number):void;

    public native static function get leftContext():String;
    public native static function get input():String;
    public native static function get lastParen():String;
    public native static function get lastMatch():String;
    public native static function get rightContext():String;

    public native function get multiline():Boolean;
    public native function get source():String;

    public native static function get $1():String;
    public native static function get $2():String;
    public native static function get $3():String;
    public native static function get $4():String;
    public native static function get $5():String;

    public native function exec(string:String):*;
    public native function test(string:String):Boolean;
}

public class String extends Object {
    public native function anchor(nameAttribute:String):String;
    public native function big():String;
    public native function blink():String;
    public native function bold():String;
    public native function charAt(pos:Number):String;
    public native function charCodeAt(pos:Number):Number;

    public native function concat(...strings):String;
    public native function fixed():String;
    public native function fontcolor(color:String):String;
    public native function fontsize(size:Number):String;
    public static native function fromCharCode(... chars):String;
    public native function indexOf(searchString:String, position:Number = -1):Number;

    public native function italics():String;
    public native function lastIndexOf(searchString:String, position:Number = -1):Number;
    native public function get length():int;
    public native function link(href:String):String;
    public native function localeCompare(other:String):int;
    public native function match(regexp:RegExp):Array;
    public native function replace(searchValue:*, replaceValue:*):String;

    public native function search(regexp:*):int;
    public native function slice(start:Number, end:Number = Number.MAX_VALUE):String;
    public native function small():String;
    public native function split(separator:*, limit:Number = -1):Array;
    public native function strike():String;
    public native function sub():String;

    public native function substr(start:Number, length:Number = -1):String;
    public native function substring(start:Number, end:Number = -1):String;
    public native function sup():String;
    public native function toLowerCase():String;
    public native function toLocaleLowerCase():String;
    public native function toLocaleUpperCase():String;
    public native function toUpperCase():String;
    public native function valueOf():String;
}

public dynamic class Error extends Object {
    public native function Error(msg:*="", id:*=0);
    public var name:String;
    public var message:String;
    native AS3 function getStackTrace():String;
}

public class EvalError extends Error {}
public class RangeError extends Error {}
public class ReferenceError extends Error {}
public class SyntaxError extends Error {}
public class TypeError extends Error {}
public class URIError extends Error {}
}

/**
 * Determines whether the specified string is a valid name for an XML element or attribute.
 * @param str - A string to evaluate.
 * @return Returns true if the str argument is a valid XML name; false otherwise.
 */
package {
public native function isXMLName(str:String):Boolean

/**
 * An XMLList object is an ordered collection of properties. An XMLList object represents an XML document, an XML fragment,
 * or an arbitrary collection of XML objects.
 * An XMLList object with one XML element is treated the same as an XML object. When there is one XML element, all methods
 * that are available for the XML object are also available for the XMLList object.
 */
public final dynamic class XMLList extends XML {
    /**
     * Creates a new XMLList object.
     * @param value - Any object that can be converted to an XMLList object by using the top-level XMLList() function.
     * @constructor
     */
    public native function XMLList(value:Object = null):XMLList

    /**
     * Calls the attribute() method of each XML object and returns an XMLList object of the results. The results match
     * the given attributeName parameter. If there is no match, the attribute() method returns an empty XMLList object.
     * @param attributeName The name of the attribute that you want to include in an XMLList object.
     * @return
     */
    public native function attribute(attributeName:*):XMLList

    /**
     * Calls the attributes() method of each XML object and returns an XMLList object of attributes for each XML object.
     * @return An XMLList object of attributes for each XML object.
     */
    public native function attributes():XMLList

    /**
     * Calls the child() method of each XML object and returns an XMLList object that contains the results in order.
     * @param propertyName - The element name or integer of the XML child.
     * @return An XMLList object of child nodes that match the input parameter.
     */
    public native function child(propertyName:Object):XMLList

    /**
     * Calls the children() method of each XML object and returns an XMLList object that contains the results.
     * @return An XMLList object of the children in the XML objects.
     */
    public native function children():XMLList

    /**
     * Calls the comments() method of each XML object and returns an XMLList of comments.
     * @return An XMLList of the comments in the XML objects.
     */
    public native function comments():XMLList

    /**
     * Checks whether the XMLList object contains an XML object that is equal to the given value parameter.
     * @param value - An XML object to compare against the current XMLList object.
     * @return If the XMLList contains the XML object declared in the value parameter, then true; otherwise false.
     */
    public native function contains(value:XML):Boolean

    /**
     * Returns a copy of the given XMLList object. The copy is a duplicate of the entire tree of nodes. The copied XML object has no parent
     * and returns null if you attempt to call the parent() method.
     * @return The copy of the XMLList object.
     */
    public native function copy():XMLList

    /**
     *  Returns all descendants (children, grandchildren, great-grandchildren, and so on) of the XML object that have the given name parameter.
     * The name parameter can be a QName object, a String data type, or any other data type that is then converted to a String data type.
     * To return all descendants, use the asterisk (*) parameter. If no parameter is passed, the string "*" is passed and returns all
     * descendants of the XML object.
     * @param name - The name of the element to match, defaults to *.
     */
    public native function descendants(name:Object = *):XMLList

    /**
     * Calls the elements() method of each XML object. The name parameter is passed to the descendants() method. If no parameter is passed,
     * the string "*" is passed to the descendants() method.
     * @param name - the name of the elements to match, defaulting to *.
     * @return An XMLList object of the matching child elements of the XML objects
     */
    public native function elements(name:Object = *):XMLList

    /**
     *  Checks whether the XMLList object contains complex content. An XMLList object is considered to contain complex content if it is not
     * empty and either of the following conditions is true:
     * The XMLList object contains a single XML item with complex content.
     * The XMLList object contains elements.
     * @return  - If the XMLList object contains complex content, then true; otherwise false.
     */
    public native function hasComplexContent():Boolean

    /**
     * Checks for the property specified by p.
     * @param p The property to match.
     * @return  - If the parameter exists, then true; otherwise false.
     */
    public native function hasOwnProperty	(p:String):Boolean

    /**
     *  Checks whether the XMLList object contains simple content. An XMLList object is considered to contain simple content if one or more
     * of the following conditions is true:
     * The XMLList object is empty
     * The XMLList object contains a single XML item with simple content
     * The XMLList object contains no elements
     * @return  - If the XMLList contains simple content, then true; otherwise false.
     */
    public native function hasSimpleContent():Boolean

    /**
     * Returns the number of properties in the XMLList object.
     * @return  - If the parameter exists, then true; otherwise false.
     */
    public native function length	():int

    /**
     * Merges adjacent text nodes and eliminates empty text nodes for each of the following: all text nodes in the XMLList, all the XML
     * objects contained in the XMLList, and the descendants of all the XML objects in the XMLList.
     * @return  - The normalized XMLList object.
     */
    public native function normalize():XMLList

    /**
     * Returns the parent of the XMLList object if all items in the XMLList object have the same parent. If the XMLList object has no parent
     * or different parents, the method returns undefined.
     * @return  - Returns the parent XML object.
     */
    public native function parent():XML

    /**
     * If a name parameter is provided, lists all the children of the XMLList object that contain processing instructions with that name.
     * With no parameters, the method lists all the children of the XMLList object that contain any processing instructions.
     * @param name The name of the processing instructions to match.
     * @return  - An XMLList object that contains the processing instructions for each XML object.
     */
    public native function processingInstructions(name:String = "*"):XMLList

    /**
     * Calls the text() method of each XML object and returns an XMLList object that contains the results.
     * @param name The name of the processing instructions to match.
     * @return  - An XMLList object of all XML properties of the XMLList object that represent XML text nodes.
     */
    public native function text():XMLList

    /**
     * Returns a string representation of all the XML objects in an XMLList object. The rules for this conversion depend on whether the XML
     * object has simple content or complex content:
     * If the XML object has simple content, toString() returns the string contents of the XML object with the following stripped out: the
     * start tag, attributes, namespace declarations, and end tag.
     * If the XML object has complex content, toString() returns an XML encoded string representing the entire XML object, including the start
     *  tag, attributes, namespace declarations, and end tag.
     * To return the entire XML object every time, use the toXMLString() method.
     * @return  - The string representation of the XML object.
     */
    public native function toString():String

    /**
     * Returns a string representation of all the XML objects in an XMLList object. Unlike the toString() method, the toXMLString()
     * method always returns the start tag, attributes, and end tag of the XML object, regardless of whether the XML object has simple content
     *  or complex content. (The toString() method strips out these items for XML objects that contain simple content.)
     * @return  - The string representation of the XML object.
     */
    public native function toXMLString():String

    public native function valueOf():XMLList;
}

/**
 * The XML class contains methods and properties for working with XML objects. The XML class (along with the XMLList, Namespace, and
 * QName classes) implements the powerful XML-handling standards defined in ECMAScript for XML (E4X) specification (ECMA-357 edition 2).
 * Use the toXMLString() method to return a string representation of the XML object regardless of whether the XML object has simple content
 * or complex content.
 */
public dynamic class XML {
    /**
     *  Determines whether XML comments are ignored when XML objects parse the source XML data. By default, the comments are ignored (true).
     * To include XML comments, set this property to false. The ignoreComments property is used only during the XML parsing, not during the call
     * to any method such as myXMLObject.child(*).toXMLString(). If the source XML includes comment nodes, they are kept or discarded during the
     * XML parsing.
     */
    public static native function get ignoreComments():Boolean
    public static native function set ignoreComments(value:Boolean):void

    /**
     * Determines whether XML processing instructions are ignored when XML objects parse the source XML data. By default, the processing
     * instructions are ignored (true). To include XML processing instructions, set this property to false. The ignoreProcessingInstructions
     * property is used only during the XML parsing, not during the call to any method such as myXMLObject.child(*).toXMLString(). If the
     * source XML includes processing instructions nodes, they are kept or discarded during the XML parsing.
     */
    public static native function get ignoreProcessingInstructions():Boolean
    public static native function set ignoreProcessingInstructions(value:Boolean):void

    /**
     * Determines whether white space characters at the beginning and end of text nodes are ignored during parsing. By default, white space
     * is ignored (true). If a text node is 100% white space and the ignoreWhitespace property is set to true, then the node is not created.
     * To show white space in a text node, set the ignoreWhitespace property to false.
     */
    public static native function get ignoreWhitespace():Boolean
    public static native function set ignoreWhitespace(value:Boolean):void

    /**
     * Determines the amount of indentation applied by the toString() and toXMLString() methods when the XML.prettyPrinting property is set
     * to true. Indentation is applied with the space character, not the tab character. The default value is 2.
     */
    public static native function get prettyIndent():int
    public static native function set prettyIndent(value:int):void

    /**
     * Determines whether the toString() and toXMLString() methods normalize white space characters between some tags. The default value is true.
     */
    public static native function get prettyPrinting():Boolean
    public static native function set prettyPrinting(value:Boolean):void

    /**
     * Creates a new XML object. You must use the constructor to create an XML object before you call any of the methods of the XML class.
     * Use the toXMLString() method to return a string representation of the XML object regardless of whether the XML object has simple
     * content or complex content.
     * @param value
     * @constructor
     */
    public native function XML(value:Object = null):XML;

    /**
     * Adds a namespace to the set of in-scope namespaces for the XML object. If the namespace already exists in the in-scope namespaces
     * for the XML object (with a prefix matching that of the given parameter), then the prefix of the existing namespace is set to undefined.
     * If the input parameter is a Namespace object, it's used directly. If it's a QName object, the input parameter's URI is used to create
     * a new namespace; otherwise, it's converted to a String and a namespace is created from the String.
     * @param The namespace to add to the XML object.
     * @return The new XML object, with the namespace added.
     */
    public native function addNamespace(ns:Object):XML

    /**
     * Appends the given child to the end of the XML object's properties. The appendChild() method takes an XML object, an XMLList object,
     * or any other data type that is then converted to a String.
     * Use the delete (XML) operator to remove XML nodes.
     * @param The XML object to append.
     * @return The resulting XML object.
     */
    public native function appendChild(ns:Object):XML

    /**
     * Returns the XML value of the attribute that has the name matching the attributeName  parameter. Attributes are found within XML
     * elements. In the following example, the element has an attribute named "gender" with the value "boy": <first gender="boy">John</first>.
     * The attributeName parameter can be any data type; however, String is the most common data type to use. When passing any object other
     * than a QName object, the attributeName parameter uses the toString() method to convert the parameter to a string.
     * If you need a qualified name reference, you can pass in a QName object. A QName object defines a namespace and the local name, which
     * you can use to define the qualified name of an attribute. Therefore calling attribute(qname) is not the same as calling
     * attribute(qname.toString()).
     * @param attributeName The name of the attribute.
     * @return An XMLList object or an empty XMLList object. Returns an empty XMLList object when an attribute value has not been defined.
     */
    public native function attribute(attributeName:*):XMLList

    /**
     * Returns a list of attribute values for the given XML object. Use the name() method with the attributes() method to return the name
     * of an attribute. Use @* to return the names of all attributes.
     * @return The list of attribute values.
     */
    public native function attributes():XMLList

    /**
     *  Lists the children of an XML object. An XML child is an XML element, text node, comment, or processing instruction.
     * Use the propertyName parameter to list the contents of a specific XML child. For example, to return the contents of a child named
     * <first>, use child.name("first"). You can generate the same result by using the child's index number. The index number identifies
     * the child's position in the list of other XML children. For example, name.child(0) returns the first child in a list.
     * Use an asterisk (*) to output all the children in an XML document. For example, doc.child("*").
     * Use the length() method with the asterisk (*) parameter of the child() method to output the total number of children. For example,
     * numChildren = doc.child("*").length().
     * @param propertyName - The element name or integer of the XML child.
     * @return An XMLList object of child nodes that match the input parameter.
     */
    public native function child(propertyName:Object):XMLList

    /**
     * Identifies the zero-indexed position of this XML object within the context of its parent.
     * @return The position of the object. Returns -1 as well as positive integers.
     */
    public native function childIndex():int

    /**
     * Lists the children of the XML object in the sequence in which they appear. An XML child is an XML element, text node, comment,
     * or processing instruction.
     * @return An XMLList object of the XML object's children.
     */

    public native function children():XMLList

    /**
     * Lists the properties of the XML object that contain XML comments.
     * @return An XMLList object of the properties that contain comments.
     */
    public native function comments():XMLList

    /**
     * Compares the XML object against the given value parameter.
     * @param value - A value to compare against the current XML object.
     * @return If the XML object matches the value parameter, then true; otherwise false.
     */
    public native function contains(value:XML):Boolean

    /**
     * Returns a copy of the given XML object. The copy is a duplicate of the entire tree of nodes. The copied XML object has no parent and
     * returns null if you attempt to call the parent() method.
     * @return The copy of the object.
     */
    public native function copy():XML

    /**
     * Returns an object with the following properties set to the default values: ignoreComments, ignoreProcessingInstructions,
     * ignoreWhitespace, prettyIndent, and prettyPrinting.
     */
    public native static function defaultSettings():Object

    /**
     * Returns all descendants (children, grandchildren, great-grandchildren, and so on) of the XML object that have the given name
     * parameter. The name parameter is optional. The name parameter can be a QName object, a String data type or any other data type that is
     * then converted to a String data type. To return all descendants, use the "*" parameter. If no parameter is passed, the string "*" is
     * passed and returns all descendants of the XML object.
     * @param name The name of the element to match.
     * @return An XMLList object of matching descendants. If there are no descendants, returns an empty XMLList object.
     */
    public native function descendants(name:Object = "*"):XMLList

    /**
     *  Lists the elements of an XML object. An element consists of a start and an end tag; for example <first></first>. The name parameter
     * is optional. The name parameter can be a QName object, a String data type, or any other data type that is then converted to a String
     * data type. Use the name parameter to list a specific element. For example, the element "first" returns "John" in this example:
     * <first>John</first>.
     * To list all elements, use the asterisk (*) as the parameter. The asterisk is also the default parameter. Use the length() method with
     * the asterisk parameter to output the total number of elements. For example, numElement = addressbook.elements("*").length().
     * @param name - The name of the element. An element's name is surrounded by angle brackets. For example, "first" is the name
     * in this example: <first></first>.
     * @return An XMLList object of the element's content. The element's content falls between the start and end tags. If you use the
     * asterisk (*) to call all elements, both the element's tags and content are returned.
     */
    public native function elements(name:Object = "*"):XMLList

    /**
     * Checks to see whether the XML object contains complex content. An XML object contains complex content if it has child elements. XML
     * objects that representing attributes, comments, processing instructions, and text nodes do not have complex content. However, an object
     * that contains these can still be considered to contain complex content (if the object has child elements).
     * @return If the XML object contains complex content, true; otherwise false.
     */
    public native function hasComplexContent():Boolean

    /**
     * Checks to see whether the XML object contains simple content. An XML object contains simple content if it represents a text node, an
     * attribute node, or an XML element that has no child elements. XML objects that represent comments and processing instructions do not
     * contain simple content.
     * @return If the XML object contains simple content, true; otherwise false.
     */
    public native function hasSimpleContent():Boolean

    /**
     * Lists the namespaces for the XML object, based on the object's parent.
     * @return An array of Namespace objects
     */
    [ArrayElementType("Namespace")]
    public native function inScopeNamespaces():Array

    /**
     *  Inserts the given child2 parameter after the child1 parameter in this XML object and returns the resulting object. If the child1
     * parameter is null, the method inserts the contents of child2 before all children of the XML object (in other words, after none).
     * If child1 is provided, but it does not exist in the XML object, the XML object is not modified and undefined is returned.
     * If you call this method on an XML child that is not an element (text, attributes, comments, pi, and so on) undefined is returned.
     * Use the delete (XML) operator to remove XML nodes.
     * @param child1 - The object in the source object that you insert before child2.
     * @param child2 - The object to insert.
     * @return The resulting XML object or undefined.
     */
    public native function insertChildAfter(child1:Object, child2:Object):*

    /**
     *  Inserts the given child2 parameter before the child1 parameter in this XML object and returns the resulting object. If the child1
     * parameter is null, the method inserts the contents of child2 after all children of the XML object (in other words, before none).
     * If child1 is provided, but it does not exist in the XML object, the XML object is not modified and undefined is returned.
     * If you call this method on an XML child that is not an element (text, attributes, comments, pi, and so on) undefined is returned.
     * Use the delete (XML) operator to remove XML nodes.
     * @param child1 - The object in the source object that you insert after child2.
     * @param child2 - The object to insert.
     * @return The resulting XML object or undefined.
     */
    public native function insertChildBefore(child1:Object, child2:Object):*

    /**
     * For XML objects, this method always returns the integer 1. The length() method of the XMLList class returns a value of 1 for an
     * XMLList object that contains only one value.
     * @return Always returns 1 for any XML object.
     */
    public native function length():int

    /**
     * Gives the local name portion of the qualified name of the XML object.
     * @return The local name as either a String or null.
     */
    public native function localName():Object

    /**
     * Gives the qualified name for the XML object.
     * @return The qualified name is either a QName or null.
     */
    public native function name():Object

    /**
     * If no parameter is provided, gives the namespace associated with the qualified name of this XML object. If a prefix parameter is
     * specified, the method returns the namespace that matches the prefix parameter and that is in scope for the XML object.
     * If there is no such namespace, the method returns undefined.
     * @param prefix The prefix you want to match, defaults to null.
     * @return The qualified name is either a QName or null.
     */
    public native function namespace(prefix:String = null):Object

    /**
     * Lists namespace declarations associated with the XML object in the context of its parent.
     * @return An array of Namespace objects.
     */
    public native function namespaceDeclarations():/*Namespace*/ Array

    /**
     * Specifies the type of node: text, comment, processing-instruction, attribute, or element.
     * @return The node type used.
     */
    public native function nodeKind():String

    /**
     * For the XML object and all descendant XML objects, merges adjacent text nodes and eliminates empty text nodes.
     * @return The resulting normalized XML object.
     */
    public native function normalize():XML

    /**
     * Returns the parent of the XML object. If the XML object has no parent, the method returns undefined.
     * @return The parent XML object. Returns either a String or undefined.
     */
    public native function parent():*

    /**
     *  Inserts a copy of the provided child object into the XML element before any existing XML properties for that element. Use the
     * delete (XML) operator to remove XML nodes.
     * @param value - The object to insert.
     * @return The resulting XML object.
     */
    public native function prependChild(value:Object):XML

    /**
     * If a name parameter is provided, lists all the children of the XML object that contain processing instructions with that name.
     * With no parameters, the method lists all the children of the XML object that contain any processing instructions.
     * @param The name of the processing instructions to match, defaulting to "*".
     * @return A list of matching child objects.
     */
    public native function processingInstructions(name:String = "*"):XMLList

    /**
     * Removes the given namespace for this object and all descendants. The removeNamespaces() method does not remove a namespace if it is
     * referenced by the object's qualified name or the qualified name of the object's attributes.
     * @param ns - The namespace to remove.
     * @return A copy of the resulting XML object.
     */
    public native function removeNamespace(ns:Namespace):XML

    /**
     * Replaces the properties specified by the propertyName parameter with the given value parameter. If no properties match propertyName,
     * the XML object is left unmodified.
     * @param propertyName - Can be a numeric value, an unqualified name for a set of XML elements, a qualified name for a set of XML elements,
     * or the asterisk wildcard ("*"). Use an unqualified name to identify XML elements in the default namespace.
     * @param value - The replacement value. This can be an XML object, an XMLList object, or any value that can be converted with toString().
     * @return The resulting XML object, with the matching properties replaced.
     */
    public native function replace(propertyName:*, value:*):XML

    /**
     * Replaces the child properties of the XML object with the specified set of XML properties, provided in the value parameter.
     * @param value - The replacement XML properties. Can be a single XML object or an XMLList object.
     * @return The resulting XML object.
     */
    public native function setChildren(value:Object):XML

    /**
     * Changes the local name of the XML object to the given name parameter.
     * @param name - The replacement name for the local name.
     */
    public native function setLocalName(name:String):void

    /**
     * Sets the name of the XML object to the given qualified name or attribute name.
     * @param name - The new name for the object.
     */
    public native function setName(name:String):void

    /**
     * Sets the namespace associated with the XML object.
     * @param ns - The new namespace.
     */
    public native function setNamespace(name:String):void

    /**
     * Sets values for the following XML properties: ignoreComments, ignoreProcessingInstructions, ignoreWhitespace, prettyIndent, and prettyPrinting.
     * @param rest An object with each of the following properties:
     * ignoreComments
     * ignoreProcessingInstructions
     * ignoreWhitespace
     * prettyIndent
     * prettyPrinting
     */
    public native static function setSettings(... rest):void

    /**
     * Retrieves the following properties: ignoreComments, ignoreProcessingInstructions, ignoreWhitespace, prettyIndent, and prettyPrinting.
     * @return An object with the following XML properties:
     * ignoreComments
     * ignoreProcessingInstructions
     * ignoreWhitespace
     * prettyIndent
     * prettyPrinting
     */
    public native static function settings():Object

    /**
     * Returns an XMLList object of all XML properties of the XML object that represent XML text nodes.
     * @return The list of properties.
     */
    public native function text():XMLList

    /**
     *  Returns a string representation of the XML object. The rules for this conversion depend on whether the XML object has simple content
     * or complex content:
     * If the XML object has simple content, toString() returns the String contents of the XML object with the following stripped out:
     the start tag, attributes, namespace declarations, and end tag.
     * If the XML object has complex content, toString() returns an XML encoded String representing the entire XML object, including
     the start tag, attributes, namespace declarations, and end tag.
     * To return the entire XML object every time, use toXMLString().
     * @return The string representation of the XML object.
     */
    public native function toString():String

    /**
     *  Returns a string representation of the XML object. Unlike the toString() method, the toXMLString() method always returns the start
     * tag, attributes, and end tag of the XML object, regardless of whether the XML object has simple content or complex content.
     * (The toString() method strips out these items for XML objects that contain simple content.)
     * @return The string representation of the XML object.
     */

    public native function toXMLString():String

    public native function valueOf():XML;
}

/**
 * The Namespace class contains methods and properties for defining and working with namespaces. There are three scenarios for using namespaces:<ul>
 * <li>Namespaces of XML objects Namespaces associate a namespace prefix with a Uniform Resource Identifier (URI) that identifies the namespace.
 * The prefix is a string used to reference the namespace within an XML object. If the prefix is undefined, when the XML is converted to a
 * string, a prefix is automatically generated.</li>
 * <li> Namespace to differentiate methods Namespaces can differentiate methods with the same name to perform different tasks. If two methods
 * have the same name but separate namespaces, they can perform different tasks.</li>
 * <li>Namespaces for access control Namespaces can be used to control access to a group of properties and methods in a class. If you place
 * the properties and methods into a private namespace, they are inaccessible to any code that does not have access to that namespace.
 * You can grant access to the group of properties and methods by passing the namespace to other classes, methods or functions.</li>
 *</ul>
 * This class shows two forms of the constructor method because each form accepts different parameters.
 */
public final class Namespace {

    /**
     * The prefix of the namespace.
     */
    public native function get prefix():String
    public native function set prefix(value:String):void

    /**
     * The Uniform Resource Identifier (URI) of the namespace.
     */
    public native function get uri():String
    public native function set uri(value:String):void

    /**
     * Creates a Namespace object according to the values of the prefixValue and uriValue parameters. This constructor requires both parameters.
     * The value of the prefixValue parameter is assigned to the prefix property as follows:<ul>
     * <li>If undefined is passed, prefix is set to undefined.</li>
     * <li>If the value is a valid XML name, as determined by the isXMLName() function, it is converted to a string and assigned to the prefix property.</li>
     * <li>If the value is not a valid XML name, the prefix property is set to undefined.</li>
     * </ul>
     * The value of the uriValue parameter is assigned to the uri property as follows:
     * If a QName object is passed, the uri property is set to the value of the QName object's uri property.
     * If the value is a Namespace object (and first parameter is not specified), a copy of the object is created.
     * Otherwise, the uriValue parameter is converted to a string and assigned to the uri property.
     * If no values is passed, the prefix and uri properties are set to an empty string.
     * @param prefixValue - The prefix to use for the namespace.
     * @param uriValue - The Uniform Resource Identifier (URI) of the namespace.
     * @constructor
     */
    public native function Namespace(prefixValue:* = undefined, uriValue:* = undefined);

    public native function valueOf():Namespace;

    /**
     * Equivalent to the Namespace.uri property.
     * @return The Uniform Resource Identifier (URI) of the namespace, as a string.
     */
    public native function toString():String
}

/**
 * QName objects represent qualified names of XML elements and attributes. Each QName object has a local name and a namespace
 * Uniform Resource Identifier (URI). When the value of the namespace URI is null, the QName object matches any namespace. Use the QName
 * constructor to create a new QName object that is either a copy of another QName object or a new QName object with a uri from a Namespace
 * object and a localName from a QName object.
 */
public final dynamic class QName {
    /**
     * The local name of the QName object.
     */
    public native function get localName():String

    /**
     * The Uniform Resource Identifier (URI) of the QName object.
     */
    public native function get uri():String


    /**
     * Creates a QName object with a URI object from a Namespace object and a localName from a QName object. If either parameter is not the
     * expected data type, the parameter is converted to a string and assigned to the corresponding property of the new QName object.
     * For example, if both parameters are strings, a new QName object is returned with a uri property set to the first parameter and a
     * localName property set to the second parameter. In other words, the following permutations, along with many others, are valid forms of
     * the constructor:
     * QName (uri:Namespace, localName:String);
     * QName (uri:String, localName: QName);
     * QName (uri:String, localName: String);
     * If you pass null for the uri parameter, the uri property of the new QName object is set to null.
     * When uri is not passed then creates a QName object that is a copy of another QName object. If the parameter passed to the constructor
     * is a QName object, a copy of the QName object is created. If the parameter is not a QName object, the parameter is converted to a
     * string and assigned to the localName property of the new QName instance. If the parameter is undefined or unspecified, a new QName
     * object is created with the localName property set to the empty string.
     * @param uri A Namespace object from which to copy the uri value. A parameter of any other type is converted to a string.
     * @param localName The QName object to be copied (when uri is not passed). Objects of any other type are converted to a string that is
     * assigned to the localName property of the new QName object.
     * When uri passed - A QName object from which to copy the localName value. A parameter of any other type is converted to a string.
     */
    public native function QName(uri:* = null, localName:* = null);

    public native function toString():String
}

/**
 * A Class object is created for each class definition in a program. Every Class object is an instance of the Class class. The Class object
 * contains the static properties and methods of the class. The class object creates instances of the class when invoked using the new
 * operator.
 */
public dynamic class Class extends Object {}
}

__AS3__$vec final dynamic class Vector$object extends Object{
  native public function Vector$object(length:uint = 0,fixed:Boolean = false):*;
  native public function set length(value:uint):*;
  native public function set fixed(f:Boolean):*;
  native AS3 function concat(... rest):Vector$object;
  native AS3 function reverse():Vector$object;
  static native private const AS3:* = "http://adobe.com/AS3/2006/builtin";
}

native public function trace(... rest):void;

package flash.display {
import flash.events.EventDispatcher;
[Event(name="mouseDown",type="flash.events.MouseEvent")]
[Event(name="mouseUp",type="flash.events.MouseEvent")]
[Event(name="mouseMove",type="flash.events.MouseEvent")]
public class Sprite extends DisplayObject{
    native public function Sprite():*;
    native public function set buttonMode(value:Boolean):void;
    native public function get name():String;
    native public function set name(value:String):void;
}

public class DisplayObject extends EventDispatcher{
    native public function get height():Number;
    native public function set height(value:Number):void;
}
}

import mypackage.ListCollectionView;

package flash.events {
public interface IEventDispatcher{
    native function addEventListener(type:String,listener:Function,useCapture:Boolean = false,priority:int = 0,useWeakReference:Boolean = false):void;
    native function removeEventListener(type:String,listener:Function,useCapture:Boolean = false):void;
}
public class EventDispatcher extends Object implements IEventDispatcher{
    native public function addEventListener(type:String,listener:Function,useCapture:Boolean = false,priority:int = 0,useWeakReference:Boolean = false):void;
    native public function removeEventListener(type:String,listener:Function,useCapture:Boolean = false):void;
}
public class Event{}
public class MouseEvent extends Event{}
public class ErrorEvent extends Event{
    static native public const ERROR:String = "error";
}
public class AccelerometerEvent extends Event{}
}

package mx.styles {
public interface IStyleClient{}
}

package mx.core {
import flash.display.Sprite;
import flash.events.EventDispatcher;
import mx.styles.IStyleClient;

public interface IVisualElementContainer{}                  // base interface for spark containers (Flex 4)
public interface IVisualElement extends EventDispatcher{    // base interface for spark elements (Flex 4)
    function set depth(value:Number):void;
}
public interface IUIComponent extends EventDispatcher{}     // base interface for Flex 3 components
public interface IContainer extends IUIComponent{}          // base interface for Flex 3 containers
public interface IDeferredInstance{}
public interface IRepeater{}
public interface IRepeaterClient{}
public interface IMXMLObject {}
[Event(name="initialize",type="mx.events.FlexEvent")]
[Event(name="creationComplete", type="mx.events.FlexEvent")]
[Effect(name="resizeEffect", event="resize")]
[Style(name="left", type="String", inherit="no")]
[Style(name="right", type="String", inherit="no")]
[Style(name="top", type="String", inherit="no")]
[Style(name="bottom", type="String", inherit="no")]
public class UIComponent extends Sprite implements IUIComponent, IVisualElement, IRepeaterClient, IStyleClient {
  public var states:Array /* of State */ = [];
  public function set transitions(value:Array):void{}
  public function get id():String{}
  public function set id(value:String):void{}
  native public function set currentState(value:String);
    
  native public function get left():Object;
  native public function set left(value:Object):void;
  
  native public function get right():Object;
  native public function set right(value:Object):void;
  
  native public function get top():Object;
  native public function set top(value:Object):void;
  
  native public function get bottom():Object;
  native public function set bottom(value:Object):void;

  [PercentProxy("percentWidth")]
  native public function get width():Number;
  native public function set width(value:Number):void;

  native public function get percentWidth():Number;
  native public function set percentWidth(value:Number):void;

  [PercentProxy("percentHeight")]
  native public function get height():Number;
  native public function set height(value:Number):void;

  native public function get percentHeight():Number;
  native public function set percentHeight(value:Number):void;

  native public function get horizontalCenter():Number;
  native public function set horizontalCenter(value:Number):void;

  native public function get verticalCenter():Number;
  native public function set verticalCenter(value:Number):void;
}
public class Application extends Container{}
public class Repeater extends UIComponent implements IRepeater{}
include "TextStyles.as"
public class Container extends UIComponent implements IContainer, IVisualElementContainer{
}
public interface IFactory {
    function newInstance():*;
}
public class ClassFactory implements IFactory {
    public function ClassFactory(generator:Class = null){}
    public function newInstance():* {}
}
public interface IFlexModuleFactory{}
public class DesignLayer extends EventDispatcher{
  public function set visible(value:Boolean):void{}
}
}

package mx.controls{
import flash.display.Sprite;
import mx.core.Container;
import mx.core.UIComponent;
import flash.events.EventDispatcher;
import mx.core.IFactory;

public class Alert extends Panel{
  static native public final function show(text:String = "", title:String = "", flags:uint = 4, parent:Sprite = null, closeHandler:Function = null, iconClass:Class = null, defaultButtonFlag:uint = 4):Alert;
}

[Event(name="click",type="flash.events.MouseEvent")]
[Style(name="color")]
public class Button extends UIComponent{
    native public function set label(value:String):void;

    [Inspectable(category="General", enumeration="left,right,top,bottom", defaultValue="right")]
    native public function get labelPlacement():String;
    native public function set labelPlacement(value:String):void;
}

public class LinkButton extends Button{}
public class CheckBox extends Button{}
public class TextInput extends UIComponent{}
public class DataGrid extends UIComponent{
  [Inspectable(category="General", arrayType="mx.controls.dataGridClasses.DataGridColumn")]
  public function get columns():Array{}
  public function set columns(value:Array):void{}
  public function set itemRenderer(value:IFactory):void{}
  public native function get dataProvider():Object;
  public native function set dataProvider(value:Object):void;
  public native function get columns():Array;
  public native function set columns(value:Array):void;
}
public class TextArea extends UIComponent{
  public function set text(value:String):void{}
}

public class RadioButtonGroup extends EventDispatcher {}

public class Tree extends UIComponent {}

public class Text extends UIComponent {
    public native function get text():String;
    public native function set text(s:String):void;
    [Inspectable(category="General")]
    public native function set target(_:String):void;
}
public class Image extends UIComponent {
    public native function get source():Object;
    public native function set source(p:Object):void;
}
}

package mx.controls.dataGridClasses {
import mx.core.IFactory;

public class DataGridColumn{
  public function get itemRenderer():IFactory{}
  public function set itemRenderer(value:IFactory):void{}
  public var itemEditor:IFactory;
  public native function get dataField():String;
  public native function set dataField(value:String):void;
}
}

package mx.containers{
import mx.core.Container;

public class Panel extends Container{}
public class VBox extends Container{}
public class HBox extends Container{}
public class Accordion extends Container{}
}

package mx.collections{
[DefaultProperty("source")]
public class XMLListCollection{
  public function set source(s:XMLList):void{}
}
public class ListCollectionView {}

public class ArrayCollection extends ListCollectionView{}
}

package mx.states{
public class State{
    public var name:String;

    [ArrayElementType("String")]
    [Inspectable(category="General")]
    public var stateGroups:Array /* of String */ = [];
}

[DefaultProperty("effect")]
class Transition {
    public var effect:IEffect;

    [Inspectable(category="General")]
    public var fromState:String = "*";

    [Inspectable(category="General")]
    public var toState:String = "*";
}
}

package spark.components {
import mx.core.IDeferredInstance;
import mx.core.IVisualElementContainer;
import mx.core.UIComponent;
import mx.core.IFactory;
import spark.components.supportClasses.ButtonBase;
import spark.components.supportClasses.SkinnableComponent;

public class Application extends SkinnableContainer{
    public var pageTitle:String;
}

public class ViewNavigator extends SkinnableContainer {
    public function pushView(viewClass:Class, data:Object = null, context:Object = null, transition:Object = null):void {}
}

public class View extends SkinnableContainer{
    public function get navigator():ViewNavigator{}
}

public class SkinnableDataContainer extends SkinnableComponent {
    public native function get itemRenderer():IFactory;
    public native function set itemRenderer(value:IFactory):void;
}

[Style(name="verticalScrollPolicy", type="String", inherit="no", enumeration="off,on,auto")]
public class List extends SkinnableDataContainer {
  public function set selectedIndices(value:Vector.<int>):void{}
}

[DefaultProperty("navigationStack")]
public class ViewNavigatorApplication extends Application {
    private function get navigationStack():NavigationStack{
        return null;
    }

    private function set navigationStack(value:NavigationStack):void{}
    public function set firstView(value:Class):void{}
}

[Event(name="click",type="flash.events.MouseEvent")]
[Style(name="color", type="uint", format="Color", inherit="yes")]
[Style(name="alignmentBaseline", type="String", enumeration="useDominantBaseline,roman,ascent,descent,ideographicTop,ideographicCenter,ideographicBottom", inherit="yes")]
[Style(name="borderAlpha")]
public class Button extends ButtonBase{
    native public function set label(value:String):void;
}

[DefaultProperty("mxmlContent")]
public class Group extends UIComponent implements IVisualElementContainer{
  [ArrayElementType("mx.core.IVisualElement")]
  public function set mxmlContent(value:Array):void{}
  override public function set width(value:Number):void{}
}

public class HGroup extends Group {}

public class DataGroup extends UIComponent{
    public function set itemRenderer(value:IFactory):void{}
}

public class ButtonBar extends SkinnableComponent {

}

public class ButtonBarButton extends SkinnableComponent {

}

public class TextInput extends SkinnableComponent {
    public native function get text():String;
    public native function set text(value:String):void;
}

public class CheckBox extends SkinnableComponent {
    public native function get text():String;
    public native function set text(value:String):void;
    public native function get label():String;
    public native function set label(value:String):void;
}

[SkinState("normal")]
[SkinState("disabled")]
[DefaultProperty("mxmlContentFactory")]
public class SkinnableContainer extends SkinnableComponent implements IVisualElementContainer{
    [InstanceType("Array")]
    [ArrayElementType("mx.core.IVisualElement")]
    public function set mxmlContentFactory(value:IDeferredInstance):void{}
}
}

package spark.components.supportClasses{
import spark.components.Group;
import mx.core.UIComponent;
import mx.core.IVisualElementContainer;
import mx.core.IDeferredInstance;

[Style(name="skinClass", type="Class")]
[Event(name="stateChangeComplete", type="mx.events.FlexEvent")]
public class SkinnableComponent extends UIComponent{
  public function get skin():UIComponent{}
}

public class Skin extends Group{}

[SkinState("disabled")]
[SkinState("down")]
[SkinState("over")]
[SkinState("up")]
[Event(name="buttonDown", type="mx.events.FlexEvent")]
public class ButtonBase extends SkinnableComponent{}
public class NavigationStack{}
}

package spark.collections {
import flash.events.EventDispatcher;

public class Sort extends EventDispatcher{
    [Inspectable(category="General", arrayType="adobe.typo.NonExistentClass")]
    public function set fields(value:Array):void{}
}
}

package flash.filters {
  public class BitmapFilterQuality {}
}

package mx.filters {
  public class BaseFilter {}
}

package spark.primitives.supportClasses {
import flash.events.EventDispatcher;
import mx.core.IVisualElement;

public class GraphicElement extends EventDispatcher implements IVisualElement{}
}

package spark.primitives {
import spark.primitives.supportClasses.GraphicElement;
import mx.graphics.IFill;

public class Rect extends GraphicElement {
    public function set fill(value:IFill):void{}
}
}

package mx.graphics{
import flash.events.EventDispatcher;

public interface IFill{}
public class SolidColor extends EventDispatcher implements IFill{
    public function set color(value:uint):void{}
}
}

package spark.core{
import mx.core.IVisualElement;
import flash.display.Sprite;

public class SpriteVisualElement extends Sprite implements IVisualElement{
    public function get id():String{}
    public function set id(value:String):void{}
    public function set depth(value:Number):void{}
    [PercentProxy("percentHeight")]
    override public function get height():Number{}
    override public function set height(value:Number):void{}
}
}

package mx.rpc.soap.mxml{
import flash.events.EventDispatcher;
public class Operation extends EventDispatcher{
    public function set name(n:String):void{}
    public function set request(r:Object):void{}
}
}

package mx.rpc.http.mxml{
import flash.events.EventDispatcher;

public class HTTPService extends EventDispatcher{
    public function set request(r:Object):void{}
    [Inspectable(category="General", defaultValue="object", enumeration="object,array,xml,flashvars,text,e4x")]
    native public function set resultFormat(_79:Object):void;
}
}

package mx.rpc.soap.mxml{
import flash.events.IEventDispatcher;
public dynamic class WebService implements IEventDispatcher{}
}

public final dynamic class XML {}
public class flash.xml.XMLNode {}

package mx.rpc.remoting {
public class RemoteObject {
    [Inspectable(category="General")]
    public function get destination():String {}
   public function set destination(name:String):void {}
}
}

package mx.rpc.remoting.mxml {
public class Operation {
    native public function set name(value:String):void;
}
}

package mx.validators {
import mx.core.IMXMLObject;
    public class EmailValidator implements IMXMLObject {
        public native function get source():Object;
        public native function set source(p:Object):void;
    }
}

package flash.utils {
[native(cls="DictionaryClass",gc="exact",instance="DictionaryObject",methods="auto")]
public dynamic class Dictionary extends Object {
    native public function Dictionary(weakKeys:Boolean = false):*;
    native private function init(weakKeys:Boolean):void;
}
}