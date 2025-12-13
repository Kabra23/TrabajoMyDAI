// java
package com.example.TrabajoMyDAI.chat;

public interface LLMClient {
    String generateResponse(String sessionId, String prompt) throws Exception;
}
