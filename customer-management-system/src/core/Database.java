package core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static Database instance = null;
    private Connection connect = null;
    private final String DB_URL = "jdbc:mysql://localhost:3306/customermanage";
    private final String DB_USERNAME = "root";
    private final String DB_PASSWORD = "";

    private Database(){
        try{
            this.connect  = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnect() {
        return connect;
    }

    public static Connection getInstance(){
        try {
            if(instance == null || instance.getConnect().isClosed()){
                instance = new Database();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return instance.getConnect();
    }
}
