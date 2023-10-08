package org.jetlinks.iam.core.request;

import org.jetlinks.iam.core.entity.ResponseMessage;
import org.jetlinks.iam.core.entity.UserDetail;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * 查询用户详情.
 *
 * @author zhangji 2023/8/10
 */
public class UserDetailRequest extends ApiRequest<UserDetail> {


    public UserDetailRequest(String token, RestTemplate restTemplate) {
        super(token, restTemplate);
    }

    @Override
    public UserDetail execute() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getToken());
        HttpEntity<?> request = new HttpEntity<>(headers);
        ResponseEntity<ResponseMessage<UserDetail>> response = this
                .getRestTemplate()
                .exchange(
                        "/user/detail",
                        HttpMethod.GET,
                        request,
                        new ParameterizedTypeReference<ResponseMessage<UserDetail>>() {
                        }
                );
        if (response.getStatusCodeValue() != 200) {
            throw new RuntimeException(response.getBody() == null ? "" : response.getBody().getMessage());
        }
        if (response.getBody() == null) {
            throw new RuntimeException("查询用户信息失败");
        }
        return response.getBody().getResult();
    }
}
