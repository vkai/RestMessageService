package service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import service.model.Message;

/**
 * Created by victorkai on 11/7/15.
 */
public interface MessageRepository extends JpaRepository<Message, Long> {

    Message getMessageById(long id);
}