package org.qiyu.live.web.starter.error;

import jakarta.servlet.http.HttpServletRequest;
import org.qiyu.live.common.interfaces.vo.WebResponseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常处理器
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public WebResponseVO errorhandler(HttpServletRequest request, Exception e) {
        LOGGER.error("{}, error is", request.getRequestURI(), e);
        return WebResponseVO.sysError("系统异常");
    }

    @ExceptionHandler(value = QiyuErrorException.class)
    @ResponseBody
    public WebResponseVO sysErrorHandler(HttpServletRequest request, QiyuErrorException e) {
        // 业务异常，参数传递有误，都会走到这里
        LOGGER.error("{}, error code is {}, error msg is {}", request.getRequestURI(), e.getErrorCode(), e.getErrorMsg());
        return WebResponseVO.bizError(e.getErrorCode(), e.getErrorMsg());
    }
}
