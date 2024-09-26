<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String REST_API_KEY = "1d5d806f57ca42e97088b8bf7a0041f5";
    String REDIRECT_URI = "http://localhost:8080/kakao/login";
    String KAKAO_URI = "https://kauth.kakao.com/oauth/authorize?client_id=" + REST_API_KEY + "&redirect_uri=" + REDIRECT_URI + "&response_type=code";
%>
<header>
    <h3>아오 어려워</h3>
    <a href="<%=KAKAO_URI%>">카카오 로그인</a>
</header>