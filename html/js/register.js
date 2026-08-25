document
    .getElementById("registerForm")
    .addEventListener("submit", registerUser);

async function registerUser(event) {

    event.preventDefault();

    const message =
        document.getElementById("registerMessage");

    message.className = "message";

    const request = {

        email:
            document.getElementById("email").value.trim(),

        userName:
            document.getElementById("userName").value.trim(),

        password:
        document.getElementById("password").value,

        firstName:
            document.getElementById("firstName").value.trim(),

        lastName:
            document.getElementById("lastName").value.trim(),

        dateOfBirth:
            document.getElementById("dateOfBirth").value || null,

        addressLine1:
            document.getElementById("addressLine1").value.trim(),

        addressLine2:
            document.getElementById("addressLine2").value.trim(),

        postCode:
            document.getElementById("postCode").value.trim(),

        city:
            document.getElementById("city").value.trim(),

        state:
            document.getElementById("state").value.trim(),

        country:
            document.getElementById("country").value.trim(),

        phoneNumber:
            document.getElementById("phoneNumber").value.trim(),

        roleType:
        document.getElementById("roleType").value
    };

    const validationError = validateForm(request);

    if (validationError) {

        message.className = "message error";
        message.innerHTML = validationError;
        return;
    }

    try {

        message.innerHTML = "Creating account...";

        const response = await fetch(
            "/user/register",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(request)
            }
        );

        if (!response.ok) {

            const errorText =
                await response.text();

            throw new Error(
                errorText || "Registration failed"
            );
        }

        message.className = "message success";

        message.innerHTML =
            "Registration successful. Verification email sent.";

        setTimeout(() => {

            window.location.href =
                "/login";

        }, 2000);

    } catch (error) {

        console.error(error);

        message.className = "message error";

        message.innerHTML =
            error.message ||
            "Registration failed.";

    }
}

function validateForm(request) {

    if (!request.firstName) {
        return "First name is required.";
    }

    if (!request.lastName) {
        return "Last name is required.";
    }

    if (!request.email) {
        return "Email is required.";
    }

    if (!isValidEmail(request.email)) {
        return "Please enter a valid email address.";
    }

    if (!request.userName) {
        return "Username is required.";
    }

    if (request.userName.length < 3) {
        return "Username must be at least 3 characters.";
    }

    if (!request.password) {
        return "Password is required.";
    }

    if (request.password.length < 8) {
        return "Password must be at least 8 characters.";
    }

    if (!isStrongPassword(request.password)) {
        return "Password must contain uppercase, lowercase, number and special character.";
    }

    if (!request.phoneNumber) {
        return "Phone number is required.";
    }

    if (!/^\d{10}$/.test(request.phoneNumber)) {
        return "Phone number must contain exactly 10 digits.";
    }

    if (!request.roleType) {
        return "Please select a role.";
    }

    if (request.dateOfBirth) {

        const dob =
            new Date(request.dateOfBirth);

        const today =
            new Date();

        if (dob > today) {
            return "Date of birth cannot be in the future.";
        }
    }

    const confirmPassword =
        document.getElementById("confirmPassword").value;

    if (request.password !== confirmPassword) {
        return "Password and Confirm Password must match.";
    }

    return null;
}

function isValidEmail(email) {

    const pattern =
        /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    return pattern.test(email);
}

function isStrongPassword(password) {

    const pattern =
        /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?#&_])[A-Za-z\d@$!%*?#&_]{8,}$/;

    return pattern.test(password);
}