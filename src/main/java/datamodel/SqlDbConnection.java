package datamodel;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.css.Match;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: turn into a service


public class SqlDbConnection extends Service {
    // instance vars
    private String serverIp;
    private String userName;
    private String password;
    private String dbName;
    private String port;
    private Connection conn;
    private String buttonName;

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
                            "CONSTRAINT computer_pk PRIMARY KEY(hostName));";
            statement.execute(createTable);
            createTable =
                    "CREATE TABLE processLog(" +
                            "hostName VARCHAR(30) NOT NULL," +
                            "processId CHAR(5) NOT NULL," +
                            "processName VARCHAR(30) NOT NULL," +
                            "CONSTRAINT process_pk PRIMARY KEY(processId,hostName)," +
                            "CONSTRAINT process_fk FOREIGN KEY(hostName) REFERENCES computer);";
            statement.execute(createTable);
            createTable =
                    "CREATE TABLE computerInfo(" +
                            "ID int IDENTITY(1,1)," +
                            "osName VARCHAR(100) NOT NULL," +
                            "osVersion VARCHAR(30) NOT NULL," +
                            "servicePackVersion CHAR(5) NOT NULL," +
                            "osArch VARCHAR(10) NOT NULL," +
                            "hostName VARCHAR(30) NOT NULL," +
                            "CONSTRAINT computerInfo_pk PRIMARY KEY(ID)," +
                            "CONSTRAINT computerInfo_fk FOREIGN KEY(hostName) REFERENCES computer);";
            statement.execute(createTable);
            createTable =
                    "CREATE TABLE networkConnections(" +
                            "ID int IDENTITY(1,1)," +
                            "hostName VARCHAR(30) NOT NULL," +
                            "localAddress VARCHAR(15) NOT NULL," +
                            "localPort CHAR(5) NOT NULL," +
                            "remoteAddress VARCHAR(15) NOT NULL," +
                            "remotePort CHAR(5) NOT NULL," +
                            "owningProcess CHAR(10) NOT NULL," +
                            "CONSTRAINT networkConnections_pk PRIMARY KEY(ID)," +
                            "CONSTRAINT networkConnections_fk FOREIGN KEY(hostName) REFERENCES computer);";
            statement.execute(createTable);
            createTable =
                    "CREATE TABLE currentUser(" +
                            "ID int IDENTITY(1,1)," +
                            "hostName VARCHAR(30) NOT NULL," +
                            "userName VARCHAR(15) NOT NULL," +
                            "CONSTRAINT currentUser_pk PRIMARY KEY(ID)," +
                            "CONSTRAINT currentUser_fk FOREIGN KEY(hostName) REFERENCES computer);";
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

    public void insertDB(String buttonName, Map<String, List<String>> wmiResults) {
        switch(buttonName) {
            case "Running Programs": insertProcess(wmiResults);
                break;
            case "Computer Info": insertComputerInfo(wmiResults);
                break;
            case "Network Connections": insertNetworkConnections(wmiResults);
                break;
            case "Logged on User": insertLoggedOnUser(wmiResults);
                break;
            default:
                break;
        }
    }

    public void insertProcess(Map<String, List<String>> wmiResults) {
        InsertProcess insertProcess = new InsertProcess(conn, wmiResults);
    }

    public void insertComputerInfo(Map<String, List<String>> wmiResults) {
        InsertComputerInfo insertComputerInfo = new InsertComputerInfo(conn, wmiResults);
    }

    public void insertNetworkConnections (Map<String, List<String>> wmiResults) {
        InsertNetworkConnections insertNetworkConnections = new InsertNetworkConnections(conn, wmiResults);
    }

    public void insertLoggedOnUser (Map<String, List<String>> wmiResults) {
        InsertLoggedOnUser insertLoggedOnUser = new InsertLoggedOnUser(conn, wmiResults);
    }

    public String selectQuery (String buttonName) {
        SelectQuery selectQuery = new SelectQuery(conn, buttonName);
        String sqlQueryResults = selectQuery.getQueryResults();
        return sqlQueryResults;
    }


//    public void dbProcessInsert(Map<String, List<String>> hostProcessDict) {
//        String host;
//        List<String> sublist;
//        int recordCount = 0;
//        String strFormat =
//                "IF NOT EXISTS\n" +
//                        "(SELECT 1 FROM processLog WHERE processId = '%s')\n" +
//                        "BEGIN\n" +
//                        "INSERT INTO dbo.processLog (hostName, processId, processName) VALUES ('%s','%s','%s')\n" +
//                        "END\n";
//        for(Map.Entry<String, List<String>> entry: hostProcessDict.entrySet()) {
//            host = entry.getKey(); // get hostname
//            sublist = entry.getValue().subList(3, entry.getValue().size());  // get list of processes
//            try (Statement statement = conn.createStatement()){
//                for(String s: sublist) {  // iterate over processes in sublist
//                    try {
//                        List<String> pid_process = getMatch(s);
//                        if (pid_process.size() == 0) {
//                            continue;
//                        } else {
//                            String insertSQL = String.format(strFormat, pid_process.get(0), host, pid_process.get(0), pid_process.get(1));
//                            statement.execute(insertSQL);
////                        System.out.printf("Host: %s, ProcessID:%s, ProcesName:%s\n", host, pid_process.get(0), pid_process.get(1));
//                            recordCount++;
//                        }
//                    } catch (Exception e) {
//                        System.out.println("Error in getMatch(): " + e);
//                        continue;
//                    }
//                }
//            } catch (Exception e) {
//                System.out.println("Error in dbProcessInsert() " + e);
//            }
//        }
//        System.out.printf("Inserted %d records into the process table\n", recordCount);
//    }


//    public List<String> getMatch(String s) {
//        List<String> matches = new ArrayList<>();
//
//        Pattern patternObj = Pattern.compile("(\\d+)\\s+(\\w+.exe)");
//        Matcher match = patternObj.matcher(s);
//
//        if(match.find()) {
//            String pid = match.group(1);
//            String process = match.group(2);
//            matches.add(pid);
//            matches.add(process);
//        }
//        return matches;
//    }
    // getters and setters
}
