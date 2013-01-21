//package server;

import java.util.*;


// Still need to do:
// -set the delay and period for the tasks to appropriate numbers
// -set max wind speed to a proper value

public class WeatherAndTime// extends Thread
{
    int currTime;
    int currRain;
    int currFog;
    int currWindSpeed;
    int currWindDir;

    int maxWindSpeed = 100;
    int currWeather = 0;
	
    private static Timer windTimer = new Timer();
    private static Timer timeTimer = new Timer();
    private static Timer weatherTimer = new Timer();
	Random generator = new Random();

    public WeatherAndTime(){
        //wind
		windTimer.scheduleAtFixedRate(new Update(0), 60000, 60000);
		//time of day
		timeTimer.scheduleAtFixedRate(new Update(1), 300000, 300000);
		//weather
		weatherTimer.scheduleAtFixedRate(new Update(2), 120000, 120000);

        System.out.println("WeatherAndTime setup complete");
    }
	
	class Update extends TimerTask
	{
		int type;
		
		public Update (int type)
		{
			this.type = type;
		}
		
		public void run()
		{
			if(type == 0)
			{ // Update wind.
                System.out.println("WeatherAndTime.Update.run(): Updating wind");
				currWindSpeed = generator.nextInt(maxWindSpeed);
				currWindDir = generator.nextInt(360); // Between 0 and 359.
                                GameData.setCurWindDir(currWindDir);
				broadcastWindChange();
				//timer.cancel;
			}
			else if (type == 1)
			{ // Update time of day.
                System.out.println("WeatherAndTime.Update.run(): Updating time");
				if (currTime == 0)
				{ // Change from dawn to midday.
					currTime = 1;
				}
				else if (currTime == 1)
				{ // Change from midday to night.
					currTime = 2;
				}
				else if (currTime == 2)
				{ // Change from night to dawn.
					currTime = 0;
				}
				broadcastTimeChange();
				//timer.cancel;
			}
			else if (type == 2)
			{ // Update weather.
                System.out.println("WeatherAndTime.Update.run(): Updating weather");
				currWeather = generator.nextInt(4);
				if (currWeather == 0)
				{ // No wind and no rain.
					currRain = 0;
					currFog = 0;
				}
				else if (currWeather == 1)
				{ // Rain, but no fog.
					currRain = 1;
					currFog = 0;
				}
				else if (currWeather == 2)
				{// Fog, but no rain.
					currRain = 0;
					currFog = 1;
				}
				else if (currWeather == 3)
				{// Both rain and fog at the same time.
					currRain = 1;
					currFog = 1;
				}
				broadcastWeatherChange();
				//timer.cancel;
			}
		}
	}
	// Broadcasts the current time of day.
	public void broadcastTimeChange ()
	{
		//time (currTime);
        ClientWriter.queueMessage(-1, "time:"+currTime);
        Server.displayWeather("Time: "+currTime);
	}

	// Broadcasts the current weather.
	public void broadcastWeatherChange ()
	{
		//fog (currFog);
		//rain (currRain);
        ClientWriter.queueMessage(-1, "fog:"+currFog);
        ClientWriter.queueMessage(-1, "rain:"+currRain);
        Server.displayWeather("Fog: "+currFog+"\n  Rain: "+currRain);
	}

	// Broadcasts the current wind speed and direction.
	public void broadcastWindChange ()
	{
		//wind (currWindSpeed, currWindDir);
        ClientWriter.queueMessage(-1, "wind:"+currWindSpeed+":"+currWindDir);
        Server.displayWeather("Wind Dir: "+currWindDir+"\n  Wind Speed: "+currWindSpeed);
	}

    public static void cancelTimers(){
        windTimer.cancel();
        timeTimer.cancel();
        weatherTimer.cancel();
    }
}
