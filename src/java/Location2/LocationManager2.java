/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Location2;

import Surrounding.SurroundingDatabase;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

//import Surrounding.SurroundingDatabase;

/**
 *
 * @author jiahuili
 */
public class LocationManager2 {
    
	
    public ArrayList<Location2> inputLocation;
    public ArrayList<Location2> outputLocation;

    /**
     * 
     * @param fileName name of input file
     * @return input data in ArrayList [Latitude, Longitude, dateTime]
     */
    public ArrayList<Location2> generateInputLocatiossFromFile(String[] inputFile){
        ArrayList<Location2> inputLocation = new ArrayList<Location2>();

        try {
                for(int i=0; i<inputFile.length; i++){
                    if (inputFile[i] != null) {
                        Location2 location2 = new Location2();
                        String[] eachLine = inputFile[i].split(",");
                        eachLine[eachLine.length-1] = eachLine[eachLine.length-1].replace("\r\n","");

                        location2.latitude = eachLine[0];
                        location2.longitude = eachLine[1];
                        location2.dateTime = eachLine[2];
                        
                        //System.out.println("l2 is: "+location2.toString());
                        inputLocation.add(location2);
                }
                }

        } catch(Exception ex){
            System.out.println("Exception: "+ex);
        }


        return inputLocation;
    }
    
   
    /**
     * 
     * @param inputLocation
     * @return
     */
public ArrayList<Location2> modePrediction(ArrayList<Location2> inputLocation){
    ArrayList<Location2> outputLocation = new ArrayList<Location2>();

    // the final big map
    //HashMap<String, String> modeMap = new HashMap<String, String>();
    // store pre step "still" mode
    //ArrayList<ArrayList<String>> stillMode = new ArrayList<ArrayList<String>>();

    /****************************************************************************************************
     *  a finalmodeMap to store mode prediction
     *  ['start time'--'end time'](string) ---- mode
     ****************************************************************************************************/

    // To store [time --> coordinates]
    HashMap<String, ArrayList<Double>> coorMap = new HashMap<String, ArrayList<Double>>();

    // To get time period, distance, speed, and acceleration
    int k = 0;
    double preSpeed = 0;
    double prePeriod = 0;
    double acceleration = 0;

    //To store preMode (3 kinds) in a big List        
    ArrayList<ArrayList<String>> preModeList = new ArrayList<ArrayList<String>>();	
    String pretime = "";
    String afttime = "";
    double period = 0;
    String coor00 = "";
    String coor01 = "";
    String coor10 = "";
    String coor11 = "";		
    double distance = 0;
    double speed = 0;

    int i = 0;
    int j = i+1;
    while(i<inputLocation.size()-1){		    			
        k += 1;

        // store coors in a map
        ArrayList<Double> coor = new ArrayList<Double>();
        coor.add(Double.parseDouble(inputLocation.get(j).latitude));
        coor.add(Double.parseDouble(inputLocation.get(j).longitude));
        coorMap.put(inputLocation.get(j).dateTime, coor);

        ArrayList<String> preMode = new ArrayList<String>();
        pretime = inputLocation.get(i).dateTime;
        afttime = inputLocation.get(j).dateTime;
        period = getPeriod(pretime, afttime);
        coor00 = inputLocation.get(i).latitude;
        coor01 = inputLocation.get(i).longitude;
        coor10 = inputLocation.get(j).latitude;
        coor11 = inputLocation.get(j).longitude;	
        
        distance = GetDistance(Double.parseDouble(coor00), Double.parseDouble(coor01), Double.parseDouble(coor10), Double.parseDouble(coor11));
        speed = 0;
        if (period != 0){
            speed = distance/period;       //speed   
        }

        if (k > 1){
            if (period != 0){
                acceleration = Math.abs((speed-preSpeed)/period);    
            }
        }
        preSpeed = speed;
        prePeriod = period;



        // if speed>240 or acceleration>4.5, ignore this node 
        // step 1: merge too large intervals
        // else add [afttime	  period   distance	  speed	  acceleration]
        if(speed>240 || acceleration>4.5){
            j++;
        }else{
            preMode.add(String.valueOf(afttime));
            preMode.add(String.valueOf(period));
            preMode.add(String.valueOf(distance));
            preMode.add(String.valueOf(speed));
            preMode.add(String.valueOf(acceleration));  
            j++;
            i=j-1;

            // 3 prediction: still -- walk -- others
            String preModeString = preMode(distance, acceleration, speed, period);
            preMode.add(preModeString);

            // add preMode to preModeList
            preModeList.add(preMode);
        } 
        
    }



    /*****************************************************************************************************
     *	if consecutive still intervals is more 300 seconds, add "stillMerged"
     *	put "stillMerged" intervals and each other interval into preSeg (ArrayList<ArrayList<String>>)
     * ***************************************************************************************************/  
    ArrayList<ArrayList<String>> preSeg = new ArrayList<ArrayList<String>>();
    double sum = 0;
    int begin = 0;
    int end = 0;
    i = 0;
    while(i<preModeList.size()){
        if(preModeList.get(i).get(5) == "still"){
            sum = 0;
            begin = i;
            while(i<preModeList.size() && preModeList.get(i).get(5) == "still"){
                sum +=  Double.parseDouble(preModeList.get(i).get(1));
                i++;
            }
            end = i-1;
            if (sum >= 300){

                ArrayList<String> modeTemp = new ArrayList<String>();
                modeTemp.add(preModeList.get(begin).get(0)+"--"+preModeList.get(end).get(0));
                modeTemp.add("stillMerged");
                preSeg.add(modeTemp);	
            }else{
                for(int t = begin; t<=end; t++){
                    ArrayList<String> modeTemp = new ArrayList<String>();
                    modeTemp.add(preModeList.get(t).get(0)+"--"+preModeList.get(t).get(0));
                    for(j=1; j<preModeList.get(t).size(); j++){
                            modeTemp.add(preModeList.get(t).get(j));
                    }
                    preSeg.add(modeTemp);

                }
            }
            i--;
        }else{
            ArrayList<String> modeTemp = new ArrayList<String>();
            modeTemp.add(preModeList.get(i).get(0)+"--"+preModeList.get(i).get(0));
            for(j=1; j<preModeList.get(i).size(); j++){
                    modeTemp.add(preModeList.get(i).get(j));
            }
            preSeg.add(modeTemp);
            
        }
        i++;
    }

    /**
     * **************************************************************************************************
     * basic mode prediction
     * **************************************************************************************************
     * 
     * 
     * step 1: merge too small and too large intervals:
     * 		   too small: 
     * 					if (a. the mode still;  b. speed is too small < 0.3m/s) set its mode the same as its forward interval
     * 		   			if there is only one interval and it satisfies a or b,  merge it into "modeMap" and its mode as "still" ;
     * 					
     * 		   too large:
     * 					if the speed is too large (larger than) set its mode the same as its forward interval
     * 		   set these intervals' mode the same as its forward one!!! no merge!!!
     * 
     * step 2: merge consecutive walk intervals
     * 		   if the time of the big interval is more than 5 min take it as a walk interval for certain
     * 		   else ????????
     * 
     * step 3: […..still…..] ----trip----[…..still…..]---- trip ----[…..still…..]…………
     * 		   for each trip:
     * 				[…..walk …..] ----segment----[…..walk …..]---- segment ----[…..walk...]………
     * 		  now each segment represents a single mode other than "still"
     * 				decide its mode with length, expectation of speed, acceleration (3 and get the average)
     * 
     */

    // merge too small intervals
    for (i=0; i<preSeg.size(); i++){
        // size == 2 ---> time+"stillMerged"
        if(preSeg.get(i).size() != 2){
            if(preSeg.get(i).get(5)=="still"){

                // if this interval's mode is "still", set the mode the same as forward mode, add "merged"
                // if there is no forward node, set mode as "still"
                // if forward node is not "stillMerged", set this mode the same as forward mode
                // if forward node is "stillMerged", set this mode as "still"
                preSeg.get(i).add("merged");
                if(i == 0){
                        preSeg.get(i).set(5, "still");
                }else if(preSeg.get(i-1).size() != 2){
                        preSeg.get(i).set(5, preSeg.get(i-1).get(5));
                }else{
                        preSeg.get(i).set(5, "still");
                }
            }
        }

    }	


    // step 2
    // merge consecutive walk intervals -- if period >= 5min, -- merge them together 
    // if merged, mode is "walkMerged", [time, period, distance, speed, mode]
    // put all merged and unmerged into mergedSeg (ArrayList<ArrayList<String>>)

    ArrayList<String> ignored = new ArrayList<String>();

    ArrayList<ArrayList<String>> mergedSeg = new ArrayList<ArrayList<String>>();
    double sumPeriod = 0;
    double sumDistance = 0;
    String beginTime = "";
    String endTime = "";
    begin = 0;
    end = 0;
    i=0;
    while (i<preSeg.size()){
        if(preSeg.get(i).get(preSeg.get(i).size()-1) == "merged"){
            String time[] = preSeg.get(i).get(0).split("--");
            ignored.add(time[0]);
            //System.out.println(time[0]);
        }
        if(preSeg.get(i).size() != 2){
            if(preSeg.get(i).get(5) == "walk"){
                begin = i;
                sumPeriod = 0;
                sumDistance = 0;
                while(i < preSeg.size() && preSeg.get(i).size()!=2 && preSeg.get(i).get(5) == "walk"){
                    if(preSeg.get(i).get(preSeg.get(i).size()-1) != "merged"){
                        sumPeriod += Double.parseDouble(preSeg.get(i).get(1));
                        sumDistance += Double.parseDouble(preSeg.get(i).get(2));	
                    }else{
                        String time[] = preSeg.get(i).get(0).split("--");
                        ignored.add(time[0]);
                        //System.out.println(time[0]);
                    }
                    i++;       				
                }
                end = i-1;
                i--;
                if(sumPeriod >= 300){
                    ArrayList<String> temp2 = new ArrayList<String>();
                    beginTime = preSeg.get(begin).get(0);
                    String[] s1 = beginTime.split("--");
                    endTime = preSeg.get(end).get(0);
                    String[] s2 = endTime.split("--");  
                    // [time, period, distance, speed, mode]
                    temp2.add(s1[0]+"--"+s2[1]);
                    temp2.add(String.valueOf(sumPeriod));
                    temp2.add(String.valueOf(sumDistance));
                    temp2.add(String.valueOf(sumDistance/sumPeriod));
                    temp2.add("walkMerged");
                    mergedSeg.add(temp2);
                }else{
                    for (k=begin; k<=end; k++){
                        mergedSeg.add(preSeg.get(k));

                    }
                }
            }else{
                mergedSeg.add(preSeg.get(i));
            }


        }else{
            mergedSeg.add(preSeg.get(i));
            
        }
        i++;
    }
    
    

    // step 3 & and mode (predict mode)
    // jump "merge interval"
    // get mean speed, 3 largest acceleration and get their mean？
    // put final mode predictions of merged and unmerged intervals into modePrediction (ArrayList<ArrayList<String>>)
    ArrayList<ArrayList<String>> modePrediction = new ArrayList<ArrayList<String>>();

    double sumSpeed = 0;
    double meanSpeed = 0;
    ArrayList<Double> largestAcc = new ArrayList<Double>();
    String mode = "";
    i = 0;
    while(i<mergedSeg.size()){
        if(mergedSeg.get(i).get(mergedSeg.get(i).size()-1) != "stillMerged" && (mergedSeg.get(i).get(mergedSeg.get(i).size()-1) != "walkMerged")){
            begin = i;
            largestAcc.clear();
            sumSpeed = 0;
            while(i<mergedSeg.size() && (mergedSeg.get(i).get(mergedSeg.get(i).size()-1) != "stillMerged" && (mergedSeg.get(i).get(mergedSeg.get(i).size()-1)) != "walkMerged")){
                if(mergedSeg.get(i).get(mergedSeg.get(i).size()-1) != "merged"){

                    sumSpeed += Double.parseDouble(mergedSeg.get(i).get(3));
                    largestAcc.add(Double.parseDouble(mergedSeg.get(i).get(4)));
                }
                i++;
            }
            end = i-1;
            i--;

            // predict mode for "others":
            ArrayList<String> temp2 = new ArrayList<String>();
            meanSpeed = sumSpeed/(end-begin+1);
            Collections.sort(largestAcc);
            Collections.reverse(largestAcc);
            mode = transModePrediction(acceleration, meanSpeed);
            beginTime = mergedSeg.get(begin).get(0);
            String[] s1 = beginTime.split("--");
            endTime = mergedSeg.get(end).get(0);
            String[] s2 = endTime.split("--");  
            temp2.add(s1[0]+"--"+s2[1]);

            temp2.add(mode);

            modePrediction.add(temp2);


        }else{
            ArrayList<String> temp = new ArrayList<String>();
            temp.add(mergedSeg.get(i).get(0));
            if (mergedSeg.get(i).get(mergedSeg.get(i).size()-1) == "stillMerged"){
                    temp.add("still");
            }else{
                    temp.add("walk");
            }
            
            modePrediction.add(temp);
            
        }
        i++;

    }
    
 


    // assign every interval a single mode
    // preModeList stores all intervals

    String time0 = "";
    j=0;
    double diff = 0;
    //int t = 0;
    for(i=0; i<preModeList.size(); i++){
            time0 = preModeList.get(i).get(0);
            if (j<modePrediction.size()){
                    String[] s = modePrediction.get(j).get(0).split("--");
                    diff = getPeriod(time0, s[1]);
            }      	
            if(diff < 0){
                    j++;
            }

            // ignore coordinates with abnormal speed according to its mode (Eg. in "vehicle", speed is 177m/s -- 2015-03-28 17:21:43)
            // only care about "speed" now
            double tempSpeed = 0;

            if (modePrediction.get(j).get(1) == "still"){
                    if (Double.parseDouble(preModeList.get(i).get(3)) > 1){

                            int start = 0;
                            if(i >= 1){
                                    start = i-1;
                            }
                            tempSpeed  = GetDistance(coorMap.get(preModeList.get(start).get(0)).get(0), coorMap.get(preModeList.get(start).get(0)).get(1), coorMap.get(preModeList.get(i).get(0)).get(0), coorMap.get(preModeList.get(i).get(0)).get(1)) / getPeriod(preModeList.get(start).get(0),  preModeList.get(i).get(0)) ;
                            while(i<preModeList.size()-1 && tempSpeed  > 1){	        				
                                    i++;
                                    tempSpeed  = GetDistance(coorMap.get(preModeList.get(start).get(0)).get(0), coorMap.get(preModeList.get(start).get(0)).get(1), coorMap.get(preModeList.get(i).get(0)).get(0), coorMap.get(preModeList.get(i).get(0)).get(1)) / getPeriod(preModeList.get(start).get(0),  preModeList.get(i).get(0)) ;
                            }
                    }
            }else if (modePrediction.get(j).get(1) == "vehicle"){
                    if (Double.parseDouble(preModeList.get(i).get(3)) > 80){

                            int start = 0;
                            if(i >= 1){
                                    start = i-1;
                            }
                            tempSpeed  = GetDistance(coorMap.get(preModeList.get(start).get(0)).get(0), coorMap.get(preModeList.get(start).get(0)).get(1), coorMap.get(preModeList.get(i).get(0)).get(0), coorMap.get(preModeList.get(i).get(0)).get(1)) / getPeriod(preModeList.get(start).get(0),  preModeList.get(i).get(0)) ;
                            while(i<preModeList.size()-1 && tempSpeed  > 80){	        				
                                    i++;
                                    tempSpeed  = GetDistance(coorMap.get(preModeList.get(start).get(0)).get(0), coorMap.get(preModeList.get(start).get(0)).get(1), coorMap.get(preModeList.get(i).get(0)).get(0), coorMap.get(preModeList.get(i).get(0)).get(1)) / getPeriod(preModeList.get(start).get(0),  preModeList.get(i).get(0)) ;
                            }
                    }
            }else if (modePrediction.get(j).get(1) == "run"){
                    if (Double.parseDouble(preModeList.get(i).get(3)) > 10){
                            int start = 0;
                            if(i >= 1){
                                    start = i-1;
                            }
                            tempSpeed  = GetDistance(coorMap.get(preModeList.get(start).get(0)).get(0), coorMap.get(preModeList.get(start).get(0)).get(1), coorMap.get(preModeList.get(i).get(0)).get(0), coorMap.get(preModeList.get(i).get(0)).get(1)) / getPeriod(preModeList.get(start).get(0),  preModeList.get(i).get(0)) ;	        			
                            while(i<preModeList.size()-1 && tempSpeed  > 10){
                                    i++;
                                    tempSpeed  = GetDistance(coorMap.get(preModeList.get(start).get(0)).get(0), coorMap.get(preModeList.get(start).get(0)).get(1), coorMap.get(preModeList.get(i).get(0)).get(0), coorMap.get(preModeList.get(i).get(0)).get(1)) / getPeriod(preModeList.get(start).get(0),  preModeList.get(i).get(0)) ;
                            }
                    }
            }else if (modePrediction.get(j).get(1) == "walk"){
                    if (Double.parseDouble(preModeList.get(i).get(3)) > 5){
                            int start = 0;
                            if(i >= 1){
                                    start = i-1;
                            }
                            tempSpeed  = GetDistance(coorMap.get(preModeList.get(start).get(0)).get(0), coorMap.get(preModeList.get(start).get(0)).get(1), coorMap.get(preModeList.get(i).get(0)).get(0), coorMap.get(preModeList.get(i).get(0)).get(1)) / getPeriod(preModeList.get(start).get(0),  preModeList.get(i).get(0)) ;	        			
                            while(i<preModeList.size()-1 && tempSpeed  > 5){
                                    i++;
                                    tempSpeed  = GetDistance(coorMap.get(preModeList.get(start).get(0)).get(0), coorMap.get(preModeList.get(start).get(0)).get(1), coorMap.get(preModeList.get(i).get(0)).get(0), coorMap.get(preModeList.get(i).get(0)).get(1)) / getPeriod(preModeList.get(start).get(0),  preModeList.get(i).get(0)) ;
                            }
                    }
            }else{
                    if(Double.parseDouble(preModeList.get(i).get(3)) > 270){
                            int start = 0;
                            if(i >= 1){
                                    start = i-1;
                            }
                            tempSpeed  = GetDistance(coorMap.get(preModeList.get(start).get(0)).get(0), coorMap.get(preModeList.get(start).get(0)).get(1), coorMap.get(preModeList.get(i).get(0)).get(0), coorMap.get(preModeList.get(i).get(0)).get(1)) / getPeriod(preModeList.get(start).get(0),  preModeList.get(i).get(0)) ;
                            while(i<preModeList.size()-1 && tempSpeed  > 270){
                                    i++;
                                    tempSpeed  = GetDistance(coorMap.get(preModeList.get(start).get(0)).get(0), coorMap.get(preModeList.get(start).get(0)).get(1), coorMap.get(preModeList.get(i).get(0)).get(0), coorMap.get(preModeList.get(i).get(0)).get(1)) / getPeriod(preModeList.get(start).get(0),  preModeList.get(i).get(0)) ;
                            }
                    }
            }


            //t++;

            // [time, lat, log, mode] in 	Location2  eachOutputLocation 
            Location2 eachOutputLocation = new Location2();
            eachOutputLocation.latitude = String.valueOf(coorMap.get(preModeList.get(i).get(0)).get(0));
            eachOutputLocation.longitude = String.valueOf(coorMap.get(preModeList.get(i).get(0)).get(1));
            eachOutputLocation.dateTime = preModeList.get(i).get(0);
            eachOutputLocation.mode = modePrediction.get(j).get(1);

            outputLocation.add(eachOutputLocation);

    }
    return  outputLocation;
    }




/***
 * get samples from output locations
 * still: pick one point
 * walk & run: every 50 meter pick a point, search places in range of 100 meters
 * vehicle: every 250 meters pick a point, search places in range of 250 meters
 * plane: every 1000 meters pick a point, (picture from the satellite??? only start and end of flight)
 * @param outputLocation output locations
 * @return samplesFromOutputLocations samples from output locations
 */

    public ArrayList<Location2> getSamplesFromOutputLocations(ArrayList<Location2> outputLocation){
                            int i=0;
                            int size = outputLocation.size();
                    ArrayList<Location2> samplesFromOutputLocations = new ArrayList<Location2>();
                    double distance = 0;
                    String mode = "";
                    while(i<size-2){
                            if(outputLocation.get(i).mode.equals("still")){
                                    Location2 eachSample = new Location2();
                                    eachSample.latitude = outputLocation.get(i).latitude;
                                    eachSample.longitude = outputLocation.get(i).longitude;
                                    eachSample.dateTime = outputLocation.get(i).dateTime;
                                    eachSample.mode = outputLocation.get(i).mode;

                                    samplesFromOutputLocations.add(eachSample);
                                    while(i<size-2 && outputLocation.get(i).mode.equals("still")){
                                            i++;
                                    }
                                    i--;
                            }else{
                                    if(outputLocation.get(i).mode.equals("walk")){
                                            distance = 50;
                                            mode = "walk";			        		

                                    }else if(outputLocation.get(i).mode.equals("run")){
                                            distance = 50;
                                            mode = "run";		        		

                                    }else if (outputLocation.get(i).mode.equals("vehicle")){
                                            distance = 250;
                                            mode = "vehicle";			        		

                                    }else if (outputLocation.get(i).mode.equals("plane")){
                                            distance = 1000;
                                            mode = "plane";

                                    }

                                    i = extractSamples(i, samplesFromOutputLocations, outputLocation, mode, distance);
                                    i--;

                            }

                            i++;

                    }

                    //add the last node
                    Location2 eachSample = new Location2();

                            eachSample.latitude = outputLocation.get(i+1).latitude;
                            eachSample.longitude = outputLocation.get(i+1).longitude;
                            eachSample.dateTime = outputLocation.get(i+1).dateTime;
                            eachSample.mode = outputLocation.get(i+1).mode;

                    return samplesFromOutputLocations;
    }



    /**
     * 
     * @param i index of outputLocation
     * @param samplesOfSameMode output ArrayList<Location2> samplesOfSameMode
     * @param outputLocation original outputLocations
     * @param mode mode of a single interval
     * @param distance distance between Node(i) & Node(i+1)
     * @return index of outputLocation (next item in an iteration over outputLocation)
     */
    public int extractSamples(int i, ArrayList<Location2> samplesOfSameMode, ArrayList<Location2> outputLocation, String mode, Double distance){
            int size = outputLocation.size();
    double dist = 0;
    Location2 eachSample = new Location2();

    // add the first point of a new mode
            eachSample = new Location2();
            eachSample.latitude = outputLocation.get(i).latitude;
            eachSample.longitude = outputLocation.get(i).longitude;
            eachSample.dateTime = outputLocation.get(i).dateTime;
            eachSample.mode = outputLocation.get(i).mode;

/*			System.out.print("111111111111");
                    System.out.print(" ");
                    System.out.print(dist);
                    System.out.print(", ");		*/
                    //System.out.print(dist);
/*       	System.out.print("{");
            System.out.print("'lat': '");
            System.out.print(eachSample.latitude);
            System.out.print("', 'lng': '");
            System.out.print(eachSample.longitude);
            System.out.print("', 'mode': '");
            System.out.print(eachSample.mode);
            System.out.print("', 'description': 'Point No.at ");
            System.out.print(eachSample.dateTime);
            System.out.println("' },");		*/

            samplesOfSameMode.add(eachSample);

            dist = 0;
            while(i<size-2 && outputLocation.get(i).mode.equals(mode)){

                    if(dist < distance){
                            // distance between Pi & Pi+1
                    dist += GetDistance(Double.parseDouble(outputLocation.get(i).latitude), Double.parseDouble(outputLocation.get(i).longitude), Double.parseDouble(outputLocation.get(i+1).latitude), Double.parseDouble(outputLocation.get(i+1).longitude));

            }else{

                    if(outputLocation.get(i+1).mode.equals(mode)){
                            eachSample = new Location2();
                            eachSample.latitude = outputLocation.get(i+1).latitude;
                            eachSample.longitude = outputLocation.get(i+1).longitude;
                            eachSample.dateTime = outputLocation.get(i+1).dateTime;
                            eachSample.mode = outputLocation.get(i+1).mode;

                            samplesOfSameMode.add(eachSample);

    /*			System.out.print("************");
                            System.out.print(" ");
                            System.out.print(dist);
                            System.out.print(", ");		*/
                            //System.out.print(dist);
   /* 			System.out.print("{");
                    System.out.print("'lat': '");
                    System.out.print(eachSample.latitude);
                    System.out.print("', 'lng': '");
                    System.out.print(eachSample.longitude);
                    System.out.print("', 'mode': '");
                    System.out.print(eachSample.mode);
                    System.out.print("', 'description': 'Point No.at ");
                    System.out.print(eachSample.dateTime);
                    System.out.println("' },");			*/


                    }
                    dist = 0;	

            }
                    i++;
            }
            return i;

    }


    /**
     * get reserved intervals based on surrounding feature and weather condition
     * @param samplesFromOutputLocations samples to filter
     * @param surroundingDatabase surrounding Database
     * @param outputLocation ouputted location (object Location2)
     * @return all reserved intervals in order of time (object Location2)
     * 1. get reseved samples' indices
     * 2. get start and end Of intervals to be rewound
     * 3. get all intervals to be rewound from outputlocations
     * 
     */
    public ArrayList<Location2> getReservedIntervals(ArrayList<Integer> samplesIndexReserved, ArrayList<Location2>samplesFromOutputLocations, SurroundingDatabase surroundingDatabase, ArrayList<Location2> outputLocation){

            // get start and end time of intervals
            System.out.println("start getting startsAndEndsOfIntervals!!!");
        ArrayList <String> startsAndEndsOfIntervals = this.getStartAndEndOfSubIntervels(samplesIndexReserved, samplesFromOutputLocations);	    
        System.out.println("startsAndEndsOfIntervals got!!!");

        System.out.println("start getting reservedIntervals!!!");
        ArrayList<Location2> reservedIntervals = getAllLocationsExtrated(startsAndEndsOfIntervals, outputLocation);
        System.out.println("reservedIntervals got!!!");

            return reservedIntervals;
    }       




    /**
     * get reserved Samples from OutputLocation
     * @param  samplesFromOutputLocations samplesFromOutputLocations
     * @param reservedSampleIndicesFromOutputLocation reservedSampleIndicesFromOutputLocation
     * @return reserved Samples from OutputLocation
     */
    public ArrayList<Location2> getReservedSamples(ArrayList<Location2> samplesFromOutputLocations, ArrayList<Integer> reservedSampleIndicesFromOutputLocation){
            System.out.println("start getting reservedSamples!!!");
            ArrayList<Location2> reservedSamplesFromOutputLocations  = new ArrayList<Location2>();
            for(Integer i: reservedSampleIndicesFromOutputLocation){
                    reservedSamplesFromOutputLocations.add(samplesFromOutputLocations.get((int)i));
            }
            System.out.println("reservedSamples got!!!");
            return reservedSamplesFromOutputLocations;
    }



    /**
     * if a single sample has one or more than one specific surrounding object, reserve this sample
     * pick one single sample to test every two samples (^*^*^*^ ----> pick ^ to test, two adjacent * are bounds of effective intervals)
     * @param samplesFromOutputLocations samples from ouput Locations
     * @param surroundingDatabase surroundingDatabase
     * @return index of reserved samples in samplesFromOutputLocations
     */
    public ArrayList<Integer> getReservedSamplesIndex(ArrayList<Location2> samplesFromOutputLocations, SurroundingDatabase surroundingDatabase){
            ArrayList<Integer> samplesIndexReserved = new ArrayList<Integer>();
            System.out.println("start getting reservedIntervals!!!");
            samplesIndexReserved = findMatchedLocationsWithLocation(samplesFromOutputLocations, surroundingDatabase);

            /*
        for(int i=0; i<samplesFromOutputLocations.size(); i+=2){
            int matchedLocations = surroundingDatabase.findMatchedLocationsWithLocation(i, Double.parseDouble(samplesFromOutputLocations.get(i).latitude), Double.parseDouble(samplesFromOutputLocations.get(i).longitude));
            //System.out.println("Found " + matchedLocations +  " Locations near inputted location: " + L.dateTime );		

            if(matchedLocations > 0){
                    samplesIndexReserved.add(i);
            }
        }		*/

            System.out.println("reservedIntervals got!!!");
            return samplesIndexReserved;
    }       



     /**
 * Uses FileReader to go through every file in the specified folder to find specified nearby locations of inputted location
 * @param samplesFromOutputLocations 
 * @param surroundingDatabase
 * @return Index of Reserved samples
 */
public ArrayList<Integer> findMatchedLocationsWithLocation(ArrayList<Location2> samplesFromOutputLocations, SurroundingDatabase surroundingDatabase){
       ArrayList<Integer> samplesIndexReserved = new ArrayList<Integer>();
   //int matchedLocations = 0;

   int i=0;

   // find country code (file name) of this location
   String countryCode = "US";

   for (final File fileEntry : surroundingDatabase.folder.listFiles()) {
       if(i != 0){		//first file is .DS_Store (with no location data)
               if(fileEntry.getName().equals(countryCode +".txt")){
                       //System.out.println("Looking into file: " + fileEntry.getName());
                       samplesIndexReserved = surroundingDatabase.SurReader.numberOfMatchedLocationsForFile(samplesIndexReserved, fileEntry, samplesFromOutputLocations);

               }

       }
       i++;       	   
   }	

   //System.out.println("found matched Locations of inputted location: " + matchedLocations);
   return samplesIndexReserved;
}       



    /**
     * get start location2 object and end location2 object of all sub intervals
     * ^*^*^*^... ----> pick ^ to test, two adjacent * are bounds of effective intervals
     * @param samplesIndexReserved index of reserved samples in samplesFromOutputLocations
     * @param samplesFromOutputLocations samplesFromOutputLocations
     * @return start and end (location2 object) of all sub intervals
     */
    public ArrayList<String> getStartAndEndOfSubIntervels(ArrayList<Integer>samplesIndexReserved, ArrayList<Location2>samplesFromOutputLocations){
            ArrayList<String> intervalsExtrated = new ArrayList<String>();

        for(int i: samplesIndexReserved){
            // add start dateTime
            if(i == 0){
                    intervalsExtrated.add(samplesFromOutputLocations.get(i).dateTime);    		
            }else{
                    intervalsExtrated.add(samplesFromOutputLocations.get(i-1).dateTime);
            }
            // add end dateTime
            if(i+1 < samplesFromOutputLocations.size()){
                            intervalsExtrated.add(samplesFromOutputLocations.get(i+1).dateTime);
                    }else{
                            intervalsExtrated.add(samplesFromOutputLocations.get(i).dateTime);
                    }		
        }


            return intervalsExtrated;
    }



    /**
     * extract all Location2 objects from outputLocation
     * @param intervalExtrated start and end (location2 object) of all sub intervals
     * @param outputLocation all location2 objects in outputLocation
     * @return all Location2 objects extrated
     */
    public ArrayList<Location2> getAllLocationsExtrated(ArrayList<String>ReservedIntervals, ArrayList<Location2> outputLocation){
            ArrayList <Location2> Extratedpoints = new ArrayList <Location2>();

        int j=0;
        String start = "";
        String end = "";
            for(Location2 l2 : outputLocation){ 
                    if(j<ReservedIntervals.size()){
                            start = ReservedIntervals.get(j);
                            end = ReservedIntervals.get(j+1);
                            if(LocationManager2.getPeriod(start,l2.dateTime)>=0){
                                    if(LocationManager2.getPeriod(l2.dateTime, end)>=0){

                    /*    		System.out.print("{");
                                    System.out.print("'lat': '");
                                    System.out.print(l2.latitude);
                                    System.out.print("', 'lng': '");
                                    System.out.print(l2.longitude);
                                    System.out.print("', 'mode': '");
                                    System.out.print(l2.mode);
                                    System.out.print("', 'description': 'Point No.at ");
                                    System.out.print(l2.dateTime);
                                    System.out.println("' },");			*/	


                                            Extratedpoints.add(l2);
                                    }else{
                                            j += 2;
                                    }
                            }
                    }

            }

            return Extratedpoints;
    }



    /**
     * get length of an interval in seconds
     * @param pretime DateTime of start of interval
     * @param afttime DateTime of end of interval
     * @return length length of this interval in seconds
     */
public static double getPeriod(String pretime, String afttime){
    double length = 0;
    double diff = 0;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1 = null;
    Date d2 = null;
    try {
        d1 = format.parse(pretime);
        d2 = format.parse(afttime);
        //ms
        diff = d2.getTime() - d1.getTime();
        //System.out.println(diff / 1000);	//seconds

    } catch (Exception e) {
        e.printStackTrace();
    }
    length = diff/1000;      //time in seconds
    return length;
}


    /**
     * predict mode of an interval
     * @param acceleration acceleration during an interval
     * @param speed speed during an interval
     * @return mode mode of an interval
     */
    public static String transModePrediction(double acceleration, double speed){
            String mode = "";	                
    if (acceleration >= 3){
        mode = "plane";
    }else if (acceleration >= 0.6){ 
            if(speed > 30){
                    mode = "plane";
            }else{
                    mode = "vehicle";
            }	
    }else{
            if (speed > 40){
            mode = "plane";
        }else if (speed >= 3.7){
            mode = "vehicle";
        }else if (speed >= 2.5){
            mode = "run";
        }else{
            mode = "walk";
        }
    }
    return mode;
    }



    /**
     * pre-processing:  3 predictions: walk -- still -- others 
     * set loose thresholds for "still" and "walk"
     * see methods from the following code
     * @param distance distance during an interval
     * @param acceleration acceleration during an interval
     * @param speed speed during an interval
     * @param period length of an interval
     * @return preMode
     */
    public static String preMode(double distance, double acceleration, double speed, double period){
            String mode = "";	                
    if (distance == 0 || (speed <= 0.2 && distance <6)){
            mode = "still";
    }else if(speed<2 && acceleration<=0.4){
        mode = "walk";
    }else{
            mode = "others";
    }
    return mode;
    }



    /**
     * convert from degree to radian 
     * @param d
     * @return radian radian of a degree
     */
    public static float rad(double d){
            return (float) (d * Math.PI / 180.0);
    }

    // To calculate distance between two coordinates
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
