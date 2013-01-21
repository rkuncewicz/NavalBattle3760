Portsmouth Team (Server)
************************

Dependancies:
-ClientWriter.java
-ConnectionListener.java
-GameData.java
-Player.java
-Server.java
-velocitytable.txt
-WeatherAndTime.java
-gameimage <DIRECTORY>
  ->frigate.png
  ->manofwar.png
  ->sloop.png
  ->water.png


To compile:
javac Server.java

To run:
java Server


To use:
Fill in Port#, Minimum Number of Players to connect before game starts,
Maximum Number of Player to connect before rejecting players (or use
the defualts). When ready press the "Start Server" button.

You can press the "Map" button to view a minimap. The map will
pop up in a new window, and will show the boats only after the game
has started.


NOTES:
In some cases the IP label gives the loopback address instead of
the one clients will need to use to connect to from other computers.
The server program has a lot of print statements in it, so the
terminal will most likely be updated too fast to be read.
You can pipeline the stdout stream to file if you wish, but if you
do make sure you don't leave Server running for too long, or you'll
end up with a large output file.
Ships cannot fire, this has not been implemented yet.
Speed does not take the wind into account currently.
Time, Wind, Rain, and Fog should all be working.
Collision with islands, edge of map, and other boats are implemented,
but not fully tested.

