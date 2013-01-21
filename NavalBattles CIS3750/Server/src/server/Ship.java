//This class isn't in the document, but I figured it'd be handy since we need
//to define the ships anyways. This would be used instead of an int in 
//Client for shipType;

public class Ship {
	
	/*Variables*/
	public double topSpeed;
	public double turnSpeed;
	public double maxHealth;
	public double firePower;
	public int shipType, length, width;
	public double hitChance;
    
	
	/*Functions - Public*/
	
	//Constructor
	public Ship(int type){ 		
		switch(type) {
			case 0: setSloop();	  break;
			case 1: setFrigate(); break;
			case 2: setMoW();	  break;
	    }
	}
	
	public static Ship getShip(int type){
	    return (new Ship(type));   
    }
	
	/*Functions - Private*/
	private void setSloop() {
		topSpeed = 90; // Top speed in m/s
		maxHealth = 3;
        shipType = 0;
        length = 20;
        width = 20;        
       	hitChance = 1.0/3.0;
	}

	private void setFrigate() {
		topSpeed = 70;
		maxHealth = 4;
        shipType = 1;
        length = 60;
        width = 20;
        hitChance = 2.0/3.0;
	}
	
	private void setMoW() {
		topSpeed = 50;
		maxHealth = 5;
        shipType = 2;
		length = 80;
		width = 20;
		hitChance = 5.0/6.0;
	}
}