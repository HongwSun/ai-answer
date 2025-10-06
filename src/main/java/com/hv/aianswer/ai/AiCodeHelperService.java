package com.hv.aianswer.ai;

import com.hv.aianswer.ai.guardrail.SafeInputGuardrail;
import dev.langchain4j.service.*;
import dev.langchain4j.service.guardrail.InputGuardrails;
import reactor.core.publisher.Flux;

@InputGuardrails({SafeInputGuardrail.class})
public interface AiCodeHelperService {

    // 流式对话
    Flux<String> chatStream(@MemoryId int memoryId, @UserMessage String userMessage);
}
