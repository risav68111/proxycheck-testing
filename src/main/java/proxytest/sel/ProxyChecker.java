package proxytest.sel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.cdimascio.dotenv.Dotenv;

public class ProxyChecker {
    public String getProxy() {

        return "";
    }

    private static Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    private static String USER = dotenv.get("P_USER");
    private static String PASSWORD = dotenv.get("P_PASS");

    private static final String URL = "jdbc:postgresql://localhost:5432/test";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static List<Map<String, Object>> allProxies() throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();

        String query = "SELECT * FROM proxy";
        try (Connection conn = getConnection();
            Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query)) {
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for(int i=1; i<= colCount; i++) {
                    row.put(meta.getColumnName(i), rs.getObject(i));
                }
                result.add(row);
            }
        }
        return result;
    }

    public static void printProxies() {

        try {
            List<Map<String, Object>> allCont = allProxies();
            for(Map<String, Object> row : allCont) {
                System.out.println(row);
            }
        } catch (Exception e) {
            System.out.println("ERROR Occured :x_x");
            e.printStackTrace();
        }

    }
}
