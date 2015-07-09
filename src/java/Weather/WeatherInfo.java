/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Weather;

/**
 *
 * @author jiahuili
 */
public class WeatherInfo {
    
    float windSpeed;
    float temperature;
    String precipitation;


    @Override
    public String toString(){
        return "Wind speed: " + windSpeed + " meter/second\nTemperature: " + temperature + "C";
    }

}
