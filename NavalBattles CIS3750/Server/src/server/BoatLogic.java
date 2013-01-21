import java.io.*;
import java.util.Scanner;
import java.text.*;


  
class BoatLogic {

	/*Functions - Public*/

	private static double ticRate = 1.0/10.0;
    private static double windMod[];
    private static DecimalFormat decimalF = new DecimalFormat("#.#");
    
	public void BoatLogic() {
	}

    
    /** Used with permission from the Drake group. Reads in the text file of wind values */
    public static void readWindValues(int size, String location) throws IOException{
        
        windMod = new double [size];
        
        int i = 0;
        
        Scanner scanner = new Scanner (new BufferedReader(new FileReader(location)));
        
        while (scanner.hasNextDouble()){
            scanner.nextDouble();
            windMod[i++] = scanner.nextDouble();
        }
        
    }
    
    
    
	public static double findSpeed(Client client){
        if (client.getSpeed() == 0){
        	client.setTrueSpeed(0);
        	return 0;
        }
        
		double speed = Ship.getShip(client.getShipType()).topSpeed;
        
        int wind_direction = Weather.getWindDirection();
        //if (wind_direction > 180) wind_direction = 360 - wind_direction;
        
        int relative_Angle = (int)Math.round(client.getRotation() - Weather.getWindDirection() + 180);
        
        // Get in the 0 to 360 range
        relative_Angle = relative_Angle % 360;
        
        // Modifiy it to 0 - 180, so the sides are symetric    
        if (relative_Angle > 180) relative_Angle = 360 - relative_Angle;
        
        client.setTrueSpeed(speed * client.getSpeed() * windMod[relative_Angle]);
        return (client.getTrueSpeed());
	}

	public static void updateShip(Client ship) {
		if (ship == null) return;
		if (ship.getHealth() <= 0) return;

        boolean print = false;
		boolean change = false;
    	double curDir = ship.getRotation();
	    double newDir = ship.getHeading();
    	int shipType = ship.getShipType();

    	if (newDir != 0){
    		curDir += newDir;
			
			if (curDir < 0) curDir = 360 + curDir;
            else if (curDir > 360) curDir = curDir - 360;
			
    		ship.setHeading (0);
			ship.setRotation (curDir);
			change = true;
    	}
		
		double curSpeed = ship.getSpeed();
		double newSpeed = ship.getSpeedChange();
		
		if (newSpeed != curSpeed){
			ship.setSpeed (newSpeed);
			change = true;
		}

        double shipSpeed = findSpeed(ship);        
        if (shipSpeed != 0.0) print = true;  
		
        double x = Math.sin(Math.toRadians(curDir)) * shipSpeed * ticRate;
        double y = Math.cos(Math.toRadians(curDir)) * shipSpeed * ticRate;
        ship.setX (ship.getX() + x);
        ship.setY (ship.getY() - y);
            
    	if (change){
    		ship.setUpdate(true);
            print = true;
    	}
    	
        if (print && Server.show_shipStatus) {
           
            String msg = "Client (" + ship.getID() + ") Co: (" + decimalF.format(ship.getX()) + "," +
                         decimalF.format(ship.getY()) + ")\tRotation:" + decimalF.format(ship.getRotation()) + 
                         "\tSP:" + decimalF.format(ship.getTrueSpeed());
                                                  
            System.out.println(msg);   
        }
    	
       	
	}

	public static void cannonFired(float xAtk, float yAtk, float xTrg, float yTrg) {
	}
}