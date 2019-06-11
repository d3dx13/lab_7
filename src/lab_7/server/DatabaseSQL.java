package lab_7.server;

import com.sun.mail.iap.ByteArray;
import jdk.nashorn.internal.runtime.ECMAException;
import lab_7.message.Account;
import lab_7.message.Message;
import lab_7.world.creation.Dancer;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseSQL {
    public static String urlStandart = "jdbc:postgresql://localhost:5432/lab7";
    public static String loginStandart = "postgres";
    public static String passwordStandart = "postgres";
    public static String urlDB = urlStandart;
    public static String loginDB = loginStandart;
    public static String passwordDB = passwordStandart;


    public static boolean saveAccount(Account account) {
        try {
            Class.forName("org.postgresql.Driver");
            Connection con = DriverManager.getConnection(urlDB, loginDB, passwordDB);
            ConcurrentHashMap<String,Account> accs = loadAccounts();
            try {
                boolean exists = true;
                for (Account acc: accs.values())
                    {
                    if (acc.login.equals(account.login)) {
                        exists = false;
                    }
                }
                if (exists) {
                    String sql = "INSERT INTO USERS (login, publicKey, privateKey, registrationDate, lastAccessTime)" +
                            " VALUES (?,?,?,?,?)";
                    PreparedStatement pst = con.prepareStatement(sql);
                    pst.setString(1, String.valueOf(account.login));
                    pst.setBytes(2, account.privateKey);
                    pst.setBytes(3, account.publicKey);
                    pst.setString(4, String.valueOf(account.registrationDate));
                    pst.setString(5, String.valueOf(account.lastAccessTime));
                    pst.executeUpdate();
                    pst.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                con.close();
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static ConcurrentHashMap<String, Account> loadAccounts() {
        try {
            Class.forName("org.postgresql.Driver");
            Connection con = DriverManager.getConnection(urlDB, loginDB, passwordDB);
            ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<String, Account>();
            try {
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM users");
                while (rs.next()) {

                    Account acc = new Account();
                    acc.login = rs.getString(1);
                    acc.publicKey = rs.getBytes(2);
                    acc.privateKey = rs.getBytes(3);
                    acc.registrationDate = rs.getString(4);
                    acc.lastAccessTime = Long.valueOf(rs.getString(5));

                    accounts.put(acc.login, acc);//???

                }
                rs.close();
                stmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                con.close();
                return accounts;
            }
        } catch (Exception e) {

            e.printStackTrace();
            return null;
        }

    }


    public static boolean newTableDefault() {
        try {
            urlDB = urlStandart;
            loginDB = loginStandart;
            passwordDB = passwordStandart;
            Class.forName("org.postgresql.Driver");
            Connection con = DriverManager.getConnection(urlDB, loginDB, passwordDB);
            if (con.isClosed()) {
                try {
                    String sql = "DROP TABLE IF EXISTS Elements;\n" +
                            " \n" +
                            "CREATE TABLE Elements\n" +
                            "(\n" +
                            "       DANCER_NAME VARCHAR(50) NOT NULL,\n" +
                            "       FEEL VARCHAR(50),\n" +
                            "       THINK VARCHAR(50),\n" +
                            "       DYNAMICS VARCHAR(50),\n" +
                            "       DANCER_POSITION VARCHAR(50),\n" +
                            "       BIRTHDAY VARCHAR(50),\n" +
                            "       danceQuality VARCHAR(50),\n" +
                            "       \"owner\" VARCHAR(50)\n" +
                            ");";
                    PreparedStatement psmt = con.prepareStatement(sql);
                    psmt.executeUpdate();
                    psmt.close();
                    return true;
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    con.close();
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean insertToDB(Dancer dancer, String login) {
        try {
            Class.forName("org.postgresql.Driver");
            Connection con = DriverManager.getConnection(urlDB, loginDB, passwordDB);
            try {
                String sql = "INSERT INTO ELEMENTS (dancer_name, feel, think, dynamics, dancer_position," +
                        " birthday, dancequality, \"owner\") VALUES (?,?,?,?,?,?,?,?)";
                PreparedStatement pst = con.prepareStatement(sql);
                //pst.setString(1,String.valueOf(dancer.hashCode()+login.hashCode()));
                pst.setString(1, dancer.name);
                pst.setString(2, dancer.feelState.name());
                pst.setString(3, dancer.thinkState.name());
                pst.setString(4, dancer.getDynamics().name());
                pst.setString(5, dancer.getPosition().name());
                pst.setString(6, dancer.birthday.toString());
                pst.setString(7, String.valueOf(dancer.getDanceQuality()));
                pst.setString(8, login);
                pst.executeUpdate();
                pst.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                con.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static LinkedList<Dancer> getFromDB(String login) {
        try {
            Class.forName("org.postgresql.Driver");
            Connection con = DriverManager.getConnection(urlDB, loginDB, passwordDB);
            LinkedList<Dancer> dancers = new LinkedList<>();
            try {
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM Elements");
                while (rs.next()) {

                    Dancer dancer = new Dancer(rs.getString("DANCER_NAME"));
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        if (!rs.getString(i).equals("..."))
                            dancer.setParam(rs.getMetaData().getColumnName(i), rs.getString(i));
                    }
                    dancer.setParam("dancer_position", rs.getString("dancer_position"));
                    dancer.birthday = OffsetDateTime.parse(rs.getString("birthday"));
                    dancers.add(dancer);

                }
                rs.close();
                stmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                con.close();
                return dancers;
            }
        } catch (Exception e) {

            e.printStackTrace();
            return null;
        }

    }

    public static void removeFromDB(Dancer dancerToKill, String login) {

        try {
            Class.forName("org.postgresql.Driver");
            Connection con = DriverManager.getConnection(urlDB, loginDB, passwordDB);
            try {
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM Elements");
                while (rs.next()) {
                    if (rs.getString("owner").equals(login)) {
                        Dancer dancer = new Dancer(rs.getString("DANCER_NAME"));
                        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                            if (!rs.getString(i).equals("..."))
                                dancer.setParam(rs.getMetaData().getColumnName(i), rs.getString(i));
                        }
                        dancer.setParam("dancer_position", rs.getString("dancer_position"));
                        dancer.birthday = OffsetDateTime.parse(rs.getString("birthday"));
                        if (dancer.equals(dancerToKill)) {
                            String toDell = "DELETE FROM Elements WHERE" + " Elements.dancer_name=\'" + dancerToKill.name + "\' AND" +
                                    " Elements.feel=\'" + dancerToKill.feelState.name() + "\' AND Elements.think=\'" +
                                    dancerToKill.thinkState.name() + "\' AND Elements.dynamics=\'" +
                                    dancerToKill.getDynamics().name() + "\' AND Elements.dancer_position=\'" +
                                    dancerToKill.getPosition().name() + "\' AND Elements.birthday=\'" +
                                    dancer.birthday + "\' AND Elements.dancequality=\'" +
                                    String.valueOf(dancerToKill.getDanceQuality()) + "\' AND Elements.owner=\'" + login + "\'";
                            stmt.executeUpdate(toDell);
                            break;
                        }

                    }
                }
                rs.close();
                stmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                con.close();
            }
        } catch (Exception e) {

            e.printStackTrace();
        }

    }


    public synchronized static String getInfoSQL(){
        return "EMPTY";
    }


}
