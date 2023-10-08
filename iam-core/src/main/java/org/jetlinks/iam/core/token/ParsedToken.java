package org.jetlinks.iam.core.token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 输入描述.
 *
 * @author zhangji 2023/9/22
 */
@Getter
@Setter
@AllArgsConstructor(staticName = "of")
public class ParsedToken {

    private String type;

    private String token;

}
