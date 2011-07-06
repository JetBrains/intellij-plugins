////////////////////////////////////////////////////////////////////////////////
//
//  ADOBE SYSTEMS INCORPORATED
//  Copyright 2003-2007 Adobe Systems Incorporated
//  All Rights Reserved.
//
//  NOTICE: Adobe permits you to use, modify, and distribute this file
//  in accordance with the terms of the license agreement accompanying it.
//
////////////////////////////////////////////////////////////////////////////////

// Original file can be found at:
// svn://opensource.adobe.com/svn/opensource/flex/sdk/trunk/modules/debugger/src/java/flex/tools/debugger/cli/ExpressionCache.java (revision 21499)

package flex.tools.debugger.cli;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Vector;

import flash.localization.LocalizationManager;
import flash.tools.debugger.Bootstrap;
import flash.tools.debugger.NoResponseException;
import flash.tools.debugger.NotConnectedException;
import flash.tools.debugger.NotSuspendedException;
import flash.tools.debugger.PlayerDebugException;
import flash.tools.debugger.Session;
import flash.tools.debugger.Value;
import flash.tools.debugger.ValueAttribute;
import flash.tools.debugger.Variable;
import flash.tools.debugger.VariableAttribute;
import flash.tools.debugger.VariableType;
import flash.tools.debugger.concrete.DValue;
import flash.tools.debugger.expression.ASTBuilder;
import flash.tools.debugger.expression.IASTBuilder;
import flash.tools.debugger.expression.NoSuchVariableException;
import flash.tools.debugger.expression.PlayerFaultException;
import flash.tools.debugger.expression.ValueExp;
import util.StringUtil;

public class ExpressionCache
{
	Session				m_session;
	IASTBuilder			m_builder;
	Vector<Object>		m_expressions;
	IntProperties		m_props;
	DebugCLI			m_cli;

	/**
	 * Returned by evaluate().
	 */
	public static class EvaluationResult
	{
		/**
		 * The value to which the expression evaluated.
		 */
		public Object value;

		/**
		 * The context that was used to evaluate the expression. Sometimes used
		 * to convert the <code>value</code> field to a <code>Value</code>
		 * with <code>context.toValue()</code>.
		 */
		public ExpressionContext context;
	}

	/**
	 * We can get at files by name or module id, eventually we will put functions in here too
	 */

	public ExpressionCache(DebugCLI cli)
	{
		m_builder = new ASTBuilder(true); // allow fdb's "*x" and "x." indirection operators
		m_expressions = new Vector<Object>();
		m_props = new IntProperties();
		m_cli = cli;
	}

	public void			clear()			{ m_expressions.clear(); }
	public void			unbind()		{ m_session = null; }
	public int			size()			{ return m_expressions.size(); }
	public Object		at(int i)		{ return m_expressions.elementAt(i); }

	void setSession(Session s)	{ m_session = s; }

	public Session		getSession()			{ return m_session; }
	public String		getPackageName(int id)	{ return m_cli.module2ClassName(id); }

	public void bind(Session s)
	{
		setSession(s);

		// propagates our properties to the session / non-critical if fails
		try { ((flash.tools.debugger.concrete.PlayerSession)s).setPreferences(m_props.map()); } catch(Exception e) {}
	}

	public EvaluationResult evaluate(ValueExp e) throws NumberFormatException, NoSuchVariableException, PlayerFaultException, PlayerDebugException
	{
		EvaluationResult result = new EvaluationResult();
		result.context = new ExpressionContext(this);
		result.value = e.evaluate(result.context);
		return result;
	}

	public ValueExp parse(String s) throws IOException, ParseException
	{
		return m_builder.parse(new StringReader(s));
	}

	public int add(Object e)
	{
		int at = m_expressions.size();
		m_expressions.add(e);
		return at+1;
	}

	//
	// Interface for accessing previous expression values and also the properties
	//
	public boolean propertyEnabled(String which)
	{
		boolean enabled = false;
		try
		{
			Number number = (Number) get(which);
			if (number != null)
				enabled = (number.intValue() != 0);
		}
		catch (Exception e)
		{
			// nothing; leave 'enabled' as false
		}
		return enabled;
	}

	// this goes in properties
	public void put(String s, int value) { m_props.put(s, value); setSessionProperty(s, value); }
	public Set<String>  keySet() { return m_props.keySet(); }

	/**
	 * Allow the session to receive property updates
	 */
	void setSessionProperty(String s, int value)
	{
		Session sess = getSession();
	    if (sess != null)
			sess.setPreference(s, value);
		Bootstrap.sessionManager().setPreference(s, value);
	}

	/**
	 * We are able to fetch properties or expressions (i.e previous expression)
	 * using this single call, despite the fact that each of these types of
	 * results lie in different data structures m_expressions and m_props.
	 * This allows us to easily perform expression evaluation without
	 * need or concern over which 'type' of $ reference we are dealing with
	 */
	public Object get(String s) throws NumberFormatException, ArrayIndexOutOfBoundsException, NoSuchElementException
	{
		Object exp = null;

		// should be of form '$n' where n is a number 0..size()
		if (s.charAt(0) != '$')
			throw new NoSuchElementException(s);

	    String num = s.substring(1);
		if (num == null || num.length() == 0)
			exp = at(size()-1);
		else if (num.equals("$")) //$NON-NLS-1$
			exp = at(size()-2);
		else
		{
			try
			{
				int index = Integer.parseInt(num);
				exp = at(index-1);
			}
			catch(NumberFormatException nfe)
			{
				// must be in the property list
				exp = m_props.getInteger(s);
			}
		}
		return exp;
	}

	//
	// Statics for formatting stuff
	//

	/**
	 * Formatting function for variable
	 */
	public static void appendVariable(StringBuilder sb, Variable v)
	{
		//sb.append('\'');
		String name = v.getName();
		sb.append(name);
		//sb.append('\'');
		sb.append(" = "); //$NON-NLS-1$
		appendVariableValue(sb, v.getValue(), name);
		//appendVariableAttributes(sb, v);
	}

	/**
	 * Given any arbitrary constant value, such as a Double, a String, etc.,
	 * format its value appropriately. For example, strings will be quoted.
	 *
	 * @param sb
	 *            a StringBuilder to which the formatted value will be appended.
	 * @param o
	 *            the value to format.
	 */
	public static void appendVariableValue(StringBuilder sb, final Object o)
	{
		Value v;

		if (o instanceof Value) {
			v = (Value) o;
		} else {
			v = new Value() {
				public int getAttributes() {
					return 0;
				}

				public String[] getClassHierarchy(boolean allLevels) {
					return new String[0];
				}

				public String getClassName() {
					return ""; //$NON-NLS-1$
				}

				public long getId() {
					return UNKNOWN_ID;
				}

				public int getMemberCount(Session s) throws NotSuspendedException,
						NoResponseException, NotConnectedException {
					return 0;
				}

				public Variable getMemberNamed(Session s, String name)
						throws NotSuspendedException, NoResponseException,
						NotConnectedException {
					return null;
				}

				public Variable[] getMembers(Session s)
						throws NotSuspendedException, NoResponseException,
						NotConnectedException {
					return new Variable[0];
				}

				public int getType() {
					if (o instanceof Number)
						return VariableType.NUMBER;
					else if (o instanceof Boolean)
						return VariableType.BOOLEAN;
					else if (o instanceof String)
						return VariableType.STRING;
					else if (o == Value.UNDEFINED)
						return VariableType.UNDEFINED;
					else if (o == null)
						return VariableType.NULL;

					assert false;
					return VariableType.UNKNOWN;
				}

				public String getTypeName() {
					return ""; //$NON-NLS-1$
				}

				public Object getValueAsObject() {
					return o;
				}

				public String getValueAsString() {
					return DValue.getValueAsString(o);
				}

				public boolean isAttributeSet(int variableAttribute) {
					return false;
				}

				public Variable[] getPrivateInheritedMembers() {
					return new Variable[0];
				}

				public Variable[] getPrivateInheritedMemberNamed(String name) {
					return new Variable[0];
				}
			};
		}

		appendVariableValue(sb, v);
	}

	public static void appendVariableValue(StringBuilder sb, Value val) { appendVariableValue(sb,val,""); } //$NON-NLS-1$

	public static void appendVariableValue(StringBuilder sb, Value val, String variableName)
	{
		int type = val.getType();
		String typeName = val.getTypeName();
		String className = val.getClassName();

		// if no string or empty then typeName is blank
		if (typeName != null && typeName.length() == 0)
			typeName = null;

        switch (type)
        {
            case VariableType.NUMBER:
            {
				double value = ((Number)val.getValueAsObject()).doubleValue();
				long longValue = (long) value;
				// The value is stored as a double; however, in practice most values are
				// actually integers.  Check to see if this is the case, and if it is,
				// then display it:
				//    - without a fraction, and
				//    - with its hex equivalent in parentheses.
				// Note, we use 'long' instead of 'int', in order to deal with the
				// ActionScript type 'uint'.
				if (longValue == value)
				{
					sb.append(longValue);
					sb.append(" (0x"); //$NON-NLS-1$
					sb.append(Long.toHexString(longValue));
					sb.append(")"); //$NON-NLS-1$
				}
				else
				{
					sb.append(value);
				}
                break;
            }

            case VariableType.BOOLEAN:
            {
                Boolean b = (Boolean)val.getValueAsObject();
                if (b.booleanValue())
                    sb.append("true"); //$NON-NLS-1$
                else
                    sb.append("false"); //$NON-NLS-1$
                break;
            }

            case VariableType.STRING:
            {
            	// Exceptions are displayed in angle brackets, e.g.
            	//     foo = <Text of exception here>
            	// Strings are displayed quoted:
            	//     foo = "Value of string here"
            	//
            	// Note that quotation marks within the string are not escaped.  This
            	// is sort of weird, but it's what we want to do, at least for now;
            	// the debugger's output is intended to be human-readable, not
            	// machine-readable, and it's easier for a person to read the string
            	// if there is no escaping of quotation marks.
            	//
            	// As a small step in the direction of avoiding that weirdness, if
            	// the string contains double-quotes but no single-quotes, we will
            	// quote it in single quotes.
            	String s = val.getValueAsString();
            	char start, end;

				if (val.isAttributeSet(ValueAttribute.IS_EXCEPTION))
				{
					start = '<';
					end = '>';
				}
				else if (s.indexOf('"') != -1 && s.indexOf('\'') == -1)
				{
					start = end = '\'';
				}
				else
				{
					start = end = '"';
				}

                sb.append(start);
                sb.append(StringUtil.escape(s));
                sb.append(end);
                break;
            }

            case VariableType.OBJECT:
            {
                sb.append("["); //$NON-NLS-1$
				sb.append(className);

				// Normally, we include the object id after the class name.
				// However, when running fdbunit, don't show object IDs, so that
				// results can reproduce consistently from one run to the next.
				if (System.getProperty("fdbunit") == null) //$NON-NLS-1$
				{
					sb.append(" "); //$NON-NLS-1$
					sb.append(StringUtil.escape(String.valueOf(val.getValueAsObject()))); // object id
				}
                if (typeName != null && !typeName.equals(className))
                {
                    sb.append(", class='"); //$NON-NLS-1$

					// Often the typename is of the form 'classname@hexaddress',
					// but the hex address is the same as the object id which
					// is returned by getValue() -- we don't want to display it
					// here.
					int at = typeName.indexOf('@');
					if (at != -1)
						typeName = typeName.substring(0, at);

                    sb.append(StringUtil.escape(typeName));
                    sb.append('\'');
                }
                sb.append(']');
                break;
            }

            case VariableType.FUNCTION:
            {
				// here we have a special case for getters/setters which
				// look like functions to us, except the attribute is set.
				sb.append('[');
				if (val.isAttributeSet(VariableAttribute.HAS_GETTER))
					sb.append(getLocalizationManager().getLocalizedTextString("getterFunction")); //$NON-NLS-1$
				else if (val.isAttributeSet(VariableAttribute.HAS_SETTER))
					sb.append(getLocalizationManager().getLocalizedTextString("setterFunction")); //$NON-NLS-1$
				else
					sb.append(getLocalizationManager().getLocalizedTextString("function")); //$NON-NLS-1$
				sb.append(' ');

                sb.append(StringUtil.escape(String.valueOf(val.getValueAsObject())));
                if (typeName != null && !typeName.equals(variableName))
                {
                    sb.append(", name='"); //$NON-NLS-1$
                    sb.append(StringUtil.escape(typeName));
                    sb.append('\'');
                }
                sb.append(']');
                break;
            }

            case VariableType.MOVIECLIP:
            {
                sb.append("["); //$NON-NLS-1$
				sb.append(className);
				sb.append(" "); //$NON-NLS-1$
                sb.append(StringUtil.escape(String.valueOf(val.getValueAsObject())));
                if (typeName != null && !typeName.equals(className))
                {
                    sb.append(", named='"); //$NON-NLS-1$
                    sb.append(StringUtil.escape(typeName));
                    sb.append('\'');
                }
                sb.append(']');
                break;
            }

            case VariableType.NULL:
            {
                sb.append("null"); //$NON-NLS-1$
                break;
            }

            case VariableType.UNDEFINED:
            {
                sb.append("undefined"); //$NON-NLS-1$
                break;
            }

            case VariableType.UNKNOWN:
            {
                sb.append(getLocalizationManager().getLocalizedTextString("unknownVariableType")); //$NON-NLS-1$
                break;
            }
        }
	}

	private static LocalizationManager getLocalizationManager()
	{
		return DebugCLI.getLocalizationManager();
	}

	public static void appendVariableAttributes(StringBuilder sb, Variable v)
	{
		if (v.getAttributes() == 0)
			return;

		sb.append("  "); //$NON-NLS-1$

		if (v.isAttributeSet(VariableAttribute.DONT_ENUMERATE))
			sb.append(", " + getLocalizationManager().getLocalizedTextString("variableAttribute_dontEnumerate")); //$NON-NLS-1$ //$NON-NLS-2$

		if (v.isAttributeSet(VariableAttribute.READ_ONLY))
			sb.append(", " + getLocalizationManager().getLocalizedTextString("variableAttribute_readOnly")); //$NON-NLS-1$ //$NON-NLS-2$

		if (v.isAttributeSet(VariableAttribute.IS_LOCAL))
			sb.append(", " + getLocalizationManager().getLocalizedTextString("variableAttribute_localVariable")); //$NON-NLS-1$ //$NON-NLS-2$

		if (v.isAttributeSet(VariableAttribute.IS_ARGUMENT))
			sb.append(", " + getLocalizationManager().getLocalizedTextString("variableAttribute_functionArgument")); //$NON-NLS-1$ //$NON-NLS-2$

		if (v.isAttributeSet(VariableAttribute.HAS_GETTER))
			sb.append(", " + getLocalizationManager().getLocalizedTextString("variableAttribute_getterFunction")); //$NON-NLS-1$ //$NON-NLS-2$

		if (v.isAttributeSet(VariableAttribute.HAS_SETTER))
			sb.append(", " + getLocalizationManager().getLocalizedTextString("variableAttribute_setterFunction")); //$NON-NLS-1$ //$NON-NLS-2$

		if (v.isAttributeSet(VariableAttribute.IS_DYNAMIC))
			sb.append(", dynamic"); //$NON-NLS-1$

		if (v.isAttributeSet(VariableAttribute.IS_STATIC))
			sb.append(", static"); //$NON-NLS-1$

		if (v.isAttributeSet(VariableAttribute.IS_CONST))
			sb.append(", const"); //$NON-NLS-1$

		if (v.isAttributeSet(VariableAttribute.PRIVATE_SCOPE))
			sb.append(", private"); //$NON-NLS-1$

		if (v.isAttributeSet(VariableAttribute.PUBLIC_SCOPE))
			sb.append(", public"); //$NON-NLS-1$

		if (v.isAttributeSet(VariableAttribute.PROTECTED_SCOPE))
			sb.append(", protected"); //$NON-NLS-1$

		if (v.isAttributeSet(VariableAttribute.INTERNAL_SCOPE))
			sb.append(", internal"); //$NON-NLS-1$

		if (v.isAttributeSet(VariableAttribute.NAMESPACE_SCOPE))
			sb.append(", " + getLocalizationManager().getLocalizedTextString("variableAttribute_hasNamespace")); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
