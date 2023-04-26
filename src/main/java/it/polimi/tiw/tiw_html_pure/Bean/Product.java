package it.polimi.tiw.tiw_html_pure.Bean;

public record Product (int codice, String nome, String descrizione, String pathFoto, String categoria) {

    @Override
    public boolean equals(Object o){

        if(o == null) return false;
        if(o == this) return true;

        if(! (o instanceof Product)) return false;

        Product p = (Product) o;
        return this.codice == p.codice;
    }
}
