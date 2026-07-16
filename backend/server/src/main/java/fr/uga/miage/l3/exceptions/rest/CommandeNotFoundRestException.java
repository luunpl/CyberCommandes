package fr.uga.miage.l3.exceptions.rest;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CommandeNotFoundRestException extends RuntimeException {
    public CommandeNotFoundRestException(String message) {
        super(message);
    }
}
