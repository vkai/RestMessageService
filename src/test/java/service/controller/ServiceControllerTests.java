package service.controller;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import service.RestMessageServiceApplication;
import service.model.ActiveMessage;
import service.model.Message;
import service.model.MessageRequestBody;
import service.repository.ActiveMessageRepository;
import service.repository.MessageRepository;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = RestMessageServiceApplication.class)
@WebAppConfiguration
public class ServiceControllerTests {

	private MockMvc mockMvc;

	private HttpMessageConverter mappingJackson2HttpMessageConverter;

	private List<Message> messages = new ArrayList<>();

    private List<ActiveMessage> activeMessages = new ArrayList<>();

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private ActiveMessageRepository activeMessageRepository;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	void setConverters(HttpMessageConverter<?>[] converters) {

		mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream().filter(
				hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().get();

		Assert.assertNotNull("the JSON message converter must not be null",
				this.mappingJackson2HttpMessageConverter);
	}

    // convert a Java object to a JSON string
    protected String json(Object object) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2HttpMessageConverter.write(
                object, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }

	@Before
	public void setup() throws Exception {
		this.mockMvc = webAppContextSetup(webApplicationContext).build();

		messageRepository.deleteAllInBatch();
		activeMessageRepository.deleteAllInBatch();

        Date date1 = Date.from(Instant.now().plusSeconds(60));
        Date date2 = Date.from(Instant.now().plusSeconds(60));

		messages.add(new Message("adam", "message 1", date1));
        messages.add(new Message("bob", "message 2", date2));
        messageRepository.save(messages);

        activeMessages.add(new ActiveMessage(messages.get(0)));
        activeMessages.add(new ActiveMessage(messages.get(1)));
        activeMessageRepository.save(activeMessages);
	}

	@Test
	public void noMessageWithIdFound() throws Exception {
		mockMvc.perform(get("/chat/10"))
				.andExpect(status().isNotFound());
	}

	@Test
	public void getMessageById() throws Exception {
        Message message = messages.get(0);
		mockMvc.perform(get("/chat/" + message.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username", is(message.getUsername())))
				.andExpect(jsonPath("$.text", is(message.getText())))
				.andExpect(jsonPath("$.expiration_date", is(message.getExpiration_date())));
	}

    @Test
    public void noMessagesForUsername() throws Exception {
        mockMvc.perform(get("/chats/tom"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void getMessagesForUsername() throws Exception {
        Message message = messages.get(0);
        mockMvc.perform(get("/chats/" + message.getUsername()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(message.getId().intValue())))
                .andExpect(jsonPath("$[0].text", is(message.getText())));

        // check that messages were expired after getting them
        mockMvc.perform(get("/chats/" + message.getUsername()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void postNewMessageWithMissingParam() throws Exception {
        Date now = new Date();
        MessageRequestBody message = new MessageRequestBody();
        message.setUsername("dave");
        mockMvc.perform(post("/chat")
                .content(json(message))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void postNewMessage() throws Exception {
        Date now = new Date();
        MessageRequestBody message = new MessageRequestBody();
        message.setUsername("dave");
        message.setText("hello");
        mockMvc.perform(post("/chat")
                .content(json(message))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", greaterThan(0)));

        // check if the message created is there
        mockMvc.perform(get("/chats/dave"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", greaterThan(0)))
                .andExpect(jsonPath("$[0].text", is(message.getText())));
    }

    @Test
    public void postNewMessageWithTimeout() throws Exception {
        Date now = new Date();
        MessageRequestBody message = new MessageRequestBody();
        message.setUsername("dave");
        message.setText("hello");
        message.setTimeout(0);
        mockMvc.perform(post("/chat")
                .content(json(message))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", greaterThan(0)));

        // check that message created is expired
        mockMvc.perform(get("/chats/dave"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
