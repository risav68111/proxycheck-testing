package proxytest.sel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    public static List<ProxyVar> allProxies() throws SQLException {
        List<ProxyVar> result = new ArrayList<>();

        String query = "SELECT * FROM proxy";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
                // ResultSetMetaData meta = rs.getMetaData();
                // int colCount = meta.getColumnCount();
            while (rs.next()) {
                ProxyVar p = new ProxyVar();
                UUID id = rs.getObject("id", UUID.class);
                p.setId(id);
                p.setProxy(rs.getString("proxy"));
                p.setUser(rs.getString("username"));
                p.setPass(rs.getString("password"));
                p.setCount(rs.getInt("count") + 1); // updated value
                p.setModifiedAt(rs.getTimestamp("modified_at").toInstant());

                result.add(p);
            }
        }
        return result;
    }

    // public static void printProxies() {
    //     try {
    //         List<Map<String, Object>> allCont = allProxies();
    //         for (Map<String, Object> row : allCont) {
    //             System.out.println(row);
    //         }
    //     } catch (Exception e) {
    //         System.out.println("ERROR Occured :x_x");
    //         e.printStackTrace();
    //     }
    //
    // }

    public static ProxyVar getNextEligibleProxy() throws SQLException {

        String selectSql = """
                    SELECT id, proxy, username, password, count, modified_at
                    FROM proxy
                    WHERE count < 3
                    ORDER BY modified_at ASC
                    LIMIT 1
                    FOR UPDATE SKIP LOCKED
                """;

        String updateSql = """
                    UPDATE proxy
                    SET count = count + 1
                    WHERE id = ?
                """;

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement selectPs = conn.prepareStatement(selectSql);
                    ResultSet rs = selectPs.executeQuery()) {

                if (!rs.next()) {
                    conn.rollback();
                    return null;
                }

                ProxyVar p = new ProxyVar();
                UUID id = rs.getObject("id", UUID.class);

                p.setId(id);
                p.setProxy(rs.getString("proxy"));
                p.setUser(rs.getString("username"));
                p.setPass(rs.getString("password"));
                p.setCount(rs.getInt("count") + 1); // updated value
                p.setModifiedAt(rs.getTimestamp("modified_at").toInstant());

                try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                    updatePs.setObject(1, id);
                    updatePs.executeUpdate();
                }

                conn.commit();
                return p;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public static void decreaseProxyCount(UUID id) throws SQLException {

        String sql = """
                    UPDATE proxy
                    SET count = GREATEST(count - 1, 0)
                    WHERE id = ?
                """;

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id);
            ps.executeUpdate();
        }
    }

}
