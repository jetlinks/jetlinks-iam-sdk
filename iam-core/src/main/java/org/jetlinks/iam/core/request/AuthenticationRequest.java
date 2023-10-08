package org.jetlinks.iam.core.request;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jetlinks.iam.core.entity.Authentication;
import org.jetlinks.iam.core.entity.ResponseMessage;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 查询当前用户权限.
 *
 * @author zhangji 2023/8/10
 */
@Slf4j
public class AuthenticationRequest extends ApiRequest<Authentication> {

    private final String clientId;

    public AuthenticationRequest(String clientId, String token, RestTemplate restTemplate) {
        super(token, restTemplate);
        this.clientId = clientId;
    }

    @Override
    public Authentication execute() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getToken());
        HttpEntity<?> request = new HttpEntity<>(headers);
        ResponseEntity<ResponseMessage<Map<String, Object>>> response;
        try {
            response = this
                    .getRestTemplate()
                    .exchange(
                            "/application/" + clientId + "/authorize/me",
                            HttpMethod.GET,
                            request,
                            new ParameterizedTypeReference<ResponseMessage<Map<String, Object>>>() {
                            }
                    );
        } catch (Exception e) {
            log.error("查询用户权限失败. ", e);
            return null;
        }

        if (response == null ||
                response.getStatusCodeValue() != 200 ||
                response.getBody() == null ||
                response.getBody().getResult() == null) {
            log.error("查询用户权限失败. {}", response.getBody() == null ? "" : response.getBody().getMessage());
            return null;
        }

        return JSONObject
                .parseObject(
                        JSONObject.toJSONString(response.getBody().getResult()),
                        Authentication.class
                );
    }
}
