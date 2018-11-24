package datamodel;

import java.sql.*;
import java.util.*;

public class SelectQuery {
    // instance vars
    private Connection conn;
    private String query;
    private Statement statement;
    private Map<String,String> queryRepo;
    private List<String> queryResults;
    private String buttonName;
    private StringBuilder sb;
    String result;
    Formatter fmt;
    private static String selectQuery;

    // constructor
    public SelectQuery(Connection conn, String buttonName) {
        queryRepo = new HashMap<>();
        createQueryRepo();  // create query repo
        this.conn = conn;
        this.query = getQueryString(buttonName);
        this.buttonName = buttonName;
        sb = new StringBuilder();
        result = "";
        fmt = new Formatter(sb, Locale.US);

        dbSelect();
    }

    // getter
    public String getQueryResults() {
        return result;
    }

    public static String getSelectQuery() {
        return selectQuery;
    }

    // methods
    public void dbSelect() {
        List<String> resultValues = new ArrayList<>();
        ResultSet resultSet = null;

        if(buttonName.equals("Inventory")) {

            try(Statement statement = conn.createStatement()) {
                resultSet = statement.executeQuery(query);
                System.out.printf("%-30s %-20s %-45s %-10s %-10s\n", "Hostname","IP Address","OS Name","OS Version","User Name");
                System.out.printf("%-30s %-20s %-45s %-10s %-10s\n", "--------","----------","-------","----------","---------");
                fmt.format("%-30s %-20s %-45s %-10s %-10s\n","Hostname","IP Address","OS Name","OS Version","User Name");
                fmt.format("%-30s %-20s %-45s %-10s %-10s\n", "--------","----------","-------","---------","---------");

                while(resultSet.next()) {
                    System.out.printf("%-30s %-20s %-45s %-10s %-10s\n", resultSet.getString(1),resultSet.getString(2),resultSet.getString(3),resultSet.getString(4),resultSet.getString(5));
                    fmt.format("%-30s %-20s %-45s %-10s %-10s\n", resultSet.getString(1),resultSet.getString(2),resultSet.getString(3),resultSet.getString(4),resultSet.getString(5));
                    result = sb.toString();
                }
            } catch (SQLException e) {
                System.out.println(e);
            }
        } else if (buttonName.equals("Find Remote Users")) {
            try(Statement statement = conn.createStatement()) {
                String strFormat = "%-30s %-20s %-20s %-20s %-25s\n";
                resultSet = statement.executeQuery(query);
                System.out.printf(strFormat,"Hostname","IP Address","Remote Address","Owning Process Id","Owning Process Name");
                System.out.printf(strFormat,"--------","----------","--------------","-----------------","-------------------");
                fmt.format(strFormat,"Hostname","IP Address","Remote Address","Owning Process Id","Owning Process Name");
                fmt.format(strFormat,"--------","----------","--------------","-----------------","-------------------");
                while(resultSet.next()) {
                    System.out.printf(strFormat, resultSet.getString(1),resultSet.getString(2),resultSet.getString(3),resultSet.getString(4),resultSet.getString(5));
                    fmt.format(strFormat, resultSet.getString(1),resultSet.getString(2),resultSet.getString(3),resultSet.getString(4),resultSet.getString(5));
                    result = sb.toString();
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        } else if (buttonName.equals("Top Processes") || (buttonName.equals("Bottom Processes"))) {
            try(Statement statement = conn.createStatement()) {
                String strFormat = "%-30s %-5s\n";
                resultSet = statement.executeQuery(query);
                System.out.printf(strFormat,"Process Name","Process Count");
                System.out.printf(strFormat,"------------","-------------");
                fmt.format(strFormat,"Process Name","Process Count");
                fmt.format(strFormat,"------------","-------------");
                while(resultSet.next()) {
                    System.out.printf(strFormat, resultSet.getString(1),resultSet.getString(2));
                    fmt.format(strFormat, resultSet.getString(1),resultSet.getString(2));
                    result = sb.toString();
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        } else {
            return;
        }
    }

    public void createQueryRepo() {
        String inventory =  "SELECT DISTINCT c.hostname,c.ipAddress,i.osName,osVersion,u.userName\n" +
                            "FROM computer c\n" +
                            "INNER JOIN computerInfo i\n" +
                            "ON c.hostName=i.hostName\n" +
                            "INNER JOIN currentUser u\n" +
                            "ON i.hostName=u.hostName\n";
        String remoteUsers = "SELECT DISTINCT c.hostName,c.ipAddress,n.remoteAddress,n.owningProcess \'owningProcessId\',p.processName \'owningProcessName\'\n" +
                "FROM computer c\n" +
                "INNER JOIN networkConnections n\n" +
                "ON c.ipAddress=n.localAddress\n" +
                "INNER JOIN processLog p\n" +
                "ON n.owningProcess=p.processId\n" +
                "WHERE n.localPort=\'3389\'\n";
        String topProcesses = "SELECT TOP 20 processName,COUNT(processName) 'Top 20 Processes'\n" +
                "FROM processLog \n" +
                "GROUP BY processName\n" +
                "ORDER BY 'Top 20 Processes' DESC\n";
        String bottomProcesses = "SELECT TOP 20 processName,COUNT(processName) 'Bottom 20 Processes'\n" +
                "FROM processLog \n" +
                "GROUP BY processName\n" +
                "ORDER BY 'Bottom 20 Processes' ASC\n";
        queryRepo.put("Inventory",inventory);
        queryRepo.put("Find Remote Users",remoteUsers);
        queryRepo.put("Top Processes",topProcesses);
        queryRepo.put("Bottom Processes",bottomProcesses);
    }

    public String getQueryString(String buttonName) {
        selectQuery = queryRepo.get(buttonName);
        return queryRepo.get(buttonName);
    }
}
