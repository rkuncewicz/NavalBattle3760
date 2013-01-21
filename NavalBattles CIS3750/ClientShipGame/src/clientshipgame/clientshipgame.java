/*
 * TODO STILL:
 * 
 */

package clientshipgame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @authors Pat Torrens
 * Rob K.
 * Marc Bodmer
 */
public class clientshipgame{
    
    static int port = 5283;
    static String ip = "localhost";

    public boolean parseargs(String[] args){
    int arglength;

    arglength = args.length;
    if(arglength == 2)
    {
        port = Integer.parseInt(args[0]);
        ip = args[1];
    }
    else
    {
        System.out.println("Using localhost and port 5283.\n");
        return false;
    }
    return true;
    }

    public static void main(String[] args) throws IOException {
        
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
       /* System.out.println ("Please enter in an ip");
        String ip = br.readLine();
        System.out.println ("Please enter in the port.");
        String portStr = br.readLine();
        int port = Integer.parseInt(portStr);
         */
                
        int shiptype = 0;

        clientshipgame c = new clientshipgame();
        if (c.parseargs(args) == false){
            ip = "localhost";
            port = 5283;
        }
        

        try {
            
             System.out.println(ip + " " + port);
             Game g = new Game (ip, port, shiptype);
             /*
             * In main I assume we are just going to make a game instance and run it. We also have to add a ready
             * state and what not since this is not covered by this skeleton.
             
             */
        } catch (IOException ex) {
            Logger.getLogger(clientshipgame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
