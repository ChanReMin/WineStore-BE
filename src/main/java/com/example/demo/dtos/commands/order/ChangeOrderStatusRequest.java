package com.example.demo.dtos.commands.order;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeOrderStatusRequest {
    private Integer status;
}
