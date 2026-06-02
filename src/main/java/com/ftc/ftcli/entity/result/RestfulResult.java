package com.ftc.ftcli.entity.result;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import com.ftc.ftcli.common.enums.RestfulResultEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 11:31:34
 * @describe
 */

@Data
@AllArgsConstructor
public class RestfulResult<T> {

    @Schema(description = "响应码")
    private int code;

    @Schema(description = "响应信息")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    /**
     * 创建成功响应
     *
     * @param code 响应码
     * @param data 响应数据
     * @return 成功响应
     */
    public static <T> RestfulResult<T> success(int code, T data) {
        return new RestfulResult<>(code, RestfulResultEnum.SUCCESS_MESSAGE.getMessage(), data);
    }

    /**
     * 创建失败响应
     *
     * @param code 响应码
     * @param msg  响应数据(String/List<String>)
     * @return 失败响应
     */
    public static RestfulResult<Object> fail(int code, Object msg) {
        String errorMessage = StrUtil.format(RestfulResultEnum.ERROR_MESSAGE.getMessage(), JSONUtil.toJsonStr(msg));
        return new RestfulResult<>(code, errorMessage, new JSONObject());
    }

    /**
     * 成功响应类
     */
    public static class Success {

        /**
         * 添加接口成功响应
         *
         * @param data 响应数据
         * @return 添加接口成功响应
         */
        public static <T> RestfulResult<T> addData(T data) {
            return RestfulResult.success(HttpStatus.CREATED.value(), data);
        }

        /**
         * 删除接口成功响应
         *
         * @return 删除接口成功响应
         */
        public static RestfulResult<Void> removeData() {
            return RestfulResult.success(HttpStatus.NO_CONTENT.value(), null);
        }

        /**
         * 查询/修改接口成功响应
         *
         * @param data 响应数据
         * @return 查询/修改接口成功响应
         */
        public static <T> RestfulResult<T> getOrUpdateData(T data) {
            return RestfulResult.success(HttpStatus.OK.value(), data);
        }
    }

    /**
     * 客户端异常响应类
     */
    public static class ClientException {

        /**
         * 异常请求响应
         *
         * @param msg 异常信息
         * @return 异常请求响应
         */
        public static RestfulResult<Object> badRequest(Object msg) {
            return RestfulResult.fail(HttpStatus.BAD_REQUEST.value(), msg);
        }

        /**
         * 未登录响应
         *
         * @param msg 异常信息
         * @return 未登录响应
         */
        public static RestfulResult<Object> unauthorized(Object msg) {
            return RestfulResult.fail(HttpStatus.UNAUTHORIZED.value(), msg);
        }

        /**
         * 未授权响应
         *
         * @param msg 异常信息
         * @return 未授权响应
         */
        public static RestfulResult<Object> forbidden(Object msg) {
            return RestfulResult.fail(HttpStatus.FORBIDDEN.value(), msg);
        }

        /**
         * 未找到数据响应
         *
         * @param msg 异常信息
         * @return 未找到数据响应
         */
        public static RestfulResult<Object> notFound(Object msg) {
            return RestfulResult.fail(HttpStatus.NOT_FOUND.value(), msg);
        }

        /**
         * 参数导致流程异常响应
         *
         * @param msg 异常信息
         * @return 参数导致流程异常响应
         */
        public static RestfulResult<Object> notAccept(Object msg) {
            return RestfulResult.fail(HttpStatus.NOT_ACCEPTABLE.value(), msg);
        }
    }

    /**
     * 服务端异常响应类
     */
    public static class ServerException {

        /**
         * 内部服务器异常响应
         *
         * @param msg 异常信息
         * @return 内部服务器异常响应
         */
        public static RestfulResult<Object> internalError(Object msg) {
            return RestfulResult.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), msg);
        }
    }
}
