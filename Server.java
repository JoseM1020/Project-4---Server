/*
 * @author Sam Klarquist, Jose Muniz
 * @version 1.0.0.1
 */
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server implements Runnable {
    Socket soc = null;
    //gameList keeps track of all important information about a game
    static ArrayList<String> gameList;//game key:current question number:
    //LoginList keeps track of all important information about the user
    static ArrayList<String> LoginList;//there are eleven parts
    static ArrayList<String> fileList = new ArrayList();//is the list from the file(updated using readUserList() method)
    static ArrayList<String> questionList = new ArrayList();
    String lowercaseLetters = "qwertyuiopasdfghjklzxcvbnm";
    String uppercaseLetters = "QWERTYUIOPASDFGHJKLZXCVBNM";
    String alphabet = uppercaseLetters + lowercaseLetters;
    boolean leaderIsWaiting = false;//just do it by using boolean variables and making them their own if statements
    boolean isGameLeader = false;
    int currentQuestion = 0;
    String gameName;
    String[] suggestionList;
    String[] choiceList;
    boolean waiting = false;
    String waitingKey;
    boolean isWaitingForStart = false;
    ArrayList<Integer> participantList;
    boolean waiter = false;
    static Pattern username = Pattern.compile("[^a-z0-9_]", Pattern.CASE_INSENSITIVE);
    static Pattern password = Pattern.compile("[^a-zA-Z0-9#&$*]+");
    static Pattern passwordUp = Pattern.compile("[A-Z]");
    static Pattern passwordDig = Pattern.compile("[0-9]");
    static ArrayList<String> temp = new ArrayList<>();

    static int threadCount = 1;
    static BufferedReader fileReader = null;

    public static void main(String[] args) throws IOException {
        try{
            String filePath = "C:\\Users\\JoseM\\Desktop\\cs180\\Projects\\Project-4---Server\\" +
                    "database\\database.txt";
            fileReader  = new BufferedReader(new FileReader(filePath));
        }catch (FileNotFoundException e){
            System.out.println("ERROR 404! FILE NOT FOUND!");
        }

        String line;
        String[] parts;
        try {
            while ((line = fileReader.readLine()) != null) { //displaying registered users and adding to array
                temp.add(line);
                parts = line.split(":");
                System.out.println("Added user: " + parts[0]);
                System.out.println("Read in user: " + line);
                //method to say user already logged in/registered
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

        ServerSocket serverSocket = new ServerSocket(6000);


        LoginList = new ArrayList<>();
        gameList = new ArrayList<>();


        readUserList();
        readQuestions();

        //should contain in the first array the game token in the second contains the usernames

        System.out.println("Listening on socket 6000");

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Connection accepted! Thread " + threadCount + "now starting...");
            threadCount++;
            Server server = new Server();
            server.setSock(socket);
            Thread thread = new Thread(server);
            thread.start();

        }


    }

    public void setSock(Socket soc) {
        this.soc = soc;
    }


    @Override
    public void run() {
        BufferedReader inFromClient = null;
        PrintWriter outToClient = null;
        String fromClient = null;
        String[] clientParts;
        String[] databaseParts = null;

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
        boolean finish = true;

        while (finish) {//change this to a variable latter but need to make it to infinite loop for now to test things


            String filePath = new String("C:\\Users\\Sam Klarquist\\Desktop\\" +
                    "file1.sql");

            while (leaderIsWaiting) {
                int[] thePlayers = loginRowSearch(waitingKey, 9);

                for (int i = 0; i < thePlayers.length; i++) {
                    int counts = 0;
                    for (int j = 0; j < participantList.size(); j++) {
                        int num = participantList.get(j);
                        if (num == thePlayers[i]) {
                            counts++;
                        }
                    }
                    if (counts == 0) {
                        outToClient.println("NEWPARTICIPANT--" + findPart(thePlayers[i], 0) + "--" + findPart(thePlayers[i], 2));
                        leaderIsWaiting = false;
                    }
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            //for joingame people
            while (isWaitingForStart) {
                String gameK = "";
                int[] arrayer = loginRowSearch(waitingKey, 9);

                for (int i = 0; i < arrayer.length; i++) {
                    if (findPart(arrayer[i], 11).equals("START")) {


                        isWaitingForStart = false;
                        getSuggestionsList(waitingKey);

                        if(currentQuestion<questionList.size()){
                            outToClient.println(   newGameWord());}
                        else{
                            outToClient.println("GAMEOVER");

                        }
                        //  outToClient.println( roundOptions(suggestionList));
                    }

                }


            }
            //waiting for all suggestions to come in
            while (waiting) {

                int counts = 0;
                getSuggestionsList(waitingKey);


                for (int i = 0; i < suggestionList.length; i++) {

                    if (suggestionList[i].equals("suggestion")) {
                        counts++;
                    }

                }

                if (counts > 0) {


                } else {
                    String[] randomizer = new String[suggestionList.length + 1];
                    for (int i = 0; i < suggestionList.length; i++) {

                        randomizer[i] = suggestionList[i];
                    }
                    String[] ans = questionList.get((currentQuestion - 1)).split("--");
                    randomizer[suggestionList.length] = ans[1];

                    randomizer = shuffler(randomizer);

                    outToClient.println(roundOptions(randomizer));
                    waiting = false;
                }
            }


            while (waiter) {

                int counts = 0;
                getChoiceList(waitingKey); //System.out.println(suggestionList.length[0]]);


                for (int i = 0; i < choiceList.length; i++) {

                    if (choiceList[i].equals("choice")) {
                        counts++;
                    }

                }

                if (counts > 0) {


                } else {

                    int[] group = loginRowSearch(waitingKey, 9);

                    String[] players = new String[group.length];

                    for (int i = 0; i < group.length; i++) {
                        players[i] = findPart(group[i], 10);
                    }


                    Fooled(waitingKey);
                    boolean gameOver=false;


                    outToClient.println(RoundResults());
                    if(currentQuestion<questionList.size()){
                        outToClient.println(   newGameWord());}
                    else{
                        outToClient.println("GAMEOVER");
                        gameOver=true;
                    }
                    if (isGameLeader) {
                        for (int g = 0; g < LoginList.size(); g++) {
                            String[] parser = LoginList.get(g).split(":");
                            for (int p = 0; p < group.length; p++) {

                                String temper = fileList.get(group[p]);
                                String[] filer = temper.split(":");
                                if (filer[0].equals(parser[0])) {
                                    fileList.set(group[p], parser[0] + ":" + parser[1] + ":" + parser[2] + ":" + parser[3] + ":" + parser[4]);
                                }


                            }


                        }

                        try {
                            writeToFile();
                        } catch (IOException ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                    //       currentQuestion++;

                    for (int i = 0; i < group.length; i++) {
                        replacePart(group[i], 6, "suggestion");
                        replacePart(group[i], 7, "choice");
                        replacePart(group[i], 8, "message");
                        replacePart(group[i], 11, "WAIT");

                        if(gameOver==true){
                            replacePart(group[i],9,"NOPE");
                            finish=false;

                        }
                    }
                    waiter = false;


                }


         /*
          try {
               Thread.sleep(2000);
           } catch (InterruptedException ex) {
               Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
           }*/

            }


            try {
                fromClient = inFromClient.readLine();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }

            clientParts = fromClient.split("--");

            //message format check
            if (clientParts[0].equals("CREATENEWUSER")) { //parts are being formatted correctly
                boolean userAlreadyExists = false;
                if (clientParts.length == 3) {

                    if ((searcher(clientParts[1], 0) != -1)) {
                        userAlreadyExists = true;

                    }




                    Matcher user = username.matcher(clientParts[1]);
                    Matcher pwUp = passwordUp.matcher(clientParts[2]);
                    Matcher pwDig = passwordDig.matcher(clientParts[2]);
                    Matcher pwSymb = password.matcher(clientParts[2]);
                    boolean pwCreds = pwSymb.find();
                    boolean u = user.find();
                    boolean pU = pwUp.find();
                    boolean pN = pwDig.find();

                    if (clientParts[1].equals("") || clientParts[1].length() >= 10 || u) {
                        outToClient.println("RESPONSE--CREATENEWUSER--INVALIDUSERNAME--");

                    } else if (searcher(clientParts[2], 1) != -1 || clientParts[2].length() >= 10 || !pU || !pN || pwCreds || clientParts[1].equals("")) {
                        outToClient.println("RESPONSE--CREATENEWUSER--INVALIDPASSWORD--");

                    } else if (userAlreadyExists) {
                        outToClient.println("RESPONSE--CREATENEWUSER--USERALREADYEXISTS--");
                    } else {

                        outToClient.println("RESPONSE--CREATENEWUSER--SUCCESS--");

                        createNewUser(clientParts[1], clientParts[2]);


                    }

                } else {
                    outToClient.println("RESPONSE--CREATENEWUSER--INVALIDMESSAGEFORMAT--");
                }


            }

            clientParts = fromClient.split("--");
            int counterU = 0; //increment by one if found in list and stores value where found for countInd
            int counterP = 0;
            if (clientParts[0].equals("LOGIN")) {
                String tempPassword = "";
                for (int i = 0; i < fileList.size(); i++) {
                    databaseParts = fileList.get(i).split(":");

                    if (clientParts[1].equals(databaseParts[0])) {
                        counterU++;
                        tempPassword = databaseParts[1];

                    }
                }
                if (databaseParts != null && clientParts[2].equals(tempPassword)) {
                    counterP++;
                }
                if (clientParts.length == 3 && !(clientParts[1] == null) && !(clientParts[2] == null)) {

                    int place = searcher(clientParts[1], 0);//this is the place where the user can be found

                    if (place == -1 || fileList == null) {
                        outToClient.println("RESPONSE--LOGIN--UNKNOWNUSER--");


                    } else if (!findPart(place, 1).equals(clientParts[2])) {
                        outToClient.println("RESPONSE--LOGIN--INVALIDPASSWORD--");


                    } else if (findPart(place, 5).equals("YES")) {
                        outToClient.println("RESPONSE--LOGIN--USERALREADYLOGGEDIN--");
                    } else {


                        replacePart(searcher(clientParts[1], 0), 5, "YES");
                        String cook = createSesionCookie();

                        replacePart(searcher(clientParts[1], 0), 10, cook);
                        outToClient.println("RESPONSE--LOGIN--SUCCESS--" + cook);
                        //TODO: Check if user logged in
                    }
                } else {
                    outToClient.println("RESPONSE--LOGIN--INVALIDMESSAGEFORMAT--");

                }


//TODO: check if user already logged in (before success)
            }

            if (clientParts[0].equals("STARTNEWGAME")) {
                int place = searcher(clientParts[1], 10);
                if (!findPart(searcher(clientParts[1], 10), 5).equals("YES")) {

                    outToClient.println("RESPONSE--STARTNEWGAME--USERNOTLOGGEDIN--");
                } else if (alreadyPlaying(clientParts[1])) {//need something to check if user already playing a game
                    outToClient.println("RESPONSE--STARTNEWGAME--FAILURE--");
                } else {
                    String key = addGameKey(clientParts[1]);
                    outToClient.println("RESPONSE--STARTNEWGAME--SUCCESS--" + key);
                    leaderIsWaiting = true;
                    isGameLeader = true;
                    gameName = key;
                    //while loop to keep track of new participants
                    waitingKey = key;
                    participantList = new ArrayList<Integer>();
                    participantList.add(searcher(clientParts[1], 10));
                }
            }

            if (clientParts[0].equals("JOINGAME")) {
                int place = searcher(clientParts[1], 10);
                if (!findPart(place, 5).equals("YES")) {
                    outToClient.println("RESPONSE--JOINGAME--USERNOTLOGGEDIN");
                } else if (searchGameKey(clientParts[2]) == -1) {//need to add
                    outToClient.println("RESPONSE--JOINGAME--GAMEKEYNOTFOUND");
                } else if (alreadyPlaying(clientParts[1])) {
                    outToClient.println("RESPONSE--JOINGAME--FAILURE");
                } else {//do something to add user to gameList
                    outToClient.println("RESPONSE--JOINGAME--SUCCESS--" + clientParts[2]);
                    replacePart(place, 9, clientParts[2]);
                    waitingKey = clientParts[2];
                    isWaitingForStart = true;

                }

            }

            if (clientParts[0].equals("ALLPARTICIPANTSHAVEJOINED")) {
                int place = searcher(clientParts[1], 10);

                if (!findPart(place, 5).equals("YES")) {
                    outToClient.println("RESPONSE--ALLPARTICIPANTSHAVEJOINED--USERNOTLOGGEDIN");
                } else if (searcher(clientParts[2], 9) == -1) {
                    outToClient.println("RESPONSE--ALLPARTICIPANTSHAVEJOINED--INVALIDGAMETOKEN");
                } else if (!isGameLeader && !gameName.equals(clientParts[2])) {
                    outToClient.println("RESPONSE--ALLPARTICIPANTSHAVEJOINED--USERNOTGAMELEADER");
                } else {
                    replacePart(place, 11, "START");
                    outToClient.println(newGameWord());
                    leaderIsWaiting = false;


                }


            }


            if (clientParts[0].equals("PLAYERSUGGESTION")) {
                int place = searcher(clientParts[1], 10);

                if (clientParts.length == 4) {
                    if (!findPart(place, 5).equals("YES")) { //check hashcode
                        outToClient.println("RESPONSE--PLAYERSUGGESTION--USERNOTLOGGEDIN");
                    } else if (searchGameKey(clientParts[2]) == -1) { //game key not equal to current (from login method or as leader)
                        outToClient.println("RESPONSE--PLAYERSUGGESTION--INVALIDGAMETOKEN");
                    } else if (false) { //unexpected message type (look over)

                    } else if (clientParts[0].equals("") || clientParts[1].equals("") ||
                            clientParts[2].equals("") || clientParts[2] == null) {
                        {
                            outToClient.println("RESPONSE--PLAYERSUGGESTION--INVALIDMESSAGEFORMAT");
                        }
                    } else {
                        //store suggestions in Loginlist and run rowsearcher method
                        waiting = true;

                        replacePart(place, 6, clientParts[3]);
                        //make method for this
                    }
                } else {

                    outToClient.println("RESPONSE--PLAYERSUGGESTION--INVALIDMESSAGEFORMAT");
                }

            }

            if (clientParts[0].equals("PLAYERCHOICE")) {


                if (clientParts.length == 4) {
                    int place = searcher(clientParts[1], 10);//this is the place where the user can be found

                    if (place == -1 || fileList == null) {
                        outToClient.println("RESPONSE--PLAYERCHOICE--UNKNOWNUSER--");


                    } else if (!findPart(place, 9).equals(clientParts[2])) {
                        outToClient.println("RESPONSE--PLAYERCHOICE--INVALIDGAMETOKEN--");


                    }
                    //do it by making variable that does it array
                    else if (findPart(place, 6).equals(clientParts[3])) {
                        outToClient.println("RESPONSE--PLAYERCHOICE--UNEXPECTEDMESSAGETYPE--");

                    } else {


                        outToClient.println("RESPONSE--PLAYERCHOICE--SUCCESS--");


                        replacePart(place, 7, clientParts[3]);
                        waiter = true;


                    }


                } else {
                    outToClient.println("RESPONSE--PLAYERCHOICE--INVALIDMESSAGEFORMAT--");
                }
            }

            //this should if the logout comand is given check wether the user was logged in or not then spit back the appropriate response
            if (clientParts[0].equals("LOGOUT")) {
                int place = searcher(clientParts[1], 10);

                if (findPart(place, 5).equals("YES")) {
                    replacePart(place, 5, "NO");

                    outToClient.println("RESPONSE--LOGOUT--SUCCESS--");
                    try {
                        writeToFile();
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    finish = false;
                } else {
                    outToClient.println("RESPONSE--LOGOUT--USERNOTLOGGEDIN--");
                }

            }


        }

    }


    static public void readQuestions() throws FileNotFoundException, IOException {

        String filePath = new String("C:\\Users\\JoseM\\Desktop\\cs180\\" +
                "Projects\\Project-4---Server\\database\\questions.txt");
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
        String filePath = new String("C:\\Users\\JoseM\\Desktop\\cs180\\Projects\\" +
                "Project-4---Server\\database\\database.txt");
        BufferedReader fileReader = new BufferedReader(new FileReader(filePath));
        LoginList = new ArrayList();
        String line;
        String[] parts = null;

        while ((line = fileReader.readLine()) != null) { //displaying registered users and adding to array
            fileList.add(line);
            parts = line.split(":");

            LoginList.add((line + ":NO:suggestion:choice:messagetouser:NOPE:hashname:WAIT"));//Username:loginstatus(YES or NO):suggestion:choice:meesage:playing(gameKey or NOPE):hashname:wait/start


            // System.out.println("Added user: " + parts[0]);

            //method to say user already logged in/registered
        }
        fileReader.close();

    }

    //returns the string with the proper game word might require int to keep track of which question needs getting
    public String newGameWord() {

        return "NEWGAMEWORD--" + questionList.get(currentQuestion++);

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
        replacePart(searchForPlayer(hashName), 9, gameKey);

        gameList.add(gameKey + ":");
        return gameKey;
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


        String temp = LoginList.get(num);

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

        String temp = LoginList.get(num);
        String[] part = temp.split(":");

        temp = part[thing];

        return temp;

    }


    public void Fooled(String gameKey) {
        int[] group = loginRowSearch(waitingKey, 9);
        getSuggestionsList(gameKey);
        String[] userList = getAUserList(gameKey, 0);
        String[] chossen = getAUserList(gameKey, 7);
        String[] suggested = getAUserList(gameKey, 6);

        int[] points = new int[userList.length];
        int[] fooled = new int[userList.length];
        int[] fooledby = new int[userList.length];
        String[] message = new String[userList.length];


        for (int k = 0; k < userList.length; k++) {
            int plave = searcher(userList[k], 0);
            points[k] = Integer.parseInt(findPart(plave, 2));
            fooled[k] = Integer.parseInt(findPart(plave, 3));
            fooledby[k] = Integer.parseInt(findPart(plave, 4));
            message[k] = "";

        }
        int placeholder = currentQuestion - 1;
        String[] ans = questionList.get(placeholder).split("--");

        for (int i = 0; i < userList.length; i++) {
            if (chossen[i].equals(ans[1])) {
                points[i] += 10;
                message[i] += "you got it right.";
            }

            for (int j = 0; j < suggested.length; j++) {
                if (chossen[i].equals(suggested[j]) && !chossen[i].equals(ans[1])) {
                    points[j] += 5;
                    fooled[j]++;
                    fooledby[i]++;
                    message[i] += " you were fooled by " + userList[j];
                    message[j] += " you fooled " + userList[i];
                }
            }


        }

        for (int k = 0; k < group.length; k++) {
            replacePart(group[k], 2, Integer.toString(points[k]));
            replacePart(group[k], 3, Integer.toString(fooled[k]));
            replacePart(group[k], 4, Integer.toString(fooledby[k]));
            replacePart(group[k], 8, message[k]);
        }


    }

    public String[] getSuggestionsList(String gameKey) {

        int[] listPlayers = loginRowSearch(gameKey, 9);

        suggestionList = new String[listPlayers.length];
        for (int i = 0; i < suggestionList.length; i++) {
            suggestionList[i] = findPart(listPlayers[i], 6);
        }

        return suggestionList;
    }

    //if num 10 returns a list of hashnames
    public String[] getAUserList(String gameKey, int num) {
        int[] listPlayers = loginRowSearch(gameKey, 9);
        String[] hashnameList = new String[listPlayers.length];
        for (int i = 0; i < hashnameList.length; i++) {
            hashnameList[i] = findPart(listPlayers[i], num);
        }

        return hashnameList;
    }


    public String[] getChoiceList(String gameKey) {

        int[] listPlayers = loginRowSearch(gameKey, 9);

        choiceList = new String[listPlayers.length];
        for (int i = 0; i < choiceList.length; i++) {
            choiceList[i] = findPart(listPlayers[i], 7);
        }

        return choiceList;
    }


    public String RoundResults() {
        int[] group = loginRowSearch(waitingKey, 9);

        String RoundOptions = "ROUNDRESULT";
        for (int i = 0; i < group.length; i++) {

            RoundOptions += "--" + findPart(group[i], 0) + "--" + findPart(group[i], 8) + "--" + findPart(group[i], 2) + "--" + findPart(group[i], 3) + "--" + findPart(group[i], 4);
        }
        return RoundOptions;

    }

    public String roundOptions(String[] array) {

        String line = "ROUNDOPTIONS";

        for (int i = 0; i < array.length; i++) {

            line += "--" + array[i];

        }


        return line;
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
        LoginList.add((userName + ":" + password + ":0:0:0:NO:suggestion:choice:messagetouser:NOPE:hashname:WAIT"));
        fileList.add(userName + ":" + password + ":0:0:0");

        try {
            writeToFile();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public boolean nextScreenChecker(String input, String gameKey, int indexOfPart) throws InterruptedException { //adjust to check for people in each game
        int counter = 0;
        while (counter != LoginList.size()) { //make it so that after so many tries, stop working.
            Thread.sleep(2000); //check over
            for (int i = 0; i < LoginList.size(); i++) {
                String temp = LoginList.get(i);
                String[] parts = (LoginList.get(i)).split(":");
                if (parts[indexOfPart].equals(input) && parts[9].equals(gameKey)) { //checks for players only in one game
                    counter++;
                }
            }
        }
        return true;
    }


    public int[] loginRowSearch(String gameKey, int index) { //checks for all instances in Loginlist of players in one game
        int counter = 0;
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

    public void writeToFile() throws IOException { //remove, update and append to array list then write to file
        String filePath = "C:\\Users\\Sam Klarquist\\Desktop\\file1.sql";
        //only make game leader write to file to prevent multiple people from writing
        PrintWriter fileWriter = new PrintWriter(new BufferedWriter(new FileWriter(filePath, false)));
        //remove and append new info from arraylist(through "line") and write over line in file

        for (String element : fileList) {
            fileWriter.println(element);
        }


        fileWriter.close();
    }

    public String[] shuffler(String[] array) {
        ArrayList<String> suggest = new ArrayList<>();


        //trun in to arraylist
        for (int i = 0; i < array.length; i++) {
            suggest.add(array[i]);
        }
        Collections.shuffle(suggest);
        for (int i = 0; i < array.length; i++) {
            array[i] = suggest.get(i);
        }

        //turn back in to array

        return array;
    }
}

