/**
 * Copyright (C) John Robbins, Elliott Sobek, Zac Batog.
 * Github profiles:
 * John Robbins (https://github.com/reboss),
 * Elliott Sobek (https://github.com/ElliottSobek),
 * Zac Batog (https://github.com/batogz) 
 */

package ccat_model;

import ccat_view.MainMenu.UserInfo;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;

/**
 *
 * @author Elliott
 */
public class UserModel {

    private final Connection con;
    private final String salt;
    private final UserInfo currentUser;
    
    /**
     * 
     * @throws java.lang.ClassNotFoundException
     * @throws java.sql.SQLException
     */
    public UserModel() throws ClassNotFoundException, SQLException {
        this.con = getConnection();
        this.salt = "M5@aG9:[2cY0";
        this.currentUser = null;
    }

    /**
     *
     * @param currentUser
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public UserModel(UserInfo currentUser) throws ClassNotFoundException, SQLException {
        this.con = getConnection();
        this.salt = "M5@aG9:[2cY0";
        this.currentUser = currentUser;
    }

    /**
     *
     * @return @throws ClassNotFoundException
     * @throws SQLException
     */
    private Connection getConnection() throws ClassNotFoundException, SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:CCAT.db");
            return connection;
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println(e);
        }
        return null;
    }

    /**
     *
     * @param user
     * @param pass
     * @return
     */
    public boolean isValidCredentials(String user, String pass) {
        if (isUserInDb(user)) {
            try {
                PreparedStatement statement
                        = con.prepareStatement("SELECT password FROM users "
                                + "WHERE username = '" + user + "'");
                ResultSet result = statement.executeQuery();
                result.next();
                return result.getString("password").equals(MD5(pass + this.salt));
            } catch (Exception e) {
                System.err.println(e);
            }
            return false;
        } else {
            // User not in database
        }
        return false;
    }

    /**
     *
     * @param user
     * @return
     */
    public boolean isAdmin(String user) {
        if (isUserInDb(user)) {
            try {
                PreparedStatement statement
                        = con.prepareStatement("SELECT users.admin FROM users "
                                + "WHERE users.username = '" + user + "'");
                ResultSet result = statement.executeQuery();
                result.next();
                int adminStatus = result.getInt("users.admin");
                return adminStatus == 1;
            } catch (SQLException | NumberFormatException e) {
                System.err.println(e);
            }
        } else {
            // User not in database
        }
        return false;
    }

    /**
     *
     * @param fName
     * @param lName
     * @param password
     * @param access
     */
    public void addUser(String fName, String lName, String password, int access) {
        // Make auto username, createusername handles for isUserInDb thus no checking needed
        String username = createUserName(fName, lName);
        String pass = MD5(password + this.salt);
        try {
            PreparedStatement posted = con.prepareStatement("INSERT INTO "
                    + "users (fName, lName, username, password, admin) VALUES "
                    + "('" + fName + "', '" + lName + "', '" + username + "', '"
                    + pass + "', " + access + ")");
            posted.executeUpdate();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    /**
     *
     * @param user
     * @param password
     * @param admin
     */
    public void updateUser(String user, String password, int admin) {
        if (isUserInDb(user)) {
            Map<String, List<String>> creds = getUsersCredentials(user);
            String pass, access = "";
            if (password.equals("")) {
                pass = MD5(creds.get(user).get(0) + this.salt);
            } else {
                pass = MD5(password + this.salt);
            }

            try {
                PreparedStatement posted = con.prepareStatement(
                        "UPDATE users SET users.password = '" + pass + "', users.admin = "
                        + access + " WHERE users.username = '" + user + "'");
                posted.executeUpdate();
            } catch (Exception e) {
                System.err.println(e);
            }
        } else {
            // User not in database
        }
    }

    /**
     *
     * @param user
     */
    public void deleteUser(String user) {
        if (isUserInDb(user) && !user.equals(this.currentUser.getUserName())) {
            try {
                PreparedStatement statement
                        = con.prepareStatement("DELETE FROM users WHERE users.username = '" + user + "'");
                statement.executeUpdate();
            } catch (Exception e) {
                System.err.println(e);
            }
        } else {
            // No user in database
        }
    }

    /**
     *
     * @param option
     * @return @throws SQLException
     */
    public Map<String, List<String>> getAnswers(String option) throws SQLException {
        String time;
        switch (option) {
            case "day":
                time = "< CONVERT(date, GETDATE())";
                break;
            case "week":
                time = "< NOW() - INTERVAL 1 WEEK";
                break;
            case "month":
                time = "< NOW() - INTERVAL 1 MONTH";
                break;
            case "quarter":
                time = "< NOW() - INTERVAL 3 MONTH";
                break;
            default:
                System.err.println("Invalid input");
                return null;
        }
        try (PreparedStatement statement = con.prepareStatement("SELECT answer, created, username, question FROM "
                + "answers WHERE answers.uid = users.id AND answers.qid = "
                + "questions.id AND answers.created" + time);
                ResultSet result = statement.executeQuery();) {
            Map<String, List<String>> answers = new HashMap<>();
            while (result.next()) {
                List<String> answerAttributes = new ArrayList<>();
                answerAttributes.add(result.getString("answer"));
                answerAttributes.add(result.getDate("created").toString());
                answerAttributes.add(result.getString("username"));
                answerAttributes.add(result.getString("question"));
                answers.put(result.getString("username"), answerAttributes);
            }
            return answers;
        } catch (Exception e) {
            System.err.println(e);
        }
        return null;
    }

    /**
     *
     * @return
     */
    public Map<String, List<String>> getUsers() {
        try (PreparedStatement statement = con.prepareStatement("SELECT * FROM users");
                ResultSet result = statement.executeQuery()) {
            Map<String, List<String>> allUsers = new HashMap<>();
            while (result.next()) {
                List<String> userAttributes = new ArrayList<>();
                userAttributes.add(Integer.toString(result.getInt("id")));
                userAttributes.add(result.getString("fName"));
                userAttributes.add(result.getString("lName"));
                userAttributes.add(Integer.toString(result.getInt("admin")));
                allUsers.put(result.getString("username"), userAttributes);
            }
            return allUsers;
        } catch (SQLException e) {
            System.err.println(e);
        }
        return null;
    }

    /**
     *
     * @throws java.sql.SQLException
     */
    public void deleteOutOfDateRecords() throws SQLException {
        try {
            PreparedStatement statement
                    = con.prepareStatement("DELETE FROM audits WHERE "
                            + "audits.created < date('now', '-60 day')");
            statement.executeUpdate();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    /**
     *
     * @param auditId
     * @throws SQLException
     */
    public void deleteSpecificAudit(int auditId) throws SQLException {
        try {
            PreparedStatement statement
                    = con.prepareStatement("DELETE FROM answers WHERE id = "
                            + "'" + auditId + "'");
            statement.executeUpdate();
        } catch (Exception e) {
            System.err.println("No such record exsists");
        }
    }
    
    /**
     * 
     * @param option
     * @return Series<Number, Number> <- xAxis, yAxis. Date/Time, Percentage
     * @throws java.sql.SQLException
     */
    public Series<Number, Number> getSeries (String option) throws SQLException, ParseException {
        switch (option) {
            case "day":
                return daySeries();
            case "week":
                return weekSeries();
            case "month":
                return monthSeries();
            case "quarter":
                return quarterSeries();
            default:
                System.err.println("Invalid input");
                return null;
        }
    }

    /**
     *
     * @param user
     * @return
     */
    private boolean isUserInDb(String user) {
        try {
            PreparedStatement statement
                    = con.prepareStatement("SELECT username FROM users "
                            + "WHERE username = '" + user + "'");
            ResultSet result = statement.executeQuery();
            result.next();
            String s = result.getString("username");
            return !(s == null);
        } catch (Exception e) {
            System.err.println(e);
        }
        return false;
    }

    /**
     *
     * @param fName
     * @param lName
     * @return
     */
    private String createUserName(String fName, String lName) {
        String username = lName + fName.charAt(0);
        int i = 0;
        while (isUserInDb(username)) {
            // If username does not have number on end add number
            if (!Character.isDigit(username.charAt(username.length() - 1))) {
                i++;
                username += Integer.toString(i);
            } else { // Replace number on end
                i++;
                username = username.replace(Integer.toString(i - 1), Integer.toString(i));
            }
        }
        return username;
    }

    /**
     *
     * @param user
     * @return
     */
    private Map<String, List<String>> getUsersCredentials(String user) {
        if (isUserInDb(user)) {
            try {
                PreparedStatement statement
                        = con.prepareStatement("SELECT users.username, users.password");
                ResultSet result = statement.executeQuery();
                Map<String, List<String>> map = new HashMap<>();
                String username;
                while (result.next()) {
                    List<String> passAndType = new ArrayList<>();
                    username = result.getString("users.username");
                    passAndType.add(result.getString("users.password"));
                    passAndType.add(result.getString("users.admin"));
                    map.put(username, passAndType);
                }
                return map;
            } catch (Exception e) {
                System.err.println(e);
            }
        } else {
            // User not in database
        }
        return null;
    }

    /**
     *
     * @param user
     * @return
     */
    private boolean isCurrentUser(String user) {
        return false;
    }

    /**
     *
     * @param pass
     * @return
     */
    private String MD5(String pass) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(pass.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 
     * @return 
     */
    private Series<Number, Number> daySeries() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * 
     * @return 
     */
    private Series<Number, Number> weekSeries() throws SQLException, ParseException {
        PreparedStatement statement = con.prepareStatement("select created, avg(score) as 'AvgAuditScore' from audits where created >= datetime('now', '-8 days') group by created");
        ResultSet result = statement.executeQuery();
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Day Series Portfolio");
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.CANADA);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new GregorianCalendar(2016, Calendar.JUNE, 02).getTime();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        System.out.println("Bob: " + Integer.toString(day));
        while (result.next()) {
//            Date convertDate = new java.sql.Date(df.parse(result.getString("created")).getTime());
//            System.out.println("This is Created columb " + result.getString("created"));
//              System.out.println("This is Created columb " + result.getTimestamp("created").toString());
//            System.out.println("This is Created columb " + result.getDate("created").toString());
            Date convertDate = new java.sql.Date(df.parse(result.getString("created")).getTime());

//            System.out.println("This is Created columb " + convertDate);
            System.out.println("This is AvgAuditScore: " + result.getInt("AvgAuditScore"));
//            series.getData().add(new XYChart.Data(result.getDate("created").toString(), result.getInt("AvgAuditScore")));
        }
        return series;
    }

    /**
     * 
     * @return 
     */
    private Series<Number, Number> monthSeries() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * 
     * @return 
     */
    private Series<Number, Number> quarterSeries() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
