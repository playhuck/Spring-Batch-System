package com.system.batch.kill_batch_system.config;

import org.springframework.batch.core.converter.JsonJobParametersConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JsonJobParametersConverter는 지금까지 사용한 DefaultJobParametersConverter를 계승한 클래스로,
 * 내부적으로 ObjectMapper를 사용해 JSON 형태의 파라미터 표기를 해석한다.
 */
@Configuration
public class JobParametersConverterConfig {

    @Bean
    public JsonJobParametersConverter jobParametersConverter() {
        return new JsonJobParametersConverter();
    }
}
