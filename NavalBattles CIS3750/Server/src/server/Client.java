import java.net.*;
import java.io.*;

public class Client implements Runnable  {
	
	/*Variables*/
	
	//Ship Positioning
    private double x; 
    private double y;
    private double speed;
	private double speedChange;
	private double trueSpeed;
	
	private boolean headingChanged = false;
	

	//What is the difference between rotation and heading?
	
    // RE: What is the difference between rotation and heading?
    //     Rotating is where the ship is actually facing, heading is the amt to turn
    
    private double rotation;
    private double heading;
	
	//Ship Information
	private int id;
	private int shipType;
	private String shipName;
	
	//Ship Status & Health
	private boolean alive;
	private double shipHealth;
	
	//Shooting
	private int shotTarget;
	private int shotPosition;
	private boolean shotFired;
	
	//Readying & Leaving
	private boolean ready;
	private boolean leavingGame;
	
	// Network Variables
    private Thread client_Thread = null;
    private Socket client_Socket = null;    
    private BufferedReader in = null;
    private PrintWriter out = null;     
	
	private String name = "";
	
	//Constructor
    public Client(Socket socket, int id) {
        this.client_Socket = socket;
        this.id = id;
        this.ready = false;
        this.shipType = -1;
        this.alive = true;
        
        try {
            // If nothing happens in a given amount of milliseconds, timeout  
            client_Socket.setSoTimeout(300000);
            
            // Create a stream for sending data to this client  
            out = new PrintWriter(new OutputStreamWriter(client_Socket.getOutputStream()));
            
            // Create a stream to recieve data from this client
            in = new BufferedReader(new InputStreamReader(client_Socket.getInputStream()));
            
        } catch (IOException e) {
            
			
        } 
		
	}	
	
	public void createThread(){	   
        client_Thread = new Thread(this);
        client_Thread.setDaemon(false);
    }
    
    
    public void startThread() { 
        client_Thread.start(); 
    }	
	
    
    // Remember if we've printed a warning type for this client. We do this to prevent
    // spamming the screen in case the client keeps being stupid.
    boolean warningPrinted[] = new boolean[50]; 
    
	
    
    public void run() {
        
		String line = ""; 
		
		while (true) {                       
			
			try {      
                // Wait for message from client      
                line = in.readLine();                
                
                // Connection error if we recieve a null.
                if (line == null)
                    throw new IOException();
                
                if (alive) interpretData(line);
				
            } catch (IllegalArgumentException e) {
                
                p("\tWarning: Client (" + id + ") " + e.getMessage());
				
            } catch (IllegalStateException e) {
				
                p("\tWarning: Client (" + id + ") " + e.getMessage()); 
                
            } catch (IOException e){
                
                p("\tIO Error with Client (" + id + ") - " + e.getMessage()); 
                break;
            }
        } 
        
		this.disconnect(); 
		
		
    }
    
	
    private void interpretData(String message) throws IllegalArgumentException,IllegalStateException {
		
        // Remove the terminating semicolon
        if (message.substring(message.length()-1).equals(";"))
            message = message.substring(0,message.length()-1);
		
		// Split the message by colons
		String part[] = message.split(":");
		
		
		// Convert the word into a number  (eg "ready" -> 1)
		int messageType = -1;        
		String commands[] = {"register","ready","fire","speed","setHeading"};
		
		boolean found = false; int c = 0;
		while (!found && c < commands.length){
			if (part[0].equals(commands[c])) {
				messageType = c;
				found = true;
			}
			c ++;
		}        
        
        // Print client message if that message is turned on
        if (!Server.battleStart || 
            (c == 2 && Server.show_fireMessages) ||
            (c == 3 && Server.show_speedMessages) ||
            (c == 4 && Server.show_headingMessages)) {      
            p("Client (" + id + ") says: \"" + message + "\""); 
        }
        
        // If the client's message has no match
        if (!found)
            throw new IllegalArgumentException("sent an unrecognized command \"" + message + "\""); 
		
		// Every message type except ready requires at least 1 argument 
		if (messageType != 1 && part.length < 2){
			throw new IllegalArgumentException("sent a \"" + commands[c-1] + "\" command without any arguments");                        
		}
        
		
        switch (messageType){
            case 0:                
                clientRegister(part[1], (part.length > 2) ? part[2]: "No Name");  break;              
				
            case 1: clientReady();              break;            
            case 2: clientFire(part[1]);        break;
            case 3: clientSetSpeed(part[1]);    break;
            case 4: clientSetHeading(part[1]);  break;
				
				// Just in case client sends a valid message of which we have no response to.           
            default:
                throw new IllegalArgumentException("sent an unrecognized command \"" + message + "\""); 
        } 
    }
    
    
    
    private synchronized void clientReady() throws IllegalStateException {
        if (Server.battleStart) 
            throw new IllegalStateException("is sending ready after the battle has started.");
		
		if (ready) 
			throw new IllegalStateException("is sending ready, when it has already done so.");
		
		if (shipType == -1)
			throw new IllegalStateException("is signalling ready before registering a ship.");                 
		
		
		p("\t-> Set Client (" + id + ") to ready"); 
		p("\t-> Sending Client (" + id + ") map");                        
		
		this.ready = true;
		
		// This client has signalled ready. Now to check to see if game can start.            
		if (ClientList.startCheck()) startGame();   
		
	}
	
    private void clientRegister(String type, String name) throws IllegalArgumentException,IllegalStateException {
        if (Server.battleStart) 
            throw new IllegalStateException("is attempting to register a ship after the battle has started.");
		
		if (ready)
			throw new IllegalStateException("is registering a ship after it has signalled ready.");
		
		if (shipType != -1)
			throw new IllegalStateException("is trying to update already set ship type.");
		
		// Strip unwanted characters from the name    
		String formattedName = name.replaceAll("[^a-zA-Z0-9]","");     
		this.name = name;
		
		// Convert the ship ID to a number, and set this client's ship type  
		try {
			int temp = Integer.parseInt(type);
			
			if (temp < 0 || temp > 2)
				throw new NumberFormatException();                        
			
			shipType = temp;
			
		} catch (NumberFormatException e){
			shipType = 0;
			throw new IllegalArgumentException("sent an unrecognized ship type. Ship type set to 0.");
		} finally {
			p("\t-> Set Client (" + id + ") ship to type " + shipType);
			p("\t-> Set Client (" + id + ") name to " + name);    
			send("registered:" + id + ";");
			sendMap();
			p("\t-> Sent Map to Client (" + id + ")"); 
		}        
    }
	
    private void clientFire(String target) throws IllegalArgumentException, IllegalStateException {
        // This function is a work in progress
        
        if (!ready || !Server.battleStart)
            throw new IllegalStateException("is trying to fire when not in battle.");	   
		
		//TODO: Add check to make sure they're facing the right direction.
		
		double hitPrct = 0;
		shotTarget = 0;
		
		//Convert string target to integer
		try {
			shotTarget = Integer.parseInt(target);
			
		} catch (NumberFormatException e){            
			throw new IllegalArgumentException("sent an invalid ship ID.");
		}
		
		
		//Get target from ClientList
		Client clTarget = ClientList.getClient(shotTarget);
		
		if ( clTarget == null )
            throw new IllegalArgumentException("sent an invalid ship ID.");
			
		ClientList.sendToAll("firing:" + id + ":" + clTarget.getID() + ";");
		
		System.out.println("clientFire: shotTarget = " + shotTarget + "clTarget = " + clTarget.getName());
		
		hitPrct = Ship.getShip(shipType).hitChance;
		
		System.out.println("clientFire: Hit Percentage = " + hitPrct);
		
		//Calc angle between Client and Target
		double angleToTarget = 0.0;
		double relativeAngle = 0.0;
		double targetX = clTarget.getX();
		double targetY = clTarget.getY();
		double distance = 0;
		double hitAngle = 0;
		boolean canHit = false;
		
		angleToTarget = Math.toDegrees(Math.atan2( targetY - y, targetX - x ));
		relativeAngle = (angleToTarget - rotation) % 360;
		
		distance = Math.pow(Math.pow(targetX - x, 2) + Math.pow(targetY - y, 2) , .5);
		hitAngle = Math.toDegrees(Math.asin(35.0/distance));
		
		if ((relativeAngle < hitAngle) || ((360 - relativeAngle) < hitAngle)
			|| (relativeAngle > (90 - hitAngle) && (relativeAngle < (90 + hitAngle)))
			|| (relativeAngle > (270 - hitAngle) && (relativeAngle < (270 + hitAngle)))){
			canHit = true;
		}
		
		if (distance < 500 && canHit && Math.random() <= hitPrct) {
			
			System.out.println("clientFire: Client " + clTarget.getID() + " has been hit. Setting health and updating clients.");
			
			//Ship has been hit.
			double temp = clTarget.getHealth() - .5;
			clTarget.setHealth(temp);
			clTarget.setUpdate(true);
			//ClientList.sendPositions();
			
			if ( temp <= 0 ) {
				//Target has been killed, disconnect them from the game.
				System.out.println("clientFire: Target is dead, removing from game.");
				clTarget.setAlive(false);
			}					
		}
		
    }
    
    private void clientSetSpeed(String speed) throws IllegalArgumentException, IllegalStateException {                   
        if (!ready || !Server.battleStart)
            throw new IllegalStateException("is trying to set speed before the battle has started.");
		
		try {
			double temp = Double.parseDouble(speed);
			
			if (temp < 0 || temp > 1)
				throw new NumberFormatException();                        
			
			//this.speed = temp;
			this.speedChange = temp;
			
		} catch (NumberFormatException e){
			throw new IllegalArgumentException("sent an invalid speed \"" + speed + "\".");    
		}     
    }
	
    private void clientSetHeading(String heading) throws IllegalArgumentException, IllegalStateException {
		if (!ready || !Server.battleStart)
            throw new IllegalStateException("is trying to set heading before the battle has started.");
		
		try {
			int temp = Integer.parseInt(heading);
			
			if (temp < -3 || temp > 3)
				throw new NumberFormatException();                        
		
			this.heading = temp;
			
		} catch (NumberFormatException e){
			throw new IllegalArgumentException("sent an invalid heading \"" + heading + "\".");  
		}           
    }
    
    
    
    
	private void startGame(){        
        p("\nAll Clients Ready. Game is starting.");
        ClientList.sendToAll("start;");  
        ClientList.sendShips();
        ClientList.setStartPositions();
        ClientList.sendPositions();
        Server.battleStart = true;
        // 3 second data push
        GameThread thread = new GameThread();
        
		
    }        
	
   	private void disconnect(){
        // User has disconnected. Clean up variables.        
        System.out.println("Client (" + id + ")  Disonnected");  
		
        try {
            out.close();in.close();
            client_Socket.close();               
        } catch (Exception e) {} 
        
        ClientList.removeClient(id);      
    }
   	
   	
   	private void sendMap(){
   	    
   	    MapManager.sendMap(this);
		
   	}
   	
    /** Send a message to this client */
    public void send(String msg) {          
        try {
            out.println(msg);
            out.flush();
        } catch (Exception e){} 
    }    
    
    public void p(String msg){
		
		System.out.println(msg);   
    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//Accessor Methods
	
	
    public boolean isReady() {
        return ready;    
    }
	
    public double getX() {
        return(x);
    }
    
    public double getY() {
        return(y);
    }
    
    public double getSpeed() {
        return(speed);
    }
	
	public double getSpeedChange() {
		return(speedChange);
	}
	
	public double getTrueSpeed(){
		return trueSpeed;
	}
    
    public double getRotation() {
        return(rotation);
    }
    
    public double getHeading() {
        return(heading);
    }
    
    public int getID() {
        return id;    
    }
    
    public String getName() {
        return name;            
    }
    
    public String getShipName() {
        return(shipName);
    }
    
    public double getHealth() {
        return(shipHealth);
    }
    
    public int getShipType() {
    	return shipType;   
    }
    
    //Mutator Methods
    public void setAlive(boolean a){
    	alive = a;
    }
    
    public void setX(double nx) {
        x = nx;
    }
    
    public void setY(double ny) {
        y = ny;
    }
    
    public void setSpeed(double ns) {
        speed = ns;
    }
    
    public void setTrueSpeed (double ts){
    	trueSpeed = ts;
    }
    
    public void setRotation(double nr) {
        rotation = nr;
    }
    
    public void setHeading(double nh) {
        heading = nh;
    }
    
    public void setShipName(String nn) {
        shipName = nn;
    }
    
    public int degreeConvert(double i){
    	return (((int)i + 180) % 360 - 180);
    }
    
    /** The string used for sending out this client's position/heading */
    public String getState(){
       
		
        System.out.println("shipState:" + id + ":" + ((int)Math.round(x)) + ":" + ((int)Math.round(y)) 
        + ":" + (int)(trueSpeed) + ":" + degreeConvert(rotation) + ":" + shipHealth + ";");
        
         return "shipState:" + id + ":" + ((int)Math.round(x)) + ":" + ((int)Math.round(y)) 
        + ":" + (int)(trueSpeed) + ":" + degreeConvert(rotation) + ":" + shipHealth + ";";
    }
    
    	
	public void setUpdate(boolean updated){
		headingChanged = updated;		
	}
	
	public boolean shipUpdated() {
		return headingChanged;	
	} 
		
    public void setHealth(double nh)
    {
        shipHealth = nh;
    }
    
    //Checks
    public boolean checkLeaveGame()
    {
        //This function makes no sense at ALL.
        return false;
    }
    
    public boolean checkShotFired()
    {
        //NEITHER DOES THIS ONE.
        
        return false;
    }
	
	
}
