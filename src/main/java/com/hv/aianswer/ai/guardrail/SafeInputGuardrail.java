package com.hv.aianswer.ai.guardrail;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;


/**
 * 安全检测输入护轨 - 使用sensitive-word依赖检测敏感词
 */
@Component
public class SafeInputGuardrail implements InputGuardrail {

    private volatile SensitiveWordBs sensitiveWordBs;

    // 从配置中获取敏感词检测是否启用的开关
    @Value("${ai.guardrail.enabled:false}")
    private boolean guardrailEnabled;

    @Value("${ai.guardrail.case-sensitive:false}")
    private boolean isIgnore;

    /**
     * 初始化敏感词配置
     */
    @PostConstruct
    public void init() {
        // 构建敏感词检测器
        sensitiveWordBs = SensitiveWordBs.newInstance()
                .ignoreCase(isIgnore) // 设置是否忽略大小写
                .init();                    // 初始化检测器
    }

    /**
     * 检测用户输入是否安全
     */
    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        // 确保sensitiveWordBs已初始化
        if (sensitiveWordBs == null) {
            synchronized (this) {
                if (sensitiveWordBs == null) {
                    init();
                }
            }
        }

        String inputText = userMessage.singleText();

        // 使用sensitive-word库检测敏感词
        if(guardrailEnabled){
            boolean containsSensitive = sensitiveWordBs.contains(inputText);
            if (containsSensitive) {
                return fatal("检测到敏感内容");
            }
        }

        return success();
    }
}