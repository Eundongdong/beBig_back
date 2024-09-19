package beBig.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

@CrossOrigin("*")
@Controller
@Slf4j
public class HomeController {

    @GetMapping("/") //getMap("/",home) 이거 하라는 말 , map을 직접 다루지 않고, 어노테이션이 다해줌
    public String home() { // 이름 아무거나해도 되고, 매개변수 고정 아님
        log.info("================> HomController /"); // 출력: build에 로깅설정해야 가능
        return "index"; // View의 이름 , 문자열 리턴, 서블릿컨피그랑 연결되서 앞 뒤 잘라줌
    }

}
