/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.me.sampleEndpoint;

import java.io.IOException;
import java.util.ArrayList;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import mainApp.App;
/**
 *
 * @author jiahuili
 */

/** 
 * @ServerEndpoint gives the relative name for the end point
 * This will be accessed via ws://localhost:8080/EchoChamber/echo
 * Where "localhost" is the address of the host,
 * "sampleEndpoint" is the name of the package
 * and "sampleEndpoint" is the address to access this class from the server
 */


@ServerEndpoint("/Endpoint")
public class sampleEndpoint {
   
    /**
     * @OnOpen allows us to intercept the creation of a new session.
     * The session class allows us to send data to the user.
     * In the method onOpen, we'll let the user know that the handshake was 
     * successful.
     */
    
    @OnOpen
    public void onOpen(Session session){
        System.out.println(session.getId() + " has opened a connection"); 
        try {
            session.getBasicRemote().sendText("Connection Established");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
 
    /**
     * When a user sends a message to the server, this method will intercept the message
     * and allow us to react to it. For now the message is read as a String.
     */
    @OnMessage
    public void onMessage(String message, Session session){
        System.out.println("Message from " + session.getId() + ": " + message);
        try {
            String output = "wer";
            String[] temp = message.split(";");
            //System.out.println("temp[0] is: " + temp[0]);
            
            ArrayList<String> output2 = App.Main(temp);     
                    
            /*int input = message;
            int a = input%10;
            int b = input/10;
            int output1 = add.addConstant(a);
            int output2 = add.multiply(b);
            System.out.println("output1 is " + output1);
            System.out.println("output2 is " + output2);
            String output = String.valueOf(output1)+" & "+String.valueOf(output2);
            */
            
            //System.out.println("output is: " + output);

            session.getBasicRemote().sendText(output2.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }       
     
    

    /**
     * The user closes the connection.
     * 
     * Note: you can't send messages to the client from this method
     */
    @OnClose
    public void onClose(Session session){
        System.out.println("Session " +session.getId()+" has ended");
    }
    
    
    
 
    
}