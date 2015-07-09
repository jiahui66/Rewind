/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Location2;

/**
 *
 * @author jiahuili
 */
public class Location2 {

    public String latitude;
    public String longitude;
    public String dateTime;
    public String mode;
    public String weather;

    public String toString(){
            String string = "";
            string += this.latitude;
            string += ",";
            string += this.longitude;
            string += ",";
            string += this.dateTime;
            string += ",";
            string += this.mode;
            return string;
    }
}
