package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by ptiago on 03-03-2015.
 */
public class GenericConfiguration implements IGenericConfiguration {
	
	private static Logger			logger	= Logger.getLogger(GenericConfiguration.class.getName());
	protected Map<String, Class<?>>	mandatoryPropertyMap;
	protected Map<String, Class<?>>	optionalPropertyMap;
	protected Map<String, Object>	propertyMap;
	
	public GenericConfiguration() {
		mandatoryPropertyMap = new HashMap<>();
		optionalPropertyMap = new HashMap<>();
		propertyMap = new HashMap<>();
	}
	
	@Override
	public Object getProperty(String propertyId) {
		return propertyMap.get(propertyId);
	}
	
	@Override
	public void setProperty(String propertyId, Object value) {
		propertyMap.put(propertyId, value);
	}
	
	@Override
	public void validate() throws InvalidConfigurationException, ClassCastException {
		List<String> nonDefinedPropertyList = new ArrayList<>();
		for (Map.Entry<String, Class<?>> propertyEntry : mandatoryPropertyMap.entrySet()) {
			String propertyId = propertyEntry.getKey();
			Class<?> propertyObject = propertyEntry.getValue();
			Object value = propertyMap.get(propertyId);
			if (value == null) {
				nonDefinedPropertyList.add(propertyId);
			} else {
				propertyObject.cast(value);
			}
		}
		if (nonDefinedPropertyList.size() > 0) throw new InvalidConfigurationException(nonDefinedPropertyList);
	}
	
	public <T> T getDefaultValue(String propertyId, T defaultValue) {
		@SuppressWarnings("unchecked")
		T value = (T) propertyMap.get(propertyId);
		if (value == null)
			return defaultValue;
		else
			return value;
	}
	
	@Override
	public Set<String> getMandatoryPropertySet() {
		return mandatoryPropertyMap.keySet();
	}
	
	@Override
	public Set<String> getOptionalPropertySet() {
		return optionalPropertyMap.keySet();
	}
	
	public Set<String> getPropertySet() {
		Set<String> fullPropertySet = new HashSet<>();
		fullPropertySet.addAll(mandatoryPropertyMap.keySet());
		fullPropertySet.addAll(optionalPropertyMap.keySet());
		return fullPropertySet;
	}
	
	@Override
	public Map<String, Object> getPropertyMap() {
		return propertyMap;
	}
	
	public Map<String, Object> getPropertyMapCopy() {
		Map<String, Object> propertyMapCopy = new HashMap<>();
		for (Map.Entry<String, Object> entry : propertyMap.entrySet())
			propertyMapCopy.put(entry.getKey(), entry.getValue());
		return propertyMapCopy;
	}
}
