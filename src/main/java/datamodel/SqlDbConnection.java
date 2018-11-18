package datamodel;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.sql.*;
import java.util.List;

// TODO: turn into a service

public class SqlDbConnection extends Service {
    // instance vars
    private String serverIp;
    private String userName;
    private String password;
    private String dbName;
    private String port;
    private Connection conn;

    // constructor
    public SqlDbConnection(String serverIp, String userName, String password, String dbName) throws ClassNotFoundException{
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
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conn = DriverManager.getConnection(connectionUrl);
            System.out.printf("Connection to db: %s, successful\n", dbName);
        } catch (SQLException | ClassNotFoundException e){
               System.out.println(e);
               System.out.printf("Connection to db: %s, failed\n", dbName);
        }
    }

    @Override
    protected Task createTask() {
        return null;
    }

    public Boolean createTables() {
        try(Statement statement = conn.createStatement()) {
            String createTable =
                    "CREATE TABLE computer(" +
                            "hostName VARCHAR(30) NOT NULL," +
                            "ipAddress VARCHAR(30) NOT NULL," +
                            "CONSTRAINT candidate_pk PRIMARY KEY(hostName));";
            statement.execute(createTable);
            return true;
        } catch(SQLException e) {
            System.out.println(e);
        }
        return false;
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

    public void dbComputerInsert(List<PingParrallel.PingResult> list) {
        System.out.printf("Attempting to insert: %d records into computer table\n", list.size());
        String strFormat =
                "IF NOT EXISTS\n" +
                        "(SELECT 1 FROM computer WHERE hostname = '%s')\n" +
                        "BEGIN\n" +
                        "INSERT INTO dbo.computer (hostname, ipaddress) VALUES ('%s','%s')\n" +
                        "END\n";
        int recordsAdded = 0;
        try(Statement statement = conn.createStatement()) {
            for(PingParrallel.PingResult item: list) {
                String insertSQL  = String.format(strFormat, item.getHostname(), item.getHostname(), item.getIpAddress());
                try {
                    statement.execute(insertSQL);
                    recordsAdded += 1;

                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        System.out.printf("Inserted %d records to computer table\n", recordsAdded);
    }

    // getters and setters
}
