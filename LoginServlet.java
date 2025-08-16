package com.aurionpro.multiQuiz;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/UserLoginServlet")
public class LoginServlet extends HttpServlet {

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String name = req.getParameter("username");
		String email = req.getParameter("email");
		String password = req.getParameter("password");
		boolean check = false;
		try {
			check = checkPassword(password, email);
		} catch (ClassNotFoundException e) {
			
			e.printStackTrace();
		} catch (SQLException e) {
			
			e.printStackTrace();
		}

		if (check == true) {
			DatabaseConectivity db = new DatabaseConectivity();
			try (Connection con = db.getConnection()) {

				String query = "SELECT * FROM users WHERE username = ? AND email = ? AND password = ?";
				try (PreparedStatement stmt = con.prepareStatement(query)) {
					stmt.setString(1, name);
					stmt.setString(2, email);
					stmt.setString(3, password);

					ResultSet rs1 = stmt.executeQuery();

					if (rs1.next()) {

						req.getSession().setAttribute("user_id", rs1.getInt("user_id"));
						resp.sendRedirect("CategoriesHtml.html");
					} else {
						resp.sendRedirect("UserLogin.html?error=Invalid credentials");
					}
				} catch (Exception e) {
					resp.sendRedirect("UserLogin.html?error=Database error");
				}
			} catch (ClassNotFoundException e1) {
				
				e1.printStackTrace();
			} catch (SQLException e1) {
				
				e1.printStackTrace();
			}
		} else {
			resp.sendRedirect("UserLogin.html?error=Database error");
		}

	}

	public static boolean checkPassword(String password, String email) throws ClassNotFoundException, SQLException {
		DatabaseConectivity db = new DatabaseConectivity();
		try (Connection con = db.getConnection()) {
			String query = "SELECT password from users where email = ? ";
			PreparedStatement ps = con.prepareStatement(query);
			ps.setString(1, email);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				String ogPassword = rs.getString("password");
				if (ogPassword.equals(password)) {
					return true;
				}
			}
		}
		return false;

	}

}
