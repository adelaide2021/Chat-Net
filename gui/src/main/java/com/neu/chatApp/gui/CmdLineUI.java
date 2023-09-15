package com.neu.chatApp.gui;

import com.neu.chatApp.common.model.message.communicationMessage.CommunicationMessage;
import com.neu.chatApp.common.model.message.communicationMessage.CommunicationMessageType;
import com.neu.chatApp.common.model.message.leaderElectionMessage.LeaderElectionMessage;
import com.neu.chatApp.client.http.ClientAPI;
import com.neu.chatApp.client.peerToPeer.data.ClientData;
import com.neu.chatApp.client.peerToPeer.handlers.JoinAndLeaveHandler;
import com.neu.chatApp.client.peerToPeer.services.Communication;
import com.neu.chatApp.client.peerToPeer.services.CommunicationImpl;
import com.neu.chatApp.common.model.message.MessageType;
import com.neu.chatApp.common.model.message.leaderElectionMessage.LeaderElectionMessageType;
import com.neu.chatApp.common.model.node.Node;
import com.neu.chatApp.common.model.node.NodeChannel;
import com.neu.chatApp.util.FormattedPrinter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.Map;

/**
 * UI for user to interact. Should be located in a different thread.
 */
@Slf4j
public class CmdLineUI implements Runnable {

    // responsible for user login and registration
    private final ClientAPI clientAPI;

    // responsible for point-to-point or multicast communication
    private final Communication communication;

    private final UIHelper uiHelper;

    private final BufferedReader bufferedReader;

    public static boolean isJoined;

    public static boolean isLeft;


    public CmdLineUI() {
        this.clientAPI = new ClientAPI();
        this.communication = new CommunicationImpl();
        this.bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        this.uiHelper = new UIHelper();
    }


    public void userInterface() throws IOException {
        FormattedPrinter.printTitle("Welcome to P2P multi agent chat application");
        leve1MenuEventHandler();
    }

    /**
     * The level 1 menu should take care of guidelines for user signup and login.
     */
    public void level1Menu() {
        FormattedPrinter.printTitle("Signup / Login");
        FormattedPrinter.printSystemMessage("Please choose a command number to continue: ");
        FormattedPrinter.printSystemMessage("<1> signup");
        FormattedPrinter.printSystemMessage("<2> login");
        FormattedPrinter.printSystemMessage("<3> exit");
        FormattedPrinter.printEnd();
    }

    public void leve1MenuEventHandler() throws IOException {
        // print menu
        level1Menu();
        // get user input
        int rightInput = getRightInput(1, 3);
        switch (rightInput) {
            // signup
            case 1:
                signupHandler();
                FormattedPrinter.printEnd();
                loginHandler();
                FormattedPrinter.printEnd();
                // login successfully go to level 2 menu
                level2MenuEventHandler();
                break;
            // login
            case 2:
                loginHandler();
                FormattedPrinter.printEnd();
                // login successfully go to level 2 menu
                level2MenuEventHandler();
                break;
            // exit
            case 3:
                onExit();
                break;
        }
        FormattedPrinter.printEnd();
    }

    public void level2Menu() {
        FormattedPrinter.printTitle("Function Navigation");
        FormattedPrinter.printSystemMessage("Please choose a command number to continue: ");
        FormattedPrinter.printSystemMessage("<1> show online users");
        FormattedPrinter.printSystemMessage("<2> send message");
        FormattedPrinter.printSystemMessage("<3> broadcast message");
        FormattedPrinter.printSystemMessage("<4> exit");
        FormattedPrinter.printEnd();
    }

    public void level2MenuEventHandler() throws IOException  {
        while (true) {
            // print menu
            level2Menu();
            // get user input
            int rightInput = getRightInput(1, 4);
            switch (rightInput) {
                // show online users
                case 1:
                    displayOnlineUsers();
                    break;
                // send message
                case 2:
                    // send message handler
                    messageSender();
                    FormattedPrinter.printLineBreaker();
                    break;
                // broadcast message
                case 3:
                    // broadcast handler
                    messageBroadcaster();
                    FormattedPrinter.printLineBreaker();
                    break;
                // exit
                case 4:
                    onExit();
                    break;
            }
            FormattedPrinter.printEnd();
        }
    }

    public void signupHandler() throws IOException {
        FormattedPrinter.printTitle("Signup");
        // get username
        String username = getRightInput(0, "username");
        FormattedPrinter.printLineBreaker();
        // get password
        String password = getRightInput(1, "password");
        FormattedPrinter.printLineBreaker();
        try {
            // call signup
            String response = clientAPI.signUp(username, password);
            FormattedPrinter.printSystemMessage(response);
        } catch (HttpClientErrorException hcee) {
            FormattedPrinter.printSystemMessage(hcee.getResponseBodyAsString());
            // recall the method to restart
            signupHandler();
        } catch (HttpServerErrorException | ResourceAccessException e) {
            log.error("Network or server traffic");
            FormattedPrinter.printSystemMessage("Server is unreachable now, please use the application later");
            onExit();
        }
    }

    public void loginHandler() throws IOException {
        FormattedPrinter.printTitle("Login");
        // get username
        String username = getRightInput(0, "username");
        FormattedPrinter.printLineBreaker();
        // get password
        String password = getRightInput(1, "password");
        FormattedPrinter.printLineBreaker();
        // get hostname
        String myHostname = ClientData.myHostname;
        // get port
        int myPort = ClientData.myPort;
        try {
            // call login
            Map<String, Object> login = clientAPI.login(username, password, myHostname, myPort);

            // parse the response of login
            // returns the hostname and port of the leader node
            Long id = Long.valueOf((Integer)login.get("id"));
            String hostname = (String) login.get("hostname");
            int port = (int) login.get("port");
            FormattedPrinter.printSystemMessage("Welcome back " + username);

            // construct my node info and check if the node is the leader node
            ClientData.myNode = new Node(id, username, false, ClientData.myHostname, ClientData.myPort);
            FormattedPrinter.printSystemMessage("Connecting to the p2p network ...");
            if (hostname.equals(ClientData.serverHostname) && port == ClientData.httpServerPort) {
                log.info("The node has become the leader node");
                ClientData.myNode.setLeader(true);
                try {
                    // if the current node is the leader, create a channel for the leader node and initiate leader election in the P2P network to inform all nodes about the new leader
                    ClientData.serverChannel = ClientData.p2PInitializer.connect(hostname, port);
                    LeaderElectionMessage leaderElectionMessage = new LeaderElectionMessage(MessageType.LEADER_ELECTION, LeaderElectionMessageType.CLIENT_REPORT, ClientData.myNode);
                    ClientData.serverChannel.writeAndFlush(leaderElectionMessage);
                    ClientData.serverChannel.close();
                    ClientData.serverChannel = null;
                } catch (SocketTimeoutException e) {
                    log.error("Failed to connect to server p2p service");
                    clientAPI.logout(id);
                    FormattedPrinter.printSystemMessage("Server service is current unavailable, please try later");
                    onExit();
                }
            } else {
                // if not the leader node invoke the join and leave handler and connect to the leader node
                JoinAndLeaveHandler.join(hostname, port);
                while (!isJoined) {
                    FormattedPrinter.printSystemMessage("Joining the p2p network ...");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {}
                }
                FormattedPrinter.printSystemMessage("You have joined the p2p network");
            }
        } catch (HttpClientErrorException hcee) {
            FormattedPrinter.printSystemMessage(hcee.getResponseBodyAsString());
            // recall the method to restart
            loginHandler();
        } catch (HttpServerErrorException | ResourceAccessException e) {
            log.error("Network or server traffic");
            FormattedPrinter.printSystemMessage("Server is unreachable now, please use the application later");
            onExit();
        }

    }

    public void messageSender() throws IOException {
        FormattedPrinter.printSystemMessage("Please input a user <id> (in [] of the online user list),");
        FormattedPrinter.printSystemMessage("input <-1> to back to navigation,");
        FormattedPrinter.printSystemMessage("input <0> to display online users:");
        if (ClientData.clientLiveNodes.size() == 0) {
            FormattedPrinter.printSystemMessage("No online users found, please try later");
            return;
        }
        // get input
        int input = getRightInput(-1, Integer.MAX_VALUE);
        if (input == -1) {
            return;
        }
        if (!ClientData.clientLiveNodes.isContain((long) input)) {
            FormattedPrinter.printSystemMessage("The user doesn't exit or not be online");
            return;
        }
        if (input == 0) {
            displayOnlineUsers();
        } else {
            // get message
            String message = getRightInput(2, "your message");
            // construct general message
            CommunicationMessage communicationMessage = new CommunicationMessage(MessageType.COMMUNICATION, CommunicationMessageType.PRIVATE_MESSAGE, ClientData.myNode.getNodeId(), (long) input, message);
            communication.send((long) input, communicationMessage);
            // print sent log
            FormattedPrinter.printSystemMessage(FormattedPrinter.formatter(false, (long) input, ClientData.clientLiveNodes.get((long) input).getNodeName(), message));
        }
    }
    public void messageBroadcaster() throws IOException {
        FormattedPrinter.printSystemMessage("Note: your message is going to be seen by all online users, ");
        FormattedPrinter.printSystemMessage("input <-1> to back to navigation,");
        FormattedPrinter.printSystemMessage("input <0> to continue: ");
        // get input
        int input = getRightInput(-1, 0);
        if (input == -1) {
            return;
        }
        if (ClientData.clientLiveNodes.size() == 0) {
            FormattedPrinter.printSystemMessage("No online users found, please try later");
            return;
        }
        String message = getRightInput(2, "your message");
        // construct general message
        CommunicationMessage generalCommunicationProtocol = new CommunicationMessage(MessageType.COMMUNICATION, CommunicationMessageType.BROADCAST_MESSAGE, ClientData.myNode.getNodeId(), message);
        communication.broadcast(generalCommunicationProtocol);
        // print sent log
        FormattedPrinter.printSystemMessage(FormattedPrinter.formatter(false, message));
    }



    /**
     * Loop until get right input from user.
     *
     * @param status status code for call different methods
     * 0 -> username check/ general string check; 1 -> password check; 2 ->
     * @param guidelines to guide user input
     * @return the valid input from user
     */
    public String getRightInput(int status, String guidelines) throws IOException {
        switch (status) {
            case 0:
                while (true) {
                    FormattedPrinter.printSystemMessage("Please input your"  + guidelines + ": ");
                    String username = bufferedReader.readLine();
                    if (uiHelper.checkInputString(username)) {
                        return username;
                    }
                    FormattedPrinter.printSystemMessage("Invalid format of "  + guidelines + ", please check and input again");
                }
            case 1:
                while (true) {
                    FormattedPrinter.printSystemMessage("Please input your " + guidelines + ": ");
                    String password = bufferedReader.readLine();
                    if (uiHelper.checkPassword(password)) {
                        return password;
                    }
                    FormattedPrinter.printSystemMessage("Invalid format of " + guidelines + ", please check and input again");
                }
            default:
                return null;
        }
    }

    /**
     * Loop until the user Integer input a valid input or exit
     * return the right input
     *
     * @param lowerBound lower bound
     * @param upperBound upper bound
     * @return the correct input
     * @throws IOException io of system terminal
     */
    public int getRightInput(int lowerBound, int upperBound) throws IOException {
        while (true) {
            String input = bufferedReader.readLine();
            if (uiHelper.integerInputChecker(input, lowerBound, upperBound)) {
                return Integer.parseInt(input);
            } else {
                FormattedPrinter.printSystemMessage("Invalid input, please try again");
            }
        }
    }
    /**
     * Display all online users.
     */
    public void displayOnlineUsers() {
        FormattedPrinter.printTitle("Current Online Users");
        System.out.println("[You] " + ClientData.myNode.getNodeName());
        if (ClientData.clientLiveNodes.size() == 0) {
            FormattedPrinter.printSystemMessage("No other online users found");
        } else {
            Iterator<NodeChannel> allNodes = ClientData.clientLiveNodes.getAllNodes();
            while (allNodes.hasNext()) {
                NodeChannel next = allNodes.next();
                System.out.println("[" + next.getNodeId() + "] " + next.getNodeName());
            }
        }
        FormattedPrinter.printEnd();
    }

    /**
     * For user exit the program, this should shut down the whole system.
     */
    public void onExit() {
        try {
            if (ClientData.myNode != null && ClientData.myNode.isLeader()) {
                // if it is the leader node, initiate a leave transaction and leader election
                JoinAndLeaveHandler.leave();
                while (!isLeft) {
                    FormattedPrinter.printSystemMessage("System is exiting ...");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {}
                }
                ClientData.serverChannel.writeAndFlush(new LeaderElectionMessage(MessageType.LEADER_ELECTION, LeaderElectionMessageType.TOKEN_RETURN, ClientData.myNode, ClientData.leaderNodeToken));
            } else {
                if (ClientData.myNode != null) {
                    // non-leader nodes report to the leader for logout
                    JoinAndLeaveHandler.leave();
                }
            }
            bufferedReader.close();
            log.info("System exited successfully");
            FormattedPrinter.printSystemMessage("See you soon!");
        } catch (IOException ignored) {}
        System.exit(0);
    }

    @Override
    public void run() {
        try {
            userInterface();
        } catch (IOException e) {
            log.error(e.getMessage());
            System.exit(1);
        }
    }


}

/**
 * The helper class of ui to do some validation operations.
 *
 */
class UIHelper {
//    String emailRegexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
//            + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

//    boolean checkEmail(String email) {
//        return Pattern
//                .compile(emailRegexPattern)
//                .matcher(email)
//                .matches();
//    }
    /**
     * A general check input string is neither empty nor composed of spaces.
     *
     * @param str the string to be checked
     * @return true if the string passed the check (neither empty nor composed of spaces), otherwise false
     */
    boolean checkInputString(String str) {
        return !str.isEmpty();
    }

    /**
     * Check password is not empty, all spaces, or less than 6 characters.
     *
     * @param password the password to be checked
     * @return true if the password is valid, otherwise false
     */
    boolean checkPassword(String password) {
        return checkInputString(password) && password.length() > 5;
    }
    /**
     * Check email if it matches the regex pattern.
     *
     * @param email the email to be checked
     * @return true if the email matched, otherwise false
     */

    /**
     * Check integer input to test if they are valid and in the right interval.
     *
     * @param input the input string
     * @param lowerBound the lower bound
     * @param upperBound the upper bound
     * @return true if the string can be parsed to an integer and in the interval [lower bound, upper bound], otherwise false
     */
    boolean integerInputChecker(String input, int lowerBound, int upperBound) {
        boolean preCheck = checkInputString(input);
        if (!preCheck) {
            return false;
        }
        int parseInt;
        try {
            parseInt = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return false;
        }
        return parseInt >= lowerBound && parseInt <= upperBound;
    }
}