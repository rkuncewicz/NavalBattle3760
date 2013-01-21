//package server;

import java.awt.Polygon;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Random;



public class GameData {
    private static final int mapWidth = 5000;
    private static final int mapHeight = 4000;
    private static double xScale = 1;
    private static double yScale = 1;

	//For collision detection
    private static final int shores[][] = {
		{ 8,  2000,1500,  3000,1500, 3250,1750, 3250,2250, 3000,2500, 2000,2500, 1750,2250, 1750,1750 },
		{ 10, 300,200,  500,150, 600,250, 400,350, 450,500, 650,450, 700,650, 350,750, 275,675, 200,300 },
        { 10, 4000,2250, 4500,2000, 4700,2400, 4600,2750, 4800,3250, 4600,3500, 3800,3500, 3500,3250, 4200,3000, 4100,2800 }
    };
	//To send to clients
	private static String islands[] = null;
    private static Polygon polygons[] = null;
    private static Polygon scaledPolygons[] = null;
    //Data for each ship
	private static final float [][] shipDatas = {
		//Velocity   ,  POHS   , MaxHealth ,  RspeedR , Width  ,  Height
		{    90.0f   , 33.33f  ,   3.0f    ,    1f    , 20.0f  ,  20.0f  }, //Sloop
		{    70.0f   , 66.67f  ,   4.0f    ,    2f    , 20.0f  ,  60.0f  }, //Frigate
		{    50.0f   , 83.33f  ,   5.0f    ,    3f    , 20.0f  ,  80.0f  }  //ManOfWar
	};
    private static int startPos[][] = null;
    private static final int defaultRain = 0;
    private static final int defaultFog = 0;
    private static final int defaultWindSpeed = 5;
    private static final int defaultWindDir = 0;
    private static final int startTime = 0;
    
    private static int curWindDir = 0;

    private static Random random = null;
    
    private static float velocityindex[] = new float [181];

    public static void initGameData(int width, int height){

		//File velocitytable = new File("/home/don/Documents/school/cis3750/gameimage/velocitytable.txt");
		//File velocitytable = new File("D:\\Domenico\\Desktop\\cis3750_server\\velocitytable.txt");
		File velocitytable = new File("velocitytable.txt");
	    if(!velocitytable.exists()){
	        //Server.displayError("FILE ERROR DOES NOT EXIST BITCHES");
			System.out.println("Unable to open velocitytable.txt file to load wind angle velocity indexes...");
	    } else {
			try {
				BufferedReader filestream = new BufferedReader(new FileReader(velocitytable));
				String line =null;

				for(int i = 0; ((line = filestream.readLine())!= null);i++){
				    velocityindex[i] = Float.parseFloat(line.trim());
				    //System.out.println("VELOCITYINEX: "+velocityindex[i]);
				}   
			} catch (Exception ex) {
			   // Server.displayError("Error geting File Stream: "+ex.toString());
				System.out.println("Error reading data from velocitytable.txt: "+ex.toString());
			}
		}

		random = new Random(System.currentTimeMillis());

		xScale = width/5000.0;
		yScale = height/4000.0;
		//xScale = width/(float)mapWidth;
		//yScale = height/(float)mapHeight;
		System.out.println("xScale="+xScale+" yScale="+yScale);

        String islandString = null;
        islands = new String[shores.length];
        polygons = new Polygon[shores.length];
        scaledPolygons = new Polygon[shores.length];
        for(int i=0; i<shores.length; i++){
            islandString = String.valueOf(((shores[i].length-1) / 2)); //number of points in island
            polygons[i] = new Polygon();
            scaledPolygons[i] = new Polygon();
            for(int j=1; j<shores[i].length - 1; j=j+2){
                polygons[i].addPoint(shores[i][j], shores[i][j+1]);
                scaledPolygons[i].addPoint((int)(Math.ceil(shores[i][j]*xScale)), (int)(Math.ceil(shores[i][j+1]*yScale)));
                islandString = islandString+":"+shores[i][j]+":"+shores[i][j+1];
            }
            islands[i] = islandString;
            System.out.println("islands["+i+"]="+islands[i]);
        }
    }

    /*public static Polygon[] getScaledPolygons(int width, int height){
        double xScale = width/5000.0;
        double yScale = height/4000.0;
        System.out.println("xScale="+xScale+" yScale="+yScale);

        System.out.println("SHORE LENGTH: "+shores.length);
        Polygon scaledpolygons[] = new Polygon[shores.length];
        for(int i=0; i<shores.length; i++){
            System.out.println("scaled["+i+"]:");
            scaledpolygons[i] = new Polygon();
            for(int j=1; j<shores[i].length - 1; j=j+2){
                scaledpolygons[i].addPoint((int)(Math.ceil(shores[i][j]*xScale)), (int)(Math.ceil(shores[i][j+1]*yScale)));
                System.out.print("("+(int)(Math.ceil(shores[i][j]*xScale))+","+(int)(Math.ceil(shores[i][j+1]*yScale))+"), ");
            }
            System.out.println();
        }
        return scaledpolygons;
    }
    */
	
    public static void generateStartPositions(int numPlayers){
        startPos = new int[numPlayers][2];

        boolean pointGood = false;
        int x=0,y=0,w=0,h=0;
        for(int i=0; i<numPlayers; i++){
            Player player = ConnectionListener.playerList.get(i);
            //DC20111123//w = (int)getScaledWidth(player.getShipType());
            //DC20111123//h = (int)getScaledHeight(player.getShipType());
            w = (int)getWidth(player.getShipType()); //DC20111123//
            h = (int)getHeight(player.getShipType()); //DC20111123//
            //DC20111123//do {
            generateXY:do{ //DC20111123//
                pointGood = true; //DC20111123//
                //DC20111123//x = random.nextInt(mapWidth);
                //DC20111123//y = random.nextInt(mapHeight);

                //Get an (X,Y) 20m away from edges of map:
                x = random.nextInt(mapWidth - 40) + 20;
                y = random.nextInt(mapHeight - 40) + 20;

                //Check that the boat centered at (X,Y) doesn't collide with any islands:
                for(int j=0; j<polygons.length; j++){
                    //if(polygons[i].contains(x, y) == false){
                    /*if(polygons[i].intersects(x,y,w,h) == false){
                        pointGood = true;
                        break;
                    }*/ //DC20111123//

                    //If the ship intersects an island, need to regenerate (X,Y)
                    if(polygons[j].intersects(x-w, y-h, w*2, h*2)){ //DC20111123//
                        System.out.println("GameData.generateStartPositions(): ("+x+","+y+") intersects island "+j);
                        pointGood = false;
                        //DC20111123//break;
                        //Stop checking other islands and re-generate (X,Y)
                        continue generateXY;
                    }
                }
                System.out.println("GameData.generateStartPositions(): ("+x+","+y+") does not intersect any islands!");
                /*
                if(!pointGood){
                    continue;
                }
                */ //DC20111123//

                Polygon playerShip = new Polygon();
                playerShip.addPoint(x-w, y-h);
                playerShip.addPoint(x+w, y-h);
                playerShip.addPoint(x-w, y+h);
                playerShip.addPoint(x+w, y+h);
                int ox=0, oy=0, ow=0, oh=0;

                for(int j=0; j<i; j++){
                    Player otherplayer = ConnectionListener.playerList.get(j);
                    ox = otherplayer.getX();
                    oy = otherplayer.getY();
                    ow = (int)getWidth(otherplayer.getShipType());
                    oh = (int)getHeight(otherplayer.getShipType());

                    //If the ship intersects an island, need to regenerate (X,Y)
                    if(playerShip.intersects(ox-ow, oy-oh, ow*2, oh*2)){ //DC20111123//
                        System.out.println("GameData.generateStartPositions(): ("+x+","+y+") intersects ship "+j);
                        pointGood = false;
                        //DC20111123//break;
                        //Stop checking other ships and re-generate (X,Y)
                        continue generateXY;
                    }
                }
                System.out.println("GameData.generateStartPositions(): ("+x+","+y+") does not intersect any ships!");
            } while(!pointGood);

            //startPos[i][0] = x;
            //startPos[i][1] = y;
            System.out.println("Player"+i+" start pos: x="+x+" y="+y);
            player.setX(x);
            player.setY(y);
        }
    }
    public static void setCurWindDir(int winddir){
        curWindDir = winddir;
    }
    public static int getCurWindDir(){
        return curWindDir;
    }
	
	/* Getters */
	//Default Weather/Time Getters
    public static int getDefaultFog() {
        return defaultFog;
    }
    public static int getDefaultRain() {
        return defaultRain;
    }
    public static int getDefaultWindDir() {
        return defaultWindDir;
    }
    public static int getDefaultWindSpeed() {
        return defaultWindSpeed;
    }
    public static int getStartTime() {
        return startTime;
    }

	//Shore Data Getters
    public static String[] getIslands() {
        return islands;
    }
	public static int[][] getShores(){
		return shores;
	}
    public static Polygon[] getPolygons(){
        return polygons;
    }
    public static Polygon[] getScaledPolygons(){
        return scaledPolygons;
    }

    public static int getMapWidth(){
        return mapWidth;
    }
    public static int getMapHeight(){
        return mapHeight;
    }
    public static double getXScale(){
        return xScale;
    }
    public static double getYScale(){
        return yScale;
    }

    //Ship Data Getters
    public static float getMaxVelocity(int shipType){
            return shipDatas[shipType][0];
    }
    public static float getHitProb(int shipType){
            return shipDatas[shipType][1];
    }
    public static float getMaxHealth(int shipType){
            return shipDatas[shipType][2];
    }
    public static float getReloadSpeed(int shipType){
            return shipDatas[shipType][3];
    }
    public static float getWidth(int shipType){
        return shipDatas[shipType][4];
    }
    public static float getScaledWidth(int shipType){
        return ((int)(Math.ceil(shipDatas[shipType][4]) * 0.1));
    }
    public static float getHeight(int shipType){
        return shipDatas[shipType][5];
    }
    public static float getScaledHeight(int shipType){
        return ((int)(Math.ceil(shipDatas[shipType][5]) * 0.1));
    }

    public static int[][] getStartPos() {
        return startPos;
    }
    public static float getVelocityFactor(int angle){
        return velocityindex[angle];
    }
}
