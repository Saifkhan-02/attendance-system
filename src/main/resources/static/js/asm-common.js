function checkAsmSession() {
  const asmId = localStorage.getItem("employeeId");
  const role = localStorage.getItem("employeeRole");
  const token = localStorage.getItem("sessionToken");

  if (!asmId || !token || role !== "ASM") {
    localStorage.clear();
    window.location.replace("index.html");
    return false;
  }

  return true;
}

function getAsmId() {
  return localStorage.getItem("employeeId");
}

function getAsmName() {
  return localStorage.getItem("employeeName") || "ASM";
}

function openAsmSidebar() {
  document.getElementById("asmSidebar").classList.add("show");
  document.getElementById("asmOverlay").classList.add("show");
}

function closeAsmSidebar() {
  document.getElementById("asmSidebar").classList.remove("show");
  document.getElementById("asmOverlay").classList.remove("show");
}

function logoutAsm() {
  if (confirm("Are you sure you want to logout?")) {
    localStorage.clear();
    window.location.replace("index.html");
  }
}

function money(value) {
  return "₹" + Number(value || 0).toLocaleString("en-IN");
}

function setText(id, value) {
  const el = document.getElementById(id);
  if (el) el.innerText = value;
}

function renderAsmSidebar(activePage = "dashboard") {
  document.body.insertAdjacentHTML("afterbegin", `
    <div id="asmOverlay" class="asm-overlay" onclick="closeAsmSidebar()"></div>

    <div id="asmSidebar" class="asm-sidebar">
      <div class="asm-side-logo">
        <img src="images/Inflix-logo-web.png" alt="Logo">
      </div>

      <div class="asm-side-user">
        <i class="fa-solid fa-user-tie"></i>
        <h5>${getAsmName()}</h5>
        <div class="text-muted">Area Sales Manager</div>
      </div>

      <a href="ASMDashboard.html" class="asm-link ${activePage === "dashboard" ? "active" : ""}">
        <i class="fa-solid fa-house"></i> Dashboard
      </a>

<a href="ASMProfile.html" class="asm-link ${activePage === "profile" ? "active" : ""}">
        <i class="fa-solid fa-user"></i> Profile
      </a>  

      <a href="ASMTourPlan.html" class="asm-link ${activePage === "tour" ? "active" : ""}">
        <i class="fa-solid fa-user"></i> My Tour Plan
      </a> 

     <!-- <a href="ASMTeam.html" class="asm-link ${activePage === "team" ? "active" : ""}">
        <i class="fa-solid fa-users"></i> My Team
      </a>
      -->
    
      <a href="ASMWorkingWith.html" class="asm-link ${activePage === "working" ? "active" : ""}">
        <i class="fa-solid fa-user-group"></i> Working With
      </a>
     <a href="ASMStockHistory.html" class="asm-link ${activePage === "stock" ? "active" : ""}">
  <i class="fa-solid fa-boxes-stacked"></i>
  <span>Stock History</span>
</a>

      <a href="ASMReports.html" class="asm-link ${activePage === "reports" ? "active" : ""}">
        <i class="fa-solid fa-chart-line"></i> Reports
      </a>

      <a href="ChangePassword.html" class="asm-link">
        <i class="fa-solid fa-key"></i> Change Password
      </a>

      <a href="#" onclick="logoutAsm()" class="asm-link asm-logout">
        <i class="fa-solid fa-right-from-bracket"></i> Logout
      </a>
    </div>
  `);
}

function renderAsmHeader(title, showBack = false, backUrl = "ASMDashboard.html") {
  document.body.insertAdjacentHTML("afterbegin", `
    <header class="asm-header">
      ${
        showBack
          ? `<button class="asm-header-btn asm-back-btn" onclick="window.location.href='${backUrl}'">
               <i class="fa-solid fa-arrow-left"></i>
             </button>`
          : `<button class="asm-header-btn asm-menu-btn" onclick="openAsmSidebar()">
               <i class="fa-solid fa-bars"></i>
             </button>`
      }

      <img src="images/Inflix-logo-web.png" class="asm-logo" alt="Logo">
      <div class="asm-header-title">${title}</div>

      ${
        showBack
          ? `<div style="width:45px"></div>`
          : `<button class="asm-header-btn asm-notify-btn">
               <i class="fa-solid fa-bell"></i>
             </button>`
      }
    </header>
  `);
}