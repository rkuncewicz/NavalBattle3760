import java.net.*;
import java.io.*;

public class Server {
       
    
	final public static String version = "0.5.4.2";
	
	
	// This stuff was going to be in a gamestate class along with weather....
	public static boolean battleStart = false;
		
	private static Thread gameThread;
	
	// Turn these off to prevent status updates spamming the screen
	public static final boolean show_speedMessages = true;
    public static final boolean show_headingMessages = true;
    public static final boolean show_fireMessages = true;
    public static final boolean show_shipStatus = false;
    public static final boolean show_weather = false;
	
	public static void main (String args[]){
	   
	   Socket clientSocket = null;
	   ServerSocket ss = null; 
       System.out.println("Server Started - Version " + version + "\n");
	   System.out.println("\tPrint incoming speed Messages:\t\t" + show_speedMessages);
       System.out.println("\tPrint incoming heading Messages:\t" + show_headingMessages);
       System.out.println("\tPrint incoming fire Messages:\t\t" + show_fireMessages + "\n");
       System.out.println("\tPrint ship status updates:\t\t" + show_shipStatus);
       System.out.println("\tPrint weather updates:\t\t\t" + show_weather + "\n");
        
       System.out.print("\tReading in wind values...");
	   
	   try {
	       	// Reads pre-calculated values for boat speed in relation to the wind
            BoatLogic.readWindValues(181,"windmod.txt");
	   
       } catch (IOException e){
            // We have no way of dealing with this calculation if it can't be read in
            System.out.println("\tCould not read in wind mod values. " + e.getMessage());
            System.exit(1); 
       }
       
       System.out.println("finished");
       
       
       System.out.print("\tReading in map...");
       
       try {
            // Reads pre-calculated values for boat speed in relation to the wind
            MapManager.readInMap(2,"map.txt");
       
       } catch (IOException e){
            // We have no way of dealing with this calculation if it can't be read in
            System.out.println("\tCould not read in map values. " + e.getMessage());
       }
       System.out.println("finished\n");
       
       try {
        
            // Create the server socket
            ss = new ServerSocket(5283);
            
            // Listen for connections            
            while ((clientSocket = ss.accept()) != null) {                 
                
                if (!battleStart){
                    // Add client to the list of clients, its ID is returned             
                    int id = ClientList.newClient(clientSocket);
                    
                    // Spawn and start this client's thread
                    ClientList.startClient(id);
                    
                    System.out.println("Client connected. Assigned ID #" + id);           
                             
                } else {
                    System.out.println("Client attempting to connect when game has already started.");  
                }
            }
        } catch (Exception e) {System.out.println(e);}
        
	}	
	
}