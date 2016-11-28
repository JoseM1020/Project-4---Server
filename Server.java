import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Created by JoseM on 11/12/2016
 * @version November 17, 2016
 */

//TODO: Invalid message format for register and login
public class Server {

    private static String filePath = "C:\\Users\\JoseM\\Desktop\\" +
            "cs180\\Projects\\Project4\\Server\\database.txt";
    private static BufferedReader fileReader = null;
    private static PrintWriter fileWriter = null;
    private String line, inputText = "";
    private String[] readParts = null; //generally used for everything being split
    public static ArrayList<String> fileList = new ArrayList(); //when creating new user, add user info to list

    private static String[] sentMessages = new String[1]; //after messages are sent, store into array to prevent multiple messages being sent

    //user gets line from file, updates it in main, then sends it tho this method to rewrite file
    public void writeToFile(int X, int Y, int Z) { //remove, update and append to array list then write to file

        //not necessary
        for (int i = 0; i < fileList.size(); i++) {
            if (fileList.get(i).equals(line)) { //send entire value at specified index of arraylist (fileList) from main
                readParts = line.split(":");
                String lineTemp = readParts[0] + ":" + readParts[1] + ":" + X + ":" + Y + ":" + Z;
                fileList.remove(i);
                fileList.add(lineTemp);
            }
        }

        try {
            fileWriter = new PrintWriter(new BufferedWriter(new FileWriter(filePath, false)));

            //remove and append new info from arraylist(through "line") and write over line in file

            if (line.contains(readParts[0])) {
                for (int i = 0; i < fileList.size(); i++) {
                    fileWriter.println(fileList.get(i));
                }
            }


            fileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        Socket socket = null;
        PrintWriter outToClient = null;
        BufferedReader inFromClient = null;
        String line = "";
        String fromClient;
        String[] parts, clientParts, databaseParts = null;

        try {
            fileReader = new BufferedReader(new FileReader(filePath));

            while ((line = fileReader.readLine()) != null) { //displaying registered users and adding to array
                fileList.add(line);
                parts = line.split(":");
                System.out.println("Added user: " + parts[0]);
                System.out.println("Read in user: " + line);
                System.out.println(fileList.get(0)); //test code
                //method to say user already logged in/registered
            }

            fileReader.close();

        } catch (FileNotFoundException e) {
            System.out.println("ERROR 404: FILE NOT FOUND!");
        }

        boolean isLoggedIn = false;

        //username and password parameter checkers
        Pattern username = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Pattern password = Pattern.compile("[#&$*]");
        Pattern passwordUp = Pattern.compile("[A-Z]");
        Pattern passwordDig = Pattern.compile("[0-9]");

        //create unigue game code for LOGIN method and creates User token
        String uppercaseLetters = "QWERTYUIOPASDFGHJKLZXCVBNM";
        String lowercaseLetters = "qwertyuiopasdfghjklzxcvbnm";
        String alphabet = uppercaseLetters + lowercaseLetters;
        Random r = new Random();
        String sessionCookie = "";
        for (int i = 0; i < 10; i++) {
            char temp = alphabet.charAt(r.nextInt(alphabet.length()));
            sessionCookie = sessionCookie + temp;
        }
        String gameToken = "";
        for (int i = 0; i < 3; i++) {
            char temp = lowercaseLetters.charAt(r.nextInt(lowercaseLetters.length()));
            gameToken = gameToken + temp;

        }

        //change the file path to check on your side


        try {
            System.out.println("Creating Socket");
            serverSocket = new ServerSocket(5000);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not create socket");
        }

        try {
            socket = serverSocket.accept();
            System.out.println("Listening on socket 5000");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            outToClient = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //while(!(inFromCLient.radLine().equals("LOGOUT--){} // Reiterates through loop until user logsout
        fromClient = inFromClient.readLine();
        clientParts = fromClient.split("--");
        //message format check
        if (clientParts[0].equals("CREATENEWUSER")) { //parts are being formatted correctly
            for (int i = 0; i < fileList.size(); i++) {
                parts = fileList.get(i).split(":");
                if (parts[0].equals(clientParts[1])) {
                    outToClient.println("RESPONSE--CREATENEWUSER--USERALREADYEXISTS--");
                    System.out.println("OUT TO CLIENT:RESPONSE--CREATENEWUSER--USERALREADYEXISTS--");
                    break;
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
                try {
                    fileWriter = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
                    fileWriter.println(clientParts[1] + ":" + clientParts[2] + ":0:0:0");

                    fileWriter.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                fileList.add(clientParts[1] + ":" + clientParts[2] + ":0:0:0");
            }
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

            if (counterU == 0 || fileList == null) {
                outToClient.println("RESPONSE--LOGIN--UNKNOWNUSER--");
                System.out.println("OUT TO CLIENT:RESPONSE--LOGIN--UNKNOWNUSER--");
            } else if (counterP == 0) {
                outToClient.println("RESPONSE--LOGIN--INVALIDPASSWORD--");
                System.out.println("OUT TO CLIENT:RESPONSE--LOGIN--INVALIDPASSWORD--");

            }

            if (counterP == 1 && counterU == 1) {
                outToClient.println("RESPONSE--LOGIN--SUCCESS--" + sessionCookie);
                System.out.println("OUT TO CLIENT:RESPONSE--LOGIN--SUCCESS--" + sessionCookie);
                isLoggedIn = true;
                //TODO: Check if user logged in
            }
            //TODO: check if user already logged in (before success)
        }
        //FIXME: 11/23/2016 check over user input and output for all messages
        if (clientParts[0].equals("STARTNEWGAME")) {
            if (!isLoggedIn) {
                outToClient.println("RESPONSE--STARTNEWGAME--USERNOTLOGGEDIN");
            }
        }


        outToClient.close();
        inFromClient.close();

    }


}
