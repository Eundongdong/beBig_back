<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String REST_API_KEY = "f8156e1595fd76d2b241ad4b4f3c4ca6";
    String REDIRECT_URI = "http://localhost:5173/user";
    String KAKAO_URI = "https://kauth.kakao.com/oauth/authorize?client_id=" + REST_API_KEY + "&redirect_uri=" + REDIRECT_URI + "&response_type=code";
%>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<a href="<%=KAKAO_URI%>">카카오 로그인</a>
</body>
</html>
