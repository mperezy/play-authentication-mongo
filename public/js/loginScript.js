$("#loginButton").click(function () {
    var email = $("#email").val();
    var password = $("#password").val();

    var login = {
        email: email,
        password: password
    };

    var token = $("input[name=csrfToken]").val();
    console.log(login);

    $.ajax({
        type: "POST",
        url: "/login",
        headers: {"X-CSRF-TOKEN": token},
        contentType: "application/json",
        data: JSON.stringify(login),
        success: function (response) {
            console.log("eres el éxito");
        },
        error: function (xhr, ajaxOptions, thrownError, error) {
            console.log(xhr.status);
            console.log(thrownError);
        }
    })
});