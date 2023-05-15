<%
    String redirectURL = request.getContextPath() + "/home";

    response.sendRedirect(redirectURL);
%>