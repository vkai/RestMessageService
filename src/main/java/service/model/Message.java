package service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by victorkai on 11/6/15.
 */
@Entity
public class Message {
    @Id
    @GeneratedValue
    private Long id;

    private String username;

    private String text;

    private Date expirationDate;

    public Message(String username, String text, Date expirationDate) {
        this.username = username;
        this.text = text;
        this.expirationDate = expirationDate;
    }

    // JPA empty constructor
    Message() { }

    @JsonIgnore
    public Date getExpirationDate() {
        return expirationDate;
    }

    @JsonIgnore
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getText() {
        return text;
    }

    public String getExpiration_date() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(expirationDate);
    }
}
