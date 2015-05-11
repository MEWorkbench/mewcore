package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.configuration;

import java.util.List;

public class InvalidConfigurationException extends Exception {

    public InvalidConfigurationException(List<String> nonSpeciefiedPropertyList) {
        super(createListMessage(nonSpeciefiedPropertyList));
    }

    private static String createListMessage(List<String> nonSpeciefiedPropertyList) {
        String unspecifiedPropertyString = "The following properties are not specified and are MANDATORY:";
        for(String unspecifiedPropertyId:nonSpeciefiedPropertyList)
            unspecifiedPropertyString += "\n"+unspecifiedPropertyId+"\n";

        return unspecifiedPropertyString;
    }
}
