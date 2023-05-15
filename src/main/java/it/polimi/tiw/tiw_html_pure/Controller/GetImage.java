package it.polimi.tiw.tiw_html_pure.Controller;

import it.polimi.tiw.tiw_html_pure.Bean.Product;
import it.polimi.tiw.tiw_html_pure.Bean.User;
import it.polimi.tiw.tiw_html_pure.DAO.ProductDAO;
import it.polimi.tiw.tiw_html_pure.Utilities.ConnectionFactory;
import it.polimi.tiw.tiw_html_pure.Utilities.TemplateFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name="Image", value="/image")
public class GetImage extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private String basePath;
    private JakartaServletWebApplication application;
    private Connection connection;

    public void init() throws UnavailableException {
        this.application = JakartaServletWebApplication.buildApplication(getServletContext());
        //this.templateEngine = TemplateFactory.getTemplateEngine(this.application);
        this.connection = ConnectionFactory.getConnection(getServletContext());

        this.basePath = getServletContext().getInitParameter("imagesPath");
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String idProduct = request.getParameter("idProduct");
        int idProdotto;
        try{
            idProdotto = Integer.parseInt(idProduct);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad parameter");
            return;
        }

        ProductDAO productDAO = new ProductDAO(connection);
        String relativePath;
        try{
            relativePath = productDAO.getFotoPathFromidProduct(idProdotto);
        }catch (SQLException ex){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error in retriving Fotopath from DB");
            return;
        }

        if(relativePath == null){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid idProduct provided");
            return;
        }

        //Open the file
        File file = new File(this.basePath, relativePath);

        if (!file.exists() || file.isDirectory()) {

            InputStream inputStream = getServletContext().getResourceAsStream("/WEB-INF/imgs/not-found-icon.png");
            // Creare un ByteArrayOutputStream per scrivere l'input stream in memoria
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // Scrivere l'input stream nel ByteArrayOutputStream
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            response.setContentType("image/png");
            response.setContentLength(outputStream.size());
            response.setHeader("Content-Disposition", "inline; filename=notfound.png");
            response.getOutputStream().write(outputStream.toByteArray());
            return;

        }

        response.setHeader("Content-Type", getServletContext().getMimeType(relativePath));
        response.setHeader("Content-Length", String.valueOf(file.length()));
        response.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
        Files.copy(file.toPath(), response.getOutputStream());

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
