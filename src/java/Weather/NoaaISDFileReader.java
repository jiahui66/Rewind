package Weather;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author jiahuili
 */

public class NoaaISDFileReader {


    File fileName;
    float matchScale = 0.5f;//0.05f;

    /**
     *
     * Given a file, a latitude and longitude, returns the distance between the file location and the given location
     *
     * @param fileName input file
     * @param latitude latitude
     * @param longitude longitude
     * @return boolean representing if the file has the information on the given location
     */
    public double matchLatitudeAndLongitudeForFile(File fileName, double latitude, double longitude){
        BufferedReader br = null;

        try {

            br = new BufferedReader(new FileReader(fileName));
            
            String sCurrentLine = br.readLine();
           

            // If character 28 of file is false, it does NOT have precipitation information so we need to ignore it
            if (sCurrentLine.charAt(27) !='4'|| sCurrentLine.charAt(27) =='7') {
                return Float.MAX_VALUE;
            }

            return GetDistance(getLatitudeFromLine(sCurrentLine), getLongitudeFromLine(sCurrentLine), latitude,
                    longitude);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return Float.MAX_VALUE;
    }

    /**
     *
     * Helper function to go through the files in the folder and checking stuff for debugging purposes
     *
     * @param fileName name of the file
     */
    public void readFile(File fileName){

        BufferedReader br = null;

        try {

            String sCurrentLine;

            br = new BufferedReader(new FileReader(fileName));
            System.out.println("Filename: " + fileName);

            while ((sCurrentLine = br.readLine()) != null) {
                //System.out.println(sCurrentLine);
                System.out.println(getLatitudeFromLine(sCurrentLine) + ", " + getLongitudeFromLine(sCurrentLine));
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


    /**
     *
     * Given a file, find weather information associated with a particular date and time(within the same hour).
     *
     * @param file inputFile
     * @param date date and time you want to know the weather information for
     * @return weatherInfo, representing the weather information for the location and the date in question
     */
    public WeatherInfo findInformationForTime(File file, Date date){

        WeatherInfo weatherInfo = null;

        BufferedReader br;
        try {
        br = new BufferedReader(new FileReader(file));

            //StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            int i = 0;
            while (line != null) {
            	
                if(dateAndHourMatch(getDateFromLine(line), date)){
               
            		weatherInfo = extractWeatherInformation(line);
                }
                i++;
                line = br.readLine();
            }
            //String everything = sb.toString();
            br.close();
        } catch(Exception e) {
            System.out.println("Unexpected problem while reading the file: " + e);
        }
        
        return weatherInfo;
    }

    /**
     *
     * Get latitude from line
     *
     * @param line input line
     * @return latitude
     */
    public double getLatitudeFromLine(String line){
        String latitude ="";

        if(line.length()>34) {
            latitude = line.substring(28, 34);
        }

        return convertLatLongToInteger(latitude);
    }

    /**
     *
     * Get longitude from a line
     *
     * @param line Input line
     * @return longitude
     */
    public double getLongitudeFromLine(String line){

        String longitude ="";

        if(line.length()>41) {
            longitude = line.substring(34, 41);
        }

        return convertLatLongToInteger(longitude);

    }

    /**
     *
     * Get date from a line
     *
     * @param line input line
     * @return date
     */
    public Date getDateFromLine(String line){

        Date result = null;

        if(line.length()>27) {
            String stringDate = line.substring(15, 27);
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmm", Locale.ENGLISH);
            try {
                result =  df.parse(stringDate);
            } catch (ParseException e) {
                System.out.println("Date could not be converted!" + e.getMessage());
            }
        }

        return result;
    }

    /**
     *
     * Get wind speed from a line
     *
     * @param line input line
     * @return wind speed
     */
    public float getWindSpeedFromLine(String line){

        float result = 0f;

        if(line.length()>69) {
            String stringSpeed = line.substring(65, 69);
            try {
                result = Float.parseFloat(stringSpeed);
            } catch (Exception e) {
                System.out.println("Wind speed could not be parsed!" + e.getMessage());
            }
        }

        return result/10f;
    }

    /**
     *
     * Get temperature from a line
     *
     * @param line input line
     * @return temperature
     */
    public float getTemperatureFromLine(String line){

        float result = 0f;

        if(line.length()>69) {
            String stringTemperature = line.substring(87, 92);
            try {
                result = Float.parseFloat(stringTemperature);
            } catch (Exception e) {
                System.out.println("Wind speed could not be parsed!" + e.getMessage());
            }
        }

        return result/10f;
    }

    /**
     * rain: AA1-4, !!!AC1, AD1		snow: AJ, AL1-4	AO1-4
     * @param line
     * @return precipitation
     */
    public String getPrecipitationFromLine(String line){
    	String precipitation = "";
    	
    	// control data:60, mandatory data: 45 
        if(line.length()>104) {
        	
            try {
            	
            	String code = line.substring(108, 111);
            	
                if(code.equals("AA1") || code.equals("AA2") || code.equals("AA3") || code.equals("AA4")){
                	
                	String condition = line.substring(111, 119);
                	
                	if(Integer.parseInt(condition.substring(0, 2))<3){
                		//System.out.println(condition.substring(2, 6));
                		if(Integer.parseInt(condition.substring(2, 6))>40){
                			precipitation = "heavy rain";
                		}else if(Integer.parseInt(condition.substring(2, 6))>20){
                			precipitation = "medium rain";
                		}else if(Integer.parseInt(condition.substring(2, 6))>0){
                			precipitation = "little rain";
                		}
                		
                	}
                }/*else if(flag == false && code.equals("AM1")){
                	String condition = line.substring(111, 119);
                	System.out.println(Integer.parseInt(condition.substring(0, 1)));
                	if(Integer.parseInt(condition.substring(0, 2))>0){
                		flag = true;
                		precipitation = "little snow";
                	}
                }*/
                
                
            } catch (Exception e) {
                System.out.println("Precepitation data could not be parsed!" + e.getMessage());
            }
        }
        
        
        
    	return precipitation;
    }
    
    
    /**
     *
     * Takes in the string form of the latitude/longitude and converts to integer (accounts for the sign and the scaling factor)
     *
     * @param string representing the latitude/longitude as a string
     * @return the latitude/longitude as an integer
     */
    private float convertLatLongToInteger(String string){

        //System.out.println("string: "+ string);
        try {
            if (string.charAt(0) == '+') {
                return Float.parseFloat(string.substring(1)) / 1000;
            } else {
                return Float.parseFloat(string.substring(1)) / 1000 * -1;
            }
        }catch(Exception e){
            return 0f;
        }

    }

    /**
     *
     * Compares the read latitude/longitude with the desired. Returns the distance between the points.
     * @param readLatitude read latitude
     * @param readLongitude read longitude
     * @param actualLatitude desired latitude
     * @param actualLongitude desired longitude
     * @return distance between the two points
     */
    public float calculateDistance(float readLatitude, float readLongitude, float actualLatitude, float actualLongitude){

        float latDistance = Math.abs(readLatitude - actualLatitude);
        float longDistance = Math.abs(readLongitude - actualLongitude);
        float squaredDistance = (float) Math.pow(latDistance,2) + (float) Math.pow(longDistance, 2);

            return (float) Math.sqrt(squaredDistance);

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
    
    
    /**
     *
     * Function to match dates to pick the closest hour available in the NOAA database
     * (i.e minutes and seconds) do not need to be checked
     *
     * @param date1 first date
     * @param date2 second date
     * @return boolean value indicating if the two dates are have the same day of the year and
     * hour of the day
     */
    public boolean dateAndHourMatch(Date date1, Date date2){

        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        boolean sameDayAndHour = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.HOUR_OF_DAY) == cal2.get(Calendar.HOUR_OF_DAY);
        //System.out.println(sameDayAndHour);
/*        System.out.println(cal1.get(Calendar.YEAR));
        System.out.println(cal2.get(Calendar.YEAR));
        System.out.println(cal1.get(Calendar.DAY_OF_YEAR));
        System.out.println(cal2.get(Calendar.DAY_OF_YEAR));
        System.out.println(cal1.get(Calendar.HOUR_OF_DAY));
        System.out.println(cal2.get(Calendar.HOUR_OF_DAY));

        if(sameDayAndHour == true){
        	System.out.println(date1);
            System.out.println(date2);
        }
*/       
        return sameDayAndHour;
    }

    public WeatherInfo extractWeatherInformation(String line){
    	WeatherInfo weatherInfo = new WeatherInfo();
    		
        weatherInfo.windSpeed = getWindSpeedFromLine(line);
        weatherInfo.temperature = getTemperatureFromLine(line);
        weatherInfo.precipitation= getPrecipitationFromLine(line);
        //System.out.println(weatherInfo.precipitation);
        return weatherInfo;
    	
    }
    
    
    
    
    
    
}

