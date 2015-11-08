package service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.model.ActiveMessage;
import service.model.Message;
import service.model.MessageRequestBody;
import service.repository.ActiveMessageRepository;
import service.repository.MessageRepository;

import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Created by victorkai on 11/6/15.
 */

@RestController
public class ServiceController {

    private final MessageRepository messageRepository;
    private final ActiveMessageRepository activeMessageRepository;

    @Autowired
    public ServiceController(MessageRepository messageRepository, ActiveMessageRepository activeMessageRepository) {
        this.messageRepository = messageRepository;
        this.activeMessageRepository = activeMessageRepository;
    }

    @RequestMapping(value = "/chat/{id}", method = RequestMethod.GET)
    public Message getMessage(@PathVariable int id) {
        Message message = messageRepository.getMessageById(id);
        if(message == null) {
            throw new ResourceNotFoundException("Could not find message with id '" + id + "'");
        }
        return message;
    }

    @RequestMapping(value = "/chats/{username}", method = RequestMethod.GET)
    public List<ActiveMessage> getMessages(@PathVariable String username) {
        Date now = new Date();
        // get unexpired messages and clear active messages
        List<ActiveMessage> messages =  activeMessageRepository.findByUsernameAndExpirationDateGreaterThan(username, now);
        activeMessageRepository.delete(messages);

        return messages;
    }

    @RequestMapping(value = "/chat", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createMessage(@RequestBody MessageRequestBody messageRequestBody) {
        String username = messageRequestBody.getUsername();
        if(username == null || username.isEmpty()) {
            throw new MissingPropertiesException("Missing required field username");
        }
        String text = messageRequestBody.getText();
        if(text == null) {
            throw new MissingPropertiesException("Missing required field text");
        }

        // calculate expiration date of message from now plus timeout
        Date expirationDate = Date.from(Instant.now().plusSeconds(messageRequestBody.getTimeout()));
        Message message = new Message(username, text, expirationDate);
        messageRepository.save(message);

        // add to active messages if not expired
        if(Instant.now().isBefore(expirationDate.toInstant())) {
            ActiveMessage activeMessage = new ActiveMessage(message);
            activeMessageRepository.save(activeMessage);
        }

        return new ResponseEntity<String>("{\"id\":" + message.getId() + "}", HttpStatus.CREATED);
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String msg) {
            super(msg);
        }
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public class MissingPropertiesException extends RuntimeException {
        public MissingPropertiesException(String msg) {
            super(msg);
        }
    }
}