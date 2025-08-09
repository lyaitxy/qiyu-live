package org.qiyu.live.web.starter.error;

/**
 * 自定义的异常接口规范
 */
public interface QiyuBaseError {
    int getErrorCode();
    String getErrorMsg();
}
