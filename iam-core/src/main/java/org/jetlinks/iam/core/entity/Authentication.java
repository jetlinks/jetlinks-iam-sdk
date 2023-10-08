package org.jetlinks.iam.core.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

/**
 * 用户权限.
 *
 * @author zhangji 2023/9/18
 */
@Getter
@Setter
public class Authentication implements Serializable {

    private static final long serialVersionUID = 2723852582263602861L;

    private User user;

    private List<Permission> permissions = new ArrayList<>();

    private List<Dimension> dimensions = new ArrayList<>();

    private Map<String, Serializable> attributes = new HashMap<>();

    @Getter
    @Setter
    public static class User implements Serializable {

        private static final long serialVersionUID = -5184895211124247462L;

        private String id;

        private String username;

        private String name;

        private String userType;

        private Map<String, Object> options;
    }


    @Getter
    @Setter
    public static class Permission implements Serializable {

        private static final long serialVersionUID = -5365061742133826245L;

        private String id;

        private String name;

        private Set<String> actions;

        private Set<DataAccessConfig> dataAccesses;

        private Map<String, Object> options;

    }

    @Getter
    @Setter
    public static class DataAccessConfig implements Serializable {

        private static final long serialVersionUID = -8809834237515792464L;

        private String action;

        private String type;

    }

    @Getter
    @Setter
    public static class Dimension implements Serializable {

        private static final long serialVersionUID = -735562814422253474L;

        private String id;

        private String name;

        private String type;

        private Map<String, Object> options;
    }

    @Getter
    @Setter
    @AllArgsConstructor(staticName = "of")
    @NoArgsConstructor
    public static class DimensionType implements Serializable {

        private static final long serialVersionUID = -1128169953753350374L;

        private String id;

        private String name;
    }
}
