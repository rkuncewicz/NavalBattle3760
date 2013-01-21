import java.util.*;
import java.awt.*;
import java.io.*;
import java.util.Scanner;

class MapManager {

	//This really shouldn't be a class. WHATEVS
	
	/*Functions - Public*/
	//Constructor
	
	public static final int width = 5000, height = 4000; // Must be multiples of 200
	static boolean[][] startPos = new boolean[10][8];
	
    public static void pickSpawn(Client client){
		int x, y;
		
		if (client == null) return;
		
		do{
			if(Math.random() > 0.5){
				x = (int)(Math.random() * 2);		// Random 0 to 9
				y = (int)(Math.random() * 2) * 2;	// Random 0 OR 7
			}else{
				y = (int)(Math.random() * 2);		// Random 0 to 7
				x = (int)(Math.random() * 2) * 2;	// Random 0 OR 9
			}
		}while (startPos[x][y]);
			
		startPos[x][y] = true;
		
		client.setX (1800);
		client.setY (2000);
		client.setSpeed (0);
		client.setTrueSpeed (0);
		
		client.setRotation(((int)(Math.random() * 360)));
		client.setHeading(0);
        client.setHealth(Ship.getShip(client.getShipType()).maxHealth);
	}
	
    public static void resetPositions(){
		for (int i = 0; i < 10; i++){
			for (int j = 0; j < 8; j++){
				startPos[i][j] = false;
			}
		}
	}
	
	static String island[];
	static int islandSize[];
	
    public static void readInMap(int size, String location) throws IOException {            
        island = new String[size];        
        islandSize = new int[size];        
        
        island[0] = "";
             
        Scanner scanner = new Scanner (new BufferedReader(new FileReader(location)));
        
        int islandAt = 0;
        while (scanner.hasNextInt()){
            int coordinate = scanner.nextInt();
            
            if (coordinate < 0) {
                islandSize[islandAt] /= 2;              
                island[++islandAt] = "";
            } else {            
                island[islandAt] += ":" + coordinate; 
                islandSize[islandAt] ++;
            }          
        }
        
        System.out.println("shore:" + islandSize[0] + island[0] + ";");    
        	   
    }
	
	public static void sendMap(Client temp){   
        
	   for (int x = 0; x < island.length; x++){
	        temp.send("shore:" + islandSize[x] + island[x] + ";"); 
            System.out.println("shore:" + islandSize[x] + island[x] + ";");    
	   }
       temp.send("shore:x;");
    }
}