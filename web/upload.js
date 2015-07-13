var webSocket;
var messages = document.getElementById("messages");


function openSocket(){
    // Ensures only one connection is open at a time
    
    if(webSocket !== undefined && webSocket.readyState !== WebSocket.CLOSED){
       writeResponse("WebSocket is already opened.");
        return;
    }
    // Create a new instance of the websocket
    webSocket = new WebSocket("ws://localhost:8080/Rewind/Endpoint");
    
      

    /**
     * Binds functions to the listeners for the websocket.
     */
    webSocket.onopen = function(event){
        // For reasons I can't determine, onopen gets called twice
        // and the first time event.data is undefined.
        // Leave a comment if you know the answer.
        if(event.data === undefined)
            return;

        writeResponse(event.data);
    };

    webSocket.onmessage = function(event){
        var data = event.data;
        //data += data;
        writeResponse(data);
    };

    webSocket.onclose = function(event){
        writeResponse("Connection closed");
    };
}

/**
 * Sends the value of the text input to the server
 */
function send(){
    var text = document.getElementById("messageinput").value;
    webSocket.send(text);
}

function closeSocket(){
    webSocket.close();
}

function writeResponse(text){
    messages.innerHTML += "<br/>" + text;
}




var input = document.getElementById("input");

function loadFile() {
    var input, file, fr;

    if (typeof window.FileReader !== 'function') {
      alert("The file API isn't supported on this browser yet.");
      return;
    }

    input = document.getElementById('fileinput');
    if (!input) {
      alert("Um, couldn't find the fileinput element.");
    }
    else if (!input.files) {
      alert("This browser doesn't seem to support the `files` property of file inputs.");
    }
    else if (!input.files[0]) {
      alert("Please select a file before clicking 'Load'");
    }
    else {
        // conect to websocket
        connectChatServer();

        
        
        file = input.files[0];
        fr = new FileReader();
        fr.onload = receivedText;
        fr.readAsText(file);
        
    }

    
    
    function connectChatServer() {
            webSocket = new WebSocket("ws://localhost:8080/sampleWebsocket/sampleEndpoint");

            //ws.binaryType = "arraybuffer";
            /*ws.onopen = function() {
                alert("Connected.")
            };

            ws.onmessage = function(evt) {
                alert("~~~~~"+evt.data);
            };*/

            webSocket.onopen = function(event){

                if(event.data === undefined)
                    return;

                writeResponse(event.data);
            };

            webSocket.onmessage = function(event){
                var data = event.data;
                //data += data;
                writeResponse(data);
            };
            
            webSocket.onclose = function() {
                alert("Connection is closed...");
            };
            webSocket.onerror = function(event) {
                alert(event.msg);
            }

        }
    
    
    function onMessage(evt) {
    console.log("received: " + evt.data);
    writeResponse(evt.data);
    }


    
    function receivedText(e) {
      
      lines = e.target.result;
      var line = JSON.parse(lines); 
      var content = line.locations;
      var i = 0;
      var batch = [];
      for(index = 0; index < content.length; index++) {
          
          text = content[index];
          for(key in text){
              if(key == "accuracy"){
                  // push to server in batches(every 500 points)
                    if(i > 499){
                        
                        inversedBatch = "";
                        for(index1 = batch.length-1; index1 >= 0; index1--){
                          // invserse the list
                          inversedBatch += batch[index1];
                          inversedBatch += ";";
                        }
                        
                          //send in batch
                        webSocket.send(inversedBatch);
                        webSocket.onmessage = function(evt){ 
                            writeResponse(evt.data);
                            // TODO: Front-End processing
                        };
                          
                        
                        i = 0;
                        batch = [];
                        
                    }
                  //console.log(i);
                    if(text.accuracy > 10){
                      // time
                      var timestamp = text.timestampMs;
                      var d = new Date(+timestamp);
                      var formattedDate = d.getFullYear()+ "-" + (d.getMonth() + 1) + "-" + d.getDate();
                      var hours = (d.getHours() < 10) ? "0" + d.getHours() : d.getHours();
                      var minutes = (d.getMinutes() < 10) ? "0" + d.getMinutes() : d.getMinutes();
                      var seconds = (d.getSeconds() < 10) ? "0" + d.getSeconds() : d.getSeconds();
                      var formattedTime = hours + ":" + minutes + ":" + seconds;
                      
                      formattedDate = formattedDate + " " + formattedTime;
                      //document.write(text.timestampMs);
                      //console.log(formattedDate);
                      
                      // latitude
                      var latitude = parseInt(text.latitudeE7,10)/10000000;
                      //console.log(latitude);
                      
                      // longitude
                      var longitude = parseInt(text.longitudeE7,10)/10000000;
                      //console.log(longitude);
                      
                      // put lat, lng, time into an array
                      var line = latitude+","+longitude+","+formattedDate;
                      //console.log(line);
                      batch[i] = line;
                      //var dict = []; // create an empty array
                      i++;
                      
                    } 
                  
                  
              }
              
          }
          
          
      
          
      }
     
      
      webSocket.send(inversedBatch);
      webSocket.onmessage = function(evt){ 
          writeResponse(evt.data);
          // TODO: Front-End processing
      };
      
    }
  }


