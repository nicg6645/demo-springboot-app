package com.example.controller;

import com.example.entity.Account;
import com.example.entity.Message;
import com.example.repository.AccountRepository;
import com.example.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class SocialMediaController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MessageRepository messageRepository;

   
    @PostMapping("/register")
    public ResponseEntity<?> registerAccount(@RequestBody Account account) {
        
        if (account.getUsername() == null || account.getUsername().isBlank()) {
            return ResponseEntity.status(400).build();
        }

        if (account.getPassword() == null || account.getPassword().length() < 4) {
            return ResponseEntity.status(400).build();
        }

        
        Optional<Account> existingAccount = accountRepository.findByUsername(account.getUsername());
        if (existingAccount.isPresent()) { //username already exists
            return ResponseEntity.status(409).build();
        }

        //persist
        Account savedAccount = accountRepository.save(account);
        return ResponseEntity.ok(savedAccount);
    }

    
    @PostMapping("/login")
    public ResponseEntity<?> loginAccount(@RequestBody Account account) {
        Optional<Account> existingAccount = accountRepository.findByUsername(account.getUsername());
        
        
        if (existingAccount.isPresent() && 
            existingAccount.get().getPassword().equals(account.getPassword())) {
            return ResponseEntity.ok(existingAccount.get());
        }
        
        return ResponseEntity.status(401).build();
    }

    /**
     * post new message
     */
    @PostMapping("/messages")
    public ResponseEntity<?> createMessage(@RequestBody Message message) {
        //valid message
        if (message.getMessageText() == null || 
            message.getMessageText().isBlank() || 
            message.getMessageText().length() > 255) {
            return ResponseEntity.status(400).build();
        }

        //account id exists
        if (!accountRepository.existsById(message.getPostedBy())) {
            return ResponseEntity.status(400).build();
        }

        // persist
        Message savedMessage = messageRepository.save(message);
        return ResponseEntity.ok(savedMessage);
    }

    /**
     * retrieve all messages
     */
    @GetMapping("/messages")
    public ResponseEntity<List<Message>> getAllMessages() {
        List<Message> messages = messageRepository.findAll();
        return ResponseEntity.ok(messages);
    }

    /**
     * retrieve a specific message by ID
     */
    @GetMapping("/messages/{messageId}")
    public ResponseEntity<?> getMessageById(@PathVariable Integer messageId) {
        Optional<Message> message = messageRepository.findById(messageId);
        return ResponseEntity.ok(message.orElse(null));
    }

    /**
     * delete a message
     */
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable Integer messageId) {
        if (messageRepository.existsById(messageId)) {
            messageRepository.deleteById(messageId);
            return ResponseEntity.ok(1);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * patch message text
     */
    @PatchMapping("/messages/{messageId}")
    public ResponseEntity<?> updateMessage(
        @PathVariable Integer messageId, 
        @RequestBody Message updatedMessage
    ) {
        // find message in db
        Optional<Message> existingMessage = messageRepository.findById(messageId);
        
        if (existingMessage.isEmpty()) { //if id does not exist
            return ResponseEntity.status(400).build();
        }

        // validate new message text
        if (updatedMessage.getMessageText() == null || 
            updatedMessage.getMessageText().isBlank() || 
            updatedMessage.getMessageText().length() > 255) {
            return ResponseEntity.status(400).build();
        }

        // set new message text
        Message message = existingMessage.get();
        message.setMessageText(updatedMessage.getMessageText());
        
        //persist
        messageRepository.save(message);
        return ResponseEntity.ok(1);
    }

    /**
     * get all messages from an account
     */
    @GetMapping("/accounts/{accountId}/messages")
    public ResponseEntity<List<Message>> getMessagesByUser(@PathVariable Integer accountId) {
        List<Message> messages = messageRepository.findByPostedBy(accountId);
        return ResponseEntity.ok(messages);
    }
}