package pt.uminho.ceb.biosystems.mew.mewcore.optimization.components;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.MOUtils;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.list.ListPairs;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;
import pt.uminho.ceb.biosystems.mew.utilities.java.StringUtils;

public class OverUnderSolutionSet {
	public static Double						FIT_THRESHOLD		= 0.00001;
	public static Double						THRESH_2			= 0.00002;
	protected List<List<Pair<String, Double>>>	solutions;
	protected List<double[]>					attributes;
	protected String[]							attributeNames;
	protected int								maxAttributeIndex	= -1;
	protected HashSet<String>					solutionHash		= null;
	
	public OverUnderSolutionSet() {
		solutions = new ArrayList<List<Pair<String, Double>>>();
		attributes = new ArrayList<double[]>();
		attributeNames = null;
	}
	
	public List<Pair<String, Double>> getSolution(int index) {
		return solutions.get(index);
	}
	
	public double[] getAttributes(int index) {
		return attributes.get(index);
	}
	
	public int size() {
		return solutions.size();
	}
	
	// assuming ordered list
	public int findSolution(List<Pair<String, Double>> targetSolution) {
		int c = -1;
		int i = 0;
		
		while (c < 0 && i < solutions.size()) {
			ListPairs sol = new ListPairs(getSolution(i));
			ListPairs ts = new ListPairs(targetSolution);
			c = sol.compareToList(ts);
			
			if (c == 0)
				return i;
			else if (c < 0)
				i++;
			else
				return -1;
		}
		
		return -1;
	}
	
	public List<Integer> findSubsetSolutions(List<Pair<String, Double>> targetSolution) {
		List<Integer> list = new ArrayList<Integer>();
		
		for (int i = 0; i < solutions.size(); i++) {
			ListPairs sol = new ListPairs(solutions.get(i));
			ListPairs ts = new ListPairs(targetSolution);
			if (ts.containsList(sol)) list.add(i);
		}
		
		return list;
	}
	
	public List<Integer> findSupersetSolutions(List<Pair<String, Double>> targetSolution) {
		List<Integer> list = new ArrayList<Integer>();
		
		for (int i = 0; i < solutions.size(); i++) {
			ListPairs sol = new ListPairs(solutions.get(i));
			ListPairs ts = new ListPairs(targetSolution);
			
			if (ts.isContained(sol)) list.add(i);
			
		}
		
		return list;
	}
	
	// checks if solution is there or a sub-solution with better fitness is there
	public boolean checkIfSolutionAndSubsetExists(List<Pair<String, Double>> solution, double... attribs) {
		Collections.sort(solution, new Comparator<Pair<String, Double>>() {
			@Override
			public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
				return o1.compareToByValue(o2);
			};
		});
		
		if (findSolution(solution) >= 0) {
			return true;
		}
		
		List<Integer> ss = findSubsetSolutions(solution);
		for (int k = 0; k < ss.size(); k++) {
			int index = ss.get(k);
			double[] attsSS = attributes.get(index);
			boolean allBetter = true;
			for (int j = 0; j < attsSS.length; j++)
				
				if (attribs[j] - attsSS[j] > THRESH_2) allBetter = false;
			
			if (allBetter) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean checkIfSolutionExistsIgnoreAttributes(List<Pair<String, Double>> solution) {
		
		Collections.sort(solution, new Comparator<Pair<String, Double>>() {
			@Override
			public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
				return o1.compareToByValue(o2);
			};
		});
		return (findSolution(solution) >= 0);
	}
	
	public void removeSupersetNonBetterSolutions(List<Pair<String, Double>> solution, double... attribs) {
		
		List<Integer> ss = findSupersetSolutions(solution);
		List<Integer> toremove = new ArrayList<Integer>();
		
		for (int k = 0; k < ss.size(); k++) {
			double[] attsSS = this.attributes.get(ss.get(k));
			boolean allBetter = true;
			for (int j = 0; j < attsSS.length; j++)
				
				if (attsSS[j] - attribs[j] > FIT_THRESHOLD) allBetter = false;
			if (allBetter) toremove.add(ss.get(k));
			
		}
		Collections.sort(toremove);
		
		for (int k = toremove.size() - 1; k >= 0; k--) {
			int index = toremove.get(k);
			removeSolution(index);
		}
		
	}
	
	public void addSolution(Set<Pair<String, Double>> solution, double... attribs) {
		
		List<Pair<String, Double>> pairedSolution = new ArrayList<Pair<String, Double>>();
		for (Pair<String, Double> k : solution)
			pairedSolution.add(k);
		
		addSolution(pairedSolution, attribs);
	}
	
	public void addSolution(List<Pair<String, Double>> solution, double... attribs) {
		
		solutions.add(solution);
		attributes.add(attribs);
		
		if (maxAttributeIndex == -1) maxAttributeIndex = attribs.length;
	}
	
	public void addSolution(int index, List<Pair<String, Double>> solution, double... attribs) {
		solutions.add(index, solution);
		attributes.add(index, attribs);
		
		if (maxAttributeIndex == -1) maxAttributeIndex = attribs.length;
	}
	
	public void removeSolution(int index) {
		this.solutions.remove(index);
		this.attributes.remove(index);
	}
	
	public void addKnockoutSolution(Set<String> solution, double... attribs) {
		List<Pair<String, Double>> pairedSolution = new ArrayList<Pair<String, Double>>();
		for (String k : solution)
			pairedSolution.add(new Pair<String, Double>(k, 0.0));
		
		addSolution(pairedSolution, attribs);
	}
	
	public int addOrderedKnockoutSolution(Set<String> solution, double... attribs) {
		
		List<Pair<String, Double>> pairedSolution = new ArrayList<Pair<String, Double>>();
		for (String k : solution)
			pairedSolution.add(new Pair<String, Double>(k, 0.0));
		
		return addOrdered(pairedSolution, attribs);
	}
	
	public boolean addOrderedNoRepeatKnockoutSolution(Set<String> solution, double... atts) {
		List<Pair<String, Double>> pairSol = new ArrayList<Pair<String, Double>>();
		for (String s : solution)
			pairSol.add(new Pair<String, Double>(s, 0.0));
		
		if (!checkIfSolutionExistsIgnoreAttributes(pairSol)) {
			addOrdered(pairSol, atts);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Implementation of an insertion sort for a <code>SolutionSet</code>
	 * 
	 * @param solution
	 *            the solution to add
	 * @param attribs
	 *            the attributes of the solution to add
	 * 
	 * @return the index of insertion or -1 if no insertion happens
	 */
	
	public int addOrdered(List<Pair<String, Double>> solution, double... attribs) {
		if (maxAttributeIndex == -1) maxAttributeIndex = attribs.length;
		
		Collections.sort(solution, new Comparator<Pair<String, Double>>() {
			@Override
			public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
				return o1.compareToByValue(o2);
			};
			
		});
		
		if (findSolution(solution) >= 0) {
			//			System.out.println("found");
			return -1;
		}
		
		if (solutions.size() == 0) {
			addSolution(0, solution, attribs);
			return 0;
		} else {
			for (int i = 0; i < solutions.size(); i++) {
				List<Pair<String, Double>> sol = solutions.get(i);
				int sizeLocal = sol.size();
				int minSize = Math.min(sol.size(), solution.size());
				for (int j = 0; j < minSize; j++) {
					String kLocal = sol.get(j).getValue();
					String kCandidate = solution.get(j).getValue();
					int comp = kLocal.compareTo(kCandidate);
					if (comp == 0) {
						if (j == minSize - 1 && sizeLocal > minSize) {
							addSolution(i, solution, attribs);
							return i;
						}
						continue;
					} else if (comp < 0)
						break;
					else {
						addSolution(i, solution, attribs);
						return i;
					}
				}
			}
			solutions.add(solution);
			attributes.add(attribs);
			return solutions.size();
		}
	}
	
	public void computeRank(int attribToUse, int attribToKeep) {
		
		for (int i = 0; i < solutions.size(); i++) {
			int klarge = 0;
			for (int j = 0; j < solutions.size(); j++) {
				
				if ((i != j) && (attributes.get(j)[attribToUse] > attributes.get(i)[attribToUse])) klarge++;
				
			}
			attributes.get(i)[attribToKeep] = klarge;
		}
	}
	
	// NOT DONE
	//	public int[] orderByAttribute(int attributeIndex){
	//		
	//		if(attributeIndex>maxAttributeIndex)
	//			throw new IllegalArgumentException("Maximum index for attributes is "+maxAttributeIndex);
	//		
	//		double[] column = getAttributeColumn(attributeIndex);
	//		
	//		Arrays.sort(column);
	//		
	//		return null;
	//	}
	
	public double[][] getAttributes() {
		if (this.attributes.size() > 0 && this.maxAttributeIndex > 0) {
			double[][] res = new double[this.attributes.size()][this.maxAttributeIndex];
			
			for (int i = 0; i < this.size(); i++)
				res[i] = this.attributes.get(i);
			
			return res;
		}
		return null;
	}
	
	public double[][] getNonDominatedAttributes() {
		double[][] atts = getAttributes();
		
		int n = MOUtils.filterNonDominatedFront(atts, atts.length, this.maxAttributeIndex);
		
		double[][] res = new double[n][];
		for (int k = 0; k < n; k++)
			res[k] = atts[k];
		
		return res;
	}
	
	public double[][] getNonDominatedAttributesPlusKOs() {
		if (attributes.size() != 0) {
			double[][] values = new double[attributes.size()][attributes.get(0).length + 1];
			
			for (int i = 0; i < attributes.size(); i++) {
				for (int k = 0; k < attributes.get(i).length; k++)
					values[i][k] = attributes.get(i)[k];
				int sizeSolution = this.getSolution(i).size();
				values[i][attributes.get(i).length] = (double) sizeSolution;
			}
			
			int n = MOUtils.filterNonDominatedFront(values, values.length, values[0].length);
			
			double[][] res = new double[n][];
			for (int k = 0; k < n; k++)
				res[k] = values[k];
			
			return res;
		} else
			return null;
		
	}
	
	public double[] getAttributeColumn(int attIndex) {
		
		if (attIndex > maxAttributeIndex) throw new IllegalArgumentException("Maximum index for attributes is " + maxAttributeIndex);
		
		double[] column = new double[attributes.size()];
		
		for (int i = 0; i < attributes.size(); i++)
			column[i] = attributes.get(i)[attIndex];
		
		return column;
		
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < this.solutions.size(); i++) {
			String sol = Arrays.toString(solutions.get(i).toArray());
			String att = Arrays.toString(attributes.get(i));
			
			sb.append(sol + "," + att);
			sb.append("\n");
		}
		
		return sb.toString();
	}
	
	public int maxNumberOfAttributes() {
		return maxAttributeIndex;
	}
	
	public OverUnderSolutionSet merge(OverUnderSolutionSet anotherSet) {
		OverUnderSolutionSet newSet = new OverUnderSolutionSet();
		
		// copy 
		newSet.maxAttributeIndex = this.maxAttributeIndex;
		for (int i = 0; i < solutions.size(); i++) {
			newSet.solutions.add(this.solutions.get(i));
			newSet.attributes.add(this.attributes.get(i));
		}
		
		for (int i = 0; i < anotherSet.size(); i++) {
			List<Pair<String, Double>> ls = anotherSet.getSolution(i);
			double[] atts = anotherSet.getAttributes(i);
			
			if (!newSet.checkIfSolutionAndSubsetExists(ls, atts)) {
				newSet.addOrdered(ls, atts);
				newSet.removeSupersetNonBetterSolutions(ls, atts);
			}
			//			else {
			//				System.out.println("solutions exists!");
			//			}
		}
		
		return newSet;
	}
	
	public OverUnderSolutionSet mergeIgnoreAttributes(OverUnderSolutionSet anotherSet) {
		
		OverUnderSolutionSet newSet = new OverUnderSolutionSet();
		
		newSet.maxAttributeIndex = this.maxAttributeIndex;
		for (int i = 0; i < solutions.size(); i++) {
			newSet.solutions.add(this.solutions.get(i));
			newSet.attributes.add(this.attributes.get(i));
		}
		
		for (int i = 0; i < anotherSet.size(); i++) {
			List<Pair<String, Double>> ls = anotherSet.getSolution(i);
			double[] atts = anotherSet.getAttributes(i);
			
			if (!newSet.checkIfSolutionExistsIgnoreAttributes(ls)) newSet.addOrdered(ls, atts);
			//			else
			//				System.out.println("solutions exists!");
		}
		
		return newSet;
	}
	
	public void mergeHashed(OverUnderSolutionSet anotherSet) {		
				
		for (int i = 0; i < anotherSet.size(); i++) {
			List<Pair<String, Double>> ls = anotherSet.getSolution(i);
			double[] atts = anotherSet.getAttributes(i);
			TreeSet<String> tset = new TreeSet<String>();
			for(Pair<String,Double> elem : ls)
				tset.add(elem.getA());
			
			String sol = StringUtils.concat(",", tset);
			
			if (!getSolutionHash().contains(sol)){ 
				addSolution(ls, atts);
				getSolutionHash().add(sol);
			}else
				System.out.println("CONTAINS!");
		}		
	}
	
	public HashSet<String> getSolutionHash(){
		if(solutionHash==null){
			solutionHash = new HashSet<String>();
			for(List<Pair<String,Double>> sol : solutions){
				TreeSet<String> tset = new TreeSet<String>();
				for(Pair<String,Double> elem : sol)
					tset.add(elem.getA());
				
				solutionHash.add(StringUtils.concat(",", tset));
			}
		}
		
		return solutionHash;
	}
	public double[] getBestSolutionForAttribute(int attributeIndex) {
		if (size() == 0) return null;
		
		double max = this.attributes.get(0)[attributeIndex];
		double[] res = this.attributes.get(0);
		
		for (int i = 1; i < size(); i++) {
			if (this.attributes.get(i)[attributeIndex] > max) {
				max = this.attributes.get(i)[attributeIndex];
				res = this.attributes.get(i);
			}
		}
		
		return res;
	}
	
	public double[] getBestSolutionForAttributeConstrained(int atrMaxIndex, int atrConstIndex, double minValue) {
		
		double max = Double.NEGATIVE_INFINITY;
		double[] res = null;
		
		for (int i = 0; i < size(); i++) {
			if (this.attributes.get(i)[atrConstIndex] > minValue && this.attributes.get(i)[atrMaxIndex] > max) {
				max = this.attributes.get(i)[atrMaxIndex];
				res = this.attributes.get(i);
			}
		}
		
		return res;
	}
	
	public double[] getBestSolutionForMultiplication(int attributeIndex1, int attributeIndex2) {
		if (size() == 0) return null;
		
		double max = this.attributes.get(0)[attributeIndex1] * this.attributes.get(0)[attributeIndex2];
		double[] res = this.attributes.get(0);
		
		for (int i = 1; i < size(); i++) {
			double mult = this.attributes.get(i)[attributeIndex1] * this.attributes.get(i)[attributeIndex2];
			if (mult > max) {
				max = mult;
				res = this.attributes.get(i);
			}
		}
		
		return res;
	}
	
	public OverUnderSolutionSet[] splitUsingSolutionSize(int minSize, int maxSize) {
		
		OverUnderSolutionSet[] res = new OverUnderSolutionSet[maxSize - minSize + 1];
		
		for (int i = 0; i < res.length; i++)
			res[i] = new OverUnderSolutionSet();
		
		for (int i = 0; i < size(); i++) {
			List<Pair<String, Double>> sol = solutions.get(i);
			int solsize = sol.size();
			
			if (solsize >= minSize && solsize <= maxSize) res[solsize - minSize].addSolution(sol, getAttributes(i));
			
		}
		
		return res;
	}
	
	public void print() {
		for (int i = 0; i < this.solutions.size(); i++) {
			
			List<Pair<String, Double>> ids = solutions.get(i);
			for (int j = 0; j < ids.size(); j++) {
				
				if (j > 0) System.out.print(" ");
				
				System.out.print(ids.get(j));
			}
			
			for (int k = 0; k < attributes.get(i).length; k++)
				System.out.print("\t" + attributes.get(i)[k]);
			
			System.out.print("\n");
		}
	}
	
	public void saveToCSVFile(String filename) throws Exception {
		FileWriter fw = new FileWriter(filename);
		BufferedWriter bw = new BufferedWriter(fw);
		
		for (int i = 0; i < this.solutions.size(); i++) {
			
			for (int k = 0; k < attributes.get(i).length; k++)
				bw.write(attributes.get(i)[k] + ",");
			
			List<Pair<String, Double>> ids = solutions.get(i);
			for (int j = 0; j < ids.size(); j++) {
				
				//				if (j>0) bw.write(" ");
				
				bw.write("," + ids.get(j).getValue() + "=" + ids.get(j).getPairValue());
			}
			
			bw.write("\n");
		}
		
		bw.close();
		fw.close();
	}
	
	public void saveToCSVFileIgnoreAttributes(String filename) throws Exception {
		FileWriter fw = new FileWriter(filename);
		BufferedWriter bw = new BufferedWriter(fw);
		
		for (int i = 0; i < this.solutions.size(); i++) {
			
			List<Pair<String, Double>> ids = solutions.get(i);
			for (int j = 0; j < ids.size(); j++) {
				
				if (j != 0) bw.write(",");
				
				bw.write(ids.get(j).getValue() + "=" + ids.get(j).getPairValue());
			}
			bw.write("\n");
		}
		
		bw.close();
		fw.close();
	}
	
	public void saveToCSVFileIgnoreExpressionValues(String filename) throws Exception {
		FileWriter fw = new FileWriter(filename);
		BufferedWriter bw = new BufferedWriter(fw);
		
		for (int i = 0; i < this.solutions.size(); i++) {
			
			for (int k = 0; k < attributes.get(i).length; k++)
				bw.write(attributes.get(i)[k] + ",");
			
			List<Pair<String, Double>> ids = solutions.get(i);
			for (int j = 0; j < ids.size(); j++) {
				bw.write("," + ids.get(j).getValue());
			}
			
			bw.write("\n");
		}
		
		bw.close();
		fw.close();
	}
	
	//	public void saveToCSVFileWithHeader(String filename, String header)
	
	public void saveNonDominatedToFile(String filename, String sep) throws Exception {
		this.saveNonDominatedToFile(filename, sep, false);
	}
	
	public void saveNonDominatedToFile(String filename, String sep, boolean useSolSize) throws Exception {
		
		double[][] nonDominated;
		if (useSolSize)
			nonDominated = getNonDominatedAttributesPlusKOs();
		else
			nonDominated = getNonDominatedAttributes();
		
		System.out.println("Non dominated size " + nonDominated.length);
		
		FileWriter fw = new FileWriter(filename);
		BufferedWriter bw = new BufferedWriter(fw);
		
		for (int i = 0; i < nonDominated.length; i++) {
			
			for (int k = 0; k < nonDominated[i].length; k++) {
				bw.write(nonDominated[i][k] + "");
				
				if (k != nonDominated[i].length - 1) bw.write(sep);
				
			}
			bw.write("\n");
		}
		
		bw.close();
		fw.close();
	}
	
	public void loadFromCSVFile(String filename, boolean ordered) throws NumberFormatException, IOException {
		FileReader fr = new FileReader(filename);
		BufferedReader br = new BufferedReader(fr);
		
		while (br.ready()) {
			String line = br.readLine();
			String[] tokens = line.split(",,");
			String[] attributtes = tokens[0].split(",");
			String[] elements = {};
			
			if (tokens.length > 1) elements = tokens[1].split(",");
			
			ListPairs idList = new ListPairs();
			for (String elem : elements) {
				String id = null;
				Double value = null;
				String[] tk = elem.split("=");
				if (tk.length == 2) {
					id = tk[0];
					value = Double.parseDouble(tk[1]);
				} else {
					id = elem;
					value = 0.0;
				}
				Pair<String, Double> pair = new Pair<String, Double>(id, value);
				idList.add(pair);
				
			}
			
			double[] atts = new double[attributtes.length];
			for (int k = 0; k < atts.length; k++)
				atts[k] = Double.parseDouble(attributtes[k]);
			
			if (ordered)
				addOrdered(idList, atts);
			else
				addSolution(idList, atts);
		}
		
		br.close();
		fr.close();
	}
	
	public void loadFromCSVFile(String filename) throws NumberFormatException, IOException {
		loadFromCSVFile(filename, true);
	}
	
	/**
	 * @return the attributeNames
	 */
	public String[] getAttributeNames() {
		
		return attributeNames;
	}
	
	/**
	 * @param attributeNames
	 *            the attributeNames to set
	 */
	public void setAttributeNames(String[] attributeNames) {
		this.attributeNames = attributeNames;
	}
	
}
