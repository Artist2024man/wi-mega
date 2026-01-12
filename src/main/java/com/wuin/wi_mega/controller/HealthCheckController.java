package com.wuin.wi_mega.controller;

import com.wuin.wi_mega.common.resp.RespModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/check")
public class HealthCheckController {

    @RequestMapping
    public RespModel check() {
        return RespModel.success();
    }

}