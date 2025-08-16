package com.aurionpro.multiQuiz;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/ResultServlet")
public class ResultServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect("UserLogin.html");
            return;
        }

        int userId = (int) session.getAttribute("user_id");
        int categoryId = (int) session.getAttribute("category_id");

        @SuppressWarnings("unchecked")
        Map<Integer, String> answers = (Map<Integer, String>) session.getAttribute("answers");

        if (answers == null || answers.isEmpty()) {
            resp.sendRedirect("CategoriesHtml.html?error=No answers found");
            return;
        }

        int score = 0;
        int total = answers.size();

        try (Connection con = new DatabaseConectivity().getConnection()) {

            StringBuilder questionIds = new StringBuilder();
            for (Integer qId : answers.keySet()) {
                questionIds.append(qId).append(",");
            }
            questionIds.deleteCharAt(questionIds.length() - 1);

            String query = "SELECT question_id, correct_option FROM questions WHERE question_id IN (" + questionIds.toString() + ")";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            Map<Integer, String> correctOptions = new HashMap<>();
            while (rs.next()) {
                correctOptions.put(rs.getInt("question_id"), rs.getString("correct_option"));
            }
            rs.close();
            stmt.close();

            for (Map.Entry<Integer, String> entry : answers.entrySet()) {
                int qId = entry.getKey();
                String userAns = entry.getValue();
                String correctOption = correctOptions.get(qId);
                if (correctOption != null && correctOption.equalsIgnoreCase(userAns)) {
                    score++;
                }
            }

            String insertQuery = "INSERT INTO results (user_id, category_id, score) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = con.prepareStatement(insertQuery);
            insertStmt.setInt(1, userId);
            insertStmt.setInt(2, categoryId);
            insertStmt.setInt(3, score);
            insertStmt.executeUpdate();

            String leaderboardQuery = "SELECT u.username, r.score FROM results r JOIN users u ON r.user_id = u.user_id WHERE r.category_id = ? ORDER BY r.score DESC LIMIT 10";
            PreparedStatement leaderboardStmt = con.prepareStatement(leaderboardQuery);
            leaderboardStmt.setInt(1, categoryId);
            ResultSet leaderboardRs = leaderboardStmt.executeQuery();

            resp.setContentType("text/html;charset=UTF-8");
            PrintWriter out = resp.getWriter();

            out.println("<!DOCTYPE html>");
            out.println("<html lang='en'>");
            out.println("<head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("<title>Quiz Result</title>");
            out.println("<link rel='stylesheet' href='Style.css' />"); 
            out.println("</head>");
            out.println("<body>");
            
            out.println("<main class='result-container'>");

            out.println("<section class='score-section'>");
            out.println("<h1>Your Score</h1>");
            out.println("<p class='score'>" + score + " / " + total + "</p>");
            out.println("</section>");

            out.println("<section class='leaderboard-section'>");
            out.println("<h2>Leaderboard for this category</h2>");
            out.println("<table class='leaderboard-table'>");
            out.println("<thead><tr><th>Username</th><th>Score</th></tr></thead>");
            out.println("<tbody>");
            while (leaderboardRs.next()) {
                String username = leaderboardRs.getString("username");
                int userScore = leaderboardRs.getInt("score");
                out.println("<tr><td>" + username + "</td><td>" + userScore + "</td></tr>");
            }
            out.println("</tbody>");
            out.println("</table>");
            out.println("</section>");

            out.println("<section class='actions-section'>");
            out.println("<a href='ResponseServlet' class='btn btn-primary'>View Detailed Responses</a>");
            out.println("<a href='CategoriesHtml.html' class='btn btn-secondary'>Take Another Test</a>");
            out.println("<a href='LogoutServlet' class='btn btn-logout'>Logout</a>");
            out.println("</section>");

            out.println("</main>");

            out.println("</body>");
            out.println("</html>");

            out.close();

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect("CategoriesHtml.html?error=Error calculating score");
        }
    }
}
