package pt.uminho.ceb.biosystems.mew.core.math;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;
import pt.uminho.ceb.biosystems.mew.utilities.io.FileUtils;
import pt.uminho.ceb.biosystems.mew.utilities.math.NumericList;
import pt.uminho.ceb.biosystems.mew.utilities.math.Quartile;
import pt.uminho.ceb.biosystems.mew.utilities.math.Statistics;
import pt.uminho.ceb.biosystems.mew.utilities.math.normalization.map.NormalizeMapQuantile;

public class PercentileTests {
	
	@Test
	public void percentilTest() throws IOException{
		Map<String, Double> map = getMapFromFile("./src/test/resources/math/KeysValues01");
		DescriptiveStatistics stats = new DescriptiveStatistics();
		
		for (String item : map.keySet()) {
			stats.addValue(map.get(item));
		}
		
		NumericList data = new NumericList(new ArrayList<Double>(map.values()));
		
		System.out.println("Q1: "+stats.getPercentile(25));
		System.out.println("Q2: "+stats.getPercentile(50));
		System.out.println("Q3: "+stats.getPercentile(75));
		System.out.println("Q4: "+stats.getPercentile(100));
		
		System.out.println("Total: " + stats.getSum());
		System.out.println("Mean: " + stats.getMean());
		System.out.println("Media: " + stats.getPercentile(50));
		System.out.println("Max: " + stats.getMax());
		
		System.out.println("Q1: "+Quartile.firstQuartileCut(data));
		System.out.println("Q2: "+Quartile.median(data));
		System.out.println("Q3: "+Quartile.thirdQuartileCut(data));

		
	}
	
	@Test
	public void quantileMapTest() throws IOException{
		
//		Q1: 9.866185000000002
//		Q2: 34.54595
//		Q3: 113.60249999999999
//		Q4: 35934.7
//		Total: 1353189.1904527673
//		Mean: 204.22414585764727
//		Media: 34.54595
//		Max: 35934.7
		
		NormalizeMapQuantile normalizer = new NormalizeMapQuantile();
		
		Map<String, Double> mapOriginal = normalizer.normalize(getMapFromFile("./src/test/resources/math/KeysValues01"));
		
		Map<String, Double> mapFinal = getMapFromFile("./src/test/resources/math/KeysValues02");
		
		for (String key : mapFinal.keySet()) {
			if(!mapOriginal.containsKey(key)){
				System.err.println("ALTO: " + key);
			}
			
			double originalValue = mapOriginal.get(key);
			double finalValue = mapFinal.get(key);
			
			if(originalValue != finalValue){
				System.err.println("ALTO: " + key + " Diff: " + originalValue + " : " + finalValue);
			}
		}
	}
	
	protected Map<String, Double> getMapFromFile(String filepath) throws IOException{
		Map<String, Double> map = new HashMap<String, Double>();
		
		List<String> list = FileUtils.readLines(filepath);
		for (String line : list) {
			if(line == null || line.isEmpty())
				continue;
			String[] columns = line.split("\\t");
			map.put(columns[0], Double.parseDouble(columns[1]));
		}
		
		return map;
	}

	@Test
	public void printQuantileTest() throws Exception{
		
		
//		Map<String, Double> map = FileUtils.readStringValueTableFile("/home/hgiesteira/Desktop/EclipseProjectsMaven/SilicoWSDev/silico_core/src/test/resources/analysis/dataintegration/KeysValues01", "\t", true, 0, 5, true);
		
//		Map<String, Map<String, String>> allData = FileUtils.readTableFileFormat("/home/hgiesteira/Desktop/EclipseProjectsMaven/SilicoWSDev/silico_core/src/test/resources/analysis/dataintegration/KeysValues01", "\t", 0, true);
		
//		Map<String, Double> quartilemap = Statistics.convertMapToQuantile(map);
		
//		MapUtils.prettyPrint(allData);
		
//		Statistics.printMapStatistics(map);
		
	}
}
