package it.polimi.tiw.tiw_html_pure.Controller;

import it.polimi.tiw.tiw_html_pure.Utilities.ConnectionFactory;
import it.polimi.tiw.tiw_html_pure.Utilities.TemplateFactory;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.sql.Connection;

@WebServlet(name = "Logout", value = "/logout")
public class Logout extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     *
     * Handles HTTP POST requests to logout the user by invalidating the session and redirecting to the login page.
     *
     * @param req the HTTP servlet request object
     *
     * @param resp the HTTP servlet response object
     *
     * @throws IOException if an I/O error occurs
     */

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        req.setCharacterEncoding("UTF-8");

        req.getSession(false).invalidate();

        resp.setCharacterEncoding("UTF-8");
        resp.sendRedirect(getServletContext().getContextPath() + "/login");
    }


}
