package com.MVReservation001.service;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.MVReservation001.dao.MemberDao;
import com.MVReservation001.dao.MovieDao;
import com.MVReservation001.dto.MovieDto;
import com.MVReservation001.dto.PageDto;
import com.MVReservation001.dto.ReservationDto;
import com.MVReservation001.dto.ReviewsDto;
import com.MVReservation001.dto.ScheduleDto;
import com.MVReservation001.dto.TheaterDto;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service
public class MovieService {

	public int addScheduleList2() {
		System.setProperty("webdriver.chrome.driver", "D:\\projectDriver\\chromedriver.exe");
		ChromeOptions options = new ChromeOptions();
		options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
		WebDriver driver = new ChromeDriver(options);
		
		return 0;
	}	
		
	
	
	
	@Autowired
	private MovieDao mvdao;
	
	@Autowired
	private MemberDao memDao;
	
	@Autowired
	private HttpSession session;

	
	public int addMovieList() throws IOException {

		//1.CGV 영화 페이지 URL
		String cgvUrl = "http://www.cgv.co.kr/movies/?lt=1&ft=0";
		
		//2. Jsoup URL 접속
		Document doc = Jsoup.connect(cgvUrl).get();

		//3.무비차트 출력 DIV 선택
		//#contents > div.wrap-movie-chart > div.sect-movie-chart
		Elements chartDiv = doc.select("#contents > div.wrap-movie-chart > div.sect-movie-chart");
		//4.1 ~ 19위 <li>태그 선택
		Elements movLi = chartDiv.select("li");
		
		int insertResult = 0;
		
		for(int i = 0; i < movLi.size(); i++) {
			//5. 1 ~ 19위 영화의 상세페이지 URL 추출
			String detailUrl = "http://www.cgv.co.kr"+ movLi.eq(i).select("div.box-image > a").eq(0).attr("href");
			System.out.println(detailUrl);
			//6.영화 상세 페이지 Document
			Document detailDoc = Jsoup.connect(detailUrl).get();
			//7.영화 상세 정보 출력 div 선택
			Elements baseMovie = detailDoc.select("#select_main > div.sect-base-movie");
			
			String mvtitle = baseMovie.select("div.box-contents > div.title > strong").eq(0).text();
			System.out.println("영화제목 : " + mvtitle);
			//SELECT * FROM MOVIES WHERE MVTITLE = ?;
			String movCheck = mvdao.selectCheckMovie(mvtitle);
			if(movCheck != null) {
				System.out.println("["+mvtitle +"] 은 등록된 영화 입니다.");
				continue;
			}
			String mvdir = baseMovie.select("div.box-contents > div.spec > dl > dd:nth-child(2)").eq(0).text();
			System.out.println("영화감독 : " + mvdir);
			
			//배우, 기본정보, 개봉일
			String mvact = baseMovie.select("div.box-contents > div.spec > dl > dd.on").eq(0).text().replace(" , ", ",");
			System.out.println("출연배우 : " + mvact);
			
			String mvinfo = baseMovie.select("div.box-contents > div.spec > dl > dd.on").eq(1).text().replace(" ", "");
			System.out.println("기본정보 : " + mvinfo);
			
			String mvdate = baseMovie.select("div.box-contents > div.spec > dl > dd.on").eq(2).text().replace("(재개봉)", "");
			System.out.println("개봉일 : " + mvdate);
			
			String mvgenre = baseMovie.select("div.box-contents > div.spec > dl > dd.on").eq(0).next().text().replace("장르 :", "").replace(", ", ",").trim();
			System.out.println("영화장르 : " + mvgenre);
			
			String mvpos = baseMovie.select("div.box-image > a").eq(0).attr("href");
			System.out.println("영화포스터 : " + mvpos  );
			
			//영화제목 ~ 영화 포스터 MovieDto에 저장
			MovieDto movie = new MovieDto();
			movie.setMvtitle(mvtitle);
			movie.setMvdir(mvdir);
			movie.setMvact(mvact);
			movie.setMvgenre(mvgenre);
			movie.setMvinfo(mvinfo);
			movie.setMvdate(mvdate);
			movie.setMvpos(mvpos);
			
			//영화 코드 생성 "MV001", "MV002", "MV003"......;
			//1. 영화코드 최대값 조회
			String maxMvcode = mvdao.selectMaxMvcode();
			System.out.println("영화코드 최대값 : " + maxMvcode); 
			String mvcode = "MV";
			if(maxMvcode == null) { // 영화코드 : "MV001" 코드 생성
				mvcode = mvcode + String.format("%03d", 1);
			} else { // 최대값 + 1 코드 생성
				int mvcodeNum = Integer.parseInt( maxMvcode.replace("MV", "") ) + 1;
				//maxMvcode : "MV005"
				//maxMvcode.replace("MV", "") : "005";
				//Integer.parseInt( "005" ); : INT값 5
				// 5 + 1 :: 6
				mvcode = mvcode + String.format("%03d", mvcodeNum);
			}
			System.out.println("영화코드 : " +mvcode);
			movie.setMvcode(mvcode);
			
			insertResult += mvdao.insertMovie(movie);
			
			System.out.println();
			
		}
		
		System.out.println("등록된 영화 수 : " + insertResult);
		
		return insertResult;
	}
//
	public int addTheaterList() throws IOException {

		String theaterList = "[{\"AreaTheaterDetailList\":[{\"RegionCode\":\"01\",\"TheaterCode\":\"0056\",\"TheaterName\":\"CGV강남\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0001\",\"TheaterName\":\"CGV강변\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0229\",\"TheaterName\":\"CGV건대입구\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0010\",\"TheaterName\":\"CGV구로\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0063\",\"TheaterName\":\"CGV대학로\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0252\",\"TheaterName\":\"CGV동대문\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0230\",\"TheaterName\":\"CGV등촌\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0009\",\"TheaterName\":\"CGV명동\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0105\",\"TheaterName\":\"CGV명동역 씨네라이브러리\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0057\",\"TheaterName\":\"CGV미아\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0288\",\"TheaterName\":\"CGV방학\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0030\",\"TheaterName\":\"CGV불광\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0046\",\"TheaterName\":\"CGV상봉\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0300\",\"TheaterName\":\"CGV성신여대입구\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0088\",\"TheaterName\":\"CGV송파\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0276\",\"TheaterName\":\"CGV수유\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0150\",\"TheaterName\":\"CGV신촌아트레온\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0040\",\"TheaterName\":\"CGV압구정\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0112\",\"TheaterName\":\"CGV여의도\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0292\",\"TheaterName\":\"CGV연남\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0059\",\"TheaterName\":\"CGV영등포\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0074\",\"TheaterName\":\"CGV왕십리\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0013\",\"TheaterName\":\"CGV용산아이파크몰\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0131\",\"TheaterName\":\"CGV중계\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0199\",\"TheaterName\":\"CGV천호\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0107\",\"TheaterName\":\"CGV청담씨네시티\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0223\",\"TheaterName\":\"CGV피카디리1958\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0164\",\"TheaterName\":\"CGV하계\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"0191\",\"TheaterName\":\"CGV홍대\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"P001\",\"TheaterName\":\"CINE de CHEF 압구정\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"01\",\"TheaterCode\":\"P013\",\"TheaterName\":\"CINE de CHEF 용산아이파크몰\",\"TheaterName_ENG\":null,\"IsSelected\":false}],\"RegionCode\":\"01\",\"RegionName\":\"서울\",\"RegionName_ENG\":\"Seoul\",\"DisplayOrder\":\"1\",\"IsSelected\":false},{\"AreaTheaterDetailList\":[{\"RegionCode\":\"02\",\"TheaterCode\":\"0260\",\"TheaterName\":\"CGV경기광주\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0255\",\"TheaterName\":\"CGV고양행신\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0257\",\"TheaterName\":\"CGV광교\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0266\",\"TheaterName\":\"CGV광교상현\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0348\",\"TheaterName\":\"CGV광명역\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0232\",\"TheaterName\":\"CGV구리\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0344\",\"TheaterName\":\"CGV기흥\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0278\",\"TheaterName\":\"CGV김포\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0188\",\"TheaterName\":\"CGV김포운양\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0298\",\"TheaterName\":\"CGV김포한강\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0351\",\"TheaterName\":\"CGV다산\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0124\",\"TheaterName\":\"CGV동백\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0041\",\"TheaterName\":\"CGV동수원\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0106\",\"TheaterName\":\"CGV동탄\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0265\",\"TheaterName\":\"CGV동탄역\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0233\",\"TheaterName\":\"CGV동탄호수공원\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0226\",\"TheaterName\":\"CGV배곧\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0155\",\"TheaterName\":\"CGV범계\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0015\",\"TheaterName\":\"CGV부천\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0194\",\"TheaterName\":\"CGV부천역\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0287\",\"TheaterName\":\"CGV부천옥길\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0049\",\"TheaterName\":\"CGV북수원\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0242\",\"TheaterName\":\"CGV산본\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0196\",\"TheaterName\":\"CGV서현\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0304\",\"TheaterName\":\"CGV성남모란\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0143\",\"TheaterName\":\"CGV소풍\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0012\",\"TheaterName\":\"CGV수원\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0274\",\"TheaterName\":\"CGV스타필드시티위례\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0073\",\"TheaterName\":\"CGV시흥\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0211\",\"TheaterName\":\"CGV안산\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0279\",\"TheaterName\":\"CGV안성\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0003\",\"TheaterName\":\"CGV야탑\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0262\",\"TheaterName\":\"CGV양주옥정\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0338\",\"TheaterName\":\"CGV역곡\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0004\",\"TheaterName\":\"CGV오리(임시휴업)\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0305\",\"TheaterName\":\"CGV오산\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0307\",\"TheaterName\":\"CGV오산중앙\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0271\",\"TheaterName\":\"CGV용인\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0113\",\"TheaterName\":\"CGV의정부\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0187\",\"TheaterName\":\"CGV의정부태흥\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0205\",\"TheaterName\":\"CGV이천\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0054\",\"TheaterName\":\"CGV일산\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0320\",\"TheaterName\":\"CGV정왕\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0055\",\"TheaterName\":\"CGV죽전(리뉴얼중)\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0148\",\"TheaterName\":\"CGV파주문산\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0310\",\"TheaterName\":\"CGV파주야당\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0181\",\"TheaterName\":\"CGV판교\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0195\",\"TheaterName\":\"CGV평촌\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0052\",\"TheaterName\":\"CGV평택\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0334\",\"TheaterName\":\"CGV평택고덕\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0214\",\"TheaterName\":\"CGV평택소사\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0309\",\"TheaterName\":\"CGV포천\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0326\",\"TheaterName\":\"CGV하남미사\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0301\",\"TheaterName\":\"CGV화성봉담\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0145\",\"TheaterName\":\"CGV화정\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"02\",\"TheaterCode\":\"0342\",\"TheaterName\":\"DRIVE IN 곤지암\",\"TheaterName_ENG\":null,\"IsSelected\":false}],\"RegionCode\":\"02\",\"RegionName\":\"경기\",\"RegionName_ENG\":\"Gyeonggi\",\"DisplayOrder\":\"2\",\"IsSelected\":false},{\"AreaTheaterDetailList\":[{\"RegionCode\":\"202\",\"TheaterCode\":\"0043\",\"TheaterName\":\"CGV계양\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"202\",\"TheaterCode\":\"0198\",\"TheaterName\":\"CGV남주안\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"202\",\"TheaterCode\":\"0021\",\"TheaterName\":\"CGV부평\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"202\",\"TheaterCode\":\"0325\",\"TheaterName\":\"CGV송도타임스페이스\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"202\",\"TheaterCode\":\"0247\",\"TheaterName\":\"CGV연수역\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"202\",\"TheaterCode\":\"0002\",\"TheaterName\":\"CGV인천\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"202\",\"TheaterCode\":\"0254\",\"TheaterName\":\"CGV인천논현\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"202\",\"TheaterCode\":\"0340\",\"TheaterName\":\"CGV인천도화\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"202\",\"TheaterCode\":\"0258\",\"TheaterName\":\"CGV인천연수\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"202\",\"TheaterCode\":\"0269\",\"TheaterName\":\"CGV인천학익\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"202\",\"TheaterCode\":\"0308\",\"TheaterName\":\"CGV주안역\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"202\",\"TheaterCode\":\"0235\",\"TheaterName\":\"CGV청라\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"202\",\"TheaterCode\":\"0339\",\"TheaterName\":\"DRIVE IN 스퀘어원\",\"TheaterName_ENG\":null,\"IsSelected\":false}],\"RegionCode\":\"202\",\"RegionName\":\"인천\",\"RegionName_ENG\":\"Incheon\",\"DisplayOrder\":\"3\",\"IsSelected\":false},{\"AreaTheaterDetailList\":[{\"RegionCode\":\"12\",\"TheaterCode\":\"0139\",\"TheaterName\":\"CGV강릉\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"12\",\"TheaterCode\":\"0144\",\"TheaterName\":\"CGV원주\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"12\",\"TheaterCode\":\"0354\",\"TheaterName\":\"CGV원통\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"12\",\"TheaterCode\":\"0281\",\"TheaterName\":\"CGV인제\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"12\",\"TheaterCode\":\"0070\",\"TheaterName\":\"CGV춘천\",\"TheaterName_ENG\":null,\"IsSelected\":false}],\"RegionCode\":\"12\",\"RegionName\":\"강원\",\"RegionName_ENG\":\"Kangwon\",\"DisplayOrder\":\"4\",\"IsSelected\":false},{\"AreaTheaterDetailList\":[{\"RegionCode\":\"205\",\"TheaterCode\":\"0261\",\"TheaterName\":\"CGV논산\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"205\",\"TheaterCode\":\"0207\",\"TheaterName\":\"CGV당진\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"03\",\"TheaterCode\":\"0007\",\"TheaterName\":\"CGV대전\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"03\",\"TheaterCode\":\"0286\",\"TheaterName\":\"CGV대전가수원\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"03\",\"TheaterCode\":\"0154\",\"TheaterName\":\"CGV대전가오\",\"TheaterName_ENG\":null,\"IsSelected\":true},{\"RegionCode\":\"03\",\"TheaterCode\":\"0202\",\"TheaterName\":\"CGV대전탄방\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"03\",\"TheaterCode\":\"0127\",\"TheaterName\":\"CGV대전터미널\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"205\",\"TheaterCode\":\"0275\",\"TheaterName\":\"CGV보령\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"205\",\"TheaterCode\":\"0091\",\"TheaterName\":\"CGV서산\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"205\",\"TheaterCode\":\"0219\",\"TheaterName\":\"CGV세종\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"03\",\"TheaterCode\":\"0206\",\"TheaterName\":\"CGV유성노은\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"205\",\"TheaterCode\":\"0331\",\"TheaterName\":\"CGV제천\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"205\",\"TheaterCode\":\"0044\",\"TheaterName\":\"CGV천안\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"205\",\"TheaterCode\":\"0332\",\"TheaterName\":\"CGV천안시청\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"205\",\"TheaterCode\":\"0293\",\"TheaterName\":\"CGV천안터미널\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"205\",\"TheaterCode\":\"0110\",\"TheaterName\":\"CGV천안펜타포트\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"205\",\"TheaterCode\":\"0228\",\"TheaterName\":\"CGV청주(서문)\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"205\",\"TheaterCode\":\"0297\",\"TheaterName\":\"CGV청주성안길\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"205\",\"TheaterCode\":\"0282\",\"TheaterName\":\"CGV청주율량\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"205\",\"TheaterCode\":\"0142\",\"TheaterName\":\"CGV청주지웰시티\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"205\",\"TheaterCode\":\"0319\",\"TheaterName\":\"CGV청주터미널\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"205\",\"TheaterCode\":\"0284\",\"TheaterName\":\"CGV충북혁신\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"205\",\"TheaterCode\":\"0328\",\"TheaterName\":\"CGV충주교현\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"205\",\"TheaterCode\":\"0217\",\"TheaterName\":\"CGV홍성\",\"TheaterName_ENG\":null,\"IsSelected\":false}],\"RegionCode\":\"03,205\",\"RegionName\":\"대전/충청\",\"RegionName_ENG\":\"Daejeon/Chungcheong\",\"DisplayOrder\":\"5\",\"IsSelected\":true},{\"AreaTheaterDetailList\":[{\"RegionCode\":\"11\",\"TheaterCode\":\"0157\",\"TheaterName\":\"CGV대구수성\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"11\",\"TheaterCode\":\"0108\",\"TheaterName\":\"CGV대구스타디움\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"11\",\"TheaterCode\":\"0185\",\"TheaterName\":\"CGV대구아카데미\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"11\",\"TheaterCode\":\"0343\",\"TheaterName\":\"CGV대구연경\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"11\",\"TheaterCode\":\"0216\",\"TheaterName\":\"CGV대구월성\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"11\",\"TheaterCode\":\"0147\",\"TheaterName\":\"CGV대구한일\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"11\",\"TheaterCode\":\"0109\",\"TheaterName\":\"CGV대구현대\",\"TheaterName_ENG\":null,\"IsSelected\":false}],\"RegionCode\":\"11\",\"RegionName\":\"대구\",\"RegionName_ENG\":\"Daegu\",\"DisplayOrder\":\"6\",\"IsSelected\":false},{\"AreaTheaterDetailList\":[{\"RegionCode\":\"05\",\"TheaterCode\":\"0061\",\"TheaterName\":\"CGV대연\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"05\",\"TheaterCode\":\"0042\",\"TheaterName\":\"CGV동래\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"05\",\"TheaterCode\":\"0337\",\"TheaterName\":\"CGV부산명지\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"05\",\"TheaterCode\":\"0005\",\"TheaterName\":\"CGV서면\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"05\",\"TheaterCode\":\"0285\",\"TheaterName\":\"CGV서면삼정타워\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"05\",\"TheaterCode\":\"0303\",\"TheaterName\":\"CGV서면상상마당\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"05\",\"TheaterCode\":\"0089\",\"TheaterName\":\"CGV센텀시티\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"05\",\"TheaterCode\":\"0160\",\"TheaterName\":\"CGV아시아드\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"207\",\"TheaterCode\":\"0335\",\"TheaterName\":\"CGV울산동구\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"207\",\"TheaterCode\":\"0128\",\"TheaterName\":\"CGV울산삼산\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"207\",\"TheaterCode\":\"0264\",\"TheaterName\":\"CGV울산신천\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"207\",\"TheaterCode\":\"0246\",\"TheaterName\":\"CGV울산진장\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"05\",\"TheaterCode\":\"0306\",\"TheaterName\":\"CGV정관\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"05\",\"TheaterCode\":\"0245\",\"TheaterName\":\"CGV하단아트몰링\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"05\",\"TheaterCode\":\"0318\",\"TheaterName\":\"CGV해운대\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"05\",\"TheaterCode\":\"0159\",\"TheaterName\":\"CGV화명\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"05\",\"TheaterCode\":\"P004\",\"TheaterName\":\"CINE de CHEF 센텀\",\"TheaterName_ENG\":null,\"IsSelected\":false}],\"RegionCode\":\"05,207\",\"RegionName\":\"부산/울산\",\"RegionName_ENG\":\"Busan/Ulsan\",\"DisplayOrder\":\"7\",\"IsSelected\":false},{\"AreaTheaterDetailList\":[{\"RegionCode\":\"204\",\"TheaterCode\":\"0263\",\"TheaterName\":\"CGV거제\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"204\",\"TheaterCode\":\"0330\",\"TheaterName\":\"CGV경산\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"204\",\"TheaterCode\":\"0323\",\"TheaterName\":\"CGV고성\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"204\",\"TheaterCode\":\"0053\",\"TheaterName\":\"CGV구미\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"204\",\"TheaterCode\":\"0240\",\"TheaterName\":\"CGV김천율곡\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"204\",\"TheaterCode\":\"0028\",\"TheaterName\":\"CGV김해\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"204\",\"TheaterCode\":\"0311\",\"TheaterName\":\"CGV김해율하\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"204\",\"TheaterCode\":\"0239\",\"TheaterName\":\"CGV김해장유\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"204\",\"TheaterCode\":\"0033\",\"TheaterName\":\"CGV마산\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"204\",\"TheaterCode\":\"0097\",\"TheaterName\":\"CGV북포항\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"204\",\"TheaterCode\":\"0272\",\"TheaterName\":\"CGV안동\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"204\",\"TheaterCode\":\"0234\",\"TheaterName\":\"CGV양산삼호\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"204\",\"TheaterCode\":\"0324\",\"TheaterName\":\"CGV진주혁신\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"204\",\"TheaterCode\":\"0023\",\"TheaterName\":\"CGV창원\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"204\",\"TheaterCode\":\"0079\",\"TheaterName\":\"CGV창원더시티\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"204\",\"TheaterCode\":\"0283\",\"TheaterName\":\"CGV창원상남\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"204\",\"TheaterCode\":\"0156\",\"TheaterName\":\"CGV통영\",\"TheaterName_ENG\":null,\"IsSelected\":false}],\"RegionCode\":\"204\",\"RegionName\":\"경상\",\"RegionName_ENG\":\"Gyeongsang\",\"DisplayOrder\":\"8\",\"IsSelected\":false},{\"AreaTheaterDetailList\":[{\"RegionCode\":\"04\",\"TheaterCode\":\"0220\",\"TheaterName\":\"CGV광양\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"04\",\"TheaterCode\":\"0221\",\"TheaterName\":\"CGV광양 엘에프스퀘어\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"206\",\"TheaterCode\":\"0295\",\"TheaterName\":\"CGV광주금남로\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"206\",\"TheaterCode\":\"0193\",\"TheaterName\":\"CGV광주상무\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"206\",\"TheaterCode\":\"0210\",\"TheaterName\":\"CGV광주용봉\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"206\",\"TheaterCode\":\"0218\",\"TheaterName\":\"CGV광주첨단\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"206\",\"TheaterCode\":\"0244\",\"TheaterName\":\"CGV광주충장로\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"206\",\"TheaterCode\":\"0090\",\"TheaterName\":\"CGV광주터미널\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"206\",\"TheaterCode\":\"0215\",\"TheaterName\":\"CGV광주하남\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"04\",\"TheaterCode\":\"0277\",\"TheaterName\":\"CGV군산\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"04\",\"TheaterCode\":\"0237\",\"TheaterName\":\"CGV나주\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"04\",\"TheaterCode\":\"0289\",\"TheaterName\":\"CGV목포\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"04\",\"TheaterCode\":\"0280\",\"TheaterName\":\"CGV목포평화광장\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"04\",\"TheaterCode\":\"0225\",\"TheaterName\":\"CGV서전주\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"04\",\"TheaterCode\":\"0114\",\"TheaterName\":\"CGV순천\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"04\",\"TheaterCode\":\"0268\",\"TheaterName\":\"CGV순천신대\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"04\",\"TheaterCode\":\"0315\",\"TheaterName\":\"CGV여수웅천\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"04\",\"TheaterCode\":\"0020\",\"TheaterName\":\"CGV익산\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"04\",\"TheaterCode\":\"0213\",\"TheaterName\":\"CGV전주고사\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"04\",\"TheaterCode\":\"0336\",\"TheaterName\":\"CGV전주에코시티\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"04\",\"TheaterCode\":\"0179\",\"TheaterName\":\"CGV전주효자\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"04\",\"TheaterCode\":\"0186\",\"TheaterName\":\"CGV정읍\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"06\",\"TheaterCode\":\"0302\",\"TheaterName\":\"CGV제주\",\"TheaterName_ENG\":null,\"IsSelected\":false},{\"RegionCode\":\"06\",\"TheaterCode\":\"0259\",\"TheaterName\":\"CGV제주노형\",\"TheaterName_ENG\":null,\"IsSelected\":false}],\"RegionCode\":\"206,04,06\",\"RegionName\":\"광주/전라/제주\",\"RegionName_ENG\":\"Gwangju/Jeonlla/Jeju\",\"DisplayOrder\":\"9\",\"IsSelected\":false}]";
		
		JsonElement je = JsonParser.parseString(theaterList);
		JsonArray incheonCgvList = je.getAsJsonArray().get(2).getAsJsonObject().get("AreaTheaterDetailList").getAsJsonArray();
		int insertResult = 0;
		
		for(JsonElement cgv : incheonCgvList) {
			String cgvThCode = cgv.getAsJsonObject().get("TheaterCode").getAsString();
			String cgvThName = cgv.getAsJsonObject().get("TheaterName").getAsString();
			String thCheck = mvdao.selectCheckTheater("T"+cgvThCode);
			if(thCheck != null) {
				continue;
			}
			String detailUrl = "http://www.cgv.co.kr/theaters/?areacode=202&theaterCode="+cgvThCode+"&date=";
			Document detailDoc = Jsoup.connect(detailUrl).get();
			detailDoc.select("#contents > div.wrap-theater > div.sect-theater > div > div.box-contents > div.theater-info > strong > a").remove();
			String cgvThAddr = detailDoc.select("#contents > div.wrap-theater > div.sect-theater > div > div.box-contents > div.theater-info > strong").text();
			String cgvThTel = detailDoc.select("#contents > div.wrap-theater > div.sect-theater > div > div.box-contents > div.theater-info > span.txt-info > em:nth-child(1)").text();
			TheaterDto theater = new TheaterDto();
			theater.setThcode("T"+cgvThCode);
			theater.setThname(cgvThName);
			theater.setThaddr(cgvThAddr);
			theater.setThtel(cgvThTel);
			System.out.println(theater);
			insertResult += mvdao.insertTheater(theater);
		}
		System.out.println("등록된 극장 수 : " + insertResult);
		return insertResult;
	}

	
	
	

	public int addScheduleList() throws Exception {
		System.setProperty("webdriver.chrome.driver", "C:\\Users\\ose02\\Downloads\\chromedriver.exe");
		ChromeOptions options = new ChromeOptions();
		options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
		WebDriver driver = new ChromeDriver(options);

		//http://www.cgv.co.kr/theaters/?areacode=202&theaterCode=0021&date=20221216
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyyMMdd");
		Date today = new Date();
		
		ArrayList<TheaterDto> thList = mvdao.selectTheaterList();
		int insertResult = 0;
		
		for(TheaterDto theater : thList) {
			String thcode = theater.getThcode().replace("T", "");
			String date = simpleFormat.format(today); // 20221216
			for(int i = 0; i < 5; i++) {
				Date afterDate = simpleFormat.parse(date);
				cal.setTime(afterDate);
				cal.add(Calendar.DATE, 1);
				date = simpleFormat.format(cal.getTime()); //20221217
				String schedulePageUrl 
	= "http://www.cgv.co.kr/reserve/show-times/?areacode=202&theaterCode="+thcode+"&date="+date;
				
				driver.get(schedulePageUrl);
				driver.switchTo().frame( driver.findElement(By.id( "ifrm_movie_time_table" )) );
				
				List<WebElement> allScheduleList 
				 = driver.findElements(By.cssSelector("body > div > div.sect-showtimes > ul > li"));
				for(WebElement scheduleElement : allScheduleList) {
					String mvtitle = "";
					try {
						mvtitle = scheduleElement.findElement(By.cssSelector("div > div.info-movie > a > strong")).getText();
					} catch (Exception e) {
						continue;
					}
					
					String scmvcode = mvdao.selectMvcode(mvtitle);
					if(scmvcode == null) {
						continue;
					}
					List<WebElement> typeHall 
					= scheduleElement.findElements(By.cssSelector("div > div.type-hall"));
					
					for(WebElement hallSchedule:typeHall) {
						String scroom = hallSchedule.findElement(By.cssSelector("div.info-hall > ul > li:nth-child(2)")).getText().replaceAll(" .*층", "");
						
						List<WebElement> timeTableList = hallSchedule.findElements(By.cssSelector("div.info-timetable > ul > li"));
						
						for(WebElement timeTable : timeTableList) {
							ScheduleDto schedule = new ScheduleDto();
							String sctime = timeTable.findElement(By.cssSelector("em")).getText();
							schedule.setScmvcode(scmvcode);
							schedule.setScthcode("T"+thcode);
							schedule.setScroom(scroom);
							schedule.setScdate(date+" "+sctime);
							System.out.println(schedule);
							try {
								insertResult += mvdao.insertSchedule(schedule);
							} catch (Exception e) {
								System.out.println("예외");
								e.printStackTrace();
							}
						}
						
						System.out.println();
					}
					System.out.println();
				}
			}
		}
		driver.quit();
		return insertResult;
	}
	
	
	

	//영화 목록 조회 기능
	public ArrayList<MovieDto> getMovieList() {
		System.out.println("MovieService 영화 목록 조회");
//		ArrayList<MovieDto> mvList = mvdao.selectMovieList();
		ArrayList<MovieDto> mvList_Mapper = mvdao.selectMovieList_Mapper();
		// 전체 예매수 조회
		int totalReCount = mvdao.selectTotalRenumber(); 
		System.out.println("totalReCount : "+totalReCount);
		for(MovieDto mvinfo : mvList_Mapper) {
			int recount = Integer.parseInt(mvinfo.getRecount());
			double rerate = ( (double)recount / totalReCount ) * 100; //12.3456...
			mvinfo.setRecount( ( Math.round(rerate*10)/10.0 )+"" );
		}
		System.out.println(mvList_Mapper);
		return mvList_Mapper;
	}
	
	



	
	
	public String getReTheaterList(String scmvcode) {
		System.out.println("MovieService 예매가능한 극장 목록 조회");
		ArrayList<TheaterDto> thList = mvdao.selectReTheaterList(scmvcode);
//		Gson gson = new Gson();
//		String json = gson.toJson(thList);
		return new Gson().toJson(thList);
	}
	
	public String getScheduleDateList(String scmvcode, String scthcode) {
		System.out.println("MovieService 예매가능한 날짜 목록 조회");
		ArrayList<ScheduleDto> dateList = mvdao.selectReScheDuleDateList(scmvcode, scthcode);
		return new Gson().toJson(dateList);
	}
	public String getReMovieList(String thcode) {
		System.out.println("MovieService 예매가능한 영화 목록 조회");
		ArrayList<MovieDto> mvList = mvdao.selectReMovieList(thcode);
		return new Gson().toJson(mvList);
	}
	public String getScheduleRoomTimeList(String scmvcode, String scthcode, String scdate) {
		System.out.println("MovieService 예매가능한 상영관 및 시간 목록 조회");
		ArrayList<ScheduleDto> roomTimeList = mvdao.selectReScheDuleRoomTimeList(scmvcode, scthcode, scdate);
		return new Gson().toJson(roomTimeList);
	}
	
	
	public String reserveMovie_PayReady(ReservationDto reInfo, HttpSession session) throws Exception {
		String loginId = (String)session.getAttribute("loginId");
		String recode = generateRecode(loginId, reInfo.getRethcode());
		reInfo.setRecode(recode);
		reInfo.setRemid(loginId);
		try {
			mvdao.insertReservation(reInfo);
		} catch (Exception e) {
			System.out.println("KAKAO결제준비 요청중 예외");
			e.printStackTrace();
			return "Fail";
		}
		
		
        StringBuilder urlBuilder = new StringBuilder("https://kapi.kakao.com/v1/payment/ready"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("cid","UTF-8") + "=TC0ONETIME"); /* cid  테스트 가맹점 코드 */
        urlBuilder.append("&" + URLEncoder.encode("partner_order_id","UTF-8") + "=" + URLEncoder.encode(recode, "UTF-8")); /* 가맹점 예매코드, 최대 100자 */
        urlBuilder.append("&" + URLEncoder.encode("partner_user_id","UTF-8") + "=" + URLEncoder.encode(loginId, "UTF-8")); /* 가맹점 회원 id, 최대 100자 */
        urlBuilder.append("&" + URLEncoder.encode("item_name","UTF-8") + "=" + URLEncoder.encode("영화예매테스트", "UTF-8")); /* 상품명, 최대 100자 */
        urlBuilder.append("&" + URLEncoder.encode("quantity","UTF-8") + "=" + URLEncoder.encode(reInfo.getRenumber()+"", "UTF-8")); /* 상품 수량 */
        urlBuilder.append("&" + URLEncoder.encode("total_amount","UTF-8") + "=" + URLEncoder.encode( (reInfo.getRenumber()*20000)+"", "UTF-8")); /* 상품 총액 */
        urlBuilder.append("&" + URLEncoder.encode("tax_free_amount","UTF-8") + "=" + URLEncoder.encode("0", "UTF-8")); /* 상품 비과세 금액 */
        urlBuilder.append("&" + URLEncoder.encode("approval_url","UTF-8") 
                                + "=" + URLEncoder.encode("http://localhost:8081/controller/reserveMovie_PayApproval?recode="+recode, "UTF-8")); /* 결제 성공 시 redirect url, 최대 255자 */
        urlBuilder.append("&" + URLEncoder.encode("cancel_url","UTF-8") 
                                + "=" + URLEncoder.encode("http://localhost:8081/controller/reserveMovie_PayCancel?recode="+recode, "UTF-8")); /* 결제 취소 시 redirect url, 최대 255자 */
        urlBuilder.append("&" + URLEncoder.encode("fail_url","UTF-8") 
        	                    + "=" + URLEncoder.encode("http://localhost:8081/controller/reserveMovie_PayFail?recode="+recode, "UTF-8")); /* 결제 실패 시 redirect url, 최대 255자 */        
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "KakaoAK "+"2ddf68347d21a70b5b53b5882f920af5");
        conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        System.out.println("Response code: " + conn.getResponseCode());
        BufferedReader rd;
        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();
        System.out.println(sb.toString());		
		JsonElement readyElement = JsonParser.parseString(sb.toString());
		JsonObject readyObj = readyElement.getAsJsonObject();
		String tid = readyObj.get("tid").getAsString();
		System.out.println("tid : " + tid);
		session.setAttribute("payTid", tid);
		
		String nextPcUrl = readyObj.get("next_redirect_pc_url").getAsString();
		System.out.println(nextPcUrl);
		
		return nextPcUrl;        		
		
	}
	public String reserveMovie_PayApproval(String tid, String pg_token, String recode) throws Exception {
		String loginId = "TESTID";  //(String)session.getAttribute("loginId");
		StringBuilder urlBuilder = new StringBuilder("https://kapi.kakao.com/v1/payment/approve"); 
        urlBuilder.append("?" + URLEncoder.encode("cid","UTF-8") + "=" + URLEncoder.encode("TC0ONETIME", "UTF-8")); 
        urlBuilder.append("&" + URLEncoder.encode("tid","UTF-8") + "=" + URLEncoder.encode(tid, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("partner_order_id","UTF-8") + "=" + URLEncoder.encode(recode, "UTF-8")); 
        urlBuilder.append("&" + URLEncoder.encode("partner_user_id","UTF-8") + "=" + URLEncoder.encode(loginId, "UTF-8")); 
        urlBuilder.append("&" + URLEncoder.encode("pg_token","UTF-8") + "=" + URLEncoder.encode(pg_token, "UTF-8"));		
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "KakaoAK "+"2ddf68347d21a70b5b53b5882f920af5");
        conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        System.out.println("Response code: " + conn.getResponseCode());
        String result = "Fail";
        BufferedReader rd;
        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            result = "Approval";
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            mvdao.deleteReservation(recode);
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();
        System.out.println(sb.toString());		
        if(result.equals("Approval")) {
        	String total = JsonParser.parseString(sb.toString()).getAsJsonObject().get("amount").getAsJsonObject().get("total").getAsString();
        	System.out.println("total : " + total);
        	mvdao.insertPayInfo(recode,tid,loginId,total);
        }
        
        
		return result;
	}
	//예매코드 생성 기능
	private String generateRecode(String loginId, String scthcode) {
		String maxRecode = mvdao.selectMaxRecode();
		System.out.println("예매 최대값 : " + maxRecode); 
		String reCode = "R";
		if(maxRecode == null) { // 예매코드 : "R0001" 코드 생성
			reCode = reCode + String.format("%04d", 1);
		} else { // 최대값 + 1 코드 생성
			int recodeNum = Integer.parseInt( maxRecode.replace("R", "") ) + 1;
			reCode = reCode + String.format("%04d", recodeNum);
		}
		System.out.println("예매코드 : " +reCode);
		return reCode;
	}
	
	//예매처리 기능
	public String reserveMovie(ReservationDto reserveInfo) {
		String recode = "";//generateRecode(); // 예매코드 생성
		reserveInfo.setRecode(recode);
		
		String loginId = (String)session.getAttribute("loginId");
		System.out.println("loginId : " + loginId);

		reserveInfo.setRemid(loginId);
		int insertResult = 0;
		try {
			insertResult = mvdao.insertReservation(reserveInfo);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("예매 처리중 예외");
		}
		return insertResult+"";
	}
	
	
	public int deleteReserveInfo(String recode) {
		int deleteResult = mvdao.deleteReservation(recode);
		return deleteResult;
	}
	
	

	
	public MovieDto getMvInfo(String selectmovie) {
		System.out.println("MovieService getMvInfo() 호출");
		// 영화정보 조회
		MovieDto mvInfo = mvdao.selectMvInfo(selectmovie);
		System.out.println(mvInfo);
		// 전체 예매수 조회
		int totalReCount = mvdao.selectTotalRenumber(); 
		System.out.println(totalReCount);
		
		//int recount = Integer.parseInt(mvInfo.getRecount());
		//double rerate = ( (double)recount / totalReCount ) * 100; //12.3456...
		//mvInfo.setRecount( ( Math.round(rerate*10)/10.0 )+"" );
		return mvInfo;
	}
	
	public ArrayList<Map<String, String>> getRvInfo(String loginId, String selectmovie, int reviewPage, int reviewPageLimit, int reviewPageNumber) {
		System.out.println("MovieService 관람평 조회 기능 호출");
		int startRow = (reviewPage - 1) * reviewPageLimit + 1; 
		int endRow = reviewPage * reviewPageLimit;
		ArrayList<Map<String, String>> rvList = mvdao.selectRvInfoList(loginId, selectmovie, startRow, endRow);
		return rvList;
	}
	
	public PageDto gerReviewPageInfo(String selectmovie, int reviewPage, int reviewPageLimit, int reviewPageNumber) {
		System.out.println("MovieService 관람평 페이지 정보 조회 기능 호출");
		//1. 해당 영화에 작성된 관람평 개수
		int reviewCount = mvdao.selectReviewCount(selectmovie);
		
		//2. 페이지 번호 최대값 ( 총 : 5개, 한페이지 2개,  )
		int maxPageNum = (int)Math.ceil( (double)reviewCount / reviewPageLimit); // Math.ceil : 올림처리 
		
		//3. 시작 페이지 번호
		int startPageNum = ( (int)Math.ceil( (double)reviewPage/reviewPageNumber ) - 1 ) * reviewPageNumber + 1;
		
		//4. 끝 페이지 번호
		int endPageNum = startPageNum + reviewPageNumber - 1;
		if(endPageNum > maxPageNum) {
			endPageNum = maxPageNum;
		}
		PageDto pageInfo = new PageDto();
		pageInfo.setReviewPage(reviewPage);
		pageInfo.setStartPageNum(startPageNum);
		pageInfo.setEndPageNum(endPageNum);
		pageInfo.setMaxPageNum(maxPageNum);
		
		return pageInfo;
		
	}
	
	
	public int cancelRemv(String selectmovie, String loginId) {
		System.out.println("MovieService 예매취소 기능 호출");
		int deResult = mvdao.deleteReMovie(selectmovie,loginId);
		return deResult;
	}
	
	public ArrayList<TheaterDto> getTheaterList() {
		System.out.println("MovieService 극장 목록 조회");
		ArrayList<TheaterDto> thList = mvdao.selectTheaterList();
		return thList;
	}
	
	public TheaterDto getTheaterInfoList(String thcode) {
		System.out.println("MovieService 선택한 극장 상세정보 기능 호출");
		TheaterDto thInfo = mvdao.selectTheaterInfo(thcode);
		return thInfo;
	}
	
	public ArrayList<ScheduleDto> getTheaterScheduleList(String thcode) {
		System.out.println("MovieService 선택한 극장의 상영스케쥴 조회 기능 호출");
		ArrayList<ScheduleDto> scList = mvdao.selectTheaterSchedulesList(thcode);
		return scList;
	}
	
	public String getMvtitleScdate_ajax(String thcode, String scdate) {
		System.out.println("MovieService 선택한 극장과 시간으로 영화정보, 상영스케쥴 조회 기능 호출");
		ArrayList<Map<String, String>> mvScInfoList = mvdao.selectMvScheduleInfoList(thcode,scdate);
		return new Gson().toJson(mvScInfoList);
	}
	
	
	
	
	



	
	
	
	
	
	
	

}
