<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko-KR">

<head>
<meta charset="utf-8">
<meta content="width=device-width, initial-scale=1.0" name="viewport">

<title>MovieListPage - 영화 목록페이지</title>
<meta content="" name="description">
<meta content="" name="keywords">

<!-- Favicons -->
<link
	href="${pageContext.request.contextPath }/resources/assets/img/favicon.png"
	rel="icon">
<link
	href="${pageContext.request.contextPath }/resources/assets/img/apple-touch-icon.png"
	rel="apple-touch-icon">

<!-- Google Fonts -->
<link href="https://fonts.gstatic.com" rel="preconnect">
<link
	href="https://fonts.googleapis.com/css?family=Open+Sans:300,300i,400,400i,600,600i,700,700i|Nunito:300,300i,400,400i,600,600i,700,700i|Poppins:300,300i,400,400i,500,500i,600,600i,700,700i"
	rel="stylesheet">

<!-- Vendor CSS Files -->
<link
	href="${pageContext.request.contextPath }/resources/assets/vendor/bootstrap/css/bootstrap.min.css"
	rel="stylesheet">
<link
	href="${pageContext.request.contextPath }/resources/assets/vendor/bootstrap-icons/bootstrap-icons.css"
	rel="stylesheet">
<link
	href="${pageContext.request.contextPath }/resources/assets/vendor/boxicons/css/boxicons.min.css"
	rel="stylesheet">
<link
	href="${pageContext.request.contextPath }/resources/assets/vendor/quill/quill.snow.css"
	rel="stylesheet">
<link
	href="${pageContext.request.contextPath }/resources/assets/vendor/quill/quill.bubble.css"
	rel="stylesheet">
<link
	href="${pageContext.request.contextPath }/resources/assets/vendor/remixicon/remixicon.css"
	rel="stylesheet">
<link
	href="${pageContext.request.contextPath }/resources/assets/vendor/simple-datatables/style.css"
	rel="stylesheet">

<!-- Template Main CSS File -->
<link
	href="${pageContext.request.contextPath }/resources/assets/css/style.css"
	rel="stylesheet">

<!-- =======================================================
  * Template Name: NiceAdmin - v2.4.1
  * Template URL: https://bootstrapmade.com/nice-admin-bootstrap-admin-html-template/
  * Author: BootstrapMade.com
  * License: https://bootstrapmade.com/license/
  ======================================================== -->
</head>

<body>

	<!-- ======= Header ======= -->
	<%@ include file="/WEB-INF/views/includes/header.jsp"%>
	<!-- End Header -->

	<!-- ======= Sidebar ======= -->
	<%@ include file="/WEB-INF/views/includes/sidebar.jsp"%>
	<!-- End Sidebar-->

	<main id="main" class="main">

		<div class="pagetitle">
			<h1>MovieListPage.jsp </h1>
		</div>
		<!-- End Page Title -->

		<section class="section">
			<div class="row">
				<h5 class="card-title">영화</h5>

				<div class="row" style="min-width: 600px;">

					<c:forEach items="${WholeMvList }" var="mv" varStatus="status">
						<div class="col-3">
							<div class="card">
								<div class="card-body">
									<h5 class="card-title py-2" style="">No.${status.index + 1 }</h5>
									<a href="${pageContext.request.contextPath }/movieInfoPage?selectmovie=${mv.mvcode }"> <img class="img-fluid" style="width: 50px;"
										alt="" src="${mv.mvpos }"> <br/>
									</a>
									<a href="${pageContext.request.contextPath }/movieInfoPage?selectmovie=${mv.mvcode }" 
									class="listTitle mt-2 mb-0"
									style="color: #012970; font-weight: 700;"
										title="${mv.mvtitle }">${mv.mvtitle }</a>
									<%-- <h6 class="listTitle mt-2 mb-0"
										style="color: #012970; font-weight: 700;"
										title="${mv.mvtitle }">${mv.mvtitle }</h6> --%>
									
									<p class="small mb-0">
										예매율 &nbsp; &nbsp; <span class="text-success pt-1 fw-bold">${mv.recount }%</span>
									</p>
									<p class="small mb-1">${mv.mvdate }개봉</p>
									<%-- <button class="btn btn-sm btn-danger"
									onclick="ticketPage(${mv.mvcode})">예매하기</button> --%>
									<a class="btn btn-sm btn-danger"
									href="${pageContext.request.contextPath }/ticketPage?selectmovie=${mv.mvcode }">예매하기</a>
								</div>
							</div>
						</div>
					</c:forEach>

					<!--	만약 예매하기 버튼을 <button> 태그로 만든다면 아래의 스트립트문을 작성하면 됨  
				<script type="text/javascript">
					function ticketPage(selMvcode) {
						location.href="${pageContext.request.contextPath }/ticketPage?selectmovie="+selMvcode;
					}
				
				</script> -->



				</div>


			</div>

		</section>

	</main>
	<!-- End #main -->

	<!-- ======= Footer ======= -->
	<%@ include file="/WEB-INF/views/includes/footer.jsp"%>
	<!-- End Footer -->

	<a href="#"
		class="back-to-top d-flex align-items-center justify-content-center"><i
		class="bi bi-arrow-up-short"></i></a>

	<!-- Vendor JS Files -->
	<script
		src="${pageContext.request.contextPath }/resources/assets/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>

	<script
		src="${pageContext.request.contextPath }/resources/assets/vendor/tinymce/tinymce.min.js"></script>

	<!-- Template Main JS File -->
	<script
		src="${pageContext.request.contextPath }/resources/assets/js/main.js"></script>

</body>

</html>