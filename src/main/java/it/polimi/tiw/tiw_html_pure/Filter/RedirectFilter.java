package it.polimi.tiw.tiw_html_pure.Filter;


import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@WebFilter("/")
public class RedirectFilter implements Filter {

    private ServletContext context;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.context = filterConfig.getServletContext();
    }
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpSession session = httpRequest.getSession();

        String pathInfo = httpRequest.getPathInfo();
        if(pathInfo == "/css/stylesheet.css"){
            response.setContentType("text/css");
            try (InputStream in = Files.newInputStream(getFilePath("/css/stylesheet.css.css"));
                 OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return;
        }
        else{
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.sendRedirect(((HttpServletRequest) request).getContextPath() + "/home");
        }

    }


    private Path getFilePath(String filename) {
        String webRoot = context.getRealPath("/");
        return Paths.get(webRoot, filename);
    }

}
