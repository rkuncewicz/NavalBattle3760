import java.net.*;

public class ClientList {

	/*Variables*/
	
    private static final int max_Players = 50;
    private static int online_Players = 0;
    
    private static Client cList[] = new Client[max_Players];   
	
	public static int newClient(Socket clientSocket){
	    
       
	    // Find first available empty spot
        int cID = 0;                            
        while(cID < cList.length){
            
            // If this spot is empty, create & add the client                    
            if (cList[cID] == null){
                                
                online_Players ++;
                cList[cID] = new Client(clientSocket, cID);
                return cID;    
            }            
            cID++;
        }
                
        // Server is full         
        return -1; 
        
    }
    
    public static boolean startCheck(){
        if (online_Players < 1)return false;
        if (Server.battleStart)return false;
        
        // If any of the online players aren't ready we can't start
        for (int x = 0; x < cList.length; x ++){
            if (cList[x] != null && !cList[x].isReady())
                return false;
        } 
        
        return true;       
    }
    
    public static void startClient(int cID){
        if (cList[cID] == null) return;        
        
        cList[cID].createThread();                    
        cList[cID].startThread();           
    }
	
		
	public static void ClientList() {
	   
	    // Set all the server spots to empty
	    for (int x = 0; x < cList.length; x++){
            cList[x] = null;    
	    }
	}
	
	
    public static void removeClient(int cID) {
        if (cID < 0 || cID >= cList.length || cList[cID] == null)return;
        
        cList[cID] = null;
        online_Players --;
        
        // TELL THE OTHER CLIENTS YOU LEFT
    
    }
  
  
    /** After the game starts, all ships IDs and usernames are sent out once */
    public static void sendShips() {
        for (int x = 0; x < cList.length; x++){
            if (cList[x] != null)
                sendToAll("ship:" + cList[x].getID() + ":" + cList[x].getShipType() + ":" + cList[x].getName() + ";");   
        }    
    }
    
    public static void updateShips() {
        for (int x = 0; x < cList.length; x++){
            if (cList[x] != null)
               BoatLogic.updateShip(cList[x]);
        }
		
		// For every updated client, send that update to everyone
		sendUpdatedPositions();
		
		// Reset all clients to not updated
		resetShipsUpdated();
    }
    
    public static void resetShipsUpdated() {
    	 for (int x = 0; x < cList.length; x++)
            if (cList[x] != null) cList[x].setUpdate(false);  	
    }
    
    
    /** Calls Map class to generate starting positions */ 
    public static void setStartPositions() {
        for (int x = 0; x < cList.length; x++){
            if (cList[x] != null)
               MapManager.pickSpawn(cList[x]);
        }
    }
    
    public static Client getClient(int cID){
        if (cID < 0 || cID >= cList.length || cList[cID] == null)return null;        
        return cList[cID];
                        
    }
    /** Sends every ship status to every client. Called at the start of the game and
    *  once every 3 minutes. */ 
    public static void sendPositions() {
        for (int x = 0; x < cList.length; x++){
            if (cList[x] != null)
                sendToAll(cList[x].getState());        
        }
    }
    
    
    public static void sendUpdatedPositions() {
        for (int x = 0; x < cList.length; x++){
            if (cList[x] != null && cList[x].shipUpdated())
                sendToAll(cList[x].getState());        
        }
    }
	
    public static synchronized void sendToAll(String msg) {
       for (int x = 0; x < cList.length; x ++){
            if (cList[x] != null) cList[x].send(msg);                
       }
	}
	
	/*
	public Client getByID(int cID)
	{
	}*/
	
	/*Functions - Private*/
}