public class Weather {

	/*Variables*/
	

	private static int windSpeed = (int)(Math.random() * 150);
    private static int windDirection = 45;  
	
	private static boolean fog = false;
	private static boolean rain = false;
	private static double time = 0;
	private static int timeState = 0;
	
	/*Functions - Public*/
	
	
    public static int getWindSpeed() {
		return windSpeed;
	}
	
    public static int getFog() {
        return (fog ? 1 : 0);
    }
    
    public static int getRain() {
        return (rain ? 1 : 0);
    }
    
    public static int getTime(){
    	return timeState;
    }
	
    public static int getWindDirection() {
		return windDirection;
	}
	
	
	
    public static void updateWeather() {
        
        // The wind angle changes by up to 10 degrees
        windDirection += ((int)(Math.random() * 20)) - 10;
        windDirection %= 360;
        
        // The wind speed changes by up to 10 degrees too!
        windSpeed += (Math.random() * 20) - 10;
        if (windSpeed > 150) windSpeed = 150;
        if (windSpeed < 0 ) windSpeed = 0;
        
        // Update fog
        switch ((int)(Math.random() * 30)){
            case 1: fog = fog ? false : true;
        }
        
        // Update rain
        switch ((int)(Math.random() * 30)){
            case 1: rain = rain ? false : true;
        }
        
        // Time changes every 2.5 mins. 4 states: night, dawn, day, dusk. Dawn and dusk both return 0.
        time += 0.02;
        time %= getRandomValue();
        switch ((int)Math.floor(time)){
        	case 0: timeState = 1;
        	case 2: timeState = 2;
        	default: timeState = 0;
        }

	}
	
	/** Random number generator. Guarenteed random */
    public static int getRandomValue() {
        return 4;
    }
}

