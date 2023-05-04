package it.polimi.tiw.tiw_html_pure;

public class InvalidParameterException extends Exception{

    public InvalidParameterException(String message){
        super(message);
    }

    public InvalidParameterException(Exception ex){
        super(ex);
    }

}
