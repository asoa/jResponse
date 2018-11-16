package datamodel;

import java.sql.*;

public class SqlDbConnection {
    // instance vars
    private String serverIp;
    private String userName;
    private String password;
    private String dbName;
    private String port;
    private Connection conn;

    // constructor

    public SqlDbConnection(String serverIp, String userName, String password, String dbName) {
        String connectionUrl = "jdbc:sqlserver://" + serverIp + ":" + "1433" + ";"
                + "databaseName=" + dbName + ";"
                + "user=" + userName + ";"
                + "password=" + password + ";"
                + "encrypt=false;"
                + "loginTimeout=30;";
        this.serverIp = serverIp;
        this.userName = userName;
        this.password = password;
        this.dbName = dbName;
           try {
            conn = DriverManager.getConnection(connectionUrl);
            System.out.printf("Connection to db: %s, sucessful", dbName);
        } catch (SQLException e){
               System.out.println(e);
               System.out.printf("Connection to db: %s, failed", dbName);
        }
    }

    public void dbSelect() {
        ResultSet resultSet = null;
        try(Statement statement = conn.createStatement()) {
            String selectSql = "SELECT * FROM dbo.candidate";
            resultSet = statement.executeQuery(selectSql);
            while(resultSet.next()) {
                System.out.println(resultSet.getString(2) + " " + resultSet.getString(3));
                System.out.println("resultSet");
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    public void dbInsert() {
        try(Statement statement = conn.createStatement()) {
            String insertSql = "INSERT INTO dbo.candidate VALUES(6, 'Alex', 'Bailey', '910-644-4179', 'ab@gmail.com', '427', 'Walberta Rd', 'Syracuse', 'New York', '13219', 'Ass Kicker', 'Awesome');\n";
            statement.executeQuery(insertSql);
        } catch (SQLException e) {
            System.out.println(e);
        }
    }


    // getters and setters
}
