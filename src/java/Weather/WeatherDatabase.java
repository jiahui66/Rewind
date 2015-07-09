package Weather;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import Location2.Location2;

/**
 *
 * @author jiahuili
 */

public class WeatherDatabase {

    File folder;
    NoaaISDFileReader ISDReader; //Integrated Surface Data Reader
    Float matchScale = 0.5f;
    /**
     * Constructing a database from a folder
     * @param folder
     */
    public WeatherDatabase(String folder){
        this.folder = new File(folder);
        ISDReader = new NoaaISDFileReader();
    }
    
    
   
  

    /**
     * step1: build a small weather database based on range distance
     * step2: Uses FileReader to go through every file in the small weather database to find the file which contains information
     * about a given location (with minimum distance)
     * step3: get weather info for each location from a specific file
     * @param reservedSamplesFromOutputLocations
     * @return weather information about the inputted locations
     * @throws ParseException
     */
    public ArrayList<WeatherInfo> getWeatherInfo(ArrayList<Location2> reservedSamplesFromOutputLocations) throws ParseException{
    	
    	ArrayList<WeatherInfo> weatherInfoList = new ArrayList<WeatherInfo>();
    	
    	// get sub weather database from weather
    	ArrayList<String> subWeatherDatabase = new ArrayList<String>();


    	// get a small weather database based on latitude and longitude from sub weather database
    	ArrayList<File> smallWeatherDatabase = new ArrayList<File>();
    	
    	// if stations are in the range of 50km form a given inputted location, put these stations into a small weather databse
    	float rangeDistance = 50000;
    	
    	//System.out.println("Looking for the desired file");
        //float minDistance = Float.MAX_VALUE;
        File fileEntry = new File("");
        
        
        // Build fileName -- [lat, lng] dictionary
        HashMap<String, ArrayList<String>> nameToLatLng = new HashMap<String, ArrayList<String>>();
        File isdHistoryFile = new File("/Users/jiahuili/Desktop/Rewind/isd-history.csv"); 

    	BufferedReader br = null;

        try {
        
            br = new BufferedReader(new FileReader(isdHistoryFile));
            String sCurrentLine = br.readLine();
            sCurrentLine = br.readLine();
  
            while(sCurrentLine != null){ 
            	sCurrentLine = sCurrentLine.replace("\"", "");
            	//System.out.println(sCurrentLine);
        		String[] line = sCurrentLine.split(",");
                String fileName = line[0]+"-"+line[1]+"-2015";
                // find all filenames with the same country Code as input
                String lat = line[6];
                String lng = line[7];
                //System.out.println(lat+lng); 
                if(!lat.equals("") && !lng.equals("")){
                	//System.out.println("############################");
                	ArrayList<String> temp = new ArrayList<String>();
                    
                    temp.add(lat);
                    temp.add(lng);
                    nameToLatLng.put(fileName, temp);
                    //System.out.println(fileName+"@@@"+lat+"@@@"+lng); 
                }
                
                sCurrentLine = br.readLine();
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
        
        // inside an iteration for a given location
        WeatherInfo temp1 = new WeatherInfo();
        // if no info matches this location
        WeatherInfo temp2 = new WeatherInfo();
        
        
        // build new databases
    	for(int i=0; i<reservedSamplesFromOutputLocations.size(); i++){
    	
    		if(i == 0){
        		System.out.println("building sub database!");
    			
    			// get sub weather database from weather
            	String countryCode = "US";
            	//File isdHistoryFile2 = new File("/Users/jiahuili/Desktop/Rewind/isd-history.csv");
            	subWeatherDatabase = getSubWeatherDatabase(isdHistoryFile, countryCode);
            	System.out.println("sub database is built!");
            	/*for(String s : subWeatherDatabase){
            		System.out.println(s.toString());
            	}*/
            	
            	
            	
            	System.out.println("building small database!");    		
            	// build a small weather database
    			//int j = 0;
        		for (final File eachFile : folder.listFiles()) { 
        			//System.out.println();
        			if(subWeatherDatabase.contains(eachFile.getName())){
        				//System.out.println(nameToLatLng.containsKey(eachFile.getName()));
        			
        				if(nameToLatLng.containsKey(eachFile.getName())){

            				//if(ISDReader.matchLatitudeAndLongitudeForFile(eachFile, Float.parseFloat(reservedSamplesFromOutputLocations.get(i).latitude), Float.parseFloat(reservedSamplesFromOutputLocations.get(i).longitude)) < 500){
            				if(GetDistance(Double.parseDouble(reservedSamplesFromOutputLocations.get(i).latitude), Double.parseDouble(reservedSamplesFromOutputLocations.get(i).longitude), Double.parseDouble(nameToLatLng.get(eachFile.getName()).get(0)), Double.parseDouble(nameToLatLng.get(eachFile.getName()).get(1))) < rangeDistance){
            					smallWeatherDatabase.add(eachFile);
            			/*		
            					System.out.println(eachFile.getName());
            					System.out.println("1111111");
            					System.out.println(GetDistance(Double.parseDouble(reservedSamplesFromOutputLocations.get(i).latitude), Double.parseDouble(reservedSamplesFromOutputLocations.get(i).longitude), Double.parseDouble(nameToLatLng.get(eachFile.getName()).get(0)), Double.parseDouble(nameToLatLng.get(eachFile.getName()).get(1))));
                        */		
            				}
        				}else{
        					if(ISDReader.matchLatitudeAndLongitudeForFile(eachFile, Float.parseFloat(reservedSamplesFromOutputLocations.get(i).latitude), Float.parseFloat(reservedSamplesFromOutputLocations.get(i).longitude)) < rangeDistance){
        						smallWeatherDatabase.add(eachFile);
        				/*		
        						System.out.println(eachFile.getName());
        						System.out.println("2222222");
            					System.out.println(ISDReader.matchLatitudeAndLongitudeForFile(eachFile, Float.parseFloat(reservedSamplesFromOutputLocations.get(i).latitude), Float.parseFloat(reservedSamplesFromOutputLocations.get(i).longitude)));
            			*/		
        					}
        				}
        				        				
        			}
        		}
        		//System.out.println(smallWeatherDatabase.toString());	
        		System.out.println("small database is built!");  
    			
    		   		
        	}else{
        		
        		//int j = 0;
        		if(GetDistance(Double.parseDouble(reservedSamplesFromOutputLocations.get(i).latitude), Double.parseDouble(reservedSamplesFromOutputLocations.get(i).longitude), Double.parseDouble(reservedSamplesFromOutputLocations.get(i-1).latitude), Double.parseDouble(reservedSamplesFromOutputLocations.get(i-1).longitude)) > rangeDistance){
        			System.out.println("building sub database!");
        			
        			// get sub weather database from weather
                	String countryCode = "US";
                	//File isdHistoryFile2 = new File("/Users/jiahuili/Desktop/Rewind/isd-history.csv");
                	subWeatherDatabase = getSubWeatherDatabase(isdHistoryFile, countryCode);
                	System.out.println("sub database is built!");
                	/*for(String s : subWeatherDatabase){
                		System.out.println(s.toString());
                	}*/
                	
                	
                	
                	System.out.println("building small database!");    		
                	// build a small weather database
        			//int j = 0;
            		for (final File eachFile : folder.listFiles()) { 
            			//System.out.println();
            			if(subWeatherDatabase.contains(eachFile.getName())){
            				//System.out.println(nameToLatLng.containsKey(eachFile.getName()));
            			
            				if(nameToLatLng.containsKey(eachFile.getName())){

                				//if(ISDReader.matchLatitudeAndLongitudeForFile(eachFile, Float.parseFloat(reservedSamplesFromOutputLocations.get(i).latitude), Float.parseFloat(reservedSamplesFromOutputLocations.get(i).longitude)) < 500){
                				if(GetDistance(Double.parseDouble(reservedSamplesFromOutputLocations.get(i).latitude), Double.parseDouble(reservedSamplesFromOutputLocations.get(i).longitude), Double.parseDouble(nameToLatLng.get(eachFile.getName()).get(0)), Double.parseDouble(nameToLatLng.get(eachFile.getName()).get(1))) < rangeDistance){
                					smallWeatherDatabase.add(eachFile);
                					
                			/*		System.out.println(eachFile.getName());
                					System.out.println("1111111");
                					System.out.println(GetDistance(Double.parseDouble(reservedSamplesFromOutputLocations.get(i).latitude), Double.parseDouble(reservedSamplesFromOutputLocations.get(i).longitude), Double.parseDouble(nameToLatLng.get(eachFile.getName()).get(0)), Double.parseDouble(nameToLatLng.get(eachFile.getName()).get(1))));
                            */
                            		
                				}
            				}else{
            					if(ISDReader.matchLatitudeAndLongitudeForFile(eachFile, Float.parseFloat(reservedSamplesFromOutputLocations.get(i).latitude), Float.parseFloat(reservedSamplesFromOutputLocations.get(i).longitude)) < rangeDistance){
            						smallWeatherDatabase.add(eachFile);
            				/*		
            						System.out.println(eachFile.getName());
            						System.out.println("2222222");
                					System.out.println(ISDReader.matchLatitudeAndLongitudeForFile(eachFile, Float.parseFloat(reservedSamplesFromOutputLocations.get(i).latitude), Float.parseFloat(reservedSamplesFromOutputLocations.get(i).longitude)));
                			*/		
            					}
            				}
            				        				
            			}
            		}
            		//System.out.println(smallWeatherDatabase.toString());	
            		System.out.println("small database is built!");  
        			
        		}else{
        			System.out.println("already have one!");

        		}
        		
        	}
        	
    		// get latitude and longitude
    		float latitude = Float.parseFloat(reservedSamplesFromOutputLocations.get(i).latitude);
            float longitude =  Float.parseFloat(reservedSamplesFromOutputLocations.get(i).longitude);
    		
            // define a minDistance 
            float minDistance = 18000;
            // find fileEntry match
            int j = 0;
            
            // if weatherinfo for this moment is null, set it the same as last weatherinfo
            
            
            // every WeatherInfo found
            WeatherInfo weatherInfo = null;
            
            for (final File eachFile : smallWeatherDatabase) {
            	if(j == 0) {
            		//Do nothing: Because first file is .DS_Store (with no weather data)
            		} else {
                        /* use minDistance to find a nearest station
                         * if(ISDReader.matchLatitudeAndLongitudeForFile(eachFile, latitude, longitude) < minDistance){
                        	minDistance = ISDReader.matchLatitudeAndLongitudeForFile(eachFile, latitude, longitude);
                            fileEntry = eachFile;
                        }
                        */
            			
            			// iterate over nearby station (in range of minDistance) to find the first one that contains precipitation data, otherwise return the weather info in the last station
            			//System.out.println(ISDReader.matchLatitudeAndLongitudeForFile(eachFile, latitude, longitude));
            			if(ISDReader.matchLatitudeAndLongitudeForFile(eachFile, latitude, longitude) < minDistance){
            				//System.out.println(eachFile.getName());
            				
            				
            				weatherInfo = findInformationForTimeInFiles(eachFile, reservedSamplesFromOutputLocations.get(i).dateTime);

            				// see last weatherInfo
            				if(weatherInfo == null){
            					weatherInfo = temp1;

            				}
            				

            				// store current weatherInfo 
            				if(weatherInfo != null){
            					temp1 = weatherInfo;	
            				}
            				
            				if(weatherInfo.precipitation != ""){
            					break;
            				}
            					
            				
            			}    
            			            			
            		}
            	j++;
            }
            
            if(weatherInfo != null){
				temp2 = weatherInfo;
			}else{
				weatherInfo = temp2;
			}
            
			
    /*        System.out.println("!!!!!!!!!!!!!!dateAndTime!!!!!!!!!!!!!!");
            System.out.println(reservedSamplesFromOutputLocations.get(i).dateTime);
            
            System.out.println("==============weather info=============");
			System.out.println(weatherInfo);
			
			if(weatherInfo.precipitation != ""){
				System.out.println("*************precipitation*************");
				System.out.println(weatherInfo.precipitation);
			}
    */
            
            //System.out.println(weatherInfo);
            
            //find info in this fileEntry
            //WeatherInfo weatherInfo = findInformationForTimeInFiles(fileEntry, reservedSamplesFromOutputLocations.get(i).dateTime);
            weatherInfoList.add(weatherInfo);
            //System.out.println(weatherInfo.toString());
    	}

        //System.out.println("File name: " + toReturn.getName()+ "; Distance: " + minDistance);
        return weatherInfoList;
    	
    }
    
    
    
    /**
     * get file names of files with the same country as inputted locations
     * @param isdHistoryFile documention
     * @param contryCode	country of given location
     * @return
     */
    public ArrayList<String> getSubWeatherDatabase(File isdHistoryFile, String contryCode){
    	ArrayList<String> fileNamesWithSameCountry = new ArrayList<String>();
    	
    	BufferedReader br = null;

        try {
        
            br = new BufferedReader(new FileReader(isdHistoryFile));
            String sCurrentLine = br.readLine();
            
            while(sCurrentLine != null){
            	
                String[] line = sCurrentLine.split(",");
                String code = line[3].replace("\"", "");
                // find all filenames with the same country Code as input
                
                if(code.equals(contryCode)){
                	
                	ArrayList<String> temp = new ArrayList<String>();
                	String code1 = line[0].replace("\"", "");
                	String code2 = line[1].replace("\"", "");
                	String fileName = code1+"-"+code2+"-"+"2015";
                	//temp.add(fileName);
                	
                	//latitude
                	//temp.add(line[6]);
                	
                	//longitude
                	//temp.add(line[7]);
                	
                	//fileNamesWithSameCountry.add(temp);
                	fileNamesWithSameCountry.add(fileName);
                }
                sCurrentLine = br.readLine();
                
                
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
        
    	return fileNamesWithSameCountry;
    }
  
    
    public WeatherInfo findInformationForTimeInFiles(File file, String date) throws ParseException{	
    	
        // change type of date
    	Date dateInDateType = new Date();
		DateFormat formatter;
		formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateInDateType = formatter.parse(date);
		//System.out.println(dateInDateType);
		//System.out.println("THIS IS THE GIVEN DATE INFO!");
		
		
		//Step 1: Get data from NOAA's actual data file (like wind speed, temperature etc.)
    	WeatherInfo toReturn = ISDReader.findInformationForTime(file, dateInDateType);

        //Step 2: Get more data from NOAA's parser results (like precipitation etc.)
        //TODO: Get results from NOAA's results
  
        return toReturn;

    }
    
    
    
    public ArrayList<String> getEachWeatherTag(WeatherInfo weatherInfo){
    	ArrayList<String> eachWeatherTag = new ArrayList<String>();
    	String windSpeed = "";
    	
    	try{
    		if(weatherInfo != null){
            	
            	// decide wind (3 levels)
            	if(weatherInfo.windSpeed <= 2.3){
            		windSpeed = "calm";
            	}else if(weatherInfo.windSpeed <= 6.7){
            		windSpeed = "lightAir";
            	}else if(weatherInfo.windSpeed <= 13.5){
            		windSpeed = "lightBreeze";
            	}else{
            		windSpeed = "gentleBreeze";
            	}
            
            	eachWeatherTag.add(windSpeed);
            	eachWeatherTag.add(weatherInfo.precipitation);
        		
        	}else{
            	eachWeatherTag.add("");
        	}

    	} catch (Exception ex){
    		ex.printStackTrace();
    	}
    	
    	return eachWeatherTag;
    }


    // To calculate distance between two coordinates
  	public double GetDistance(double lat1, double lng1, double lat2, double lng2){
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
