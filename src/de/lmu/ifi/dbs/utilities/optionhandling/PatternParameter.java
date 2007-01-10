package de.lmu.ifi.dbs.utilities.optionhandling;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import de.lmu.ifi.dbs.distance.distancefunction.DistanceFunction;

/**
 * Parameter class for a parameter specifying a pattern.
 * 
 * @author Steffi Wanka
 * 
 */
public class PatternParameter extends Parameter<String, String> {

	/**
	 * Constructs a pattern parameter with the given name and description.
	 * 
	 * @param name
	 *            the parameter name
	 * @param description
	 *            the parameter description
	 */
	public PatternParameter(String name, String description) {
		super(name, description);

	}

	/**
	 * Constructs a pattern parameter with the given name, description, and
	 * parameter constraint.
	 * 
	 * @param name
	 *            the name of this parameter
	 * @param description
	 *            the description of this parameter
	 * @param con
	 *            a parameter constraint for this parameter
	 */
	public PatternParameter(String name, String description, ParameterConstraint<String> con) {
		this(name, description);
		addConstraint(con);
	}

	@Override
	public void setValue(String value) throws ParameterException {

		if (isValid(value)) {
			this.value = value;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.lmu.ifi.dbs.utilities.optionhandling.Option#isValid(java.lang.String)
	 */
	public boolean isValid(String value) throws ParameterException {

		try {
			Pattern.compile(value);
		} catch (PatternSyntaxException e) {
			throw new WrongParameterValueException("Given pattern \"" + value + "\" for parameter \"" + getName()
					+ "\" is no valid regular expression!");
		}
		try {
			for (ParameterConstraint<String> con : this.constraints) {
				con.test(value);
			}
		} catch (ParameterException ex) {
			throw new WrongParameterValueException("Parameter constraint error for pattern" +
					" parameter "+getName() +": "+ex.getMessage());
		}
		return true;
	}

}
