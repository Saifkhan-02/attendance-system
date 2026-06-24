function checkEmployeeSession() {
  const employeeId = localStorage.getItem("employeeId");
  const token = localStorage.getItem("sessionToken");

  if (!employeeId || !token) {
    localStorage.clear();
    window.location.href = "index.html";
    return;
  }

  fetch(`${BASE_URL}/employee/session/check/${employeeId}?token=${token}`)
    .then(response => {
      if (!response.ok) {
        throw new Error("Session Expired");
      }
      return response.text();
    })
    .catch(() => {
      alert("Session Expired, Please login again.");
      localStorage.clear();
      window.location.href = "index.html";
    });
}

checkEmployeeSession();

setInterval(() => {
    checkEmployeeSession();
}, 30000);