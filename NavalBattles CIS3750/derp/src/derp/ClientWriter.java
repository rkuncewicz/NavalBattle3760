/*
    Class: ClientWriter
    Creator: Domenico Commisso
    Created on: Monday November 14th, 2011

    NOTE: If it is desired to stop the thread when it has finished
          handling the last thing in the queue uncomment the sections
          labeled with: "//UNCOMMENT IF STOPPING THREAD IS WANTED"
*/

//package server;

import java.util.ArrayList;

public class ClientWriter extends Thread {
    private static ArrayList<String> queue = new ArrayList<String>();
    private static ArrayList<Player> list = null;
    private static WeatherAndTime weatherAndTime = null;

    public ClientWriter(){
        list = ConnectionListener.playerList;
    }

    public static void queueMessage(int sendTo, String message){
        queue.add(String.valueOf(sendTo)+"~"+message);
        System.out.println("CLIENTWRITER.queueMessage() queued up:"+String.valueOf(sendTo)+"~"+message);

        /*
        //if thread is stopped, start it
        if(this.isStopped()){ this.start(); }
        */ // UNCOMMENT IF STOPPING THREAD IS WANTED
    }

    @Override
    public void run(){
        sendInitialData();
        queueHandler();
    }

    private void sendInitialData(){
        Player player = null;
        Player to = null;

        System.out.println("CLIENTWRITER.sendInitialData(): sending time");

        //send time to every player
        for(int i=0; i<list.size(); i++){
            to = list.get(i);
            to.send("time:"+GameData.getStartTime());
        }

        System.out.println("CLIENTWRITER.sendInitialData(): sending wind");
        //send wind to every player
        for(int i=0; i<list.size(); i++){
            to = list.get(i);
            to.send("wind:"+GameData.getDefaultWindSpeed()+":"+GameData.getDefaultWindDir());
        }

        System.out.println("CLIENTWRITER.sendInitialData(): sending fog");
        //send fog to every player
        for(int i=0; i<list.size(); i++){
            to = list.get(i);
            to.send("fog:"+GameData.getDefaultFog());
        }

        System.out.println("CLIENTWRITER.sendInitialData(): sending rain");
        //send rain to every player
        for(int i=0; i<list.size(); i++){
            to = list.get(i);
            to.send("rain:"+GameData.getDefaultRain());
        }

        Server.displayWeather("Time: "+GameData.getStartTime()+"\n  Wind Dir: "+GameData.getDefaultWindDir()+"\n  Wind Speed: "+GameData.getDefaultWindSpeed()
                              +"\n  Fog: "+GameData.getDefaultFog()+"\n  Rain: "+GameData.getDefaultRain());

        System.out.println("CLIENTWRITER.sendInitialData(): sending player datas");
        //For every player, broadcast info
        for(int i=0; i<list.size(); i++){
            player = list.get(i);
            //send ship to every player
            for(int j=0; j<list.size(); j++){
                to = list.get(j);
                to.send("ship:"+player.getShipID()+":"+player.getShipType());
            }

            //send id,xrel,yrel,speed,direction,health
            for(int j=0; j<list.size(); j++){
                to = list.get(j);
                to.send("shipState:"+player.getShipID()+":"+player.getX()+":"+player.getY()+":"
                      +player.getSpeed()+":"+player.getCurHeading()+":"+player.getHealth());
            }
        }
        //send start to every player
        for(int i=0; i<list.size(); i++){
            to = list.get(i);
            to.send("start");
        }
        weatherAndTime = new WeatherAndTime();
        ConnectionListener.setGameStarted(true);
        ConnectionListener.startGameHandler();
    }

    private void queueHandler(){
        int sendTo = 0;
        String message = null;
        String[] parsed = null;

        while(true){
            if(queue.size() > 0) {
                message = queue.remove(0);
                if(message == null){
                    continue;
                }
                parsed = message.split("~", 2);
                System.out.print("CLIENTWRITER.queueHandler(): parsed=");
                for(int i=0; i<parsed.length; i++){
                    System.out.print(parsed[i]+",");
                }
                System.out.println("\nCLIENTWRITER.queueHandler(): parsed.length="+parsed.length);
                if(parsed.length < 2){
                    Server.displayError("CLIENTWRITER.queueHandler() ERROR: Message could not be parsed:"+message);
                    continue;
                }
                try{
                    sendTo = Integer.parseInt(parsed[0]);
                } catch(Exception e) {
                    System.out.println("CLIENTWRITER.queueHandler() error: unable to parse sendTo integer:"+e.toString());
                    continue;
                }
                message = parsed[1];

                //ArrayList<Player> list = ConnectionListener.playersList;

                //If sendTo is (-1), then broadcast message to everyone
                if(sendTo == -1){
                    //For each player in the list...
                    for(int i=0; i<list.size(); i++){
                        //...send the message to that Player calling Player.send()
                        list.get(i).send(message);
                    }
                } else {
                    if(sendTo < list.size()) {
                        list.get(sendTo).send(message);
                    } else {
                        System.out.println("CLIENTWRITER.queueHandler() error: sendTo="+sendTo+" greater than listsize="+list.size());
                    }
                }
            }
            if(Server.getKillServer()){ break; }
        }
     /* stop thread
      */ // UNCOMMENT IF STOPPING THREAD IS WANTED
        Server.displayError("CLIENTWRITER.queueHandler() finished.");
    }

}
