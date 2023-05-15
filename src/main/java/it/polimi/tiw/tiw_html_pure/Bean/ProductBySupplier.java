package it.polimi.tiw.tiw_html_pure.Bean;

import java.util.Objects;

public record ProductBySupplier (int idProduct, int idSupplier) {

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null || ( this.getClass() != obj.getClass() ) )
            return false;
        ProductBySupplier temp = (ProductBySupplier) obj;
        return ( idSupplier == temp.idSupplier ) && ( idProduct == temp.idProduct );

    }

    @Override
    public int hashCode(){
        return Objects.hash(idProduct,idSupplier);
    }

}
