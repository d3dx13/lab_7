package lab_7.server;

import jdk.nashorn.internal.runtime.ECMAException;
import lab_7.world.creation.Dancer;

import java.sql.*;
import java.util.concurrent.PriorityBlockingQueue;

public class DatabaseSQL {
    public static String urlStandart = "jdbc:postgresql://localhost:5432/lab7";
    public static String loginStandart = "postgres";
    public static String passwordStandart = "postgres";

    public static boolean insertToDB(Dancer dancer)
    {
        try {
            Class.forName("org.postgresql.Driver");
            Connection con = DriverManager.getConnection(urlStandart,loginStandart,passwordStandart);
            try {
                String sql = "INSERT INTO LAB7 (DANCER_NAME, FEEL_STATE, THINK_STATE, BIRTHDAY, DANCE_QUALITY)" +
                        " VALUES (?,?,?,?,?)";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1,dancer.name);
                pst.setString(2,dancer.feelState.toString());
                pst.setString(3,dancer.thinkState.toString());
                //pst.setDate(4, java.sql.Date.from(dancer.birthday.toInstant())); ???
                pst.setInt(5,dancer.getDanceQuality());
                pst.executeUpdate();
                pst.close();

            }
            finally { con.close(); return true;}
        }
        catch (Exception e)
        { e.printStackTrace();return false; }
    }

    public static PriorityBlockingQueue<Dancer> getFromDB()
    {
        try {
            Class.forName("org.postgresql.Driver");
            Connection con = DriverManager.getConnection(urlStandart, loginStandart, passwordStandart);
            PriorityBlockingQueue<Dancer> dancers = new PriorityBlockingQueue<>();
            try {
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM LAB7");
                while (rs.next())
                {
                    Dancer dancer = new Dancer(rs.getString("DANCER_NAME"));
                    for(int i=2;i<=rs.getMetaData().getColumnCount();i++)
                    {
                            dancer.setParam(rs.getMetaData().getColumnName(i),rs.getString(i));
                    }
                    dancer.setParam("dancer_position",rs.getString("dancer_position"));
                    dancers.add(dancer);
                }

                rs.close();
                stmt.close();
            }
            finally
            {
                con.close();
                return dancers;
            }
        }
        catch (Exception e) {

            e.printStackTrace();
            return null;
        }

    }

}
