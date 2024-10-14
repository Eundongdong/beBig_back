package beBig.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.OperationsSorter;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api(){
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .globalResponseMessage(RequestMethod.POST, getArrayList())
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("beBig.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfo("beBig REST API Documentation",
                "REST Api Documentation",
                "1.0",
                "3.35.68.38:8080",
                new Contact("Bbbbick","https://www.notion.so/eundongdong/Bbbbick-BeBig-879744fc89154868b8715f77a18b57bf?pvs=4","test@test.email"),
                "비빅", "http://3.35.68.38:80",
                new ArrayList<VendorExtension>());
    }

    private ArrayList<ResponseMessage> getArrayList() {
        ArrayList<ResponseMessage> lists = new ArrayList<ResponseMessage>();
        lists.add(new ResponseMessageBuilder().code(500).message("500 ERROR").build());
        lists.add(new ResponseMessageBuilder().code(403).message("403 ERROR").build());
        lists.add(new ResponseMessageBuilder().code(401).message("401 ERROR").build());
        return lists;
    }
    @Bean
    public UiConfiguration uiconfig() {
        return UiConfigurationBuilder
                .builder().operationsSorter(OperationsSorter.ALPHA)
                .build();
    }
}