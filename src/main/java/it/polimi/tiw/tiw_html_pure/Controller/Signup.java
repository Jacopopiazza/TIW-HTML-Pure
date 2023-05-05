package it.polimi.tiw.tiw_html_pure.Controller;

import com.sun.source.tree.ReturnTree;
import it.polimi.tiw.tiw_html_pure.Bean.User;
import it.polimi.tiw.tiw_html_pure.DAO.UserDAO;
import it.polimi.tiw.tiw_html_pure.Utilities.ConnectionFactory;
import it.polimi.tiw.tiw_html_pure.Utilities.TemplateFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
@WebServlet(name = "Signup", value = "/register")

public class Signup extends HttpServlet {
        private static final long serialVersionUID = 1L;
        private TemplateEngine templateEngine;
        private JakartaServletWebApplication application;
        private Connection connection;

        public void init() throws UnavailableException {
            this.application = JakartaServletWebApplication.buildApplication(getServletContext());
            this.templateEngine = TemplateFactory.getTemplateEngine(this.application);
            this.connection = ConnectionFactory.getConnection(getServletContext());
        }


        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            final IWebExchange webExchange = this.application.buildExchange(request, response);
            final WebContext ctx = new WebContext(webExchange, request.getLocale());

            if (request.getParameter("error") != null) {
                Integer errorCode;
                String errorMsg = "";
                try {
                    errorCode = Integer.parseInt(request.getParameter("error"));
                } catch (NumberFormatException ex) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid error parameter");
                    return;
                }
                switch (errorCode) {
                    case 1:
                        errorMsg = "Tutti i campi vanno compilati";
                        break;
                    case 2:
                        errorMsg = "le password non corrispondono";
                        break;
                    case 3:
                        errorMsg = "indirizzo email non valido";
                        break;
                    case 4:
                        errorMsg = "indirizzo email già registrato";
                        break;
                    default:
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid error parameter");
                        return;
                }
                ctx.setVariable("error", errorMsg);
            }

            response.setCharacterEncoding("UTF-8");
            this.templateEngine.process("register",ctx, response.getWriter());
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            String email = req.getParameter("email");
            String password = req.getParameter("password");
            String confPassowrd = req.getParameter("passwordConfirmation");
            String nome = req.getParameter("nome");
            String cognome = req.getParameter("cognome");
            String via = req.getParameter("via");
            String civico = req.getParameter("civico");
            String citta = req.getParameter("citta");
            String provincia = req.getParameter("provincia");
            String cap = req.getParameter("cap");
            String stato = req.getParameter("stato");




            if (email == null || email.isEmpty() || password == null || password.isEmpty() ||
                confPassowrd == null || confPassowrd.isEmpty() || nome == null || nome.isEmpty() ||
                cognome == null || cognome.isEmpty() || via == null || via.isEmpty() ||
                civico == null || civico.isEmpty() || citta == null || citta.isEmpty() ||
                provincia == null || provincia.isEmpty() || cap == null || cap.isEmpty() ||
                 stato == null || stato.isEmpty()){
                resp.sendRedirect(getServletContext().getContextPath() + "/register?error=1");
                return;
            }

            if(!password.equals(confPassowrd)){
                resp.sendRedirect(getServletContext().getContextPath() + "/register?error=2");
                return;
            }

            UserDAO userDAO = new UserDAO(connection);
            try {
                if ( !UserDAO.isValidEmail(email) ) {
                    resp.sendRedirect(getServletContext().getContextPath() + "/register?error=3");
                    return;
                }
                if( userDAO.doesEmailExist(email)){
                    resp.sendRedirect(getServletContext().getContextPath() + "/register?error=4" );
                    return;
                }
                userDAO.createUser(email,nome,cognome,password,via,civico,cap,citta,stato,provincia);

            } catch (SQLException e) {
                e.printStackTrace();
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while checking credentials");
                return;
            }


            String path = getServletContext().getContextPath() + "/login";
            resp.sendRedirect(path);
        }

        /**
         * Close the {@link Connection} to the database.
         */
        @Override
        public void destroy() {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

