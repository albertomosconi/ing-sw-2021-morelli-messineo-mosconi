package it.polimi.ingsw.view;

import it.polimi.ingsw.client.Client;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.network.setup.CreateRoomMessage;
import it.polimi.ingsw.network.setup.JoinPrivateRoomMessage;
import it.polimi.ingsw.network.setup.JoinPublicRoomMessage;
import it.polimi.ingsw.network.setup.Room;

import java.io.PrintStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Scanner;

public class CLI implements UI {

    private final Client client;
    private final Scanner input;
    private final PrintStream output;
    private String username;
    
    public CLI(Client client) {
        this.client = client;
        input = new Scanner(System.in);
        output= new PrintStream(System.out);
    }

    public void run(){}

    public void setup(){
        int selection;
        output.println("Good morning Sir,");
        output.println("how shall I call you?");
        username = input.nextLine();
        output.println("Welcome" + username + ", nice to meet you");
        output.println("Only online game available for now");

        output.println("Do wou want to create a room or join an existing one?");
        selection = askIntegerInput("1: create\n2: join", 1, 2);

        if (selection == 1) {
            int playersNum = askIntegerInput("How many players is this game for?",1,4);

            output.println("Is this a private game? [y/n]");
            boolean privateGame = input.nextLine().toLowerCase().startsWith("y");
            client.sendMessage(new CreateRoomMessage(privateGame, playersNum, username));
        }
        else {
            output.println("Do you have a RoomID or do you want to join a public game");
            selection = askIntegerInput("1: RoomId, 2: PublicGame", 1, 2);

            if (selection == 1) {
                int roomId = askIntegerInput("Insert roomId", 1000, 9999);
                client.sendMessage(new JoinPrivateRoomMessage(roomId, username));
            }
            else {
                int playersNumber = askIntegerInput("Insert desired number of players [0 for random]", 0, 4);
                client.sendMessage(new JoinPublicRoomMessage(playersNumber, username));
            }
        }
    }

    @Override
    public void displayRoomDetails(ArrayList<String> players, int playersNum, int RoomId) {
        output.println("Game details:");
        output.println(players);
        output.println(playersNum);
        output.println(RoomId);

    }

    public int askIntegerInput(String message, int minBoundary, int maxBoundary) {
        int selection;
        while (true) {
            output.println(message);
            try {
                selection = Integer.parseInt(input.nextLine());
                if (selection < minBoundary || selection > maxBoundary) {
                    throw new InvalidParameterException();
                } else {
                    break;
                }
            } catch (NumberFormatException | InvalidParameterException e) {
                output.println("selection not valid");
            }
        }
        return selection;
    }
}