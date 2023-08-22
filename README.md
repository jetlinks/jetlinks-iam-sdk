# jetlinks-iam-sdk
基于JetLinks Iot平台的用户统一认证SDK


为独立应用提供用户管理、权限管理。基于响应式和SpringBoot实现

# 功能

- 单点登录
- 查询用户信息、权限信息、菜单信息
- 可配置的自定义菜单与权限，并且可以集成到JetLinks Iot平台

# 模块简介

|模块|说明|  
| ------------- |:----------:| 
|iam-core|权限核心功能|
|iam-sdk |基础通用功能| 
|iam-examples|示例| 


# 使用说明
1. 在JetLinks Iot平台的`应用管理`中添加一个`API服务`应用，按需添加页面集成
2. 添加sdk依赖
```yaml
<dependency>
    <groupId>org.jetlinks.iam</groupId>
    <artifactId>iam-sdk</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
3. 在项目配置文件`application.yml`中添加应用配置，配置API服务中的`appId`和`secureKey`
```yaml
jetlinks:
  api:
    client:
      config:
        client-api-path: http://127.0.0.1:8080  #当前服务-接口地址
        server-api-path: http://127.0.0.1:9000/api #用户中台-接口地址
        client-id: client-id #应用ID
        client-secret: client-secret #应用密钥
        redirect-uri: http://127.0.0.1:8080 # 授权后的重定向地址
        authorization-url: http://127.0.0.1:9000/#/oauth
```
4. 配置菜单和权限信息
```yaml
jetlinks:
  api:
    client:
      menu:
        - id: sdk
          owner: sdk  #外部菜单所属应用ID
          name: sdk示例
          code: sdk-view  #编码。编码需在系统内唯一
          url: /sdk
          accessSupport: unsupported  #数据权限控制
        - id: device
          parentId: sdk
          owner: sdk  #外部菜单所属应用ID
          name: 设备管理
          code: sdk-device  #编码。编码需在系统内唯一
          url: /device.html
          permissions:  #绑定权限信息
            - permission: device  #权限ID
              name: 设备管理
              actions:  #支持的权限操作
                - query
          accessSupport: support  #数据权限控制
          assetType: device  #关联资产类型,accessSupport为support有值
          indirectMenus:  #关联菜单,accessSupport为indirect时不能为空
          buttons:  #按钮定义信息
            - id: view
              name: 查看
              permissions: #权限信息
                - permission: device  #权限ID
                  name: 设备管理
                  actions:  #支持的权限操作
                    - query
            - id: add
              name: 新增
              permissions:
                - permission: device
                  name: 设备管理
                  actions:
                    - save
            - id: update
              name: 编辑
              permissions:
                - permission: device
                  name: 设备管理
                  actions:
                    - update
            - id: delete
              name: 删除
              permissions:
                - permission: device
                  name: 设备管理
                  actions:
                    - delete
```
> 也可以实现`org.jetlinks.iam.core.service.MenuSupplier`来定义菜单和权限。

5. 注入`org.jetlinks.iam.sdk.service.UserService`，提供用户相关接口

6. 项目可查询当前用户的菜单和权限，自定义授权判断。