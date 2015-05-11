package pt.uminho.ceb.biosystems.mew.mewcore.utils;

import pt.uminho.ceb.biosystems.mew.mewcore.cmd.searchtools.ClusterConfigurationGenerator;
import pt.uminho.ceb.biosystems.mew.mewcore.cmd.searchtools.SearchConfiguration;

public class TestConditionalProperties {
	
	public static void main(String... args) throws Exception{
		String conf = "files/testConditionalProperties/test.conf";
		
		SearchConfiguration config = new SearchConfiguration(conf);
		ClusterConfigurationGenerator gen = new ClusterConfigurationGenerator(config, 1);
		gen.setBaseDir("files/testConditionalProperties/");
		gen.build();
		
	}
	
}
