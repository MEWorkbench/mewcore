package pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Interface containing methods regarding generic configuration
 * 
 * @author ptiago on 03-03-2015.
 */
public interface IGenericConfiguration extends Serializable {
	/**
	 * Returns the property registered in the configuration
	 * 
	 * @param propertyId
	 *            name of the property - e.g. jecoli.ismaximization
	 * @return Object of a predefined value
	 */
	Object getProperty(String propertyId);

	/**
	 * Registers or overwrites a property in the configuration
	 * 
	 * @param propertyId
	 *            - property identification
	 * @param value
	 *            to register in the configuration
	 */
	void setProperty(String propertyId, Object value);

	/**
	 * Validates the types of the objects registered in the configuration
	 * 
	 * @throws InvalidConfigurationException
	 *             - Exception regarding invalid object specifications or
	 *             missing objects in the configuration
	 */
	void validate() throws InvalidConfigurationException;

	/**
	 *
	 * @return set of mandatory properties
	 */
	Set<String> getMandatoryPropertySet();

	/**
	 *
	 * @return set of optional properties
	 */
	Set<String> getOptionalPropertySet();

	/**
	 *
	 * @return The merge of the mandatory and optional property sets
	 */
	Set<String> getPropertySet();

	/**
	 *
	 * @return The property map as a map - It may be deprecated in the future
	 *         (to fully encapsulate this element)
	 */
	Map<String, Object> getPropertyMap();

}
