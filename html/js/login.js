document
    .getElementById("loginForm")
    .addEventListener("submit", login);

async function login(event) {

    event.preventDefault();

    const username =
        document.getElementById("username").value;

    const password =
        document.getElementById("password").value;

    const message =
        document.getElementById("loginMessage");

    if (!username) {

        message.innerHTML =
            "Username is required.";

        return;
    }

    if (!password) {

        message.innerHTML =
            "Password is required.";

        return;
    }

    try {

        message.innerHTML =
            "Signing in...";

        const response =
            await fetch("/user/login", {

                method: "POST",

                headers: {

                    "Authorization":
                        "Bearer mock-token",

                    "X-Mock-User":
                    username
                }
            });

        if (!response.ok) {
            throw new Error("Login failed");
        }

        const user =
            await response.json();

        localStorage.setItem(
            "currentUser",
            JSON.stringify(user)
        );

        window.location.href =
            "/dashboard";

    } catch (error) {

        console.error(error);

        message.innerHTML =
            "Invalid username or password.";
    }
}