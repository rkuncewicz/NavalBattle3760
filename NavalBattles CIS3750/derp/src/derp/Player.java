/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//package server;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;

/**
 *
 * @author don
 */
public class Player extends Thread {
    
    Socket playerSocket;
    
    //OutputStream outStream = null;
    //InputStream inStream = null;
    BufferedReader inStream = null;
    BufferedWriter outStream = null;
    //ByteArrayOutputStream clientinput = new ByteArrayOutputStream();

    private boolean playerRegistered = false;
    private boolean readyStatus = false;
    private int x = 0;
    private int y = 0;
    private int curHeading = 0;

    long timestamp = 0;
    
    private float speed = 0;
    private int shipType = -1;
    private int shipID = -1;
    private float health = 0;
    
    static ClientWriter clientwriter;
    
    public Player(Socket player,int nextID){
        playerSocket = player;
        try {
            //inStream = playerSocket.getInputStream();
            inStream = new BufferedReader(new InputStreamReader(playerSocket.getInputStream()));
            //outStream = playerSocket.getOutputStream();
            outStream = new BufferedWriter(new OutputStreamWriter(playerSocket.getOutputStream()));
        } catch(Exception e) {
            Server.displayError("Unable to get streams from client socket: "+e.toString());
            System.out.println("WHOA!!!");
        }
        shipID = nextID;
        System.out.println("New player created:"+shipID);
    }
    
    
    synchronized void send(String msg){
        try{
            msg = msg + ";\n";
            outStream.write(msg, 0, msg.length());
            outStream.flush();

        }catch(Exception e){
            Server.displayError("Player "+shipID+" Send Error: "+e.toString());
            System.out.println("Player"+shipID+".send() error: "+e);
            disconnectPlayer();
            return;
        }
    }
    
    public int getShipID() {
        return shipID;
    }

    public int getShipType() {
        return shipType;
    }

    public int getCurHeading() {
        return curHeading;
    }

    public void setCurHeading(int curHeading) {
        this.curHeading = curHeading;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
    
    @Override
    public void run(){
        System.out.println("Player running");

        int len;
        int buffer = 0;		/*buffer Counter Use to Slow down Server*/
        //byte [] buf = null;
        char[] buff = new char[1000];
        while (true){
            try{
                /*Read the InputStream and converts the Data to String*/
                //buf = new byte[1000];
                buff[0] = '\0';
                len = 0;
                try{
                    //len = inStream.read(buf);
                    len = inStream.read(buff, 0, buff.length - 1);
                    buff[len] = '\0';
                    System.out.println("Player"+shipID+".run(): buff.length="+buff.length);
                    System.out.println("Player"+shipID+".run(): len="+len);
                }catch (Exception e){
                    System.out.println("Player"+shipID+".run() read error: "+e);
                    disconnectPlayer();
                    return;
                }

                if(len == -1){ //END OF STREAM
                    System.out.println("LEN = "+len+"!!!");
                    System.out.println("playerSocket.isConnected() = "+playerSocket.isConnected());
                    System.out.println("playerSocket.isClosed() = "+playerSocket.isClosed());
                    System.out.println("playerSocket.isInputShutdown() = "+playerSocket.isInputShutdown());
                    System.out.println("playerSocket.isOutputShutdown() = "+playerSocket.isOutputShutdown());
                    disconnectPlayer();
                    return;
                }

                //clientinput.reset();
                //System.out.println("made it here");
                //clientinput.write(buf,0,len);
                //System.out.println("made it here");
                
                //String streammsg = new String(clientinput.toByteArray());
                String streammsg = String.valueOf(buff, 0, len);
                System.out.println("Player"+shipID+".run(): streammsg.length()="+streammsg.length());
                
                String[] messages =  streammsg.split(";", -1);
                
                String message = null;

                System.out.println("Player"+shipID+".run(): Received: "+streammsg+">");

                for(int i = 0; i < messages.length;i++){
                    boolean gameStarted = ConnectionListener.getGameStarted();

                    message = messages[i];
                    System.out.println("Player"+shipID+".run(): Handling: "+message);

                    if(message.length() == 0){
                        System.out.println("Player"+shipID+".run(): finished handling full message: "+streammsg);
                        continue;
                    }

                    if(message.startsWith("disconnect")){
                        disconnectPlayer();
                        return;
                    } else if(!gameStarted){
                        if(message.startsWith("register")){
                            String[] splitmsg = message.split(":", -1);
                            shipType = Integer.parseInt(splitmsg[1]);

                            if(shipType < 0 || shipType > 2){
                                send("gameover");
                                Server.decrementConnected();
                                ConnectionListener.playerList.remove(this);
                                outStream.close();
                                inStream.close();
                                playerSocket.close();
                                return;
                            }

                            health = GameData.getMaxHealth(shipType);

                            send("registered:"+shipID);

                            if(!playerRegistered) {
                                String[] island = GameData.getIslands();
                                
                                this.sleep(200);
                                for(int j = 0;j < island.length;j++){
                                    send("shore:"+island[j]);
                                    this.sleep(200);
                                }
                                send("shore:x");
                                
                                System.out.println("Player"+shipID+": Finished sending shore points");
                                playerRegistered = true;
                            }
                        }else if(message.startsWith("ready")){
                            if(playerRegistered){
                                readyStatus = true;
                                Server.incrementReady();
                                
                                int numConnected = Server.getNumConnected();
                                if(numConnected >= ConnectionListener.getMinPlayers() && numConnected == Server.getNumReady()){
                                    // Game Ready to start
                                    GameData.generateStartPositions(ConnectionListener.playerList.size());
                                    clientwriter = new ClientWriter();
                                    clientwriter.start();
                                }
                            }
                        } else {
                            System.out.println("Player"+shipID+": UNKNOWN MESSAGE: "+message);
                        }
                    } else if(gameStarted){
                        if(message.startsWith("speed:")){
                            
                            String[] splitmsg = message.split(":", -1);
                            float precentmax = Float.parseFloat(splitmsg[1]);
                            
                            float shipVF = GameData.getMaxVelocity(shipType);
                            
                            
                            float vIF = 0.0f;
                            
                            int tempcurwind = GameData.getCurWindDir();
                            
                            if(tempcurwind > 180){
                                tempcurwind = tempcurwind - 360;
                            }

                            boolean headingNegative = false;
                            boolean windNegative = false;

                            if(curHeading < 0){
                                headingNegative = true;
                            }
                            if(tempcurwind < 0){
                                windNegative = true;
                            }

                            int absHeading = Math.abs(curHeading);
                            int absWind = Math.abs(tempcurwind);

                            int angle = 0;
                            angle = Math.abs(curHeading - tempcurwind);

                            if( absHeading > 90 && absWind > 90){
                                angle = (180 - absHeading) + (180 - absWind);
                            } else if(angle > 180 && angle < 360){ //then one angle negative, one positive
                                if(headingNegative){
                                    if(curHeading < -90 && tempcurwind < 90){
                                        angle = absHeading + absWind;
                                    } else if(curHeading > -90 && tempcurwind > 90){
                                        angle = (180 + curHeading) + (180 - tempcurwind);
                                    }
                                } else if(windNegative){
                                    if(tempcurwind < -90 && curHeading < 90){
                                        angle = absHeading + absWind;
                                    } else if(tempcurwind > -90 && curHeading > 90){
                                        angle = (180 + tempcurwind) + (180 - curHeading);
                                    }
                                }
                                                           
                            
                            }
                            vIF = GameData.getVelocityFactor(angle);
                            
                            vIF = 1;

                            speed = precentmax*shipVF*vIF;

                            ClientWriter.queueMessage(-1, "shipState:"+this.getShipID()+":"+this.getX()+":"+this.getY()+":"
                                                    +this.getSpeed()+":"+this.getCurHeading()+":"+this.getHealth());  //DC20111123//

                        }else if(message.startsWith("setHeading:")){
                            
                            long now = System.currentTimeMillis(); //DC20111123//
                            if(now - timestamp > 100){  //DC20111123//
                                timestamp = now;  //DC20111123//
                                String[] splitmsg = message.split(":", -1);
                                int newsetHeading = Integer.parseInt(splitmsg[1]);
                                
                                //if(newsetHeading>= -180 && newsetHeading <= 180){
                                if( (shipType == 0 && newsetHeading >= -3 && newsetHeading <= 3)
                                  || (shipType == 1 && newsetHeading >= -2 && newsetHeading <= 2)
                                  || (shipType == 2 && newsetHeading >= -1 && newsetHeading <= 1)) {  //DC20111123//
                                    //futHeading = newsetHeading;
                                    curHeading = curHeading + newsetHeading;  //DC20111123//
                                    if( curHeading > 180 ){ 
                                        curHeading = -180 + (curHeading - 180);
                                    } else if( curHeading < -180 ){
                                        curHeading = 180 + (curHeading + 180);
                                    }
                                    //futHeading = curHeading; //TODO: Remove futHeading?  //DC20111123//
                                    ClientWriter.queueMessage(-1, "shipState:"+this.getShipID()+":"+this.getX()+":"+this.getY()+":"
                                                            +this.getSpeed()+":"+this.getCurHeading()+":"+this.getHealth());  //DC20111123//
                                }else{
                                    System.out.println("Player"+shipID+": Uknown Heading Number");
                                }
                            } else {
                                System.out.println("Player"+shipID+": set heading REJECTED! Last message was only "+(now - timestamp)+" milliseconds ago");
                            }  //DC20111123//
                            
                        }else if(message.startsWith("fire:")){
                            
                            System.out.println("Player"+shipID+": recieved fire");
                        }else{
                            
                            System.out.println("Player"+shipID+": UNKNOWN MESSAGE: "+message);
                        }
                    }
                }
            } catch (Exception e){
                System.out.println("Player"+shipID+".run() general error: "+e);
            }
        }
    }

    public void disconnectPlayer(){
        boolean gameStarted = ConnectionListener.getGameStarted();
        System.out.println("Player"+shipID+".disconnectPlayer(): Player disconnecting...");
        System.out.println("Player"+shipID+".disconnectPlayer(): gameStarted="+gameStarted
                          +" numConnected="+ConnectionListener.playerList.size()+" numReady="+Server.getNumReady());

        //if the game has started, notify players this player lost (because they disconnected)
        if(gameStarted) {
            //If more than one player connected, tell them this person "died"
            if(ConnectionListener.playerList.size() > 1) {
                this.setSpeed(0.0f);
                this.setHealth(0.0f);
                ClientWriter.queueMessage(-1, "shipState:"+this.getShipID()+":"+this.getX()+":"+this.getY()+":"
                          +this.getSpeed()+":"+this.getCurHeading()+":"+this.getHealth());
                send("shipState:"+this.getShipID()+":"+this.getX()+":"+this.getY()+":"
                          +this.getSpeed()+":"+this.getCurHeading()+":"+this.getHealth());
                send("gameover");
            } else { //If this is the last player, end the game
                System.out.println("Player"+shipID+".disconnectPlayer(): All players disconnected! Ending Game!");
                ConnectionListener.playerList.get(0).send("shipState:"+this.getShipID()+":"+this.getX()+":"+this.getY()+":"
                          +this.getSpeed()+":"+this.getCurHeading()+":"+this.getHealth());
                ConnectionListener.playerList.get(0).send("gameover");
                Server.serverShutDown();
            }
        } else { //if game not started, check if remaining players are all ready
            int numConnected = Server.getNumConnected();
            if(numConnected >= ConnectionListener.getMinPlayers() && numConnected == Server.getNumReady()) {
                // Game Ready to start
                GameData.generateStartPositions(ConnectionListener.playerList.size());
                clientwriter = new ClientWriter();
                clientwriter.start();
            }
        }

        Server.decrementConnected();
        if(readyStatus){
            Server.decrementReady();
            readyStatus = false; //not really needed...
        }  //DC20111123//

        ConnectionListener.playerList.remove(this);

        try {
            outStream.close();
        } catch(Exception e){ System.out.println("Player"+shipID+": unable to close outStream: "+e.toString()); }
        try {
            inStream.close();
        } catch(Exception e){ System.out.println("Player"+shipID+"unable to close inStream: "+e.toString()); }
        try {
            playerSocket.close();
        } catch(Exception e){ System.out.println("Player"+shipID+":unable to close playerSocket: "+e.toString()); }
    }
    
}
