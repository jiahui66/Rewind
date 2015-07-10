/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainApp;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.ejb.Stateless;


import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.ejb.Stateless;


//import Audio.AudioManager;
//import Audio.AudioSnap;
//import Location.Location;
//import Location.LocationManager;
import Location2.Location2;
import Location2.LocationManager2;
import Surrounding.SurroundingDatabase;
import Weather.WeatherDatabase;
import Weather.WeatherInfo;
//import Surrounding.SurroundingDatabase;
//import Weather.WeatherDatabase;
//import Weather.WeatherInfo;
//import Weather.ishJava;

import java.io.File;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author jiahuili
 */
public class App {

    public static ArrayList<String> Main(String[] inputFile){
        // temp output
        ArrayList<String> temp = new ArrayList<String>();
        
        try {
            
            
            //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
            //I. Location Manager
            //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=           
            LocationManager2 locationManager2 = new LocationManager2();
            
            ArrayList<Location2> inputLocation = locationManager2.generateInputLocatiossFromFile(inputFile);
            //System.out.println(inputLocation.toString());
            
            
            
            ArrayList<Location2> outputLocation = locationManager2.modePrediction(inputLocation);   
            //System.out.println(outputLocation.toString());
            
            
            

            
            //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
            //II. Select and Filter Sample coordinates
            //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

            SurroundingDatabase surroundingDatabase = new SurroundingDatabase("/Users/jiahuili/Desktop/Rewind/surroungdingsExtracted");

            // select samples
            ArrayList<Location2> samplesFromOutputLocations = locationManager2.getSamplesFromOutputLocations(outputLocation);  
            
            // get reserved samples' indices
            ArrayList<Integer> reservedSampleIndicesFromOutputLocations  = locationManager2.getReservedSamplesIndex(samplesFromOutputLocations, surroundingDatabase);
             
            // get reserved samples (Object Location2)
            ArrayList<Location2> reservedSamplesFromOutputLocations  = locationManager2.getReservedSamples(samplesFromOutputLocations, reservedSampleIndicesFromOutputLocations);

            // get final reserved intervals 
            ArrayList<Location2>  ReservedIntervals = locationManager2.getReservedIntervals(reservedSampleIndicesFromOutputLocations, samplesFromOutputLocations, surroundingDatabase, outputLocation);
            
            
            
            
            
            
            

            //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
            //III. Find the weather file with information about the weather
            //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

             
             WeatherDatabase weatherDatabase = new WeatherDatabase("/Users/jiahuili/Desktop/Rewind/weather");
            System.out.println("start getting weather database!");	

            //to store all weatherInfo
            ArrayList<WeatherInfo> weatherInfoList = new ArrayList<WeatherInfo>();

            //to store all weatherTags
            ArrayList<ArrayList<String>> weatherTag = new ArrayList<ArrayList<String>>();

            weatherInfoList = weatherDatabase.getWeatherInfo(ReservedIntervals);


            /**
             * if weather info for this interval is null, set temperature the same, ignore wind
             * temperature -- season???
             * temperature the same as previous one, set wind ????
             * when can a person feel wind with up to which speed???
             */
            for(WeatherInfo w : weatherInfoList){
                // add WeatherTag [season, windSpeed]
                ArrayList<String> eachWeatherTag = weatherDatabase.getEachWeatherTag(w);
                weatherTag.add(eachWeatherTag);
                //System.out.println("Found weatherInfo tag: \n" +eachWeatherTag);		
            }
   
        
    
    
            
            
            //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
            //IV. Put Weather Tag and Mode together
            //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

            for(int i=0; i<weatherTag.size(); i++){
                String outputInfo = "";
                
                String tempTime = ReservedIntervals.get(i).dateTime.toString();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                java.util.Date milliseconds =  dateFormat.parse(tempTime);

                //outputInfo = ReservedIntervals.get(i).latitude+","+ReservedIntervals.get(i).longitude+","+String.valueOf(milliseconds.getTime())+","+ReservedIntervals.get(i).mode+","+weatherTag.get(i).toString();

                outputInfo = ReservedIntervals.get(i).toString()+","+weatherTag.get(i).toString();
                temp.add(outputInfo);
                System.out.println(outputInfo);
            }

               
        
        } catch (Exception ex){
            System.out.println("Exception: "+ex);
        }
        return temp;
    }
     


}
