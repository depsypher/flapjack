define(["jquery"], function ($) {

	return function() {
		var accountForm = $("#accountForm");
		accountForm.submit(function(e) {
			e.preventDefault();
			$.ajax({
				type: "POST",
				url: "account/setup",
				data: accountForm.serialize(),
				success: function(res, status, xhr) {
					if (res.status === "success") {
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
	};

});
