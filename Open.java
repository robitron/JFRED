
 import java.io.*; 
 
 class Open { 
	
	Open(String url) 
	{ 
	
		try { 
				Process P1 = Runtime.getRuntime().exec("open " + url) ;
			} 
				catch (IOException ex) 
			{
				//nutin
			}
	}
 }