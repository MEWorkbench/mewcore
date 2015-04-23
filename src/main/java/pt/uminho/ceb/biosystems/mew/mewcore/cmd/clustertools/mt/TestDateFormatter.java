package pt.uminho.ceb.biosystems.mew.mewcore.cmd.clustertools.mt;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class TestDateFormatter {

	public static void main(String... args){


		//		long init = System.currentTimeMillis();
		//		
		//		TestDateFormatter.wait(1000);

		//		long end = System.currentTimeMillis();


		//		long total = end - init;
		long total = 523423026;
		Date date = new Date(total);
		SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss:SSS");		
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
//		String dateFormatted = TestDateFormatter.convertDateString(formatter.format(date));
//		System.out.println("Execution took: "+dateFormatted);
		System.out.println(formatter.format(date));
		
		String s = String.format("%dd:%dh:%dm:%ds:%dms",
				TimeUnit.MILLISECONDS.toDays(total),
				TimeUnit.MILLISECONDS.toHours(total) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(total)),
			    TimeUnit.MILLISECONDS.toMinutes(total) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(total)),
			    TimeUnit.MILLISECONDS.toSeconds(total) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(total)),
			    total - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(total))
			);
		System.out.println(s);
	}

	public static void wait (int n){
		long t0,t1;
		t0=System.currentTimeMillis();
		do{
			t1=System.currentTimeMillis();
		}
		while (t1-t0<n);
	}
	
	public static String convertDateString(String in){
		String[] tokens = in.split(":");
		
		return tokens[0]+"h"+tokens[1]+"m"+tokens[2]+"s"+tokens[3]+"ms";
	}
}
