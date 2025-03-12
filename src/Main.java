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
            while(flag){
                System.out.println("Welcome to Hotel Management System! ");
                Scanner sc = new Scanner(System.in);
                System.out.println("1. Reserve a Room");
                System.out.println("2. Check Reservation");
                System.out.println("3. Check Room Number");
                System.out.println("4. Edit Reservation");
                System.out.println("5. Delete Reservation");
                System.out.println("0. Exit System");
                System.out.println("Choose an option: ");
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
                        break;
                    case 5 :
                        break;
                    default :
                        flag = false;
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading config.properties file: " + e.getMessage());
        } catch (Exception e){
            System.out.println(e.getMessage());
        }

    }
    public static void reserveRoom(Scanner sc, Statement st){
        try {
            System.out.println("Enter guest name : ");
            String guest_name = sc.next();
            System.out.println("Enter room choice : ");
            int room_num = sc.nextInt();
            System.out.println("Enter mobile number : ");
            String mobile_num = sc.next();
            String query = "INSERT INTO reservations (guest_name, room_alloted, mobile_no) " +
                    "VALUES ('" + guest_name + "', " + room_num + ", '" + mobile_num + "')";
            try(st){
                int rowsEffected = st.executeUpdate(query);
                if(rowsEffected > 0){
                    System.out.println("Reservation Successful!");
                }else{
                    throw new RuntimeException("Room number might be reserved already!!");
                }
            }
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }
    public static void viewReservations(Statement stm) throws SQLException{
        String query = "SELECT * FROM reservations";
        try(stm){
            ResultSet rs = stm.executeQuery(query);
            System.out.println("---- Current Reservations ----");
            System.out.println("+-----------------+-------------------------+---------------+-------------------+---------------------------+");
            System.out.println("| Reservation ID  | Guest Name              | Room Number   | Mobile Number     | Date of Reservation       |");
            System.out.println("+-----------------+-------------------------+---------------+-------------------+---------------------------+");
            while (rs.next()){
                int res_id = rs.getInt("reservation_id");
                String g_name = rs.getString("guest_name");
                int room_no = rs.getInt("room_alloted");
                String mobile = rs.getString("mobile_no");
                String res_date = rs.getTimestamp("reservation_date").toString();
                System.out.printf("| %-15d | %-23s | %-13d | %-17s | %-25s |\n", res_id, g_name, room_no, mobile, res_date);
            }
            System.out.println("+-----------------+-------------------------+---------------+-------------------+---------------------------+");
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
    public static void checkRoomNumber(Scanner sc, Statement stm){
        try(stm){
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