package org.jetlinks.iam.core.request;

import com.alibaba.fastjson.JSONObject;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.builder.AuthenticationBuilderFactory;
import org.hswebframework.web.authorization.simple.builder.SimpleAuthenticationBuilderFactory;
import org.hswebframework.web.authorization.simple.builder.SimpleDataAccessConfigBuilderFactory;
import org.hswebframework.web.crud.web.ResponseMessage;
import org.hswebframework.web.exception.BusinessException;
import org.jetlinks.iam.core.service.PermissionCodec;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * 查询当前用户权限.
 *
 * @author zhangji 2023/8/10
 */
public class AuthenticationRequest extends ApiRequest<Mono<Authentication>> {

    private final PermissionCodec permissionCodec;

    private static final AuthenticationBuilderFactory builder = new SimpleAuthenticationBuilderFactory(
            new SimpleDataAccessConfigBuilderFactory()
    );

    public AuthenticationRequest(String token, WebClient client, PermissionCodec permissionCodec) {
        super(token, client);
        this.permissionCodec = permissionCodec;
    }

    @Override
    public Mono<Authentication> execute() {
        return getClient()
                .get()
                .uri("/authorize/me")
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

                    Map<String, Object> authentication = decodePermission(msg.getResult());

                    return builder
                            .create()
                            .json(JSONObject.toJSONString(authentication))
                            .build();
                });
    }

    /**
     * 解码权限标识
     *
     * @param map 用户权限数据集合
     * @return
     */
    @SuppressWarnings("all")
    private Map<String, Object> decodePermission(Map<String, Object> map) {
        if (map.get("permissions") != null) {
            for (Map<String, Object> permission : (List<Map<String, Object>>) map.get("permissions")) {
                permission.put("id", permissionCodec.decode((String) permission.get("id")));
            }
        }
        if (map.get("dimensions") != null) {
            for (Map<String, Object> dimension : (List<Map<String, Object>>) map.get("dimensions")) {
                if (dimension.get("options") != null) {
                    Map<String, Object> options = (Map<String, Object>) dimension.get("options");
                    if (options.get("dataAccess") != null) {
                        for (Map<String, Object> dataAccess : (List<Map<String, Object>>) options.get("dataAccess")) {
                            dataAccess.put("assetType", permissionCodec.decode((String) dataAccess.get("assetType")));
                        }
                    }
                }
            }
        }
        return map;
    }
}
