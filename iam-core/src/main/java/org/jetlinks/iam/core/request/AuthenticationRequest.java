package org.jetlinks.iam.core.request;

import com.alibaba.fastjson.JSONObject;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Dimension;
import org.hswebframework.web.authorization.builder.AuthenticationBuilderFactory;
import org.hswebframework.web.authorization.simple.builder.SimpleAuthenticationBuilderFactory;
import org.hswebframework.web.authorization.simple.builder.SimpleDataAccessConfigBuilderFactory;
import org.hswebframework.web.crud.web.ResponseMessage;
import org.hswebframework.web.exception.BusinessException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 查询当前用户权限.
 *
 * @author zhangji 2023/8/10
 */
public class AuthenticationRequest extends ApiRequest<Mono<Authentication>> {

    private final String clientId;

    private static final AuthenticationBuilderFactory builder = new SimpleAuthenticationBuilderFactory(
            new SimpleDataAccessConfigBuilderFactory()
    );

    public AuthenticationRequest(String clientId, String token, WebClient client) {
        super(token, client);
        this.clientId = clientId;
    }

    @Override
    public Mono<Authentication> execute() {
        return getClient()
                .get()
                .uri("/application/" + clientId + "/authorize/me")
                .headers(headers -> headers.setBearerAuth(getToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseMessage<Map<String, Object>>>() {
                })
                .mapNotNull(msg -> {
                    if (msg.getStatus() != 200) {
                        throw new BusinessException(msg.getMessage());
                    }
                    if (msg.getResult() == null) {
                        throw new BusinessException("查询用户权限失败");
                    }

                    Authentication auth = builder
                            .create()
                            .json(JSONObject.toJSONString(msg.getResult()))
                            .build();

                    // 维度去重
                    Iterator<Dimension> dimensionIterator = auth.getDimensions().iterator();
                    Set<String> dimensionIds = new HashSet<>();
                    while (dimensionIterator.hasNext()) {
                        if (!dimensionIds.add(dimensionIterator.next().getId())) {
                            dimensionIterator.remove();
                        }
                    }

                    return auth;
                });
    }
}
