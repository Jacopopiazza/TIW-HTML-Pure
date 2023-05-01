package it.polimi.tiw.tiw_html_pure.Bean;

import java.text.DecimalFormat;
import java.util.List;

public class Supplier {

    private int codice;
    private String nome;
    private double valutazione;
    private Double sogliaSpedizioneGratuita = null;
    private List<DeliveryCost> fasceSpedizione;

    public Supplier(int codice, String nome, double valutazione, Integer sogliaSpedizioneGratuita, List<DeliveryCost> fasceSpedizione){
        this.codice = codice;
        this.nome = nome;
        this.valutazione = valutazione;
        this.sogliaSpedizioneGratuita = sogliaSpedizioneGratuita == null ? null : sogliaSpedizioneGratuita / 100.00;
        this.fasceSpedizione = fasceSpedizione;

    }

    public Supplier(int codice, String nome, double valutazione, List<DeliveryCost> fasceSpedizione){
        this(codice,nome,valutazione,null,fasceSpedizione);
    }

    public int getCodice() {
        return codice;
    }

    public String getNome() {
        return nome;
    }

    public double getValutazione() {
        return valutazione;
    }

    public Double getSogliaSpedizioneGratuita() {
        return sogliaSpedizioneGratuita;
    }

    public List<DeliveryCost> getFasceSpedizione() {
        return fasceSpedizione;
    }

}
