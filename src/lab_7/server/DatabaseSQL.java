package lab_7.server;

import jdk.nashorn.internal.runtime.ECMAException;
import lab_7.world.creation.Dancer;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.concurrent.PriorityBlockingQueue;

public class DatabaseSQL {
    public static String urlStandart = "jdbc:postgresql://localhost:5432/lab7";
    public static String loginStandart = "postgres";
    public static String passwordStandart = "postgres";

    public static boolean insertToDB(Dancer dancer,String login)
    {
        try {
            Class.forName("org.postgresql.Driver");
            Connection con = DriverManager.getConnection(urlStandart,loginStandart,passwordStandart);
            try {
                String sql = "INSERT INTO ELEMENTS (dancer_name, feel, think, dynamics, dancer_position," +
                        " birthday, dancequality, creator) VALUES (?,?,?,?,?,?,?,?)";
                PreparedStatement pst = con.prepareStatement(sql);
                //pst.setString(1,String.valueOf(dancer.hashCode()+login.hashCode()));
                pst.setString(1,dancer.name);
                pst.setString(2,dancer.feelState.toString());
                pst.setString(3,dancer.thinkState.toString());
                pst.setString(4,dancer.getDynamics().toString());
                pst.setString(5,dancer.getPosition().toString());
                pst.setString(6, dancer.birthday.toString());
                pst.setString(7,String.valueOf(dancer.getDanceQuality()));
                pst.setString(8,login);
                pst.executeUpdate();
                pst.close();
            }
            catch (Exception e) {e.printStackTrace();}
            finally { con.close(); return true;}
        }
        catch (Exception e)
        { e.printStackTrace();return false; }
    }



    public static LinkedList<Dancer> getFromDB(String login)
    {
        try {
            Class.forName("org.postgresql.Driver");
            Connection con = DriverManager.getConnection(urlStandart, loginStandart, passwordStandart);
            LinkedList<Dancer> dancers = new LinkedList<>();
            try {
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM Elements");
                while (rs.next())
                {
                    if (rs.getString("creator").equals(login)) {
                        Dancer dancer = new Dancer(rs.getString("DANCER_NAME"));
                        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                            if (!rs.getString(i).equals("..."))
                                dancer.setParam(rs.getMetaData().getColumnName(i), rs.getString(i));
                        }
                        dancer.setParam("dancer_position", rs.getString("dancer_position"));
                        dancer.birthday = OffsetDateTime.parse(rs.getString("birthday"));
                        dancers.add(dancer);
                    }
                }
                rs.close();
                stmt.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
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

    public static void removeFromDB(Dancer dancerToKill,String login)
    {

        try {
            Class.forName("org.postgresql.Driver");
            Connection con = DriverManager.getConnection(urlStandart, loginStandart, passwordStandart);
            try {
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM Elements");
                while (rs.next())
                {
                    if (rs.getString("creator").equals(login)) {
                        Dancer dancer = new Dancer(rs.getString("DANCER_NAME"));
                        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                            if (!rs.getString(i).equals("..."))
                                dancer.setParam(rs.getMetaData().getColumnName(i), rs.getString(i));
                        }
                        dancer.setParam("dancer_position", rs.getString("dancer_position"));
                        dancer.birthday = OffsetDateTime.parse(rs.getString("birthday"));
                        if (dancer.equals(dancerToKill))
                        {
                            String toDell = "DELETE FROM Elements WHERE"+" Elements.dancer_name=\'"+dancerToKill.name+"\' AND"+
                                    " Elements.feel=\'"+dancerToKill.feelState.toString()+"\' AND Elements.think=\'"+
                                    dancerToKill.thinkState.toString()+"\' AND Elements.dynamics=\'"+
                                    dancerToKill.getDynamics().toString()+"\' AND Elements.dancer_position=\'"+
                                    dancerToKill.getPosition().toString()+"\' AND Elements.birthday=\'"+
                                    dancer.birthday+"\' AND Elements.dancequality=\'"+
                                    String.valueOf(dancerToKill.getDanceQuality())+"\' AND Elements.creator=\'"+ login+"\'";
                            stmt.executeUpdate(toDell);
                            break;
                        }

                    }
                }
                rs.close();
                stmt.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                con.close();
            }
        }
        catch (Exception e) {

            e.printStackTrace();
        }

    }

}
