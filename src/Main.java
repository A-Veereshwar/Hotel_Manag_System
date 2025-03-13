import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("src/config.properties")) {
            properties.load(fis);
            //load db credentials
            String db_username = properties.getProperty("db.username");
            String db_password = properties.getProperty("db.password");
            String url = "jdbc:postgresql://localhost:5432/HMS";

            //Establish connection with db
            Connection connection = DriverManager.getConnection(url, db_username,db_password);
            Statement stm = connection.createStatement();
            boolean flag = true;
            System.out.println("---------------------------------------------");
            System.out.println("Welcome to Hotel Management System! ");
            while(flag){
                System.out.println("---------------------------------------------");
                Scanner sc = new Scanner(System.in);
                System.out.println("1. Reserve a Room");
                System.out.println("2. View Reservations");
                System.out.println("3. Check Room Number");
                System.out.println("4. Edit Reservation");
                System.out.println("5. Delete Reservation");
                System.out.println("0. Exit System");
                System.out.println("---------------------------------------------");
                System.out.println("Enter an option: ");
                int option = sc.nextInt();
                switch (option){
                    case 1 :
                        reserveRoom(sc, stm);
                        break;
                    case 2 :
                        viewReservations(stm);
                        break;
                    case 3 :
                        checkRoomNumber(sc, stm);
                        break;
                    case 4 :
                        editReservation(connection, sc);
                        break;
                    case 5 :
                        deleteReservation(connection, sc);
                        break;
                    default :
                        flag = false;
                        System.out.println("Thank you!");
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading config.properties file: " + e.getMessage());
        } catch (Exception e){
            System.out.println(e.getMessage());
        }

    }

    //Delete a reservation using id and name

    public static void deleteReservation(Connection connection, Scanner sc) throws SQLException{
        Statement stm = connection.createStatement();
        try{
            System.out.println("---------------------------------------------");
            System.out.println("Enter guest ID : ");
            int id = sc.nextInt();
            System.out.println("Enter guest name to delete record: ");
            String guest_name = sc.next();
            String query = "DELETE FROM reservations "+
                    "WHERE reservation_id = "+id+" AND guest_name = '"+guest_name+"'";
            int recordsEffected = stm.executeUpdate(query);
            if(recordsEffected > 0){
                System.out.println("Successfully Deleted User with ID: "+id);
            }else{
                System.out.println("Error in deleting the User! ");
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    //Edit data in the Reservations table

    public static void editReservation(Connection conn,Scanner sc) throws SQLException{
        Statement stm = conn.createStatement();
        try{
            System.out.println("---------------------------------------------");
            System.out.println("Enter Guest ID to edit: ");
            int id = sc.nextInt();
            sc.nextLine();
            if(!reservationExists(stm, id)){
                System.out.println("Seems like there's no guest with that ID !!");
                return;
            }
            System.out.println("Change Guest Name: y/n ?");
            String changeVal = sc.next().toLowerCase();
            if(changeVal.equals("y") || changeVal.equals("yes")){
                System.out.println("Enter New Name: ");
                String changed_name = sc.next();
                String query = "UPDATE reservations"+
                        " SET guest_name = '"+changed_name+
                        "' WHERE reservation_id = "+id;
                stm.executeUpdate(query);
            }
            System.out.println("Change Room Number: y/n ?");
            changeVal = sc.next().toLowerCase();
            if(changeVal.equals("y") || changeVal.equals("yes")){
                System.out.println("Enter New Room Number: ");
                int new_room_number = sc.nextInt();
                String query = "UPDATE reservations"+
                        " SET room_alloted = "+new_room_number+
                        " WHERE reservation_id = "+id;
                stm.executeUpdate(query);
            }
            System.out.println("Change mobile number: y/n ?");
            changeVal = sc.next().toLowerCase();
            if(changeVal.equals("y") || changeVal.equals("yes")){
                System.out.println("Enter New Mobile Number: ");
                String new_mobile_number = sc.next();
                String query = "UPDATE reservations"+
                        " SET mobile_no = '"+new_mobile_number+
                        "' WHERE reservation_id = "+id;
                stm.executeUpdate(query);
            }
            System.out.println("Update Successful!");
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    // Method to check if reservation exists with the given id
    public static boolean reservationExists(Statement stm, int id){
        try{
            String query = "SELECT reservation_id FROM reservations WHERE reservation_id = "+id;
            ResultSet rs = stm.executeQuery(query);
            return rs.next();
        }catch (Exception e){
            System.out.println(e.getMessage());
            return false;
        }
    }

    //Reserve a Room (if available)

    public static void reserveRoom(Scanner sc, Statement st){
        try {
            System.out.println("---------------------------------------------");
            System.out.println("Enter guest name : ");
            String guest_name = sc.next();
            System.out.println("Enter room choice : ");
            int room_num = sc.nextInt();
            System.out.println("Enter 10 digit mobile number : ");
            String mobile_num = sc.next();
            String query = "INSERT INTO reservations (guest_name, room_alloted, mobile_no) " +
                    " VALUES ('" + guest_name + "', " + room_num + ", '" + mobile_num + "')";
            try{
                int rowsEffected = st.executeUpdate(query);
                if(rowsEffected > 0){
                    System.out.println("Reservation Successful!");
                }else{
                    throw new RuntimeException("Room number might be reserved already!!");
                }
            }catch (SQLException e){
                System.out.println(e.getMessage());
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    //View Entire Reservations Table

    public static void viewReservations(Statement stm) {
        String query = "SELECT * FROM reservations";
        try{
            ResultSet rs = stm.executeQuery(query);
            System.out.println("---- Current Reservations ----");
            System.out.println("+-----------------+-------------------------+---------------+-------------------+----------------------------+");
            System.out.println("| Reservation ID  | Guest Name              | Room Number   | Mobile Number     | Date of Reservation        |");
            System.out.println("+-----------------+-------------------------+---------------+-------------------+----------------------------+");
            while (rs.next()){
                int res_id = rs.getInt("reservation_id");
                String g_name = rs.getString("guest_name");
                int room_no = rs.getInt("room_alloted");
                String mobile = rs.getString("mobile_no");
                String res_date = rs.getTimestamp("reservation_date").toString();
                System.out.printf("| %-15d | %-23s | %-13d | %-17s | %-25s |\n", res_id, g_name, room_no, mobile, res_date);
            }
            System.out.println("+-----------------+-------------------------+---------------+-------------------+----------------------------+");
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    //Check Room Number Reserved with guest_id & guest_name

    public static void checkRoomNumber(Scanner sc, Statement stm){
        try{
            System.out.println("---------------------------------------------");
            System.out.println("Enter Guest ID: ");
            int check_id = sc.nextInt();
            System.out.println("Enter Guest Name: ");
            String guest_name = sc.next();
            String query = "SELECT room_alloted FROM reservations "+
                    "WHERE reservation_id = "+check_id+" AND guest_name = '"+guest_name+"'";
            ResultSet rs = stm.executeQuery(query);
            if(rs.next()){
                int room_num = rs.getInt("room_alloted");
                System.out.println();
                System.out.println("Room number for reservation ID: "+check_id+" and Guest: "+guest_name+" is at : "+room_num);
                System.out.println();
            }else{
                System.out.println("No reservation was found with this details!!!");
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}