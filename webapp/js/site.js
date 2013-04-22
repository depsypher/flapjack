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