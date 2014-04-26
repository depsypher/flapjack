define(["jquery", "persona"], function ($) {

	var currentUser, signinLink, signoutLink, loginInfo;

	signinLink = $("#login");
	if (signinLink) {
		signinLink.on("click", function() {
			navigator.id.request({
				siteName: "Flapjack"
			});
		});
	}

	signoutLink = $("#logout");
	if (signoutLink) {
		signoutLink.on("click", function() {
			navigator.id.logout();
		});
	}

	loginInfo = $("#loginInfo");
	currentUser = loginInfo.data("email");

	if (loginInfo.data("sessiontimeout") === true) {
		navigator.id.logout();
	}

	var accountDialog = $("#accountDialog");
	if (accountDialog) {
		accountDialog.modal({ show: true });
		accountDialog.submit(function(e) {
			e.preventDefault();
			$.ajax({
				type: "POST",
				url: "account/setup",
				data: accountDialog.serialize(),
				success: function(res, status, xhr) {
					if (res.status === "success") {
						accountDialog.modal("hide");
						document.location.reload(true);
					} else {
						alert(res.errorMessage);
					}
					return true;
				},
				error: function(res, status, xhr) {
					return alert("login failure" + res);
				}
			});
			return false;
		});
		$("#accountDialogAbort").click(function(e) {
			$.ajax({
				type: "POST",
				url: "auth/logout",
				success: function(res, status, xhr) {
					navigator.id.logout();
					location.href = "";
				},
				error: function(res, status, xhr) {
					return alert("login failure" + res);
				}
			});
		});
	}

	navigator.id.watch({
		loggedInUser: currentUser,
		onlogin: function(assertion) {
			$.ajax({
				type: "POST",
				url: "auth/login",
				data: {
					assertion: assertion
				},
				success: function(res, status, xhr) {
					if (currentUser == "") {
						window.location.reload();
					}
				},
				error: function(res, status, xhr) {
					navigator.id.logout();
					alert("login failure" + res);
				}
			});
		},
		onlogout: function() {
			$.ajax({
				type: "POST",
				url: "auth/logout",
				success: function(res, status, xhr) {
					if (currentUser !== "") {
						window.location.reload();
					}
				},
				error: function(res, status, xhr) {
					alert("logout failure" + res);
				}
			});
		}
	});
});