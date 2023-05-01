package it.polimi.tiw.tiw_html_pure.Bean;

import java.util.Objects;

public record ProductBySupplier (int codiceProdotto, int codiceFornitore) {

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null || ( this.getClass() != obj.getClass() ) )
            return false;
        ProductBySupplier temp = (ProductBySupplier) obj;
        return ( codiceFornitore == temp.codiceFornitore ) && ( codiceProdotto == temp.codiceProdotto );

    }

    @Override
    public int hashCode(){
        return Objects.hash(codiceProdotto,codiceFornitore);
    }

}
