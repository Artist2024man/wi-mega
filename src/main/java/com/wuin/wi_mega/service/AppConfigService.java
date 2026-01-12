package com.wuin.wi_mega.service;

import java.util.List;

public interface AppConfigService {

    <T> List<T> getList(String paramKey, Class<T> clazz);

}
