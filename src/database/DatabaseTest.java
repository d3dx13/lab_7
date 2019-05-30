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
                String sql = "INSERT INTO ELEMENTS (dancer_name, feel, think, dynamics, dancer_position," +
                        " birthday, dancequality, creator) (?,?,?,?,?,?,?,?)";
                PreparedStatement pst = con.prepareStatement(sql);
                //pst.setString(1,String.valueOf(dancer.hashCode()+login.hashCode()));
                pst.setString(1,"lala");
                pst.setString(2,"ta");
                pst.setString(3,"udu");
                pst.setString(4,"sasd");
                pst.setString(5,"saaa");
                pst.setString(6, "ass");
                pst.setString(7,"Sddf");
                pst.setString(8,"aaaaaa");
                pst.executeUpdate();
                pst.close();
                /*Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM LAB7");
                while (rs.next())
                {
                    //String str = rs.getString("name_id") + ":" + rs.getString("first_name");
                    //System.out.println("Name:"+str);
                }

                rs.close();
                stmt.close();*/
            }
            finally {
                con.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
