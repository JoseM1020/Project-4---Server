/*
 * @author Jose Muniz, Sam Klarquist
 * @version 1.00
 */
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO: Write to file after user exits
public class Server implements Runnable {
    static int counter = 1; //aesthetics

    ServerSocket serverSocket = null;
    private Thread thread;
    Socket soc = null;
    Server server;
    static private String[] listOfLoggedInUsers;//this is a list that should be shared between all the Servers to say who's logged in and who is not
    static private String[] answers;
    String playersChoice;
    String player;
    String playerSuggestion;
    boolean playingGame = false;
    //gameList keeps track of all important information about a game
    static ArrayList<String> gameList;//game key:current question number:
    //LoginList keeps track of all important information about the user
    static ArrayList<String> LoginList;//Username:loginstatus(YES or NO):suggestion:choice
    static ArrayList<String> fileList = new ArrayList();//is the list from the file(updated using readUserList() method)
    static ArrayList<String> questionList = new ArrayList();
    String lowercaseLetters = "qwertyuiopasdfghjklzxcvbnm";
    String uppercaseLetters = "QWERTYUIOPASDFGHJKLZXCVBNM";
    String alphabet = uppercaseLetters + lowercaseLetters;

    private String gameKey;
    private boolean isLeader;
    static int NextScreenCounter;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(6000);


        LoginList = new ArrayList<String>();
        gameList = new ArrayList<String>();


        readUserList();


        //should contain in the first array the game token in the second contains the usernames

        //comment
        System.out.println("Listening on socket 5000");

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Connection accepted! Thread " + counter + " now starting..." );
                counter++;
            Server server = new Server();
            server.setSock(socket);
            Thread thread = new Thread(server);
            thread.start();
        }


    }

    public void setSock(Socket soc) {
        this.soc = soc;
    }


    public Server() {


    }

    @Override
    public void run() {
        BufferedReader inFromClient = null;
        PrintWriter outToClient = null;
        BufferedReader fileReader = null;
        PrintWriter fileWriter = null;
        String line;
        String fromClient = null;
        String[] parts = null;
        String[] clientParts = null;
        String[] databaseParts = null;
        ArrayList<String> fileList = new ArrayList(); //when creating new user, add user info to list

        boolean isLoggedIn = false;

        Pattern username = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Pattern password = Pattern.compile("[#&$*]");
        Pattern passwordUp = Pattern.compile("[A-Z]");
        Pattern passwordDig = Pattern.compile("[0-9]");
        try {
            inFromClient = new BufferedReader(new InputStreamReader(soc.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            outToClient = new PrintWriter(soc.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }


        while (true) {//change this to a variable latter but need to make it to infinite loop for now to test things

            //change the file path to check on your side
      /*  try {
            fileReader = new BufferedReader(new FileReader(filePath));
        } catch (FileNotFoundException e) {
            System.out.println("ERROR 404: FILE NOT FOUND!");
        }

        try {
            while ((line = fileReader.readLine()) != null) { //displaying registered users and adding to array
                fileList.add(line);
                parts = line.split(":");
                System.out.println("Added user: " + parts[0]);
                System.out.println("Read in user: " + line);
                //method to say user already logged in/registered
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }*/


            try {
                fromClient = inFromClient.readLine();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (fromClient != null) {
                System.out.println(fromClient);
            }
            clientParts = fromClient.split("--");
            //message format check
            if (clientParts[0].equals("CREATENEWUSER")) { //parts are being formatted correctly


                for (int i = 0; i < fileList.size(); i++) {
                    parts = fileList.get(i).split(":");
                    if (searchForPlayer(clientParts[1]) == -1) {
                        outToClient.println("RESPONSE--CREATENEWUSER--USERALREADYEXISTS--");
                        System.out.println("OUT TO CLIENT:RESPONSE--CREATENEWUSER--USERALREADYEXISTS--");

                    }

                }


                Matcher user = username.matcher(clientParts[1]);
                Matcher pwChar = username.matcher(clientParts[2]);
                Matcher pwSymb = password.matcher(clientParts[2]);
                Matcher pwUp = passwordUp.matcher(clientParts[2]);
                Matcher pwDig = passwordDig.matcher(clientParts[2]);
                boolean u = user.find();
                boolean p1 = pwChar.find();
                boolean p2 = pwSymb.find();
                boolean p3 = pwUp.find();
                boolean p4 = pwDig.find();
                boolean pwCreds;
                if (p1 && p2 && p3 && p4) {
                    pwCreds = true;
                } else {
                    pwCreds = false;
                }

                if (searchForPlayer(clientParts[1]) != -1 || clientParts[1].length() >= 10 || u) {
                    outToClient.println("RESPONSE--CREATENEWUSER--INVALIDUSERNAME--");
                    System.out.println("OUT TO CLIENT:RESPONSE--CREATENEWUSER--INVALIDUSERNAME--");
                } else if (searchForPassword(clientParts[2]) != -1 || clientParts[2].length() >= 10 || pwCreds) {
                    outToClient.println("RESPONSE--CREATENEWUSER--INVALIDPASSWORD--");
                    System.out.println("OUT TO CLIENT:RESPONSE--CREATENEWUSER--INVALIDPASSWORD--");
                } else if(clientParts[0].equals("")||clientParts[1].equals("") ||
                        clientParts[2].equals("")|| clientParts[2]==null){
                    outToClient.println("RESPONSE--CREATENEWUSER--INVALIDMESSAGEFORMAT");
                } else {
                    System.out.println(clientParts[1]);
                    outToClient.println("RESPONSE--CREATENEWUSER--SUCCESS--");
                    System.out.println("OUT TO CLIENT:RESPONSE--CREATENEWUSER--SUCCESS--");

                    createNewUser(clientParts[1], clientParts[2]);


                }
/*
            if (clientParts[1].equals("") || clientParts[1].length() >= 10 || u) {
                outToClient.println("RESPONSE--CREATENEWUSER--INVALIDUSERNAME--");
                System.out.println("OUT TO CLIENT:RESPONSE--CREATENEWUSER--INVALIDUSERNAME--");
            } else if (clientParts[2].equals("") || clientParts[2].length() >= 10 || pwCreds) {
                outToClient.println("RESPONSE--CREATENEWUSER--INVALIDPASSWORD--");
                System.out.println("OUT TO CLIENT:RESPONSE--CREATENEWUSER--INVALIDPASSWORD--");
            } else {
                System.out.println(clientParts[1]);
                outToClient.println("RESPONSE--CREATENEWUSER--SUCCESS--");
                System.out.println("OUT TO CLIENT:RESPONSE--CREATENEWUSER--SUCCESS--");
                fileWriter.println(clientParts[1] + ":" + clientParts[2] + ":0:0:0");
                fileList.add(clientParts[1] + ":" + clientParts[2] + ":0:0:0");
            }*/

            }

            clientParts = fromClient.split("--");
            int counterU = 0; //increment by one if found in list and stores value where found for countInd
            int counterP = 0;
            if (clientParts[0].equals("LOGIN")) {
                String tempPassword = "";
                for (int i = 0; i < fileList.size(); i++) {
                    databaseParts = fileList.get(i).split(":");
                    System.out.println(databaseParts[0]);
                    if (clientParts[1].equals(databaseParts[0])) {
                        counterU++;
                        tempPassword = databaseParts[1];

                    }
                }
                if (databaseParts != null && clientParts[2].equals(tempPassword)) {
                    counterP++;
                }

                if (searcher(clientParts[1], 0) == -1 || fileList == null) {
                    outToClient.println("RESPONSE--LOGIN--UNKNOWNUSER--");
                    System.out.println("OUT TO CLIENT:RESPONSE--LOGIN--UNKNOWNUSER--");

                } else if (searcher(clientParts[2], 1) == -1) {
                    outToClient.println("RESPONSE--LOGIN--INVALIDPASSWORD--");
                    System.out.println("OUT TO CLIENT:RESPONSE--LOGIN--INVALIDPASSWORD--");

                } else if (findPart(searcher(clientParts[1], 0), 5).equals("YES")) {
                    outToClient.println("RESPONSE--LOGIN--USERALREADYLOGGEDIN--");
                } else if(clientParts[0].equals("")||clientParts[1].equals("") ||
                        clientParts[2].equals("")|| clientParts[2]==null){
                    outToClient.println("RESPONSE--LOGIN--INVALIDMESSAGEFORMAT");

                } else {


                    System.out.println(clientParts[1]);
                    System.out.println(searcher(clientParts[1], 0));
                    replacePart(searcher(clientParts[1], 0), 5, "YES");
                    String cook = createSesionCookie();
                    System.out.println("OUT TO CLIENT:RESPONSE--LOGIN--SUCCESS--" + cook);
                    replacePart(searcher(clientParts[1], 0), 10, cook);
                    outToClient.println("RESPONSE--LOGIN--SUCCESS--" + cook);
                    //TODO: Check if user logged in
                }
                //TODO: check if user already logged in (before success)
            }

            if (clientParts[0].equals("STARTNEWGAME")) {
                if (!findPart(searcher(clientParts[1], 10), 5).equals("YES")) {

                    outToClient.println("RESPONSE--STARTNEWGAME--USERNOTLOGGEDIN--");
                } else if (alreadyPlaying(clientParts[1])) {//need something to check if user already playing a game
                    outToClient.println("RESPONSE--STARTNEWGAME--FAILURE--"); //set a string in login list for playing player
                } else {
                    String key = addGameKey(clientParts[1]);
                    outToClient.println("RESPONSE--STARTNEWGAME--SUCCESS--" + key);
                    gameKey=key;
                    isLeader=true;
                    //if success, set player
                    //set private string to key


                }
            }

            if (clientParts[0].equals("JOINGAME")) {

                if (!isLogged(clientParts[1])) {
                    outToClient.println("RESPONSE--JOINGAME--USERNOTLOGGEDIN");
                } else if (searchGameKey(clientParts[2]) == -1) {//need to add
                    outToClient.println("RESPONSE--JOINGAME--GAMEKEYNOTFOUND");
                } else if (alreadyPlaying(clientParts[1])) {
                    outToClient.println("RESPONSE--JOINGAME--FAILURE");
                } else {//do something to add user to gameList
                    outToClient.println("RESPONSE--JOINGAME--SUCCESS");
                }
                //if success, set string in login for playing game to yes

            }

            if (clientParts[0].equals("ALLPARTICIPANTSHAVEJOINED")) {

                if(false) { //look over
                    outToClient.println("RESPONSE--ALLPARTICIPANTSHAVEJOINED--USERNOTLOGGEDIN");
                }
                //if logged in and have same game key for 2 peeps
                else if(!gameKey.equals(clientParts[2])) {
                    outToClient.println("RESPONSE--ALLPARTICIPANTSHAVEJOINED--INVALIDGAMETOKEN");
                }
                //not equal game leader gametoken
                else if(!isLeader) {
                    outToClient.println("RESPONSE--ALLPARTICIPANTSHAVEJOINED--USERNOTGAMELEADER");
                } else{

                }


            }


            if (clientParts[0].equals("PLAYERSUGGESTION")) {

                if(!isLoggedIn){ //check hashcode
                    outToClient.println("RESPONSE--PLAYERSUGGESTION--USERNOTLOGGEDIN");
                } else if(false) { //game key not equal to current (from login method or as leader)
                    outToClient.println("RESPONSE--PLAYERSUGGESTION--INVALIDGAMETOKEN");
                } else if(false){ //unexpected message type (look over)

                } else if(clientParts[0].equals("")||clientParts[1].equals("") ||
                        clientParts[2].equals("")|| clientParts[2]==null){
                    {outToClient.println("RESPONSE--PLAYERSUGGESTION--INVALIDMESSAGEFORMAT");}
                } else{
                    //store suggestions in Loginlist and run rowsearcher method
                    outToClient.println("ROUNDOPTIONS--"); //write all suggestions
                    //make method for this
                }


            }

            if (clientParts[0].equals("PLAYERCHOICE")) {

                if (clientParts[1].equals("")) {//username is logged in and in the database(need to create a method for it)

                }

            }

            //this should if the logout comand is given check wether the user was logged in or not then spit back the appropriate response
            if (clientParts[0].equals("LOGOUT")) {
                if (isLoggedIn == true) {
                    isLoggedIn = false;
                    outToClient.println("RESPONSE--LOGOUT--SUCCESS--");
                } else {
                    outToClient.println("RESPONSE--LOGOUT--USERNOTLOGGEDIN--");
                }

            }


        }

      /*  try {
            inFromClient.close();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }


    static public void readQuestions() throws FileNotFoundException, IOException {

        String filePath = new String("C:\\Users\\JoseM\\Desktop\\cs180\\Projects\\" +
                "Project-4---Server\\database\\questions.txt");
        BufferedReader fileReader = new BufferedReader(new FileReader(filePath));

        String line;
        String quest;
        String[] parts = null;
        while ((line = fileReader.readLine()) != null) { //displaying registered users and adding to array

            quest = line.replace(":", "--");
            questionList.add(quest);


        }
        fileReader.close();

    }

    static public void readUserList() throws FileNotFoundException, IOException {
        String filePath = new String("C:\\Users\\JoseM\\Desktop\\cs180\\Projects" +
                "\\Project-4---Server\\database\\database.txt");
        BufferedReader fileReader = new BufferedReader(new FileReader(filePath));
        LoginList = new ArrayList();
        String line;
        String[] parts = null;

        while ((line = fileReader.readLine()) != null) { //displaying registered users and adding to array
            fileList.add(line);
            System.out.println(line);
            parts = line.split(":");

            LoginList.add((line + ":NO:suggestion:choice: :NOPE:hashname"));//Username:loginstatus(YES or NO):suggestion:choice:meesage:playing(gameKey or NOPE):hashname
            //possibly add suggestion indicators EX. suggestion:1, first suggestion, suggestion:2, second suggestion

            System.out.println(LoginList);


            // System.out.println("Added user: " + parts[0]);

            //method to say user already logged in/registered
        }
        fileReader.close();

    }

    //returns the string with the proper game word might require int to keep track of which question needs getting
    public String gameWord(int num) {

        return "NEWGAMEWORD--" + questionList.get(num);

    }

    //adds a new game
    public void gameTokenAdder(String token, String leader) {

        gameList.add(token + ":" + leader);

    }

    //this will return back a number >=0 and if it doesn't then it isn't on the list
    public int searchGameKey(String gameKey) {
        String[] part;
        for (int i = 0; i < gameList.size(); i++) {
            part = gameList.get(i).split(":");

            if (part[0].equals(gameKey)) {
                return i;
            }
        }

        return -1;


    }

    public String addGameKey(String hashName) {

        String gameKey = createGameToken();
        replacePart(searchForPlayer(hashName), 9, gameKey + ":");

        gameList.add(gameKey + ":");
        return gameKey;
    }

    public void addGameUser(String hashName) {

    }

    public int searchForPlayer(String hashName) {
        String[] part;
        for (int i = 0; i < LoginList.size(); i++) {
            part = LoginList.get(i).split(":");

            if (part[10].equals(hashName)) {
                return i;
            }
        }

        return -1;


    }

    public int searchForPassword(String password) {
        String[] part;
        for (int i = 0; i < LoginList.size(); i++) {
            part = LoginList.get(i).split(":");

            if (part[1].equals(password)) {
                return i;
            }
        }

        return -1;


    }

    //this can be used to search for anything on the game list (0): username, (1) password, (2)
    public int searcher(String thing, int num) {
        String[] part;
        for (int i = 0; i < LoginList.size(); i++) {
            part = LoginList.get(i).split(":");

            if (part[num].equals(thing)) {
                return i;
            }
        }

        return -1;


    }

    //method in progress
    public String gitter(String thing, int num) {
        String[] part;
        for (int i = 0; i < LoginList.size(); i++) {
            part = LoginList.get(i).split(":");

            if (part[num].equals(thing)) {
                return part[i];
            }
        }

        return "";


    }

    //num is the specific list and thing specifies where in the string the thing is being replaced
    public void replacePart(int num, int thing, String replacer) {

        System.out.println(LoginList);
        String temp = LoginList.get(num);
        System.out.println(temp);
        String[] part = temp.split(":");

        temp = "";
        for (int i = 0; i < part.length; i++) {
            if (i != thing) {
                temp += part[i];
            } else {
                //  System.out.print(replacer);
                temp += replacer;
            }
            if (i < part.length - 1) {
                temp += ":";
            }
        }

        LoginList.set(num, temp);

    }

    public String findPart(int num, int thing) {

        System.out.println(LoginList);
        String temp = LoginList.get(num);
        System.out.println(temp);
        String[] part = temp.split(":");

        temp = "";

        temp = part[thing];

        return temp;

    }

    public boolean isLogged(String a) {

        int num = searchGameKey(a);

        if (num == -1) {
            String[] parts = LoginList.get(num).split(":");


            if (parts[5].equals("YES")) {
                return true;
            }
        }

        return false;
    }

    public void Fooled(String gameKey, String players) {


    }

    public void Fooler(String gameKey, String players) {


    }

    public String RoundOptions() {


        return "";
    }

    public boolean alreadyPlaying(String hashname) {
        int num = searcher(hashname, 10);
        if (num != -1) {

            if (findPart(num, 9).equals("NOPE")) {
                return false;
            } else {
                return true;
            }

        }

        return false;
    }

    public String createSesionCookie() {
        Random r = new Random();
        String sessionCookie = "";
        for (int i = 0; i < 10; i++) {
            char temp = alphabet.charAt(r.nextInt(alphabet.length()));
            sessionCookie = sessionCookie + temp;
        }
        return sessionCookie;

    }

    public String createGameToken() {
        Random r = new Random();
        String gameToken = "";
        for (int i = 0; i < 3; i++) {
            char temp = lowercaseLetters.charAt(r.nextInt(lowercaseLetters.length()));
            gameToken = gameToken + temp;

        }
        return gameToken;
    }

    public void createNewUser(String userName, String password) {
        LoginList.add((userName + ":" + password + ":0:0:0:NO:suggestion:choice: :NOPE:hashname"));
    }

    public boolean nextScreenChecker(String input, String gameKey, String hashCode, int indexOfPart) throws InterruptedException { //adjust to check for people in each game
        while (counter != LoginList.size()) { //make it so that after so many tries, stop working.
            Thread.sleep(2000); //check over
            for (int i = 0; i < LoginList.size(); i++) {
                String temp = LoginList.get(i);
                String[] parts = (LoginList.get(i)).split(":");
                if (parts[indexOfPart].equals(input) && parts[9].equals(gameKey)) { //checks for players only in one game
                    NextScreenCounter++;
                }
            }
        }
        return true;
    }

    public int[] loginRowSearch(String gameKey, int index) { //checks for all instances in Loginlist of players in one game
        int counter=0;
        for (int i = 0; i < LoginList.size(); i++) {
            String temp = LoginList.get(i);
            String[] parts = (LoginList.get(i)).split(":");
            if (parts[index].equals(gameKey)) { //checks for players only in one game
                counter++;
            }

        }
        int[] tempArray = new int[counter];
        counter = 0;
        for (int i = 0; i < LoginList.size(); i++) {
            String temp = LoginList.get(i);
            String[] parts = (LoginList.get(i)).split(":");
            if (parts[index].equals(gameKey)) { //checks for players only in one game
                tempArray[counter] = i;
                counter++;
            }

        }

        return tempArray;
    }

    public String RoundOptions(String[] array){
        String RoundOptions = "ROUNDOPTIONS";
        for (String element : array){
            RoundOptions+="--"+element;
        }
        return RoundOptions;
    }
}
