/* ================================================================
   EcoPickup · WasteRequestServlet.java
   Member 2 – Backend Developer
   Role: Handle logic, request processing, status updates
   Technology: Java Servlets (Jakarta EE / Tomcat)
   ================================================================ */

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/* ──────────────────────────────────────────────
   1. REQUEST MODEL
────────────────────────────────────────────── */
class WasteRequest {
  public String id;
  public String name;
  public String phone;
  public String address;
  public String wasteType;
  public String pickupDate;
  public String status;
  public String submittedAt;

  public WasteRequest() {}

  public WasteRequest(String name, String phone, String address,
                      String wasteType, String pickupDate) {
    this.id          = generateId();
    this.name        = name;
    this.phone       = phone;
    this.address     = address;
    this.wasteType   = wasteType;
    this.pickupDate  = pickupDate;
    this.status      = "Pending";           // default status
    this.submittedAt = LocalDate.now().toString();
  }

  /* Generate unique Request ID: REQ + timestamp suffix */
  private static String generateId() {
    long suffix = System.currentTimeMillis() % 100000;
    return "REQ" + suffix;
  }
}

/* ──────────────────────────────────────────────
   2. DATABASE HELPER
   (Member 3 provides DB — this class connects)
────────────────────────────────────────────── */
class DBHelper {
  private static final String URL  = "jdbc:mysql://localhost:3306/ecopickup";
  private static final String USER = "root";
  private static final String PASS = "";   // change to your MySQL password

  public static Connection getConnection() throws SQLException {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      throw new SQLException("MySQL Driver not found", e);
    }
    return DriverManager.getConnection(URL, USER, PASS);
  }
}

/* ──────────────────────────────────────────────
   3. MAIN SERVLET  →  /WasteRequestServlet
────────────────────────────────────────────── */
@WebServlet("/WasteRequestServlet")
public class WasteRequestServlet extends HttpServlet {

  /* ── POST: Submit a new pickup request ─────── */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {

    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    PrintWriter out = res.getWriter();

    // 1. Read form fields
    String name      = req.getParameter("name");
    String phone     = req.getParameter("phone");
    String address   = req.getParameter("address");
    String wasteType = req.getParameter("wasteType");
    String date      = req.getParameter("pickupDate");

    // 2. Validate inputs
    String validationError = validate(name, phone, address, wasteType, date);
    if (validationError != null) {
      res.setStatus(400);
      out.print("{\"success\":false,\"error\":\"" + validationError + "\"}");
      return;
    }

    // 3. Create request object (generates ID + sets Pending)
    WasteRequest request = new WasteRequest(name, phone, address, wasteType, date);

    // 4. Save to database
    boolean saved = addRequest(request);
    if (!saved) {
      res.setStatus(500);
      out.print("{\"success\":false,\"error\":\"Database error. Try again.\"}");
      return;
    }

    // 5. Return success + generated ID
    out.print("{\"success\":true,\"requestId\":\"" + request.id + "\",\"status\":\"Pending\"}");
  }

  /* ── GET: Fetch request status by ID ─────── */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {

    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    PrintWriter out = res.getWriter();

    String id = req.getParameter("id");

    if (id == null || id.trim().isEmpty()) {
      res.setStatus(400);
      out.print("{\"success\":false,\"error\":\"Request ID is required\"}");
      return;
    }

    WasteRequest found = fetchRequest(id.trim().toUpperCase());

    if (found == null) {
      res.setStatus(404);
      out.print("{\"success\":false,\"error\":\"No request found for ID: " + id + "\"}");
      return;
    }

    // Return request as JSON
    out.print(toJson(found));
  }

  /* ──────────────────────────────────────────
     LOGIC FUNCTIONS
  ────────────────────────────────────────── */

  /* Validate all fields */
  private String validate(String name, String phone, String address,
                           String wasteType, String date) {
    if (name    == null || name.trim().isEmpty())     return "Name is required";
    if (phone   == null || !phone.matches("\\d{10}")) return "Enter a valid 10-digit phone number";
    if (address == null || address.trim().isEmpty())   return "Address is required";
    if (wasteType == null || wasteType.trim().isEmpty()) return "Please select a waste type";
    if (date    == null || date.trim().isEmpty())      return "Please select a pickup date";

    // Date must not be in the past
    try {
      LocalDate picked = LocalDate.parse(date);
      if (picked.isBefore(LocalDate.now())) return "Pickup date cannot be in the past";
    } catch (Exception e) {
      return "Invalid date format";
    }

    return null; // all valid
  }

  /* Add new request to DB */
  private boolean addRequest(WasteRequest r) {
    String sql = "INSERT INTO requests(id, name, phone, address, waste_type, date, status, submitted_at) "
               + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    try (Connection con = DBHelper.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, r.id);
      ps.setString(2, r.name);
      ps.setString(3, r.phone);
      ps.setString(4, r.address);
      ps.setString(5, r.wasteType);
      ps.setString(6, r.pickupDate);
      ps.setString(7, r.status);
      ps.setString(8, r.submittedAt);
      ps.executeUpdate();
      return true;
    } catch (SQLException e) {
      System.err.println("DB Insert Error: " + e.getMessage());
      return false;
    }
  }

  /* Fetch single request by ID */
  private WasteRequest fetchRequest(String id) {
    String sql = "SELECT * FROM requests WHERE id = ?";
    try (Connection con = DBHelper.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, id);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        WasteRequest r = new WasteRequest();
        r.id          = rs.getString("id");
        r.name        = rs.getString("name");
        r.phone       = rs.getString("phone");
        r.address     = rs.getString("address");
        r.wasteType   = rs.getString("waste_type");
        r.pickupDate  = rs.getString("date");
        r.status      = rs.getString("status");
        r.submittedAt = rs.getString("submitted_at");
        return r;
      }
    } catch (SQLException e) {
      System.err.println("DB Fetch Error: " + e.getMessage());
    }
    return null;
  }

  /* Convert WasteRequest to JSON string */
  private String toJson(WasteRequest r) {
    return "{"
      + "\"success\":true,"
      + "\"id\":\""         + r.id          + "\","
      + "\"name\":\""       + r.name        + "\","
      + "\"phone\":\""      + r.phone       + "\","
      + "\"address\":\""    + r.address     + "\","
      + "\"wasteType\":\""  + r.wasteType   + "\","
      + "\"pickupDate\":\"" + r.pickupDate  + "\","
      + "\"status\":\""     + r.status      + "\","
      + "\"submittedAt\":\"" + r.submittedAt + "\""
      + "}";
  }
}

/* ──────────────────────────────────────────────
   4. STATUS UPDATE SERVLET  →  /UpdateStatus
   (Used by Admin panel)
────────────────────────────────────────────── */
@WebServlet("/UpdateStatus")
class UpdateStatusServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {

    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    PrintWriter out = res.getWriter();

    String id        = req.getParameter("id");
    String newStatus = req.getParameter("status");

    // Allowed statuses
    List<String> allowed = Arrays.asList("Pending", "In Progress", "Done");
    if (id == null || newStatus == null || !allowed.contains(newStatus)) {
      res.setStatus(400);
      out.print("{\"success\":false,\"error\":\"Invalid request or status\"}");
      return;
    }

    boolean updated = updateStatus(id.trim(), newStatus);
    if (updated) {
      out.print("{\"success\":true,\"id\":\"" + id + "\",\"newStatus\":\"" + newStatus + "\"}");
    } else {
      res.setStatus(500);
      out.print("{\"success\":false,\"error\":\"Update failed\"}");
    }
  }

  private boolean updateStatus(String id, String status) {
    String sql = "UPDATE requests SET status = ? WHERE id = ?";
    try (Connection con = DBHelper.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, status);
      ps.setString(2, id);
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      System.err.println("DB Update Error: " + e.getMessage());
      return false;
    }
  }
}