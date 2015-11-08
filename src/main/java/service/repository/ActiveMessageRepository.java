package service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import service.model.ActiveMessage;

import java.util.Date;
import java.util.List;

/**
 * Created by victorkai on 11/7/15.
 */
public interface ActiveMessageRepository extends JpaRepository<ActiveMessage, Long> {

    List<ActiveMessage> findByUsernameAndExpirationDateGreaterThan(String username, Date date);
}
