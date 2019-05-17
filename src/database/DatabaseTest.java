package database;
import java.sql.*;

public class DatabaseTest {
    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://localhost:5432/lab7";
            String login = "postgres";
            String password = "postgres";
            Connection con = DriverManager.getConnection(url,login,password);
            try {
                String leName = "Lewis";
                String leSurName="Hamilton";
                String sql = "INSERT INTO LAB7 (FIRST_NAME, LAST_NAME) VALUES (?,?)";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1,leName);
                pst.setString(2,leSurName);
                pst.executeUpdate();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM LAB7");
                while (rs.next())
                {
                    String str = rs.getString("name_id") + ":" + rs.getString("first_name");
                    System.out.println("Name:"+str);
                }

                rs.close();
                stmt.close();
            }
            finally {
                con.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
