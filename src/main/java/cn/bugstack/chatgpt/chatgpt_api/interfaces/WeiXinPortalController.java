package cn.bugstack.chatgpt.chatgpt_api.interfaces;

import cn.bugstack.chatglm.model.ChatCompletionRequest;
import cn.bugstack.chatglm.model.ChatCompletionResponse;
import cn.bugstack.chatglm.model.Model;
import cn.bugstack.chatglm.model.Role;
import cn.bugstack.chatglm.session.OpenAiSession;
import cn.bugstack.chatgpt.chatgpt_api.application.IWeiXinValidateService;
import cn.bugstack.chatgpt.chatgpt_api.domain.receive.model.MessageTextEntity;
import cn.bugstack.chatgpt.chatgpt_api.infrastructure.util.XmlUtil;
import com.alibaba.fastjson.JSON;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;


@RestController
public class WeiXinPortalController {
    private final Logger logger = LoggerFactory.getLogger(WeiXinPortalController.class);

    @Value("${wx.config.originalid}")
    private String originalId;
    /** 状态；open = 开启、close 关闭 */
    @Value("${chatglm.sdk.config.enable}")
    private boolean enable;
    @Resource
    private IWeiXinValidateService weiXinValidateService;
    @Resource
    private OpenAiSession openAiSession;
    @Resource
    private ThreadPoolTaskExecutor taskExecutor;
    private Map<String, String> chatGPTMap = new ConcurrentHashMap<>();
    public WeiXinPortalController() {}

    /**
     * 处理微信服务器发来的get请求，进行签名的验证
     * <p>
     * appid     微信端AppID
     * signature 微信端发来的签名
     * timestamp 微信端发来的时间戳
     * nonce     微信端发来的随机字符串
     * echostr   微信端发来的验证字符串
     */
    @GetMapping(produces = "text/plain;charset=utf-8")
    public String validate(@RequestParam(value = "signature", required = false) String signature,
                           @RequestParam(value = "timestamp", required = false) String timestamp,
                           @RequestParam(value = "nonce", required = false) String nonce,
                           @RequestParam(value = "echostr", required = false) String echostr) {
        try {
            logger.info("微信公众号验签信息{}开始 [{}, {}, {}]", signature, timestamp, nonce, echostr);
            if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {
                throw new IllegalArgumentException("请求参数非法，请核实!");
            }
            boolean check = weiXinValidateService.checkSign(signature, timestamp, nonce);
            logger.info("微信公众号验签信息完成 check：{}", check);
            if (!check) {
                return null;
            }
            return echostr;
        } catch (Exception e) {
            logger.error("微信公众号验签信息失败 [{}, {}, {}, {}]",signature, timestamp, nonce, echostr, e);
            return null;
        }
    }

    /**
     * 此处是处理微信服务器的消息转发的
     */
    @PostMapping(produces = "application/xml; charset=UTF-8")
    public String post(@RequestBody String requestBody,
                       @RequestParam(name="openid", required = false) String openid) {
        try {
            logger.info("接收微信公众号信息请求开始 {}", requestBody);
            MessageTextEntity message = XmlUtil.xmlToBean(requestBody, MessageTextEntity.class);
            // 异步任务
            if (chatGPTMap.get(message.getContent().trim()) == null || "NULL".equals(chatGPTMap.get(message.getContent().trim()))) {
                // 反馈信息[文本]
                MessageTextEntity res = new MessageTextEntity();
                res.setToUserName(openid);
                res.setFromUserName(originalId);
                res.setCreateTime(String.valueOf(System.currentTimeMillis() / 1000L));
                res.setMsgType("text");
                res.setContent("消息处理中，请再回复我一句【" + message.getContent().trim() + "】");
                if (chatGPTMap.get(message.getContent().trim()) == null) {
                    doChatGPTTask(message.getContent().trim());
                }

                return XmlUtil.beanToXml(res);
            }

            // 反馈信息[文本]
            MessageTextEntity res = new MessageTextEntity();
            res.setToUserName(openid);
            res.setFromUserName(originalId);
            res.setCreateTime(String.valueOf(System.currentTimeMillis() / 1000L));
            res.setMsgType("text");
            res.setContent(chatGPTMap.get(message.getContent().trim()));
            String result = XmlUtil.beanToXml(res);
            logger.info("接收微信公众号信息请求完成 {}", result);
            chatGPTMap.remove(message.getContent().trim());
            return result;
        } catch (Exception e) {
            logger.error("接收微信公众号信息请求失败 {}", requestBody, e);
            return "";
        }
    }
    public void doChatGPTTask(String content) {
        chatGPTMap.put(content, "NULL");
        taskExecutor.execute(() -> {
            StringBuilder stringBuilder = new StringBuilder();
            CountDownLatch countDownLatch = new CountDownLatch(1);
            // 入参；模型、请求信息
            ChatCompletionRequest request = new ChatCompletionRequest();
            request.setModel(Model.GLM_4); // GLM_3_5_TURBO、GLM_4
            request.setIsCompatible(enable);
            request.setMessages(new ArrayList<ChatCompletionRequest.Prompt>() {
                private static final long serialVersionUID = -7988151926241837899L;
                {
                    add(ChatCompletionRequest.Prompt.builder()
                            .role(Role.user.getCode())
                            .content(content)
                            .build());
                }
            });
            // 请求
            try {
                openAiSession.completions(request, new EventSourceListener() {
                    @Override
                    public void onEvent(EventSource eventSource, @Nullable String id, @Nullable String type, String data) {
                        if ("[DONE]".equals(data)) {
                            logger.info("[输出结束] Tokens {}", JSON.toJSONString(data));
                            return;
                        }
                        ChatCompletionResponse response = JSON.parseObject(data, ChatCompletionResponse.class);
                        stringBuilder.append(response.getData());
                        logger.info("测试结果：{}", JSON.toJSONString(response));
                    }
                    @Override
                    public void onClosed(EventSource eventSource) {
                        logger.info("对话完成");
                        countDownLatch.countDown();
                    }
                    @Override
                    public void onFailure(EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
                        logger.error("对话失败", t);
                        countDownLatch.countDown();
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            // 等待
            try {
                countDownLatch.await();
                chatGPTMap.put(content, stringBuilder.toString());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
