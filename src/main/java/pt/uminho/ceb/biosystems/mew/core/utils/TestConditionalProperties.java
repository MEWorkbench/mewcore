package pt.uminho.ceb.biosystems.mew.core.utils;

import pt.uminho.ceb.biosystems.mew.core.cmd.searchtools.ClusterConfigurationGenerator;
import pt.uminho.ceb.biosystems.mew.core.cmd.searchtools.SearchConfiguration;

public class TestConditionalProperties {
	
	public static void main(String... args) throws Exception{
		String conf = "files/testConditionalProperties/test.conf";
		
		SearchConfiguration config = new SearchConfiguration(conf);
		ClusterConfigurationGenerator gen = new ClusterConfigurationGenerator(config, 1);
		gen.setBaseDir("files/testConditionalProperties/");
		gen.build();
		
	}
	
}
