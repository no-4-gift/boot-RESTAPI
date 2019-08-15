package com.rest.api.model.response;

import lombok.*;
import java.util.List;

@Getter
@Setter
public class ListResult<T> extends CommonResult {
    private List<T> list;
}
