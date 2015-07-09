/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Surrounding;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;

import Location2.Location2;
//import Weather.NoaaISDFileReader;

/**
 *
 * @author jiahuili
 */
public class SurroundingDatabase {

    public File folder;
    public SurroundingFileReader SurReader;
    //Float matchScale = 0.5f;
	
    /**
     * Constructing a database from a folder
     * @param folder
     */
    public SurroundingDatabase(String folder){
        this.folder = new File(folder);
        this.SurReader = new SurroundingFileReader();
        
    }
	
    /**
     * 
     * @param fileName input file is "surrounding.txt"
     * @return ListOfInterestedSurroundings ArrayList of Interested Surroundings
     */
    public ArrayList<String> buildListOfInterestedSurroundings(String fileName){
    	ArrayList<String> ListOfInterestedSurroundings = new ArrayList<String>();
    	
    	BufferedReader br = null;
    	
    	try {
    		
    		br = new BufferedReader(new FileReader(fileName));
    
    		for(String sCurrentLine; (sCurrentLine = br.readLine()) != null; ) {
    			ListOfInterestedSurroundings.add(sCurrentLine);
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
    	
    	return ListOfInterestedSurroundings;
    }
    

   
    
    /**
     * write new Surrounding Database with specific location feature codes
     * @param surroundingFeatures file "surrounding.txt" with specific location feature codes
     */
    public void writeToNewSurroundingDatabase(String surroundingFeatures){
    	    
        int i=0;
        ArrayList<String> ListOfInterestedSurroundings = buildListOfInterestedSurroundings(surroundingFeatures);
    	
        System.out.println("ListOfInterestedSurroundings has been built!!!");
        
        for (final File fileEntry : folder.listFiles()) {
     	   if(i != 0){		//first file is .DS_Store (with no location data)
     		  System.out.println("start write file " + fileEntry.getName() + ": ");
         	  SurReader.writeToNewSurroundingFile(fileEntry, ListOfInterestedSurroundings, new File("/Users/jiahuili/Desktop/Rewind/surroungdingsExtracted/"+fileEntry.getName()));
     	   }
     	   i++;
     	   
        }

    }
  
   
  
    

   
	

}
