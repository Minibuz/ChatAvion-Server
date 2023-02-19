package fr.chatavion.server.tcp;

import fr.chatavion.server.networking.tcp.controller.Controller;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
public class ControllerTest {

    @Autowired
    private Controller controller;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void contextLoads() {
        assertThat(controller).isNotNull();
    }

    @Test
    public void defaultMessage() throws Exception {
        this.mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("It works")));
    }

    @Test
    public void returnHistory() throws Exception {
        Path file = Path.of("test.log");
        Files.createFile(file);
        Files.write(file, "test@Leo:::Message1".getBytes());
        this.mockMvc.perform(get("/history/test/0?amount=1")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":0,\"user\":\"Leo\",\"message\":\"Message1\"}]"));
    }

    @Test
    public void validateCommunityTrue() throws Exception {
        this.mockMvc.perform(get("/community/test")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void validateCommunityFalse() throws Exception {
        this.mockMvc.perform(get("/community/testt")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    public void sendMessage() throws Exception {
        this.mockMvc.perform(
                post("/message/test").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\": \"leo\", \"message\": \"test\"}"))
                        .andDo(print())
                        .andExpect(status().isCreated())
                        .andExpect(content().string("true"));
    }
}