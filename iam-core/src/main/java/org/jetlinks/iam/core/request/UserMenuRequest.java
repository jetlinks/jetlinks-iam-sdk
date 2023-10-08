package org.jetlinks.iam.core.request;

import com.alibaba.fastjson.JSONObject;
import org.jetlinks.iam.core.entity.MenuView;
import org.jetlinks.iam.core.entity.ResponseMessage;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * 查询用户拥有的菜单.
 *
 * @author zhangji 2023/8/14
 */
public class UserMenuRequest extends ApiRequest<List<MenuView>> {

    private final String clientId;

    public UserMenuRequest(String clientId, String token, RestTemplate restTemplate) {
        super(token, restTemplate);
        this.clientId = clientId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MenuView> execute() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getToken());
        HttpEntity<?> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = this
                .getRestTemplate()
                .exchange(
                        "/application/" + clientId + "/menu/tree",
                        HttpMethod.GET,
                        request,
                        String.class
                );
        if (response.getBody() == null) {
            throw new RuntimeException("查询用户菜单失败");
        }
        ResponseMessage<List<MenuView>> responseMessage = JSONObject.parseObject(response.getBody(), ResponseMessage.class);
        if (response.getStatusCodeValue() != 200) {
            throw new RuntimeException(response.getBody() == null ? "" : responseMessage.getMessage());
        }

        return responseMessage.getResult();
    }
}
