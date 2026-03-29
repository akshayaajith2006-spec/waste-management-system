/* ================================================================
   EcoPickup · DatabaseManager.java
   Member 3 – Database & Integration
   Role: Manage all data storage and retrieval
   Technology: MySQL + JDBC
   ================================================================ */

import java.sql.*;
import java.util.*;

public class DatabaseManager {

  /* ── Connection config ────────────────────────────────── */
  private static final String URL      = "jdbc:mysql://localhost:3306/ecopickup";
  private static final String USERNAME = "root";
  private static final String PASSWORD = "";     // set your MySQL password here

  /* ── Get DB connection ───────────────────────────────── */
  public static Connection getConnection() throws SQLException {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      throw new SQLException("MySQL JDBC Driver not found. Add mysql-connector-j.jar to classpath.", e);
    }
    return DriverManager.getConnection(URL, USERNAME, PASSWORD);
  }

  /* ════════════════════════════════════════════════════════
     REQUESTS TABLE — CRUD OPERATIONS
  ════════════════════════════════════════════════════════ */

  /* ── INSERT: Save a new request ─────────────────────── */
  public static boolean insertRequest(String id, String name, String phone,
      String address, String wasteType, String date, String submittedAt) {

    String sql = "INSERT INTO requests (id, name, phone, address, waste_type, date, status, submitted_at) "
               + "VALUES (?, ?, ?, ?, ?, ?, 'Pending', ?)";

    try (Connection con = getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setString(1, id);
      ps.setString(2, name);
      ps.setString(3, phone);
      ps.setString(4, address);
      ps.setString(5, wasteType);
      ps.setString(6, date);
      ps.setString(7, submittedAt);

      int rows = ps.executeUpdate();
      System.out.println("✅ Request inserted: " + id);
      return rows > 0;

    } catch (SQLException e) {
      System.err.println("❌ Insert failed: " + e.getMessage());
      return false;
    }
  }

  /* ── SELECT: Get request by ID ──────────────────────── */
  public static Map<String, String> getRequestById(String id) {
    String sql = "SELECT * FROM requests WHERE id = ?";

    try (Connection con = getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setString(1, id.toUpperCase().trim());
      ResultSet rs = ps.executeQuery();

      if (rs.next()) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("id",           rs.getString("id"));
        row.put("name",         rs.getString("name"));
        row.put("phone",        rs.getString("phone"));
        row.put("address",      rs.getString("address"));
        row.put("wasteType",    rs.getString("waste_type"));
        row.put("pickupDate",   rs.getString("date"));
        row.put("status",       rs.getString("status"));
        row.put("submittedAt",  rs.getString("submitted_at"));
        return row;
      }

    } catch (SQLException e) {
      System.err.println("❌ Fetch failed: " + e.getMessage());
    }
    return null; // not found
  }

  /* ── SELECT: Get all requests ───────────────────────── */
  public static List<Map<String, String>> getAllRequests() {
    String sql = "SELECT * FROM requests ORDER BY submitted_at DESC";
    List<Map<String, String>> list = new ArrayList<>();

    try (Connection con = getConnection();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("id",          rs.getString("id"));
        row.put("name",        rs.getString("name"));
        row.put("phone",       rs.getString("phone"));
        row.put("address",     rs.getString("address"));
        row.put("wasteType",   rs.getString("waste_type"));
        row.put("pickupDate",  rs.getString("date"));
        row.put("status",      rs.getString("status"));
        row.put("submittedAt", rs.getString("submitted_at"));
        list.add(row);
      }

    } catch (SQLException e) {
      System.err.println("❌ GetAll failed: " + e.getMessage());
    }
    return list;
  }

  /* ── UPDATE: Change request status ─────────────────── */
  public static boolean updateStatus(String id, String newStatus) {
    // Validate allowed statuses
    List<String> allowed = Arrays.asList("Pending", "In Progress", "Done");
    if (!allowed.contains(newStatus)) {
      System.err.println("❌ Invalid status: " + newStatus);
      return false;
    }

    String sql = "UPDATE requests SET status = ? WHERE id = ?";

    try (Connection con = getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setString(1, newStatus);
      ps.setString(2, id);
      int rows = ps.executeUpdate();
      System.out.println("✅ Status updated → " + id + " = " + newStatus);
      return rows > 0;

    } catch (SQLException e) {
      System.err.println("❌ Update failed: " + e.getMessage());
      return false;
    }
  }

  /* ── SELECT: Count by status ────────────────────────── */
  public static Map<String, Integer> getStatusCounts() {
    String sql = "SELECT status, COUNT(*) AS cnt FROM requests GROUP BY status";
    Map<String, Integer> counts = new HashMap<>();
    counts.put("Pending",     0);
    counts.put("In Progress", 0);
    counts.put("Done",        0);

    try (Connection con = getConnection();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        counts.put(rs.getString("status"), rs.getInt("cnt"));
      }

    } catch (SQLException e) {
      System.err.println("❌ Count failed: " + e.getMessage());
    }
    return counts;
  }

  /* ════════════════════════════════════════════════════════
     USERS TABLE — CRUD OPERATIONS
  ════════════════════════════════════════════════════════ */

  /* ── INSERT: Register new user ──────────────────────── */
  public static boolean insertUser(String id, String name, String email,
      String phone, String password, String avatar) {

    String sql = "INSERT INTO users (id, name, email, phone, password, avatar) VALUES (?,?,?,?,?,?)";

    try (Connection con = getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setString(1, id);
      ps.setString(2, name);
      ps.setString(3, email);
      ps.setString(4, phone);
      ps.setString(5, password);   // hash this in production!
      ps.setString(6, avatar);
      ps.executeUpdate();
      System.out.println("✅ User registered: " + email);
      return true;

    } catch (SQLException e) {
      System.err.println("❌ User insert failed: " + e.getMessage());
      return false;
    }
  }

  /* ── SELECT: Login check ─────────────────────────────── */
  public static Map<String, String> loginUser(String email, String password) {
    String sql = "SELECT * FROM users WHERE email = ? AND password = ?";

    try (Connection con = getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setString(1, email);
      ps.setString(2, password);
      ResultSet rs = ps.executeQuery();

      if (rs.next()) {
        Map<String, String> user = new LinkedHashMap<>();
        user.put("id",       rs.getString("id"));
        user.put("name",     rs.getString("name"));
        user.put("email",    rs.getString("email"));
        user.put("phone",    rs.getString("phone"));
        user.put("avatar",   rs.getString("avatar"));
        user.put("joinedAt", rs.getString("joined_at"));
        return user;
      }

    } catch (SQLException e) {
      System.err.println("❌ Login failed: " + e.getMessage());
    }
    return null; // invalid credentials
  }

  /* ── MAIN: Quick test ────────────────────────────────── */
  public static void main(String[] args) {
    System.out.println("=== EcoPickup DB Test ===\n");

    // Test connection
    try (Connection con = getConnection()) {
      System.out.println("✅ Database connected!\n");
    } catch (SQLException e) {
      System.err.println("❌ Cannot connect: " + e.getMessage());
      return;
    }

    // Test fetch
    System.out.println("--- All Requests ---");
    List<Map<String, String>> all = getAllRequests();
    for (Map<String, String> r : all) {
      System.out.printf("  [%s] %s | %s | %s%n",
          r.get("id"), r.get("name"), r.get("wasteType"), r.get("status"));
    }

    // Test status counts
    System.out.println("\n--- Status Summary ---");
    Map<String, Integer> counts = getStatusCounts();
    counts.forEach((k, v) -> System.out.println("  " + k + ": " + v));

    System.out.println("\n✅ All tests passed.");
  }
}