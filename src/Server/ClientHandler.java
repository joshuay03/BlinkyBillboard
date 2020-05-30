package Server;

import BillboardSupport.Billboard;
import BillboardSupport.DummyBillboards;
import BillboardSupport.Schedule;
import Exceptions.AuthenticationFailedException;
import Exceptions.InvalidTokenException;
import Exceptions.NoSuchUserException;
import SocketCommunication.*;

import java.io.*;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.Collator;
import java.time.LocalDateTime;
import java.util.*;

import static SocketCommunication.ServerRequest.LOGIN;
import static SocketCommunication.ServerRequest.VIEWER_CURRENTLY_SCHEDULED;


/**
 * A class to handle each client individually on an assigned thread.
 */
public class ClientHandler extends Thread {
    private final int BILLBOARD_ID = 0,
            CREATOR = 1,
            BACKGROUND_COLOUR = 2,
            MESSAGE_COLOUR = 3,
            INFORMATION_COLOUR = 4,
            MESSAGE = 5,
            INFORMATION = 6,
            IMAGE = 7;
    private DataInputStream input;
    private DataOutputStream output;
    private Socket client;
    private blinkyDB database;

    public ClientHandler(Socket client, DataInputStream input, DataOutputStream output, blinkyDB database) {
        this.client = client;
        this.input = input;
        this.output = output;
        this.database = database;
    }

    @Override
    public void run() {
        Response outputData;
        boolean closed = false;
        while (!closed) {
            try {
                ObjectInputStream inputObject = new ObjectInputStream(input);
                ObjectOutputStream outputWriter = new ObjectOutputStream(output);
                try {
                    // Cast the data into a request object to find out what the client wants
                    Request req = (Request) inputObject.readObject();
                    outputData = handleInboundRequest(req); // Handle the client's request and retrieve the response for that request
                    outputWriter.writeObject(outputData); // Replaced below statement with a generic object writer
                } catch (IllegalStateException e) {
                    outputWriter.writeObject(new Response(false, e.getMessage())); // Send a response to the client, informing it that an invalid request has been sent
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                closed = closeConnection();
                System.out.println("Connection closed");
            }
        }
    }

    /**
     * A function which takes a request and returns a response object, to be sent back
     *
     * @param req The request to handle
     * @return A response
     */
    public Response handleInboundRequest(Request req) throws SQLException {
        final Response permissionDeniedResponse = new Response(false, "Permission denied, please log out and log back in.");
        User authenticatedUser = null;
        List<ServerRequest> authlessRequests = Arrays.asList(LOGIN, VIEWER_CURRENTLY_SCHEDULED);
        if (!authlessRequests.contains(req.getRequestType())) // Verify the token before continuing, except for LOGIN requests
        {
            try {
                Token sessionAuthentication = Token.validate(req.getSession().token);
                try {
                    authenticatedUser = new User(sessionAuthentication.username, database);
                } catch (NoSuchUserException e) {
                    return new Response(false, "The user this token was assigned to is not registered.");
                }
                // Get current timestamp
                Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                // Check if the token is expired
                if (now.after(sessionAuthentication.expiryDate)) {
                    return new Response(false, "Token has expired.");
                }
            } catch (InvalidTokenException e) {
                return new Response(false, "Token verification failed.");
            }
        }

        Collator collator = Collator.getInstance(Locale.ENGLISH);

        // *************************************************************************************
        // LOGIC SWITCHING
        // *************************************************************************************
        //<editor-fold desc="REQUEST TYPE SWITCHING">
        // Example handle login
        switch (req.getRequestType()) {
            case VIEWER_CURRENTLY_SCHEDULED: {
                return new Response(true, DummyBillboards.messagePictureAndInformationBillboard());
            }
            case LOGIN: {
                // EXAMPLE how to use the request given from the client
                Credentials credentials;
                try {
                    credentials = req.getCredentials();
                } catch (Exception e) {
                    return new Response(false, "Missing username or password");
                }
                try {
                    return new Response(true, new Session(credentials, database));
                } catch (AuthenticationFailedException | NoSuchUserException e) {
                    return new Response(false, "Cannot create session.");
                }
            }
            case LIST_BILLBOARDS: {
                Response res = null; // null needs to be replaced with the server.
                // logic to return list of billboards e.g. new Response(true, BillboardList());
                List<Billboard> billboardList;
                try {
                    billboardList = database.getBillboards();

                } catch (SQLException e) {
                    return new Response(false, "There was an SQL error");
                }
                // The billboard list now has all of the returned billboards - convert to an array and return
                return new Response(true, billboardList.toArray(new Billboard[0]));
            }
            case GET_BILLBOARD_INFO:
                // check if session is valid e.g. expired, if not return failure and trigger relogin - already done above

                // this is triggered inside the BillboardList()); GUI
                // The control panel send the server the Billboard Name and valid session
                int billboardID = req.getBillboardID();

                List<Billboard> results = database.getBillboards(Integer.toString(billboardID), "billboard_id");

                //server responds with billboards contents
                if (results != null) return new Response(true, results.get(0));
                else return new Response(false, "Could not find Billboard with that ID");

            // FIXME - change to Insert/Change after database changes
            case CREATE_BILLBOARD: {
                assert authenticatedUser != null;
                Billboard billboard;
                try {
                    billboard = req.getBillboard();
                } catch (Exception e) {
                    return new Response(false, "Invalid billboard object");
                }

                if (authenticatedUser.CanCreateBillboards()) {
                    List<Billboard> billboards = database.getBillboards();

                    Optional<Billboard> billboardMatch = billboards.stream().filter(x -> x.equals(billboard)).findFirst();

                    if (billboardMatch.isEmpty()) {
                        database.createBillboard(billboard, authenticatedUser.getSaltedCredentials().getUsername());
                        return new Response(true, "Success");
                    } else {
                        Billboard existingBillboard = billboardMatch.get();

                        Schedule schedule = existingBillboard.getSchedule();

                        if (schedule == null) {
                            // TODO: need to be able to get username from authUser
                            database.createBillboard(billboard, authenticatedUser.getSaltedCredentials().getUsername());
                            return new Response(true, "Success");
                        } else {
                            if (authenticatedUser.CanEditAllBillboards()) {
                                //replace billboard in db with billboard from request
                                // TODO: make editBillboard()
                                database.editBillboard(billboard, authenticatedUser.getSaltedCredentials().getUsername());
                            } else {
                                return new Response(false, "Invalid billboard edit permissions.");
                            }
                        }
                    }
                } else {
                    return permissionDeniedResponse;
                }
            }
            break;
            case EDIT_BILLBOARD:
                // check if session is valid e.g. expired, if not return failure and trigger relogin

                // this request will only happen if User has 'Edit all Billboards' permission
                // Client sends server billboardName, contents (billboard ID, creator) and valid session token

                // if billboard info searched is not valid e.g corresponding billboardName, id, and creator nonexistent or incorrect send back error

                // Edit can be made by this user to any billboard on list (even if currently scheduled)
                // if edit is made replace contents of billboard with new


            case DELETE_BILLBOARD:
                try {
                    assert authenticatedUser != null;
                    if (authenticatedUser.CanEditAllBillboards()) {
                        database.DeleteBillboard(req.getBillboardID()); // TODO - change argument to string
                        return new Response(true, "The billboard has successfully been deleted.");
                    } else
                        return permissionDeniedResponse;
                } catch (SQLException e) {
                    e.printStackTrace();
                    return new Response(false, "Billboard does not exist.");
                }

            case VIEW_SCHEDULED_BILLBOARDS:
            {
                // this request will only happen is user has 'Schedule Billboards' permission
                // should be triggered inside the ScheduleBillboards() GUI
                try {
                    assert authenticatedUser != null;

                    // client will send server a valid session
                    List<Schedule> allScheduledBillboards = database.getSchedules(LocalDateTime.now());

                    // if session token is valid server will respond with list of billboards that have been scheduled
                    // including billboardName, creator, time scheduled, and duration
                    return new Response(true, allScheduledBillboards);
                } catch (Exception e){}

                return new Response(false, "No billboards currently scheduled");
            }

                break;
            case SCHEDULE_BILLBOARD:
                // TODO - implement


                // check if session is valid e.g. expired, if not return failure and trigger relogin

                // this request will only happen is user has 'Schedule Billboards' permission
                // triggered inside the ScheduleBillboards() GUI

                //Client will send server billboardName, timeScheduled, duration, and valid session
                // (there also might be more information e.g for handling recurrence)

                // if billboard name is not found return error

                // Else server will add new billboard to schedule

                // if there is a billboard previously scheduled for the time of the newly scheduled billboard
                // new billboard will take precedence over previously scheduled billboard but will not delete previously scheduled

                // e.g billboard A scheduled from 10:00-11:00 and billboard B is then scheduled from 10:30-11
                // billboard A will display until 10:30 then billboard B will be displayed

                // once billboard is scheduled Server will send back an acknowledgement of success

                break;
            case REMOVE_SCHEDULED:
                try {
                    assert authenticatedUser != null;
                    if (authenticatedUser.CanEditAllBillboards()) {
                        database.UnscheduleBillboard(req.getBillboardID());
                        return new Response(true, "Billboard has been removed from the schedule.");
                    } else {
                        return permissionDeniedResponse;
                    }
                } catch (SQLException e) { // Will catch if the billboard does not exist or is not scheduled.
                    return new Response(false, "Billboard lookup failed.");
                }

            case LIST_USERS: {
                // request only happens if user has 'Edit Users' permission
                assert authenticatedUser != null;
                if (authenticatedUser.CanEditUsers()) {
                    // triggered inside EditUsers() GUI
                    try {
                        List<String> usernames = new ArrayList<>();
                        ResultSet rs = database.LookUpAllUserDetails();
                        while (rs.next()) {
                            usernames.add(rs.getString("user_name"));
                        }
                        return new Response(true, usernames);
                        // Server responds with list of usernames (CRA then says "and any other information your teams feels appropriate")
                    } catch (SQLException e) {
                        return new Response(false, "Lookup failed.");
                    }

                } else {
                    return permissionDeniedResponse;
                }
            }
            case CREATE_USER:
                // check if session is valid e.g. expired, if not return failure and trigger relogin
                // TODO - implement
                // request only happens if user has 'Edit Users' permission
                // triggered inside EditUsers() GUI
                if (authenticatedUser.CanEditUsers()) {
                    // Client will send server username, list of permissions, hashedPassword, and valid session token
                    // TODO - Fix spec compliance
                    User newUser = req.getUser();

                    // if username already exist send error
                    if (database.LookUpUserDetails(newUser.getSaltedCredentials().getUsername()).next() != false) {

                    } else {


                    }


                    // else Server will create user and send back acknowledgement of success

                } else {
                    return permissionDeniedResponse;
                }


                break;
            case GET_USER_PERMISSION:
                // check if session is valid e.g. expired, if not return failure and trigger relogin
                // TODO - implement
                // Client will send server a username and valid session token

                // if session user is requesting their own details return details, no permissions required

                // if session user is requesting details of another user, check permissions = 'Edit Users' == true then return details

                // else return false send error

                break;
            case SET_USER_PERMISSION:

                // request only happens if user has 'Edit Users' permission
                // triggered inside EditUsers() GUI
                if (authenticatedUser.CanEditUsers()) {

                    //FIXME - should only be passing credentials object through on this one
                    try {
                        User userToModify = new User(req.getUsername(), database);

                        boolean canEditUsers = userToModify.CanEditUsers();

                        boolean canEditAllBillboards = userToModify.CanEditAllBillboards();

                        boolean canScheduleBillboards = userToModify.CanScheduleBillboards();

                        boolean canCreateBillboards = userToModify.CanCreateBillboards();

                        userToModify.setEditAllBillBoards(canEditAllBillboards);
                        userToModify.setScheduleBillboards(canScheduleBillboards);
                        userToModify.setCanCreateBillboards(canCreateBillboards);

                        // Special case - can't remove own admin permissions
                        // FIXME - need to implement a nuanced response which explains that the rest of the perms changes were successful
                        if (collator.compare(authenticatedUser.getSaltedCredentials().getUsername(), req.getUsername()) != 0) {
                            userToModify.setEditUsers(canEditUsers);
                        }


                        database.UpdateUserDetails(userToModify);


                    } catch (NoSuchUserException e) {
                        e.printStackTrace();
                    }

                } else return permissionDeniedResponse;

                // Client will send server username(user whose permissions are to be changed),
                // list of permissions, and valid session token

                // if username does not exist return error

                // else if session user is requesting to remove their own "Edit User" permission return error

                // else Server change that users permissions and send back acknowledgement of success

                break;
            case SET_USER_PASSWORD:
                // TODO - implement in GUI

                //If the user has the edit users permission, or if they are just trying to change their own password,
                // they may....
                if (authenticatedUser.CanEditUsers() || collator.compare(authenticatedUser.getSaltedCredentials().getUsername(), req.getUsername()) == 0) {

                    User userToChange = null;

                    try {
                        userToChange = new User(req.getUsername(), database);
                    } catch (NoSuchUserException e) {
                        return new Response(false, "Could not find user");
                    }

                    if (userToChange != null) {
                        userToChange.setPasswordFromCredentials(userToChange.getSaltedCredentials(), database);
                        database.UpdateUserDetails(userToChange);
                    }

                } // else return false send error
                else return permissionDeniedResponse;


                break;
            case DELETE_USER:
                // check if session is valid e.g. expired, if not return failure and trigger relogin

                // request only happens if user has 'Edit Users' permission
                // triggered inside EditUsers() GUI
                if (authenticatedUser.CanEditUsers()) {

                    // Client will send username of user to be deleted and valid session token
                    String deletionCandidate = req.getUsername();
                    // if username != to username of session user (no user can delete themselves)
                    // Server will delete the user and send back acknowledgement of success
                    Collator collator = Collator.getInstance(Locale.ENGLISH);
                    if (collator.compare(req.getSession().serverUser.getSaltedCredentials().getUsername(), deletionCandidate) == 0) {
                        return new Response(false, "User cannot delete their own account");
                    } else {
                        try {
                            database.DeleteUser(deletionCandidate);
                            // TODO - respond if deletion successful
                        } catch (SQLException e) {
                            e.printStackTrace();
                            // TODO - respond if no user exists
                        }
                    }

                    // TODO if username deleted = creator of a billboard, billboard will no longer have owner registered in DB
                    // CRA says for team to decide what will happen in this circumstance

                } else return new Response(false, permissionDeniedResponse);
                break;
            case LOGOUT:

                // TODO - implement

                // server will expire session token and send back and acknowledgement
                break;
        }
        // If the request is invalid:
        return new Response(false, String.format("%s is not a valid request type", req.getRequestType()));
        //throw new IllegalStateException("Invalid request type: " + req.getRequestType());
    }

    //</editor-fold>

    public boolean closeConnection() {
        boolean closed = false; // For testing purposes only
        try {
            System.out.println("The connection to the client is being closed.");
            client.close();
            input.close();
            output.close();
            closed = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return closed;
    }
}
