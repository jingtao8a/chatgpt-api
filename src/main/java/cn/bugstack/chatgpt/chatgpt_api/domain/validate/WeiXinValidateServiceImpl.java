package cn.bugstack.chatgpt.chatgpt_api.domain.validate;

import cn.bugstack.chatgpt.chatgpt_api.application.IWeiXinValidateService;
import cn.bugstack.chatgpt.chatgpt_api.infrastructure.util.sdk.SignatureUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WeiXinValidateServiceImpl implements IWeiXinValidateService {
    @Value("${wx.config.token}")
    private String token;

    @Override
    public boolean checkSign(String signature, String timestamp, String nonce) {
        return SignatureUtil.check(token, signature, timestamp, nonce);
    }
}
