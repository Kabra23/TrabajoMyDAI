// java
package com.example.TrabajoMyDAI.data.services;

import com.example.TrabajoMyDAI.chat.ChatMessage;
import com.example.TrabajoMyDAI.chat.ChatMessageRepository;
import com.example.TrabajoMyDAI.chat.LLMClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ChatService {
    private final ChatMessageRepository repo;
    private final LLMClient llm;

    public ChatService(ChatMessageRepository repo, LLMClient llm) {
        this.repo = repo;
        this.llm = llm;
    }

    @Transactional
    public String handleUserMessage(String sessionId, String userMessage) throws Exception {
        ChatMessage user = new ChatMessage();
        user.setSessionId(sessionId);
        user.setSender("USER");
        user.setContent(userMessage);
        repo.save(user);

        String botResponse = llm.generateResponse(sessionId, userMessage);

        ChatMessage bot = new ChatMessage();
        bot.setSessionId(sessionId);
        bot.setSender("BOT");
        bot.setContent(botResponse);
        repo.save(bot);

        return botResponse;
    }

    public List<ChatMessage> getHistory(String sessionId) {
        return repo.findBySessionIdOrderByTimestampAsc(sessionId);
    }
}
