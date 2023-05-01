package it.polimi.tiw.tiw_html_pure.Bean;

import it.polimi.tiw.tiw_html_pure.DAO.SupplierDAO;

public class DeliveryCost {
    private int id;
    private int codiceFornitore;
    private int numeroMinimoArticoli;
    private Integer numeroMassimoArticoli = null;
    private double prezzoSpedizione;

    public DeliveryCost(int id, int codiceFornitore, int numeroMinimoArticoli, Integer numeroMassimoArticoli, double prezzoSpedizione){

        this.id = id;
        this.codiceFornitore = codiceFornitore;
        this.numeroMinimoArticoli = numeroMinimoArticoli;
        this.numeroMassimoArticoli = numeroMassimoArticoli;
        this.prezzoSpedizione = prezzoSpedizione / 100.00;

    }

    public DeliveryCost(int id, int codiceFornitore, int numeroMinimoArticoli, double prezzoSpedizione){

        this(id,codiceFornitore,numeroMinimoArticoli,null, prezzoSpedizione);

    }

    public int getId() {
        return id;
    }

    public int getCodiceFornitore() {
        return codiceFornitore;
    }

    public int getNumeroMinimoArticoli() {
        return numeroMinimoArticoli;
    }

    public Integer getNumeroMassimoArticoli() {
        return numeroMassimoArticoli;
    }

    public double getPrezzoSpedizione() {
        return prezzoSpedizione;
    }
}
