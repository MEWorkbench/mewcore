package pt.uminho.ceb.biosystems.mew.mewcore.utils;

public enum DistanceMetric {

	SAD{

		@Override
		public String getDescription() {		
			return "Sum of Absolute Differences";
		}

		public String getFormula(){
			return "SAD(A,B) = \u2211|A-B|"; 
		}

		public double calculate(double[] values1, double[] values2) {
			double distance = 0.0;

			for(int i=0; i<values1.length; i++)
				distance+=Math.abs(values1[i]-values2[i]);

			return distance;
		}
	},
	SSD{

		@Override
		public String getDescription() {
			return "Sum of Squared Differences";
		}

		@Override
		public String getFormula() {
			return "SSD(A,B) = \u2211(A-B)\u00B2";
		}

		public double calculate(double[] values1, double[] values2) {
			double distance = 0.0;

			for(int i=0; i<values1.length; i++)
				distance+=Math.pow(values1[i]-values2[i],2);

			return distance;
		}

	},
	HAMMING{

		@Override
		public String getDescription() {		
			return "Hamming Distance";
		}

		@Override
		public String getFormula() {
			return "";
		}

		//TODO
		public double calculate(double[] values1, double[] values2) {
			return 0;
		}		
	},
	JACCARD{

		@Override
		public String getDescription() {
			return "Jaccard Distance";
		}

		@Override
		public String getFormula() {
			return "Jd(A,B) = 1- Ji(A,B) = (|A \u222A B| - |A \u2229 B|) / |A \u222A B|";
		}

		public double calculate(double[] values1, double[] values2) {
			
			int n11 = 0;
			int n10 = 0;
			int n01 = 0;
			
			for(int i=0; i<values1.length; i++){
				boolean up1 = values1[i]!=0.0;
				boolean up2 = values2[i]!=0.0;
								
				if(up1 && up2) n11++;
				else if(up1) n10++;
				else if(up2) n01++;
			}
			
			int union = n01+n10+n11;
			
			return (union<=0) ? 0.0d : ((double) (n10+n01))/ ((double) union);			
		}
	},
	EUCLIDEAN{

		@Override
		public String getDescription() {
			return "Euclidean Distance";
		}

		@Override
		public String getFormula() {
			return "\u221A(\u2211(A-B)\u00B2)";
		}

		@Override
		public double calculate(double[] values1, double[] values2) {
			double sumXY2 = 0.0;
			for(int i = 0, n = values1.length; i < n; i++) {
				sumXY2 += Math.pow(values1[i] - values2[i], 2);
			}
			return Math.sqrt(sumXY2);
		}
	};

	public abstract String getDescription();
	public abstract String getFormula();
	public abstract double calculate(double[] values1, double[] values2);


	public static void main(String... args){
		System.out.println(DistanceMetric.SAD.getDescription()+" = "+DistanceMetric.SAD.getFormula());
		System.out.println(DistanceMetric.SSD.getDescription()+" = "+DistanceMetric.SSD.getFormula());
		System.out.println(DistanceMetric.HAMMING.getDescription()+" = "+DistanceMetric.HAMMING.getFormula());
		System.out.println(DistanceMetric.JACCARD.getDescription()+" = "+DistanceMetric.JACCARD.getFormula());
		System.out.println(DistanceMetric.EUCLIDEAN.getDescription()+" = "+DistanceMetric.EUCLIDEAN.getFormula());
		
		System.out.println(DistanceMetric.JACCARD.calculate(new double[]{1,1,1,1}, new double[]{0,0,0,0}));
	}
}
