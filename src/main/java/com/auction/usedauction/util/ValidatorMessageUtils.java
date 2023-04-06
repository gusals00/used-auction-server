package com.auction.usedauction.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.Arrays;

@RequiredArgsConstructor
@Component
@Slf4j
public class ValidatorMessageUtils {

    private final MessageSource messageSource;

    public String getValidationMessage(BindingResult bindingResult) {
        ObjectError objectError = null;

        if (bindingResult.hasFieldErrors()) { // fieldError 일  경우
            objectError = bindingResult.getFieldErrors().get(0);
        } else {// globalError 일 경우
            objectError = bindingResult.getGlobalErrors().get(0);
        }

        return getErrorMessage(objectError);
    }

    private String[] getMessageCodes(ObjectError objectError) {
        return objectError.getCodes(); // NotBlank.YearMonthPeriodReq.id,NotBlank.id,NotBlank.java.lang.String,NotBlank
    }

    private String getErrorMessage(ObjectError objectError) {
        String message = null;
        String[] messageCodes = getMessageCodes(objectError);
        for (String messageCode : messageCodes) {
            log.info("messageCode={}",messageCode);
        }

        Object[] args = getArgs(objectError);

        for (String messageCode : messageCodes) {
            try {
                message = messageSource.getMessage(messageCode, args, null);
                if (message != null) {
                    break;
                }

            } catch (NoSuchMessageException ex) {}
        }

        if (message == null) { // messageSource 에 메시지가 존재하지 않을 경우 defaultMessage return
            return objectError.getDefaultMessage();
        }

        return message; //  messageSource 에 메시지가 존재하는 경우
    }

    private Object[] getArgs(ObjectError objectError) {
        Object[] arguments = objectError.getArguments();
        if (arguments!=null) {
            arguments = Arrays.stream(arguments) // hibernate, javax validation 에서 필요없는 args 제거 후 리턴
                    .filter(ValidatorMessageUtils::isUsableArgument)
                    .toArray();
        }
        return arguments;
    }

    private static boolean isUsableArgument(Object arg) { // 필요없는 argument 제거
        return ! StringUtils.contains(arg.toString(), "org.springframework.context.support.DefaultMessageSourceResolvable");
    }

}
