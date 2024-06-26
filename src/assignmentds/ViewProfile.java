package assignmentds;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class ViewProfile {
    
    public static void main(User user) {

        // Call displayProfile method with the provided user
        displayProfile(user);
        Home.main(user);
    }
    
    public static void displayOtherStudentProfile(User user) {
        System.out.println("Email: " + user.getEmail());
        System.out.println("Username: " + user.getUsername());
        System.out.println("Location: (" + user.getLocationCoordinate().getLatitude() + ", " + user.getLocationCoordinate().getLongitude() + ")");
        System.out.println("Friends: ");
        for (User friend : user.getFriends()) {
            System.out.println("- " + friend.getUsername());
        }
    }

    public static void displayProfile(User user) {
        boolean validChoice = false;
        Scanner sc = new Scanner(System.in);

        do {
            displaySelection();
            System.out.print("Choice: ");
            char choice = sc.next().charAt(0);
            sc.nextLine();
            switch (choice) {
                case '1' :
                    validChoice = true;
                    String blue = "\u001B[34m"; 
                    System.out.println(blue + "   ___            __ _ _         _____        __                            _   _             \n" +
                                       blue + "  / _ \\_ __ ___  / _(_) | ___    \\_   \\_ __  / _| ___  _ __ _ __ ___   __ _| |_(_) ___  _ __  \n" +
                                       blue + " / /_)/ '__/ _ \\| |_| | |/ _ \\    / /\\/ '_ \\| |_ / _ \\| '__| '_ ` _ \\ / _` | __| |/ _ \\| '_ \\ \n" +
                                       blue + "/ ___/| | | (_) |  _| | |  __/ /\\/ /_ | | | |  _| (_) | |  | | | | | | (_| | |_| | (_) | | | |\n" +
                                       blue + "\\/    |_|  \\___/|_| |_|_|\\___| \\____/ |_| |_|_|  \\___/|_|  |_| |_| |_|\\__,_|\\__|_|\\___/|_| |_|\n" +
                                       blue + "                                                                                              ");
                    System.out.println("Email: " + user.getEmail());
                    System.out.println("Username: " + user.getUsername());
                    System.out.println("Role: " + getRoleName(user.getRole()));
                    System.out.println("Location Coordinate: " + user.getLocationCoordinate());

                    // Additional information based on role
                    switch (user.getRole()) {
                        case 1: // Young Students
                            System.out.println("\nPoints: " + user.getCurrentPoints());
                            System.out.println("Friends: ");
                            displayFriends(user.getFriends());
                            System.out.println("Parents: ");
                            displayParents(ParentChildRelationship.loadParent(user.getUsername()));
                            break;
                        case 2: // Parents
                            System.out.println("\nPast Bookings: ");
                            displayChildrensPastBookings(user.getUsername());
                            System.out.println("\nChildren: ");
                            displayChildren(ParentChildRelationship.loadChildren(user.getUsername()));
                            break;
                        case 3: // Educators
                            System.out.println("\nNumber of Quizzes Created: " + CreateQuiz.getNumQuizzesCreated(user.getUsername(), user.getRole()));
                            System.out.println("Number of Events Created: " + CreateEvent.getNumEventsCreated(user.getUsername(), user.getRole()));
                            //MODIFIED BY DY
                            break;
                        default:
                            System.out.println("Invalid role.");
                    }
                break;
                case '2' :
                    validChoice = true;

                    if (user.getRole() == 1) {
                        if (checkParentMax(user.getUsername())){
                            System.out.print("Enter username of parent : ");
                            String parentUsername = sc.next();
                            String parentEmail = verifyUser(parentUsername, 2);
                            if (parentEmail != null) {
                                System.out.println("Parent found with email : " + parentEmail);
                                System.out.print("Is this the correct parent? Type 'Yes' to confirm: ");
                                String confirm = sc.next();
                                sc.nextLine();
                                if ("yes".equalsIgnoreCase(confirm)) {
                                    if (!relationshipExists(parentUsername, user.getUsername())) {
                                        addRelationship(parentUsername, user.getUsername());
                                        displayProfile(user);
                                    } else {
                                        System.out.println("\nRelationship already existed\n");
                                        displayProfile(user);
                                    }
                                } else {
                                    System.out.println("\nAdding parent operation failed.\n");
                                    validChoice = false;
                                }
                            } else {
                                System.out.println("\nParent with the username entered not found.\n");
                                validChoice = false;
                            }
                        } else {
                            System.out.println("\nCurrent account already register with 2 parents.\n");
                            validChoice = false;
                        }
                    } else if (user.getRole() == 2) {
                        System.out.print("Enter username of the child : ");
                        String childUsername = sc.next();
                        String childEmail = verifyUser(childUsername, 1);
                        if (childEmail!=null) {
                            System.out.println("Child found with email : " + childEmail);
                            System.out.print("Is this the correct child? Type 'Yes' to confirm: ");
                            String confirm = sc.next();
                            sc.nextLine();
                            if ("yes".equalsIgnoreCase(confirm)) {
                                if (!relationshipExists(user.getUsername(), childUsername)) {
                                    addRelationship(user.getUsername(), childUsername);
                                    displayProfile(user);
                                } else {
                                    System.out.println("\nRelationship already existed\n");
                                    displayProfile(user);
                                }
                            } else {
                                System.out.println("\nAdding child operation failed\n");
                                validChoice = false;
                            }
                        } else {
                            System.out.println("\nChild with the username entered not found.\n");
                            validChoice = false;
                        }
                    } else if (user.getRole() == 3) {
                        System.out.println("\nEducator does not require adding parents or children.\n");
                        validChoice = false;
                    }
                break;
                default:
                    System.out.println("Invalid selection");
            }
        } while (!validChoice);
    }

    private static void displaySelection( ) {
        System.out.println("\n<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>");
        System.out.println("1. View Profile");
        System.out.println("2. Add Parent or Child");
        System.out.println("<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>");
    }

    private static boolean checkParentMax(String username) {
        String query = "SELECT COUNT(parent_username) AS parent_count FROM userdb.parentchildrelationship WHERE child_username = ?";
        try {
            Connection conn = DBOperations.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt("parent_count");
                return count < 2; // Returns true if less than 2 parents are registered
            }
        } catch (SQLException e) {
            System.out.println("Checking parent count error : " + e.getMessage());
        }
        return false;
    }

    public static boolean relationshipExists(String parentUsername, String childUsername) {
        String sql = "SELECT 1 FROM userdb.parentchildrelationship WHERE parent_username = ? AND child_username = ?";
        try (Connection conn = DBOperations.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, parentUsername);
            pstmt.setString(2, childUsername);
            ResultSet rs = pstmt.executeQuery();
            boolean exists = rs.next();
            System.out.println("Checking existence for " + parentUsername + " and " + childUsername + ": " + exists);
            return exists;
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
        }
        return false;
    }

    private static String verifyUser(String username, int role) {
        String query = "SELECT email FROM userdb.users WHERE username = ? AND role = ?";

        try (Connection conn = DBOperations.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query)){
            preparedStatement.setString(1, username);
            preparedStatement.setInt(2, role);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("email");
            }
        } catch (SQLException e) {
            System.out.println("Query failed : " + e.getMessage());
        }
        return null;
    }

    private static void addRelationship(String parentUsername, String childUsername) {
        String magenta = "\u001B[35m";
        String query = "INSERT INTO userdb.parentchildrelationship (parent_username, child_username) VALUES (?, ?)";

        try (Connection conn = DBOperations.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, parentUsername);
            preparedStatement.setString(2, childUsername);
            preparedStatement.executeUpdate();
            System.out.println(magenta + "  ___     _      _   _             _    _                _    _        _                            __      _ _      _ \n" +
                magenta + " | _ \\___| |__ _| |_(_)___ _ _  __| |_ (_)_ __   __ _ __| |__| |___ __| |  ____  _ __ __ ___ ______/ _|_  _| | |_  _| |\n" +
                magenta + " |   / -_) / _` |  _| / _ \\ ' \\(_-< ' \\| | '_ \\ / _` / _` / _` / -_) _` | (_-< || / _/ _/ -_|_-<_-<  _| || | | | || |_|\n" +
                magenta + " |_|_\\___|_\\__,_|\\__|_\\___/_||_/__/_||_|_| .__/ \\__,_\\__,_\\__,_\\___\\__,_| /__/\\_,_\\__\\__\\___/__/__/_|  \\_,_|_|_|\\_, (_)\n" +
                magenta + "                                         |_|                                                                    |__/   ");
        } catch (SQLException e) {
            System.out.println("Insertion failed : " + e.getMessage());
        }
    }

    // Method to get role name based on role number
    private static String getRoleName(int role) {
        switch (role) {
            case 1:
                return "Young Students";
            case 2:
                return "Parents";
            case 3:
                return "Educators";
            default:
                return "Unknown";
        }
    }

    // Method to display friends list
    private static void displayFriends(List<User> friends) {
        for (User friend : friends) {
            System.out.println("- " + friend.getUsername());
        }
    }

    // Method to display parents list
    private static void displayParents(List<String> parents) {
        for (String parent : parents) {
            System.out.println("- " + parent);
        }
    }

    // Method to display children list
    private static void displayChildren(List<String> children) {
        for (String child : children) {
            System.out.println("- " + child);
        }
    }
    
    private static void displayChildrensPastBookings(String parentUsername) {
        List<String> childrenUsernames = ParentChildRelationship.loadChildren(parentUsername);

        for (String childUsername : childrenUsernames) {
            System.out.println("\nPast Bookings for Child: " + childUsername);
            displayPastBookingsForChild(parentUsername, childUsername);
        }
    }

    // Method to display past bookings for a specific child belonging to the parent
    private static void displayPastBookingsForChild(String parentUsername, String childUsername) {
        String query = "SELECT destination, tour_date FROM userdb.tourbookings WHERE parent_username = ? AND child_username = ? AND tour_date <= CURDATE()";
        try (Connection conn = DBOperations.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, parentUsername);
            pstmt.setString(2, childUsername);
            ResultSet rs = pstmt.executeQuery();
            boolean hasBookings = false;
            while (rs.next()) {
                hasBookings = true;
                String destination = rs.getString("destination");
                String tourDate = rs.getString("tour_date");
                System.out.println("- Destination: " + destination + ", Date: " + tourDate);
            }
            if (!hasBookings) {
                System.out.println("No past bookings found for child: " + childUsername);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching past bookings for child: " + e.getMessage());
        }
    }
}





