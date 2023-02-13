package fr.chatavion.server.tcp;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @GetMapping(path = "/")
    public String test() {
        return "It works";
    }
}
