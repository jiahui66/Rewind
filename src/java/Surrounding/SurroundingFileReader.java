/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Surrounding;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;

import Location2.Location2;
/**
 *
 * @author jiahuili
 */
public class SurroundingFileReader {
    

    File fileName;
    double matchScale = 0.5f;//0.05f;
    
     
    
    /**
     * Given a file, returns indices of all satisfied samples from samplesFromOutputLocations 
     * @param samplesIndexReserved to fill this arrayList
     * @param fileName input file
     * @param samplesFromOutputLocations samplesFromOutputLocations 
     * @return Index of Reserved samples
     */
    public ArrayList<Integer> numberOfMatchedLocationsForFile(ArrayList<Integer> samplesIndexReserved, File fileName, ArrayList<Location2> samplesFromOutputLocations){
    	
    	BufferedReader br = null;
    	int matchedLocationsInFile = 0;
    	ArrayList<ArrayList<Double>> tempRangeList = new ArrayList<ArrayList<Double>>();
    	
    	
    	Double latTemp = (double) 0;
		Double lngTemp = (double) 0;
    	
    	// iterate over all samples
		for(int j=0; j<samplesFromOutputLocations.size();j++){
			Double d = Double.valueOf(samplesFromOutputLocations.get(j).latitude);
			Double e2 = Double.valueOf(samplesFromOutputLocations.get(j).longitude);
			
			double radius = 100;
			
			// build an initial list
			if(j==0){
				latTemp = d;
				lngTemp = e2;
		    	try {  		
		    		br = new BufferedReader(new FileReader(fileName));
		    		
					for(String sCurrentLine; (sCurrentLine = br.readLine()) != null; ) {
		    			String[] sCurrentLineArray = sCurrentLine.split("	");
		    		
		    			// temporarily store some nodes
						if(Math.abs(Double.parseDouble(sCurrentLineArray[4])-latTemp)<=0.3 && Math.abs(Double.parseDouble(sCurrentLineArray[5])-lngTemp)<=0.3){
							// in range of 50km, build a set representing a small range of data
							ArrayList<Double> temp = new ArrayList<Double>();
							temp.add(Double.parseDouble(sCurrentLineArray[4]));
							temp.add(Double.parseDouble(sCurrentLineArray[5]));
							tempRangeList.add(temp);
						}										
					}
					
		    	} catch (IOException e) {
		    		e.printStackTrace();
		    	} finally {
		    		try {
		    			if (br != null)br.close();
		    		} catch (IOException ex) {
		    			ex.printStackTrace();
		    		}
		    	}
		    	
		    	
				}else{
					// if input is too far away from temporary original node, build a new list
			  		if(Math.abs(latTemp-d)>=0.3 || Math.abs(lngTemp-e2)>=0.3){
			  		/*	System.out.println("^^^^^^^^^^");
			  			System.out.println(latTemp);	
			  			System.out.println(d);	
			  			System.out.println(lngTemp);	
			  			System.out.println(e2);			*/
			  			latTemp = d;
						lngTemp = e2;
						tempRangeList.clear();
						try {  		
				    		br = new BufferedReader(new FileReader(fileName));
						
							for(String sCurrentLine; (sCurrentLine = br.readLine()) != null; ) {
				    			String[] sCurrentLineArray = sCurrentLine.split("	");
				    		
				    			// temporarily store some nodes
								if(Math.abs(Double.parseDouble(sCurrentLineArray[4])-latTemp)<=0.3 && Math.abs(Double.parseDouble(sCurrentLineArray[5])-lngTemp)<=0.3){
									// in range of 50km, build a set representing a small range of data
									ArrayList<Double> temp = new ArrayList<Double>();
									temp.add(Double.parseDouble(sCurrentLineArray[4]));
									temp.add(Double.parseDouble(sCurrentLineArray[5]));
									tempRangeList.add(temp);
								}														
							}
							
				    	} catch (IOException e) {
				    		e.printStackTrace();
				    	} finally {
				    		try {
				    			if (br != null)br.close();
				    		} catch (IOException ex) {
				    			ex.printStackTrace();
				    		}
				    	}
						
						
					}
			  		
			  		
				}
				
				//System.out.println(tempRangeList.size());
				//System.out.println("check new  node "+d+" "+e2);
	  			matchedLocationsInFile = numberOfMatchedLocationsForTempList(tempRangeList, d, e2, radius);	
	  			
				if (matchedLocationsInFile > 0){
					samplesIndexReserved.add(j);
					System.out.println("node "+d+" "+e2+" is reserved!!!!");
				}
			}	  		
    		 		    	
    	

    	return samplesIndexReserved;
    }
    
    
    
    public int numberOfMatchedLocationsForTempList(ArrayList<ArrayList<Double>> tempRangeList, double d, double e2, double radius){
    	int number = 0;
    	//System.out.print("size is: ");
    	//System.out.println(tempRangeList.size());
    	for(int j=0; j<tempRangeList.size(); j++){
    		//System.out.println(GetDistance(d, e, tempRangeList.get(j).get(0), tempRangeList.get(j).get(1)));
			if(GetDistance(d, e2, tempRangeList.get(j).get(0), tempRangeList.get(j).get(1)) <= radius){
				number++;
				return number;
			}
		
		}
    	return number;
    }
    
    
    
    
    
    
    /**
     * generate a new file of locations with specific location feature codes from corresponding original file
     * @param input each file in original surrounding folder
     * @param surrounding ArrayList with specific location feature codes 
     * @param output name(address) of output file
     */
    public void writeToNewSurroundingFile(File input, ArrayList<String> surrounding, File output){
    	
    	BufferedReader br = null;
    	BufferedWriter writer = null;
    	
    	try {
    		
    		writer = new BufferedWriter(new FileWriter(output));
    		
    		br = new BufferedReader(new FileReader(input));
    		
    		for(String sCurrentLine; (sCurrentLine = br.readLine()) != null; ) {
    			String[] sCurrentLineArray = sCurrentLine.split("	");
    			
    			if (sCurrentLineArray[6].equals("H") || sCurrentLineArray[6].equals("T") || sCurrentLineArray[6].equals("V") || surrounding.contains(sCurrentLineArray[7])) {		
    	            
    				writer.write(sCurrentLine);
    				writer.newLine();
    				//this.write(sCurrentLine, output);
        		}
    	    }		
    		 		    	

    	} catch (IOException e) {
    		e.printStackTrace();
    	} finally {
    		try {
    			if (br != null)
    				br.close();
    			if ( writer != null)
                    writer.close( );
    		} catch (IOException ex) {
    			ex.printStackTrace();
    		}
    	}
    	
    	System.out.println(input.getName() +" has been written!!!");

    }
    
    
  
    /**
     * 
     * @param lat1 read latitude1
     * @param lng1 read longitude1
     * @param lat2 read latitude2
     * @param lng2 read longitude2
     * @return s*1000 distance between two coordinates in meters
     */
   public static double GetDistance(double lat1, double lng1, double lat2, double lng2){
		double EARTH_RADIUS = (double) 6378.137;
		double radLat1 = (double) Math.toRadians(lat1);
		double radLat2 = (double) Math.toRadians(lat2);
		double a = (double) (Math.toRadians(lat1) -Math.toRadians(lat2));
		double b = (double) (Math.toRadians(lng1) -Math.toRadians(lng2));
		double s = (double) (2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) + Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2))));
		s = s * EARTH_RADIUS;
	    return s*1000;
	}



}
