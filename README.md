# _MoviePick_
## ✨ Description
```
CGV의 실제 상영정보를 크롤링하여 예매기능, 유저 채팅 기능 페이지를 구현하였습니다.
```


## 🔍 Overview
### _1. 영화, 극장, 상영 정보 크롤링_
![image](https://github.com/MoongBan/MoviePick_sources/assets/122944951/b36841bb-7260-4bd3-b64c-2b5a063b940b) 
(상영 스케쥴 크롤링)
- CGV의 영화 및 상영관 정보를 크롤링하여 영화리스트, 극장리스트 페이지 제작함.




### _2. 회원 가입_
![image](https://github.com/MoongBan/MoviePick_sources/assets/122944951/91f1258d-3ed1-4c6f-9fba-b57d07dc0d14)
- 회원 가입 기능.
### _3.영화 리스트 출력_
![image](https://github.com/MoongBan/MoviePick_sources/assets/122944951/ff620b96-9154-41d6-929e-1bcbabfa5d07)
- 영화리스트는 영화제목, 예매율, 개봉일 등 저장된 정보를 예매율 순으로 출력하며, 클릭시 해당 영화, 극장의 상세페이지로 이동함. 
- 영화마다 예매하기 버튼이 있음. 버튼을 클릭하면 영화가 선택된 채로 예매하기 페이지로 이동되어 AJAX를 통해 해당 영화를 관람 가능한 극장 목록이 출력됨.
### _4. 영화별 상세 조회 페이지_
![image](https://github.com/MoongBan/MoviePick_sources/assets/122944951/b7ee78c9-0c1d-4f40-9a5c-cad2726abc5f)
- 영화의 정보를 출력 가능하며, 예매페이지로 이동 버튼.
- 페이징 처리된 관람평 조회 기능.(예매 내역 페이지에서 작성한 관람평)
### _5. 극장 리스트 출력_
![image](https://github.com/MoongBan/MoviePick_sources/assets/122944951/389dd055-d8ab-431f-9ece-2886d2491383)
- 극장리스트에서 극장을 선택하면 극장이 선택된 채로 예매하기 페이지로 이동되어 AJAX를 통해 해당 극장에서 관람 가능한 영화 목록이 출력됨.
### _6. 극장별 상영 스케쥴 조회_
![image](https://github.com/MoongBan/MoviePick_sources/assets/122944951/705441f4-ee83-4bc8-b050-166edf093640)
- 3의 극장 리스트 중 원하는 극장을 선택하면 해당 극장에서 상영 가능한 스케쥴 리스트 출력
### _7. 영화 예매_ 
![image](https://github.com/MoongBan/MoviePick_sources/assets/122944951/e3313a64-c6f6-40ab-bac6-5429117ca4e6)
- 영화, 극장의 버튼을 누를 때마다 각각 예매 가능한 정보가 있는 정보만 활성화 되며, 상영관, 예매인원을 입력하면 KAKAO PAY API를 이용한 예매 가능!

### _8. 예매 내역 확인_ 
![image](https://github.com/MoongBan/MoviePick_sources/assets/122944951/8ca13ccc-e621-4751-bb28-0f4afd0adcd1)
- 예매내역을 출력. 상영 전이라면 예매취소 버튼 활성화, 상영 이후라면 관람평을 작성 OR 조회 버튼 활성화. 

### _9. 유저간 채팅_
![image](https://github.com/MoongBan/MoviePick_sources/assets/122944951/d13d0765-2fc6-4d41-8c18-582342037a91)
- WebSocket 이용하여 다른 유저와 채팅 가능. 입장 및 퇴장 알림 구현.
--- 
## ✏️ERD
![MoviePick_ERD](https://github.com/MoongBan/MoviePick_sources/assets/122944951/428849e9-4ff6-4699-88d9-7bcfabc226c3)



## 🤼‍♂️Author
🐺 Park MinSeo





