package cn.bugstack.chatgpt.chatgpt_api.application;

public interface IWeiXinValidateService {
    boolean checkSign(String signature, String timestamp, String nonce);
}
