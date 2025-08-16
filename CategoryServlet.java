package com.aurionpro.multiQuiz;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/CategoryServlet")
public class CategoryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String categoryIdStr = req.getParameter("category_id");
        if (categoryIdStr == null) {
            resp.sendRedirect("AdminDashboard.html");
            return;
        }

        int categoryId = Integer.parseInt(categoryIdStr);

        try (Connection con = new DatabaseConectivity().getConnection()) {
            String query = "SELECT u.username, r.score FROM results r JOIN users u ON r.user_id = u.user_id WHERE r.category_id = ? ORDER BY r.score DESC";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();

            resp.setContentType("text/html;charset=UTF-8");
            PrintWriter out = resp.getWriter();

            out.println("<html>");
            out.println("<head>");
            out.println("<title>Results for Category " + categoryId + "</title>");
            out.println("<link rel='stylesheet' href='Style.css'>");
            out.println("</head>");
            out.println("<body>");
            out.println("<div class='result-container'>");
            out.println("<h2>Results for Category ID: " + categoryId + "</h2>");
            out.println("<table class='leaderboard-table'>");
            out.println("<thead><tr><th>Username</th><th>Score</th></tr></thead>");
            out.println("<tbody>");

            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                String username = rs.getString("username");
                int score = rs.getInt("score");
                out.println("<tr><td>" + username + "</td><td>" + score + "</td></tr>");
            }

            if (!hasResults) {
                out.println("<tr><td colspan='2'>No results found for this category.</td></tr>");
            }

            out.println("</tbody>");
            out.println("</table>");
            out.println("<div class='actions'>");
            out.println("<a href='AdminDashboard.html' class='btn btn-secondary'>Back to Dashboard</a>");
            out.println("</div>");
            out.println("</div>");
            out.println("</body>");
            out.println("</html>");
            out.close();

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            resp.sendRedirect("AdminDashboard.html?error=Error fetching results");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");

        if (action == null) {
            resp.sendRedirect("AdminDashboard.html?error=Action required");
            return;
        }

        try (Connection con = new DatabaseConectivity().getConnection()) {
            if ("add".equalsIgnoreCase(action)) {
                String categoryName = req.getParameter("category_name");
                if (categoryName == null || categoryName.trim().isEmpty()) {
                    resp.sendRedirect("AdminDashboard.html?error=Category name required");
                    return;
                }

                String insertQuery = "INSERT INTO categories (category_name) VALUES (?)";
                PreparedStatement stmt = con.prepareStatement(insertQuery);
                stmt.setString(1, categoryName.trim());
                int rows = stmt.executeUpdate();

                if (rows > 0) {
                    resp.sendRedirect("AdminDashboard.html?success=Category added successfully");
                } else {
                    resp.sendRedirect("AdminDashboard.html?error=Failed to add category");
                }

            } else if ("delete".equalsIgnoreCase(action)) {
                String categoryIdStr = req.getParameter("category_id");
                if (categoryIdStr == null) {
                    resp.sendRedirect("AdminDashboard.html?error=Category ID required");
                    return;
                }

                int categoryId = Integer.parseInt(categoryIdStr);

                String deleteQuestionsQuery = "DELETE FROM questions WHERE category_id = ?";
                PreparedStatement stmt1 = con.prepareStatement(deleteQuestionsQuery);
                stmt1.setInt(1, categoryId);
                stmt1.executeUpdate();

                String deleteCategoryQuery = "DELETE FROM categories WHERE category_id = ?";
                PreparedStatement stmt2 = con.prepareStatement(deleteCategoryQuery);
                stmt2.setInt(1, categoryId);
                int rows = stmt2.executeUpdate();

                if (rows > 0) {
                    resp.sendRedirect("AdminDashboard.html?success=Category deleted successfully");
                } else {
                    resp.sendRedirect("AdminDashboard.html?error=Failed to delete category");
                }

            } else {
                resp.sendRedirect("AdminDashboard.html?error=Invalid action");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            resp.sendRedirect("AdminDashboard.html?error=Database error");
        }
    }
}
