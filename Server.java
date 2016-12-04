/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sam Klarquist
 */
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by JoseM on 11/12/2016.
 */
public class Server implements Runnable {
    ServerSocket serverSocket = null;
    private Thread thread;
    Socket soc = null;
    Server server;
    static private String[] listOfLoggedInUsers;//this is a list that should be shared between all the Servers to say who's logged in and who is not
    static private String[] answers;
    String playersChoice;
    String player;
    String playerSuggestion;
    boolean playingGame=false;
    //gameList keeps track of all important information about a game
    static ArrayList<String> gameList;//game key:current question number:
    //LoginList keeps track of all important information about the user
    static ArrayList<String> LoginList;//there are eleven parts
    static  ArrayList<String> fileList = new ArrayList();//is the list from the file(updated using readUserList() method)
    static ArrayList<String> questionList = new ArrayList();
    String lowercaseLetters = "qwertyuiopasdfghjklzxcvbnm";
    String uppercaseLetters = "QWERTYUIOPASDFGHJKLZXCVBNM";
    String alphabet = uppercaseLetters + lowercaseLetters;
    boolean leaderIsWaiting=false;//just do it by using boolean variables and making them their own if statements
    boolean isGameLeader=false;
    int currentQuestion = 0;
    String gameName;
    String[] suggestionList;
    String[] choiceList;
    boolean waiting=false;
    String waitingKey;
    boolean isWaitingForStart=false;
    private String cook = "";
    PrintWriter fileWriter=null;

    static String filePath = "C:\\Users\\Sam Klarquist\\Desktop\\" +
            "file2.sql";
    static String questionPath ="C:\\Users\\Sam Klarquist\\Desktop\\" +
            "file2.sql";

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(6000);




        LoginList=new  ArrayList<String>();
        gameList=new ArrayList<String>();




        readUserList() ;


        //should contain in the first array the game token in the second contains the usernames

        //comment
        System.out.println("Listening on socket 5000");

        while(true){
            Socket socket = serverSocket.accept();
            Server server = new Server();
            server.setSock(socket);
            Thread thread = new Thread(server);
            thread.start();
        }




    }

    public void setSock(Socket soc){
        this.soc=soc;
    }


    public Server(){



    }

    @Override
    public void run() { BufferedReader inFromClient = null; PrintWriter outToClient = null;
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


        while(true){//change this to a variable latter but need to make it to infinite loop for now to test things

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

            if(leaderIsWaiting){

                outToClient.println("NEWPARTICIPANT--Alice--0");

            }


            while(isWaitingForStart){


            }

            while(waiting){
                int counts = 0;
                for(int i = 0; i<suggestionList.length;){
                    if(suggestionList.equals("NOPE")){
                        counts++;
                    }

                }
                if(counts>0){
                    getSuggestionsList(waitingKey);

                }
                else{
                    waiting=false;
                }





                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }





            try {
                fromClient = inFromClient.readLine();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }

            System.out.println(fromClient);

            clientParts = fromClient.split("--");

            //message format check
            if (clientParts[0].equals("CREATENEWUSER")) { //parts are being formatted correctly

                if(clientParts.length==3){
                    for (int i = 0; i < fileList.size(); i++) {
                        parts = fileList.get(i).split(":");
                        if (searchForPlayer(clientParts[1])==-1) {
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

                    if (searchForPlayer(clientParts[1])!=-1 || clientParts[1].length() >= 10 || u) {
                        outToClient.println("RESPONSE--CREATENEWUSER--INVALIDUSERNAME--");
                        System.out.println("OUT TO CLIENT:RESPONSE--CREATENEWUSER--INVALIDUSERNAME--");
                    } else if (searchForPassword(clientParts[2])!=-1 || clientParts[2].length() >= 10 || pwCreds) {
                        outToClient.println("RESPONSE--CREATENEWUSER--INVALIDPASSWORD--");
                        System.out.println("OUT TO CLIENT:RESPONSE--CREATENEWUSER--INVALIDPASSWORD--");
                    } else {
                        System.out.println(clientParts[1]);
                        outToClient.println("RESPONSE--CREATENEWUSER--SUCCESS--");
                        System.out.println("OUT TO CLIENT:RESPONSE--CREATENEWUSER--SUCCESS--");

                        createNewUser(clientParts[1] , clientParts[2]);


                    }

                }else{outToClient.println("RESPONSE--CREATENEWUSER--INVALIDMESSAGEFORMAT--");}
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

                int place =  searcher(clientParts[1],0);//this is the place where the user can be found

                if (place==-1 || fileList == null) {
                    outToClient.println("RESPONSE--LOGIN--UNKNOWNUSER--");
                    System.out.println("OUT TO CLIENT:RESPONSE--LOGIN--UNKNOWNUSER--");


                }

                else if (!findPart(place,1).equals(clientParts[2])) {
                    outToClient.println("RESPONSE--LOGIN--INVALIDPASSWORD--");
                    System.out.println("OUT TO CLIENT:RESPONSE--LOGIN--INVALIDPASSWORD--");

                }
                else if(findPart(place,5).equals("YES")){
                    outToClient.println("RESPONSE--LOGIN--USERALREADYLOGGEDIN--");
                }

                else {


                    System.out.println(clientParts[1]);
                    System.out.println (searcher(clientParts[1],0));
                    replacePart(searcher(clientParts[1],0),5,"YES");
                    cook = createSesionCookie();
                    System.out.println("OUT TO CLIENT:RESPONSE--LOGIN--SUCCESS--"+cook);
                    replacePart(searcher(clientParts[1],0),10,cook);
                    outToClient.println("RESPONSE--LOGIN--SUCCESS--"+cook);
                    //TODO: Check if user logged in
                }
                //TODO: check if user already logged in (before success)
            }

            if (clientParts[0].equals("STARTNEWGAME")) {
                int place =  searcher(clientParts[1],10);
                if (!findPart(searcher(clientParts[1],10),5).equals("YES")) {

                    outToClient.println("RESPONSE--STARTNEWGAME--USERNOTLOGGEDIN--");
                }
                else if(alreadyPlaying(clientParts[1])){//need something to check if user already playing a game
                    outToClient.println("RESPONSE--STARTNEWGAME--FAILURE--");
                }
                else{
                    String key=   addGameKey(clientParts[1]);
                    outToClient.println("RESPONSE--STARTNEWGAME--SUCCESS--"+key);
                    leaderIsWaiting=true;
                    isGameLeader=true;
                    gameName = key;
                    //while loop to keep track of new participants

                }
            }

            if (clientParts[0].equals("JOINGAME")) {
                int place =  searcher(clientParts[1],10);
                if(  !findPart(place,5).equals("YES")){
                    outToClient.println("RESPONSE--JOINGAME--USERNOTLOGGEDIN");}

                else if(searchGameKey(clientParts[2])==-1){//need to add
                    outToClient.println("RESPONSE--JOINGAME--GAMEKEYNOTFOUND");}
                else if(alreadyPlaying(clientParts[1])){
                    outToClient.println("RESPONSE--JOINGAME--FAILURE");}
                else{//do something to add user to gameList
                    outToClient.println("RESPONSE--JOINGAME--SUCCESS--"+clientParts[2]);
                    replacePart(place,9,clientParts[2]);

                    isWaitingForStart=true;

                }

            }

            if (clientParts[0].equals("ALLPARTICIPANTSHAVEJOINED")) {
                int place =  searcher(clientParts[1],10);
                if(!findPart(place,5).equals("YES")){
                    outToClient.println("RESPONSE--ALLPARTICIPANTSHAVEJOINED--USERNOTLOGGEDIN");}
                else if(searcher(clientParts[2],9)==-1){
                    outToClient.println("RESPONSE--ALLPARTICIPANTSHAVEJOINED--INVALIDGAMETOKEN");}
                else if(!isGameLeader&&!gameName.equals(clientParts[2])){
                    outToClient.println("RESPONSE--ALLPARTICIPANTSHAVEJOINED--USERNOTGAMELEADER");}
                else{
                    newGameWord();


                }


            }


            if (clientParts[0].equals("PLAYERSUGGESTION")) {
                int place =  searcher(clientParts[1],10);


                if(!findPart(place,5).equals("YES")){ //check hashcode
                    outToClient.println("RESPONSE--PLAYERSUGGESTION--USERNOTLOGGEDIN");
                } else if(searchGameKey(clientParts[2])==-1) { //game key not equal to current (from login method or as leader)
                    outToClient.println("RESPONSE--PLAYERSUGGESTION--INVALIDGAMETOKEN");
                } else if(false){ //unexpected message type (look over)

                } else if(clientParts[0].equals("")||clientParts[1].equals("") ||
                        clientParts[2].equals("")|| clientParts[2]==null){
                    {outToClient.println("RESPONSE--PLAYERSUGGESTION--INVALIDMESSAGEFORMAT");}
                } else{
                    //store suggestions in Loginlist and run rowsearcher method
                    waiting =true;

                    outToClient.println("ROUNDOPTIONS--"); //write all suggestions
                    //make method for this
                }

            }

            if (clientParts[0].equals("PLAYERCHOICE")) {


                if(clientParts.length==3){
                    int place =  searcher(clientParts[1],10);//this is the place where the user can be found

                    if (place==-1 || fileList == null) {
                        outToClient.println("RESPONSE--PLAYERCHOICE--UNKNOWNUSER--");
                        System.out.println("OUT TO CLIENT:RESPONSE--LOGIN--UNKNOWNUSER--");


                    }

                    else if (!findPart(place,9).equals(clientParts[2])) {
                        outToClient.println("RESPONSE--PLAYERCHOICE--INVALIDGAMETOKEN--");
                        System.out.println("OUT TO CLIENT:RESPONSE--LOGIN--INVALIDPASSWORD--");

                    }
                    //do it by making variable that does it array
                    else if(findPart(place,5).equals("YES")){
                        outToClient.println("RESPONSE--PLAYERCHOICE--UNEXPECTEDMESSAGETYPE--");

                    }

                    else {


                        System.out.println(clientParts[1]);
                        System.out.println (searcher(clientParts[1],0));
                        replacePart(searcher(clientParts[1],0),5,"YES");
                        String cook = createSesionCookie();
                        System.out.println("OUT TO CLIENT:RESPONSE--LOGIN--SUCCESS--"+cook);
                        replacePart(searcher(clientParts[1],0),10,cook);
                        outToClient.println("RESPONSE--PLAYERCHOICE--SUCCESS--"+cook);
                        //TODO: Check if user logged in
                    }


                }else{
                    outToClient.println("RESPONSE--PLAYERCHOICE--INVALIDMESSAGEFORMAT--");
                }
            }

            //this should if the logout comand is given check wether the user was logged in or not then spit back the appropriate response
            if (clientParts[0].equals("LOGOUT")) {
                if(isLoggedIn==true){
                    isLoggedIn=false;
                    outToClient.println("RESPONSE--LOGOUT--SUCCESS--");
                }
                else{
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




    static public void readQuestions()throws FileNotFoundException, IOException{

        BufferedReader  fileReader = new BufferedReader(new FileReader(questionPath));

        String line;
        String quest;
        String[] parts = null;
        while ((line = fileReader.readLine()) != null) { //displaying registered users and adding to array

            quest = line.replace(":", "--");
            questionList.add(quest);


        }
        fileReader.close();

    }

    static public void readUserList() throws FileNotFoundException, IOException{
        BufferedReader  fileReader = new BufferedReader(new FileReader(filePath));
        LoginList = new ArrayList();
        String line;
        String[] parts = null;

        while ((line = fileReader.readLine()) != null) { //displaying registered users and adding to array
            fileList.add(line);
            System.out.println(line);
            parts = line.split(":");

            LoginList.add((line+":NO:suggestion:choice:messagetouser:NOPE:hashname:wait"));//Username:loginstatus(YES or NO):suggestion:choice:meesage:playing(gameKey or NOPE):hashname:wait/start

            System.out.println(LoginList);


            // System.out.println("Added user: " + parts[0]);

            //method to say user already logged in/registered
        }
        fileReader.close();

    }
    //returns the string with the proper game word might require int to keep track of which question needs getting
    public String newGameWord(){

        return "NEWGAMEWORD--"+questionList.get(currentQuestion++);

    }

    //adds a new game
    public void gameTokenAdder(String token, String leader){

        gameList.add(token+":"+leader);

    }
    //this will return back a number >=0 and if it doesn't then it isn't on the list
    public int searchGameKey(String gameKey){
        String[] part;
        for(int i=0; i<gameList.size();i++){
            part= gameList.get(i).split(":");

            if(part[0].equals(gameKey)){
                return i;
            }
        }

        return -1;


    }

    public String addGameKey(String hashName){

        String gameKey= createGameToken();
        replacePart(searchForPlayer(hashName), 9,gameKey+":");

        gameList.add(gameKey+":");
        return gameKey;
    }

    public void addGameUser(String hashName, String gameKey){

        String temp =   gameList.get(searchGameKey(gameKey));

        String[] parts = temp.split(":");


        temp="";
        for(int i=0; i<parts.length;i++){
            temp+=parts[i];
        }
        temp+=":"+hashName;

    }

    public int searchForPlayer(String hashName){
        String[] part;
        for(int i=0; i<LoginList.size();i++){
            part= LoginList.get(i).split(":");

            if(part[10].equals(hashName)){
                return i;
            }
        }

        return -1;


    }

    public int searchForPassword(String password){
        String[] part;
        for(int i=0; i<LoginList.size();i++){
            part= LoginList.get(i).split(":");

            if(part[1].equals(password)){
                return i;
            }
        }

        return -1;


    }
    //this can be used to search for anything on the game list (0): username, (1) password, (2)
    public int searcher(String thing,int num){
        String[] part;
        for(int i=0; i<LoginList.size();i++){
            part= LoginList.get(i).split(":");

            if(part[num].equals(thing)){
                return i;
            }
        }

        return -1;


    }
    //method in progress
    public String gitter(String thing,int num){
        String[] part;
        for(int i=0; i<LoginList.size();i++){
            part= LoginList.get(i).split(":");

            if(part[num].equals(thing)){
                return part[i];
            }
        }

        return "";


    }
    //num is the specific list and thing specifies where in the string the thing is being replaced
    public void replacePart(int num, int thing,String replacer){

        System.out.println(LoginList);
        String temp=LoginList.get(num);
        System.out.println(temp);
        String[] part= temp.split(":");

        temp="";
        for(int i=0;i<part.length;i++){
            if(i!=thing){
                temp+=part[i];}
            else{
                //  System.out.print(replacer);
                temp+=replacer;
            }
            if(i<part.length-1){
                temp+=":";
            }
        }

        LoginList.set(num,temp);

    }

    public String findPart(int num, int thing){

        System.out.println(LoginList);
        String temp=LoginList.get(num);
        System.out.println(temp);
        String[] part= temp.split(":");

        temp="";

        temp = part[thing];

        return temp;

    }

    public boolean isLogged(String a){

        int num = searchGameKey(a);

        if(num==-1){
            String[] parts=LoginList.get(num).split(":");


            if(parts[5].equals("YES")){
                return true;
            }}

        return false;
    }

    public void Fooled(String gameKey, String players){
        getSuggestionsList(gameKey);
        String[] userList =   getAUserList(gameKey, 10);
        String[] choiceList =   getAUserList(gameKey, 7);
        int[] points= new int[userList.length];
        String[] message = new String[userList.length];
        String answer= questionList.get(currentQuestion);


        for(int k=0; k<userList.length;k++){
            int plave =searcher(userList[k],10);
            points[k]= Integer.parseInt(findPart(plave,2));
            message[k]="";

        }

        for(int i =0; i<userList.length;i++){
            for(int j =0; j<userList.length;j++){
                if(choiceList[i].equals(answer)){}
                if(suggestionList[i].equals(choiceList[j])){
                    points[]

                }


            }
        }

    }

    public String[] getSuggestionsList(String gameKey){

        int[] listPlayers= loginRowSearch(gameKey,9); //fix
        suggestionList =new String[listPlayers.length];
        for(int i=0;i<suggestionList.length;i++){
            suggestionList[i]= findPart(listPlayers[i],6);}

        return suggestionList;
    }
    //if num 10 returns a list of hashnames
    public String[] getAUserList(String gameKey, int num){
        int[] listPlayers= loginRowSearch(gameKey,9);
        String[] hashnameList=new String[listPlayers.length];
        for(int i=0;i<hashnameList.length;i++){
            hashnameList[i]= findPart(listPlayers[i],num);}

        return hashnameList;
    }

    public void Fooler(String gameKey, String players){


    }


    public String RoundResults(String[] array){

        String RoundOptions = "ROUNDRESULTS";
        for (String element : array){
            RoundOptions+="--"+element;
        }
        return RoundOptions;

    }

    public boolean alreadyPlaying(String hashname){
        int num  = searcher(hashname,10);
        if(num!=-1){

            if(findPart(num,9).equals("NOPE")){
                return false;
            }
            else{
                return true;
            }

        }

        return false;
    }

    public String createSesionCookie(){
        Random r = new Random();
        String sessionCookie = "";
        for (int i = 0; i < 10; i++) {
            char temp = alphabet.charAt(r.nextInt(alphabet.length()));
            sessionCookie = sessionCookie + temp;
        }
        return sessionCookie;

    }
    public String createGameToken(){
        Random r = new Random();
        String gameToken = "";
        for (int i = 0; i < 3; i++) {
            char temp = lowercaseLetters.charAt(r.nextInt(lowercaseLetters.length()));
            gameToken = gameToken + temp;

        }
        return gameToken;
    }

    public void createNewUser(String userName, String password){
        LoginList.add((userName+":"+password +":0:0:0:NO:suggestion:choice:messagetouser:NOPE:hashname:wait"));
    }

    public void scoreUpdater(){ //updates score for all players: use row search method to find instances of players

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

    public int loginRowSearch(String hashCode, int index) { //returns index of player from hashcode
        int userLine = 0;
        for (int i = 0; i < LoginList.size(); i++) {
            String temp = LoginList.get(i);
            String[] parts = (LoginList.get(i)).split(":");
            if (parts[index].equals(hashCode)) { //checks for players only in one game
                userLine = i;
            }

        }
        return userLine;
    }

    public String[] shuffler(){
        ArrayList<String> suggest = new ArrayList<>();


        //trun in to arraylist
        for(int i = 0; i<choiceList.length;i++){
            suggest.add(choiceList[i]);
        }
        Collections.shuffle(suggest);
        for (int i = 0; i<choiceList.length;i++){
            choiceList[i]=suggest.get(i);
        }

        //turn back in to array

      return choiceList;
    }


      public void writeToFile() throws IOException { //remove, update and append to array list then write to file

        if(isGameLeader) { //only make game leader write to file to prevent multiple people from writing
            fileWriter = new PrintWriter(new BufferedWriter(new FileWriter(filePath, false)));

            //remove and append new info from arraylist(through "line") and write over line in file

            for (String element : fileList) {
                fileWriter.println(element);
            }


            fileWriter.close();
        }



}

}