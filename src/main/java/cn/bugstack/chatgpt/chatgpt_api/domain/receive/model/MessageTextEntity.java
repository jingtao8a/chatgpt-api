package cn.bugstack.chatgpt.chatgpt_api.domain.receive.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

@Data
public class MessageTextEntity {
    @XStreamAlias("URL")
    private String URL;
    @XStreamAlias("ToUserName")
    private String toUserName;
    @XStreamAlias("FromUserName")
    private String fromUserName;
    @XStreamAlias("CreateTime")
    private String createTime;
    @XStreamAlias("MsgType")
    private String msgType;
    @XStreamAlias("Content")
    private String content;
    @XStreamAlias("MsgId")
    private String msgId;
    @XStreamAlias("Event")
    private String event;
    @XStreamAlias("EventKey")
    private String eventKey;
}
