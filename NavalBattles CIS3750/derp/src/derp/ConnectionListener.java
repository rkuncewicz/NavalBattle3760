/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Polygon;

/**
 *
 * @author don
 */
public class ConnectionListener extends Thread{
    
    private int port = 0;
    private static int maxPlayer = 0;
    private static int minPlayer = 0;
    private static boolean gameStarted = false;
    public static ArrayList<Player> playerList = new ArrayList<Player>();
    private static boolean listen = true;
    
    private static int nextID = 0;
    
    Socket clientSocket = null;
    ServerSocket serverSocket = null;

	private static Timer gameTimer = new Timer();
    private static GameUpdater gameUpdater = null;
    
    public ConnectionListener(int port, int maxplayers, int minplayers){
        this.port = port;
        ConnectionListener.minPlayer = minplayers;
        ConnectionListener.maxPlayer = maxplayers;
        gameUpdater = new GameUpdater();
    }

    public static void setGameStarted(boolean gameStarted) {
        ConnectionListener.gameStarted = gameStarted;
    }
    public static boolean getGameStarted(){
        return gameStarted;
    }
    
    @Override
    public void run(){
        if(listen){
            clientListen();
        }
    }

    public static void startGameHandler(){
        gameTimer.scheduleAtFixedRate(gameUpdater, 1000, 1000);
    }
    
    public void clientListen(){

        /*Sets up the ServerSocket*/
        try{
            serverSocket = new ServerSocket(port);
        }
        catch(IOException e){
            Server.displayError("Server Socket Error: "+e.toString());
            System.out.println("IO "+e);
        }    
        
        /*Check for Client Connection*/
        while (true) {
                try{
                    clientSocket = serverSocket.accept();
                    
                    
                    if(playerList.size() >= maxPlayer || gameStarted){
                        System.out.println("Rejecting person!");
                        Server.displayLog("Reject connection from: "+clientSocket.getInetAddress().toString());    
                        clientSocket.close();
                        continue;
                    }
                    Server.displayLog("Recieved connection from: "+clientSocket.getInetAddress().toString());
                    Player player = new Player(clientSocket,nextID);
                    playerList.add(player);
                    player.start();
                    nextID++;

                    Server.incrementConnected();
                } catch (IOException e) {
                    if(gameStarted){
                        System.out.println("GAME STARTING");
                        return;
                    }
                    Server.displayError("Client Socket Error: "+e.toString());
                    System.out.println("IOaccept "+e);
                }
                
                System.out.println("Waiting for next connection...");
        }        
    }

    public class GameUpdater extends TimerTask {
        private Polygon polygons[] = null;
        private int sendUpdate = 0;

        public GameUpdater(){
            System.out.println("ConnectionListener.run(): started");
            polygons = GameData.getPolygons();
        }

        @Override
        public void run(){
            Player player = null;
            int curHeading = 0;
            //int futHeading = 0;
            int shipType = 0;
            float turningSpeed = 0.0f;
            float speed = 0.0f;
            int pX=0,pY=0,w=0,h=0;

            updatePlayer:for(int i=0; i<playerList.size(); i++){
                try{
                    player = playerList.get(i);

                    //If player is defeated, don't update them
                    if(player.getHealth() <= 0){
                        continue;
                    }

                    shipType = player.getShipType();
                    curHeading = player.getCurHeading();
                    //futHeading = player.getFutHeading();
                    speed = player.getSpeed();
                    pX = player.getX();
                    pY = player.getY();
                    w = (int)GameData.getWidth(player.getShipType());
                    h = (int)GameData.getHeight(player.getShipType());
                } catch(Exception e){
                    System.out.println("ConnectionListener.GameUpdater.run(): Error: "+e.toString());
                    i--;
                    continue;
                }

                /*
                turningSpeed = GameData.getTurningSpeed(shipType);

                if(futHeading - curHeading < 0){
                    if(curHeading - turningSpeed < 0){
                        curHeading = 359 - (int)(Math.floor(curHeading - turningSpeed));
                    } else {
                        curHeading = (int)(Math.floor(curHeading - turningSpeed));
                    }
                    System.out.println("0-ConnectionListener(GameUpdater): CurHeading = "+curHeading);

                    if(curHeading < futHeading){
                        curHeading = futHeading;
                    }
                } else if(futHeading - curHeading > 0){
                    if(curHeading + turningSpeed > 359){
                        curHeading = (int)(Math.floor(curHeading + turningSpeed)) - 359;
                    } else {
                        curHeading = (int)(Math.floor(curHeading + turningSpeed));
                    }
                    System.out.println("1-ConnectionListener(GameUpdater): CurHeading = "+curHeading);

                    if(curHeading > futHeading){
                        curHeading = futHeading;
                    }
                }
                */ //DC20111123//


                //Update player's (X,Y) coordinate
                /*pX = pX + (int)(speed * Math.sin(curHeading));
                if(curHeading >= -90 && curHeading <= 90){
                    pY = pY - (int)(speed * Math.cos(curHeading));
                } else {
                    pY = pY + (int)(speed * Math.cos(curHeading));
                }
                */

                if(curHeading > 0 && curHeading < 90){
                    pX = pX + (int)(speed * Math.sin(Math.toRadians(curHeading)));
                    pY = pY - (int)(speed * Math.cos(Math.toRadians(curHeading)));
                } else if(curHeading == 90){
                    pX = pX + (int)(speed);
                } else if(curHeading > 90 && curHeading < 180){
                    pX = pX + (int)(speed * Math.sin(Math.toRadians(curHeading-90)));
                    pY = pY + (int)(speed * Math.cos(Math.toRadians(curHeading-90)));
                } else if(curHeading == 180 || curHeading == -180){
                    pY = pY + (int)(speed);
                } else if(curHeading < 0 && curHeading > -90){
                    pX = pX - (int)(speed * Math.sin(Math.toRadians(-curHeading)));
                    pY = pY - (int)(speed * Math.cos(Math.toRadians(-curHeading)));
                } else if(curHeading == 90){
                    pX = pX - (int)(speed);
                }  else if(curHeading < -90 && curHeading > -180){
                    pX = pX - (int)(speed * Math.sin(Math.toRadians(-curHeading-90)));
                    pY = pY + (int)(speed * Math.cos(Math.toRadians(-curHeading-90)));
                } else if(curHeading == 0){
                    pY = pY - (int)(speed);
                }
                
                System.out.println("ConnectionListener.GameUpdater.run(): Player"+i+" pos: pX="+pX+" pY="+pY+" speed="+speed+" heading="+curHeading);
                player.setX(pX);
                player.setY(pY);

                //Check player collision with edges of map
                if(pX < 0 || pX > GameData.getMapWidth() || pY < 0 || pY > GameData.getMapHeight()){
                    gameOverPlayer(player);
                    continue;
                }

                //Check player collision with island
                /*for(int j=0; j<polygons.length; j++){
                    if(polygons[i].intersects(pX,pY,w,h) == true){
                        gameOverPlayer(player);
                        break;
                    }
                }*/ //DC20111123//
                //Check that the boat centered at (X,Y) doesn't collide with any islands:
                for(int j=0; j<polygons.length; j++){
                    //If the ship intersects an island, need to regenerate (X,Y)
                    if(polygons[j].intersects(pX-w, pY-h, w*2, h*2)){ //DC20111123//
                        System.out.println("ConnectionListener.GameUpdater.run(): PLAYER"+i+" CRASHED INTO ISLAND "+j);
                        gameOverPlayer(player);
                        continue updatePlayer;
                    }
                }

                //Check player collision with other players?
                Polygon playerShip = new Polygon();
                playerShip.addPoint(pX-w, pY-h);
                playerShip.addPoint(pX+w, pY-h);
                playerShip.addPoint(pX-w, pY+h);
                playerShip.addPoint(pX+w, pY+h);
                int ox=0, oy=0, ow=0, oh=0;

                for(int j=0; j<ConnectionListener.playerList.size(); j++){
                    if(i == j){ //don't compare ship to itself
                        continue;
                    }
                    Player otherplayer = ConnectionListener.playerList.get(j);
                    ox = otherplayer.getX();
                    oy = otherplayer.getY();
                    ow = (int)GameData.getWidth(otherplayer.getShipType());
                    oh = (int)GameData.getHeight(otherplayer.getShipType());

                    //If the ship intersects an island, need to regenerate (X,Y)
                    if(playerShip.intersects(ox-ow, oy-oh, ow*2, oh*2)){ //DC20111123//
                        System.out.println("ConnectionListener.GameUpdater.run(): PLAYER"+i+" CRASHED INTO PLAYER "+j);
                        gameOverPlayer(player);
                        continue updatePlayer;
                    }
                }


                //sendUpdate every other loop
                if(sendUpdate >= 3){
                    //Broadcast updated values
                    ClientWriter.queueMessage(-1, "shipState:"+player.getShipID()+":"+player.getX()+":"+player.getY()+":"
                                            +player.getSpeed()+":"+player.getCurHeading()+":"+player.getHealth());
                }
            }

            if(sendUpdate >= 3){
                sendUpdate = sendUpdate = 0; //don't send update next loop
            } else {
                sendUpdate = sendUpdate + 1;  //send update next loop
            }
        }
    }

    public static void gameOverPlayer(Player player){
        //player.setSpeed(0.0f);
        //player.setHealth(0.0f);
        //ClientWriter.queueMessage(-1, "shipState:"+player.getShipID()+":"+player.getX()+":"+player.getY()+":"
        //                                    +player.getSpeed()+":"+player.getCurHeading()+":"+player.getHealth());
        player.disconnectPlayer();
    }

    public static int getMinPlayers(){
        return minPlayer;
    }
}
