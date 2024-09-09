package beBig.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // 얘 붙이면 자바Bean 으로 등록이 된다. 예전에는 @component 썼었음 -> 디스패치에 인지가 되어야한다 라는 뜻
@Slf4j // log 라고 하는 멤버 변수 자동으로 생김 -> 이제부터 콘솔 출력 이걸로 할거임 이제 system.out 이거 안씀
public class HomeController {

    @GetMapping("/") //getMap("/",home) 이거 하라는 말 , map을 직접 다루지 않고, 어노테이션이 다해줌
    public String home() { // 이름 아무거나해도 되고, 매개변수 고정 아님
        log.info("================> HomController /"); // 출력: build에 로깅설정해야 가능
        return "index"; // View의 이름 , 문자열 리턴, 서블릿컨피그랑 연결되서 앞 뒤 잘라줌
    }

}
