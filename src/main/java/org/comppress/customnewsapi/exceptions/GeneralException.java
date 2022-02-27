package org.comppress.customnewsapi.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GeneralException extends RuntimeException {

    private final String message;

    private final String variable;

}
