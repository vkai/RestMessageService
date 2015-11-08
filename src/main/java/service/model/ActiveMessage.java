package service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by victorkai on 11/7/15.
 */
@Entity
public class ActiveMessage {

    @Id
    private Long id;

    private String username;

    private String text;

    private Date expirationDate;

    public ActiveMessage(Long id, String username, String text, Date expirationDate) {
        this.id = id;
        this.username = username;
        this.text = text;
        this.expirationDate = expirationDate;
    }

    public ActiveMessage(Message message) {
        this.id = message.getId();
        this.username = message.getUsername();
        this.text = message.getText();
        this.expirationDate = message.getExpirationDate();
    }

    // JPA empty constructor
    ActiveMessage() { }


    @JsonIgnore
    public Date getExpirationDate() {
        return expirationDate;
    }

    @JsonIgnore
    public String getUsername() {
        return username;
    }

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }
}
