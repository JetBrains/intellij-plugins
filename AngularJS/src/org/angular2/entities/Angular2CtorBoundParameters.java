package org.angular2.entities;

import java.util.Collection;
import java.util.Collections;

/**
 * A simple object which hold constructor's bound parameters.
 */
public class Angular2CtorBoundParameters {
	public final Collection<Angular2DirectiveCtorParameter> attributes;

	private Angular2CtorBoundParameters(
				final Collection<Angular2DirectiveCtorParameter> attributes) {
		this.attributes = attributes;
	}

	public static Angular2CtorBoundParameters of(
				final Collection<Angular2DirectiveCtorParameter> attributes) {
		return new Angular2CtorBoundParameters(attributes);
	}

	public static Angular2CtorBoundParameters empty() {
		return new Angular2CtorBoundParameters(Collections.emptyList());
	}
}
