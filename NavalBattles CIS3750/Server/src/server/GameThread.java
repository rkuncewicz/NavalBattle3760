public class GameThread implements Runnable {

    private Thread thread;

    public GameThread(){
        thread = new Thread(this);
        thread.start();       
    }
    
    
    /** Thread to force status updates every 3 second */
    public void run() {       
        long startTime, endTime, totalTime, difference;
        
        totalTime = 0;
        
        while (Server.battleStart) {
            
            
            ClientList.updateShips();
            try{                           
                Thread.currentThread().sleep(100);
            } catch(Exception e){} 
            totalTime += 100;            
            
            // Every 3 seconds  
            if (totalTime >= 3000){
                totalTime = 0;
                
                // Update positions
                System.out.println("Three seconds passed, sending out update..."); 
                ClientList.sendPositions();                
                
                // Update weather
				//Weather was updating too frequently
				Weather.updateWeather();                 
				if (Server.show_weather) System.out.print("New Weather: ");
							   
				String msg = "wind:" + Weather.getWindSpeed() + ":" + Weather.getWindDirection()  + ";";                        
				ClientList.sendToAll(msg);                               
				if (Server.show_weather) System.out.print("\"" + msg + "\" ");
				
				msg = "fog:" + Weather.getFog() + ";";                        
				ClientList.sendToAll(msg);                               
				if (Server.show_weather) System.out.print("\"" + msg + "\" ");
				
				msg = "rain:" + Weather.getRain() + ";";                        
				ClientList.sendToAll(msg);                               
				if (Server.show_weather) System.out.print("\"" + msg + "\"\n");
				
				msg = "time:" + Weather.getTime() + ";";                        
				ClientList.sendToAll(msg);                               
				if (Server.show_weather) System.out.print("\"" + msg + "\"\n");
            }            
            
            
        }

    }    
    
}