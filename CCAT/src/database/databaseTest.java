/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Elliott
 */
public class databaseTest {

    private final Connection con;

    public databaseTest() {
        this.con = getConnection();
    }

    private Connection getConnection() {
        try {
            System.out.println("1");
//            Class.forName("org.sqlite.JDBC"); // Fails in jar
            InputStream in = this.getClass().getResourceAsStream("org.sqlite.JDBC"); // Works but unsure if needed
//            URL in = this.getClass().getResource("org.slite.JDBC");
            System.out.println("2");
            Connection conn = DriverManager.getConnection("jdbc:sqlite::resource:ccat.sqlite"); // Current fail line
            System.out.println("3");
            return conn;
        } catch (SQLException e) {
            System.err.println("Connection failed");
            System.exit(1);
        }
        return null;
    }

    public List<String> readDB() {
        try {
            PreparedStatement statement = con.prepareStatement("SELECT * FROM test");
            ResultSet result = statement.executeQuery();
            List<String> list = new ArrayList<>();
            while (result.next()) {
                list.add(result.getString("Name"));
            }
            result.close();
            statement.close();
            return list;
        } catch (Exception e) {
            System.err.println(e);
        }

        return null;
    }

}
