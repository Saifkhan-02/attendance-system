const BASE_URL =
    window.location.hostname === "127.0.0.1" ||
    window.location.hostname === "localhost"
        ? "http://localhost:8080"
        : "https://attendance-system-2-ij3y.onrender.com";

        // alert("BASE_URL = " + BASE_URL);