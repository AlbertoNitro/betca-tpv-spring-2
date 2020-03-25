package es.upm.miw.betca_tpv_spring.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

@Document
public class Invoice {

    private static final String DATE_FORMAT = "yyyy";

    @Id
    private String id;
    private LocalDateTime creationDate;
    private BigDecimal baseTax;
    private BigDecimal tax;

    public void setId(int idOfYear) {
        this.id = new SimpleDateFormat(DATE_FORMAT).format(new Date()) + idOfYear;
    }

    @DBRef
    private Ticket ticket;
    @DBRef
    private User user;

    public Invoice() {
        creationDate = LocalDateTime.now();
    }

    public Invoice(int idOfYear, User user, Ticket ticket) {
        this();
        this.id = new SimpleDateFormat(DATE_FORMAT).format(new Date()) + idOfYear;
        this.user = user;
        this.ticket = ticket;
        this.tax = BigDecimal.ZERO;
        this.baseTax = BigDecimal.ZERO;
    }

    public String getId() {
        return id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public int simpleId() {
        return Integer.parseInt(String.valueOf(id).substring(DATE_FORMAT.length()));
    }

    public BigDecimal getBaseTax() {
        return baseTax;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public User getUser() {
        return user;
    }

    public void setBaseTax(BigDecimal baseTax) {
        this.baseTax = baseTax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj != null && getClass() == obj.getClass() && (id.equals(((Invoice) obj).id));
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "id='" + id + '\'' +
                ", creationDate=" + creationDate +
                ", baseTax=" + baseTax +
                ", tax=" + tax +
                ", ticket=" + ticket +
                ", user=" + user +
                '}';
    }
}
